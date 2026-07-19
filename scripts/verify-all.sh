#!/usr/bin/env bash
#
# VALIDATION INTERNE (remplace la CI cloud tant qu'elle n'est pas financée).
# Exécute tous les contrôles — y compris les tests d'intégration PostGIS sur
# une base RÉELLE éphémère — et écrit un rapport daté versionnable dans
# reports/verification/. Toute fusion vers main doit s'appuyer sur un rapport
# vert du commit concerné.
set -uo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

TS="$(date -u +%Y-%m-%dT%H-%M-%SZ)"
REPORT_DIR="reports/verification"
REPORT="$REPORT_DIR/$TS.md"
mkdir -p "$REPORT_DIR"

DB_NAME="urgence_sante_ci"
DB_USER="urgence_sante"
DB_PASSWORD="ci_$(date +%s)"
DB_PORT="${CI_DB_PORT:-55433}"
DB_CONTAINER="urgence-ci-postgis"

declare -a STEP_NAMES STEP_STATUS STEP_NOTES
overall=0

record() { STEP_NAMES+=("$1"); STEP_STATUS+=("$2"); STEP_NOTES+=("$3"); }

run_step() { # nom, commande... — capture, statut, note = dernière ligne utile
  local name="$1"; shift
  local start end log status note
  log="$(mktemp)"
  start=$(date +%s)
  if "$@" >"$log" 2>&1; then status="✅"; else status="❌"; overall=1; fi
  end=$(date +%s)
  note="$(grep -aE 'BUILD SUCCESS|BUILD FAILURE|No issues found|All tests passed|Some tests failed|valid|ÉCHEC|OK :' "$log" \
      | tail -1 | sed -E 's/\x1b\[[0-9;]*m//g' | cut -c1-100)"
  record "$name" "$status ($((end-start)) s)" "${note:-voir sortie}"
  echo "── $name : $status ($((end-start)) s) ${note:+— $note}"
  [[ "$status" == "❌" ]] && tail -30 "$log"
  rm -f "$log"
}

cleanup() { docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true; }
trap cleanup EXIT

echo "▶ Validation interne — commit $(git rev-parse --short HEAD) — $TS"

# 1. Limite de 300 lignes
run_step "Qualité — limite 300 lignes" bash scripts/check-file-length.sh

# 2. Contrat OpenAPI
run_step "Contrat OpenAPI (Redocly)" bash scripts/validate-openapi.sh

# 3. Base PostGIS éphémère VIERGE pour les tests d'intégration
db_ready=1
docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true
if docker run -d --name "$DB_CONTAINER" \
     -e POSTGRES_DB="$DB_NAME" -e POSTGRES_USER="$DB_USER" -e POSTGRES_PASSWORD="$DB_PASSWORD" \
     -p "127.0.0.1:$DB_PORT:5432" postgis/postgis:16-3.4 >/dev/null 2>&1; then
  stable=0
  for _ in $(seq 1 60); do
    if docker exec "$DB_CONTAINER" pg_isready -q -U "$DB_USER" -d "$DB_NAME" 2>/dev/null; then
      stable=$((stable+1)); [[ $stable -ge 3 ]] && break
    else stable=0; fi
    sleep 2
  done
  [[ $stable -ge 3 ]] || db_ready=0
else
  db_ready=0
fi

