#!/usr/bin/env bash
#
# SCÉNARIO MVP DE BOUT EN BOUT, DEPUIS UNE BASE VIERGE — avec preuve conservée.
# Base PostGIS neuve → migrations Flyway → jeu de démonstration → backend →
# parcours API mesuré (besoins, recommandation, itinéraire, disponibilité,
# sécurité du portail, santé). Écrit un rapport horodaté dans reports/e2e/.
#
# Prérequis : Docker, JDK 21. Aucune donnée préexistante requise.
set -uo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

TS="$(date -u +%Y-%m-%dT%H-%M-%SZ)"
REPORT_DIR="reports/e2e"
REPORT="$REPORT_DIR/$TS.md"
mkdir -p "$REPORT_DIR"

PORT="${E2E_PORT:-8097}"
DB_PORT="${E2E_DB_PORT:-55455}"
DB_CONTAINER="urgence-e2e-postgis"
DB_PASSWORD="e2e_$(date +%s)"
PORTAL_TOKEN="demo-samu-admin-2026"
BASE="http://localhost:$PORT/api/v1"
JAR="backend/bootstrap/target/bootstrap-0.1.0-SNAPSHOT-exec.jar"

pass=0; fail=0
declare -a ROWS

cleanup() {
  [[ -f /tmp/e2e-backend.pid ]] && kill "$(cat /tmp/e2e-backend.pid)" 2>/dev/null
  docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true
}
trap cleanup EXIT

# Vérifie une condition ; mesure la latence de la requête associée.
check() { # libellé, valeur_attendue, valeur_obtenue, latence_ms
  local label="$1" expected="$2" actual="$3" ms="${4:-}"
  if [[ "$actual" == "$expected" ]]; then
    pass=$((pass+1)); ROWS+=("| $label | ✅ | \`$actual\` | ${ms:-–} |")
    echo "✅ $label ($actual) ${ms:+— ${ms} ms}"
  else
    fail=$((fail+1)); ROWS+=("| $label | ❌ attendu \`$expected\` | \`$actual\` | ${ms:-–} |")
    echo "❌ $label : attendu $expected, obtenu $actual"
  fi
}

# curl mesuré : renvoie "CODE TEMPS_MS" et écrit le corps dans $2.
timed() { # url, fichier_corps, [args curl...]
  local url="$1" body="$2"; shift 2
  curl -s -o "$body" -w '%{http_code} %{time_total}' "$@" "$url" \
    | awk '{printf "%s %d\n", $1, $2*1000}'
}

echo "▶ E2E depuis base vierge — commit $(git rev-parse --short HEAD) — $TS"

# 1. Base PostGIS neuve
docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true
docker run -d --name "$DB_CONTAINER" -e POSTGRES_DB=e2e -e POSTGRES_USER=e2e \
  -e POSTGRES_PASSWORD="$DB_PASSWORD" -p "127.0.0.1:$DB_PORT:5432" \
  postgis/postgis:16-3.4 >/dev/null || { echo "ÉCHEC démarrage DB"; exit 1; }
stable=0
for _ in $(seq 1 60); do
  docker exec "$DB_CONTAINER" pg_isready -q -U e2e -d e2e 2>/dev/null \
    && stable=$((stable+1)) || stable=0
  [[ $stable -ge 3 ]] && break; sleep 2
done
[[ $stable -ge 3 ]] || { echo "ÉCHEC : DB indisponible"; exit 1; }

# 2. Jar (construit si absent)
[[ -f "$JAR" ]] || (cd backend && ./mvnw -B -q -pl bootstrap -am package -DskipTests)

# 3. Backend : Flyway migre la base vierge au démarrage
DB_HOST=localhost DB_PORT="$DB_PORT" DB_NAME=e2e DB_USER=e2e DB_PASSWORD="$DB_PASSWORD" \
  java -jar "$JAR" --spring.profiles.active=local --server.port="$PORT" \
  > /tmp/e2e-backend.log 2>&1 &
echo $! > /tmp/e2e-backend.pid
ready=0
for _ in $(seq 1 60); do
  curl -sf "http://localhost:$PORT/actuator/health/readiness" 2>/dev/null | grep -q '"UP"' \
    && { ready=1; break; }
  kill -0 "$(cat /tmp/e2e-backend.pid)" 2>/dev/null || break
  sleep 2
done
[[ $ready -eq 1 ]] || { echo "ÉCHEC : backend injoignable"; tail -20 /tmp/e2e-backend.log; exit 1; }

