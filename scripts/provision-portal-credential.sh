#!/usr/bin/env bash
#
# Provisionne un nouveau credential portail (admin ou opérateur) contre le
# backend LOCAL (base démarrée par scripts/local-up.sh, backend arrêté ou non
# — ce script lance son propre processus jetable, indépendant du serveur HTTP
# déjà en ligne). Construit le jar si absent. Affiche le jeton en clair UNE
# SEULE FOIS sur stdout (jamais journalisé) : à transmettre à l'opérateur de
# façon sécurisée, puis à effacer de l'historique du terminal.
#
# Usage :
#   scripts/provision-portal-credential.sh --label "Régulation SAMU" --role ADMIN
#   scripts/provision-portal-credential.sh --label "Hôpital X" --role FACILITY_OPERATOR --facility-id <uuid>
#
# Prérequis : infrastructure/.env présent, base PostGIS locale démarrée
# (scripts/local-up.sh). Production : voir
# docs/operations/PORTAL_CREDENTIAL_PROVISIONING.md (invocation via
# `docker compose run`, réseau Docker distinct de ce poste).
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

LABEL=""
ROLE=""
FACILITY_ID=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --label) LABEL="$2"; shift 2 ;;
    --role) ROLE="$2"; shift 2 ;;
    --facility-id) FACILITY_ID="$2"; shift 2 ;;
    *) echo "Option inconnue : $1" >&2; exit 1 ;;
  esac
done
[[ -n "$LABEL" ]] || { echo "ÉCHEC : --label requis"; exit 1; }
[[ -n "$ROLE" ]] || { echo "ÉCHEC : --role requis (ADMIN ou FACILITY_OPERATOR)"; exit 1; }

ENV_FILE="infrastructure/.env"
[[ -f "$ENV_FILE" ]] || { echo "ÉCHEC : $ENV_FILE manquant (copiez infrastructure/.env.example)"; exit 1; }
# shellcheck disable=SC1090
source "$ENV_FILE"
DB_PORT_HOST="${POSTGRES_PORT:-5432}"

JAR="backend/bootstrap/target/bootstrap-0.1.0-SNAPSHOT-exec.jar"
if [[ ! -f "$JAR" ]]; then
  echo "▶ Construction du jar (absent)"
  (cd backend && ./mvnw -B -q -pl bootstrap -am package -DskipTests)
fi

ARGS=(--spring.profiles.active=local
      --spring.main.web-application-type=none
      --identity.provision.enabled=true
      --identity.provision.label="$LABEL"
      --identity.provision.role="$ROLE")
[[ -n "$FACILITY_ID" ]] && ARGS+=(--identity.provision.facility-id="$FACILITY_ID")

DB_PASSWORD="$POSTGRES_PASSWORD" DB_HOST=localhost DB_PORT="$DB_PORT_HOST" \
DB_NAME="${POSTGRES_DB:-urgence_sante}" DB_USER="${POSTGRES_USER:-urgence_sante}" \
  java -jar "$JAR" "${ARGS[@]}"
