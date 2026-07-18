#!/usr/bin/env bash
# Arrête la démonstration : backend puis base (volume supprimé).
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

if [[ -f /tmp/urgence-sante-demo.pid ]]; then
  kill "$(cat /tmp/urgence-sante-demo.pid)" 2>/dev/null || true
  rm -f /tmp/urgence-sante-demo.pid
  echo "backend arrêté"
fi

docker compose -f infrastructure/docker-compose.yml --env-file infrastructure/.env down -v
echo "✓ Démo arrêtée."
