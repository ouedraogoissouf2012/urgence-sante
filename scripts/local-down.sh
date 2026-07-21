#!/usr/bin/env bash
# Arrête l'environnement local : backend puis base (volume supprimé).
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

if [[ -f /tmp/urgence-sante-local.pid ]]; then
  kill "$(cat /tmp/urgence-sante-local.pid)" 2>/dev/null || true
  rm -f /tmp/urgence-sante-local.pid
  echo "backend arrêté"
fi

docker compose -f infrastructure/docker-compose.yml --env-file infrastructure/.env down -v
echo "✓ Environnement local arrêté."
