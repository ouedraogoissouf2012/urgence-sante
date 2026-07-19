#!/usr/bin/env bash
#
# TEST E2E NAVIGATEUR de l'application patient (parcours complet dans un vrai
# moteur web, Chrome headless via chromedriver). Déterministe (fausses
# dépendances), donc CI-compatible.
#
# Prérequis : SDK Android/Flutter, chromedriver correspondant à la version de
# Chrome, lancé sur le port 4444 :
#   chromedriver --port=4444 &
#
# Sur Windows, le chemin du dépôt contient un caractère non-ASCII (« à ») qui
# fait échouer le compilateur de shaders Flutter : exécuter depuis un chemin
# ASCII (git worktree sous C:\tmp\...). Le job CI (Linux) n'a pas ce souci.
set -uo pipefail

ROOT="$(git rev-parse --show-toplevel)"
APP="$ROOT/frontend/apps/patient_mobile"
PORT="${CHROMEDRIVER_PORT:-4444}"

if ! curl -sf "http://localhost:$PORT/status" >/dev/null 2>&1; then
  echo "ÉCHEC : chromedriver introuvable sur :$PORT — lancez 'chromedriver --port=$PORT &'"
  exit 1
fi

cd "$APP"
echo "▶ E2E navigateur (Chrome headless) — parcours patient"
flutter drive \
  --driver=test_driver/integration_test.dart \
  --target=integration_test/parcours_e2e_test.dart \
  -d web-server --browser-name=chrome --headless "$@"