if [[ $db_ready -eq 1 ]]; then
  record "Base PostGIS éphémère (vierge)" "✅" "port $DB_PORT, migrations appliquées par Flyway pendant les tests"
  # 4. Backend complet, TESTS D'INTÉGRATION INCLUS (base externe fournie)
  export IT_DB_URL="jdbc:postgresql://localhost:$DB_PORT/$DB_NAME"
  export IT_DB_USER="$DB_USER" IT_DB_PASSWORD="$DB_PASSWORD"
  run_step "Backend + tests PostGIS réels (mvn verify)" \
    bash -c "cd backend && ./mvnw -B --no-transfer-progress verify"
  # Preuve anti-skip : les 3 classes d'intégration doivent afficher Skipped: 0.
  executed=$(grep -h "Tests run:" \
      backend/bootstrap/target/surefire-reports/com.urgencesante.FacilityApiIntegrationTest.txt \
      backend/bootstrap/target/surefire-reports/com.urgencesante.PostgisMigrationIntegrationTests.txt \
      backend/bootstrap/target/surefire-reports/com.urgencesante.ReferentialIntegrityIntegrationTest.txt 2>/dev/null \
      | grep -c "Skipped: 0")
  if [[ "$executed" -ge 3 ]]; then
    record "Anti-skip PostGIS (3 classes, Skipped: 0)" "✅" "tests d'intégration réellement exécutés"
  else
    record "Anti-skip PostGIS (3 classes, Skipped: 0)" "❌" "des tests d'intégration ont été ignorés"
    overall=1
  fi
  unset IT_DB_URL IT_DB_USER IT_DB_PASSWORD
else
  record "Base PostGIS éphémère (vierge)" "❌" "Docker indisponible : tests d'intégration NON exécutés"
  overall=1
fi
cleanup

# 5. Frontend : analyse + tests de tous les membres
run_step "Frontend — flutter analyze" bash -c "cd frontend && flutter pub get >/dev/null && flutter analyze"
for member in packages/app_foundation packages/design_system packages/api_client \
              apps/patient_mobile apps/hospital_portal; do
  run_step "Frontend — tests $member" bash -c "cd frontend/$member && flutter test"
done

# 6. APK (optionnel : dépend du SDK Android ET d'un chemin ASCII)
# Le compilateur de shaders Flutter échoue sur un chemin non-ASCII (Windows) ;
# ce dépôt vit sous « propre à moi » (accent). Quand c'est le cas, on délègue
# à la CI GitHub Actions (job « APK Android », build Linux en chemin ASCII).
if printf '%s' "$ROOT" | LC_ALL=C grep -q '[^ -~]'; then
  record "APK Android (debug)" "⚪ N/A" "chemin non-ASCII : build délégué à la CI (job APK, Linux)"
elif [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME:-/nonexistent}/platforms" ]]; then
  run_step "APK Android (debug)" \
    bash -c "cd frontend/apps/patient_mobile && flutter build apk --debug -t lib/main_development.dart"
else
  record "APK Android (debug)" "⚪ N/A" "SDK Android absent de ce poste (dette tracée)"
fi

# ── Rapport ──────────────────────────────────────────────────────────────────
{
  echo "# Rapport de validation interne"
  echo ""
  echo "| | |"
  echo "|---|---|"
  echo "| Date (UTC) | $TS |"
  echo "| Commit | $(git rev-parse HEAD) |"
  echo "| Branche | $(git rev-parse --abbrev-ref HEAD) |"
  echo "| Machine | $(uname -sm) |"
  echo "| Java | $(java -version 2>&1 | head -1 | tr -d '\"') |"
  echo "| Flutter | $(flutter --version 2>/dev/null | head -1) |"
  echo ""
  echo "| Contrôle | Statut | Note |"
  echo "|---|---|---|"
  for i in "${!STEP_NAMES[@]}"; do
    echo "| ${STEP_NAMES[$i]} | ${STEP_STATUS[$i]} | ${STEP_NOTES[$i]} |"
  done
  echo ""
  if [[ $overall -eq 0 ]]; then
    echo "**Résultat global : ✅ VALIDE** — ce commit peut être fusionné vers main."
  else
    echo "**Résultat global : ❌ ÉCHEC** — fusion interdite."
  fi
} > "$REPORT"

echo ""
echo "Rapport : $REPORT"
[[ $overall -eq 0 ]] && echo "✓ VALIDATION INTERNE VERTE" || echo "✗ VALIDATION INTERNE EN ÉCHEC"
exit $overall