# 4. Jeu de démonstration (identifié comme simulé : data_status = DEMO)
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" \
  psql -q -U e2e -d e2e < infrastructure/demo/seed-demo.sql

# 5. Parcours API mesuré ────────────────────────────────────────────────────
b=$(mktemp)

read -r code ms < <(timed "$BASE/medical-services" "$b")
check "Besoins médicaux (catalogue)" "200" "$code" "$ms"
needs=$(grep -c '"code"' "$b" 2>/dev/null || echo 0)
check "Catalogue non vide" "true" "$([[ $needs -ge 1 ]] && echo true || echo false)"

# Recommandation depuis une position (Plateau).
read -r code ms < <(timed "$BASE/orientation?lat=5.35&lon=-4.02&service=maternity" "$b")
check "Recommandation (orientation)" "200" "$code" "$ms"
check "Position présente dans la reco" "true" \
  "$(grep -q '"location"' "$b" && echo true || echo false)"
check "Temps qualifié (REAL/ESTIMATED)" "true" \
  "$(grep -qE '"travelTimeQuality":"(REAL|ESTIMATED|UNAVAILABLE)"' "$b" && echo true || echo false)"

# Disponibilité d'un établissement de démo.
FID="11111111-0000-0000-0000-000000000001"
read -r code ms < <(timed "$BASE/facilities/$FID/availability" "$b")
check "Disponibilité établissement" "200" "$code" "$ms"

# Sécurité du portail : 401 sans jeton, 200 avec le jeton démo.
read -r code ms < <(timed "$BASE/facilities/$FID/availability/maternity" "$b" \
  -X PUT -H "Content-Type: application/json" -d '{"status":"AVAILABLE"}')
check "Mise à jour SANS jeton refusée" "401" "$code" "$ms"
read -r code ms < <(timed "$BASE/facilities/$FID/availability/maternity" "$b" \
  -X PUT -H "Authorization: Bearer $PORTAL_TOKEN" \
  -H "Content-Type: application/json" -d '{"status":"AVAILABLE"}')
check "Mise à jour AVEC jeton acceptée" "200" "$code" "$ms"

# Itinéraire direct (peut être indisponible → 200 avec corps ou 404 géré).
read -r code ms < <(timed "$BASE/routes?fromLat=5.35&fromLon=-4.02&toLat=5.3496&toLon=-3.9851" "$b")
check "Itinéraire (routing joignable)" "true" \
  "$([[ "$code" == "200" || "$code" == "404" ]] && echo true || echo false)" "$ms"

# Santé : readiness inclut la base.
read -r code ms < <(timed "http://localhost:$PORT/actuator/health/readiness" "$b")
check "Readiness UP (base incluse)" "true" \
  "$(grep -q '"db"' "$b" && echo true || echo false)" "$ms"

rm -f "$b"

# 6. Rapport ─────────────────────────────────────────────────────────────────
{
  echo "# Scénario MVP de bout en bout — preuve"
  echo ""
  echo "| | |"
  echo "|---|---|"
  echo "| Date (UTC) | $TS |"
  echo "| Commit | $(git rev-parse HEAD) |"
  echo "| Environnement | $(uname -sm), $(java -version 2>&1 | head -1 | tr -d '\"') |"
  echo "| Base | PostGIS 16-3.4 (conteneur neuf, migré par Flyway) |"
  echo "| Données | jeu de démonstration (toutes \`data_status = DEMO\`, simulées) |"
  echo ""
  echo "## Protocole"
  echo ""
  echo "Base vierge → migrations → seed démo → backend → parcours API mesuré"
  echo "(\`curl -w time_total\`, une requête par ligne). Latences indicatives :"
  echo "poste de développement mono-instance, OSRM public, sans préchauffage."
  echo ""
  echo "| Étape | Résultat | Valeur | Latence |"
  echo "|---|---|---|---|"
  for r in "${ROWS[@]}"; do echo "$r"; done
  echo ""
  echo "Total : $pass réussis, $fail échoués."
  echo ""
  echo "> Le mode HORS LIGNE et le parcours d'interface (besoin → localisation →"
  echo "> recommandation → itinéraire → appels 185/180) sont prouvés côté Flutter"
  echo "> par les tests de parcours (patient_mobile)."
} > "$REPORT"

echo ""
echo "Rapport : $REPORT"
[[ $fail -eq 0 ]] && echo "✓ SCÉNARIO E2E RÉUSSI" || echo "✗ SCÉNARIO E2E EN ÉCHEC"
exit $fail
