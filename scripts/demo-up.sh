#!/usr/bin/env bash
#
# Démarre la démonstration MVP de bout en bout :
#   base PostGIS → backend → jeu de données simulées → statuts initiaux.
#
# Prérequis : Docker en marche, jar construit (sinon construit ici), et
# infrastructure/.env présent (copié depuis .env.example).
# Variables : DEMO_PORT (défaut 8090), POSTGRES_PORT lu depuis infrastructure/.env.
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

DEMO_PORT="${DEMO_PORT:-8090}"
ENV_FILE="infrastructure/.env"
[[ -f "$ENV_FILE" ]] || { echo "ÉCHEC : $ENV_FILE manquant (copiez infrastructure/.env.example)"; exit 1; }
# shellcheck disable=SC1090
source "$ENV_FILE"
DB_PORT_HOST="${POSTGRES_PORT:-5432}"

echo "▶ 1/5 Base PostGIS (port hôte $DB_PORT_HOST)"
docker compose -f infrastructure/docker-compose.yml --env-file "$ENV_FILE" up -d
# Attend une disponibilité STABLE : l'image postgres redémarre pendant l'init,
# un simple « healthy » peut être prématuré. On exige 3 succès consécutifs.
stable=0
for _ in $(seq 1 60); do
  if docker exec urgence-sante-postgis pg_isready -q \
       -U "${POSTGRES_USER:-urgence_sante}" -d "${POSTGRES_DB:-urgence_sante}" 2>/dev/null; then
    stable=$((stable + 1))
    [[ $stable -ge 3 ]] && break
  else
    stable=0
  fi
  sleep 2
done
[[ $stable -ge 3 ]] || { echo "ÉCHEC : PostGIS indisponible"; exit 1; }

JAR="backend/bootstrap/target/bootstrap-0.1.0-SNAPSHOT-exec.jar"
if [[ ! -f "$JAR" ]]; then
  echo "▶ 2/5 Construction du jar (absent)"
  (cd backend && ./mvnw -B -q -pl bootstrap -am package -DskipTests)
else
  echo "▶ 2/5 Jar présent"
fi

echo "▶ 3/5 Backend sur le port $DEMO_PORT"
start_backend() {
  # CORS de démonstration : le portail Flutter Web tourne sur un port local
  # aléatoire ; on autorise localhost/127.0.0.1 (interdit en production par
  # CorsPolicy). Surchargeable : CORS_ALLOWED_ORIGINS=... bash scripts/demo-up.sh
  DB_PASSWORD="$POSTGRES_PASSWORD" DB_HOST=localhost DB_PORT="$DB_PORT_HOST" \
  DB_NAME="${POSTGRES_DB:-urgence_sante}" DB_USER="${POSTGRES_USER:-urgence_sante}" \
  CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://localhost:*,http://127.0.0.1:*}" \
    java -jar "$JAR" --spring.profiles.active=local --server.port="$DEMO_PORT" \
    > /tmp/urgence-sante-demo.log 2>&1 &
  echo $! > /tmp/urgence-sante-demo.pid
  for _ in $(seq 1 45); do
    curl -sf "http://localhost:$DEMO_PORT/api/v1/medical-services" >/dev/null 2>&1 && return 0
    kill -0 "$(cat /tmp/urgence-sante-demo.pid)" 2>/dev/null || return 1
    sleep 2
  done
  return 1
}
start_backend || { echo "  reprise (démarrage précoce possible)…"; sleep 5; start_backend; } \
  || { echo "ÉCHEC : backend injoignable (voir /tmp/urgence-sante-demo.log)"; exit 1; }

echo "▶ 4/5 Jeu de données simulées (15 établissements)"
docker exec -i -e PGPASSWORD="$POSTGRES_PASSWORD" urgence-sante-postgis \
  psql -q -U "${POSTGRES_USER:-urgence_sante}" -d "${POSTGRES_DB:-urgence_sante}" \
  < infrastructure/demo/seed-demo.sql

echo "▶ 5/5 Statuts initiaux (via l'API, comme un agent)"
put() {
  curl -sf -X PUT "http://localhost:$DEMO_PORT/api/v1/facilities/$1/availability/$2" \
    -H "Content-Type: application/json" -d "{\"status\":\"$3\"}" >/dev/null
}
put 11111111-0000-0000-0000-000000000001 maternity AVAILABLE
put 11111111-0000-0000-0000-000000000007 maternity SATURATED
put 11111111-0000-0000-0000-000000000014 maternity CLOSED
put 11111111-0000-0000-0000-000000000002 emergency AVAILABLE
put 11111111-0000-0000-0000-000000000006 emergency LIMITED

echo ""
echo "✓ Démo prête : http://localhost:$DEMO_PORT/api/v1"
echo "  Guide : docs/demo/DEMO_GUIDE.md — arrêt : scripts/demo-down.sh"
