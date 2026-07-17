#!/usr/bin/env bash
#
# Valide le contrat OpenAPI avec Redocly (règles figées dans redocly.yaml).
# Échoue avec un code non nul si la spécification est invalide.
#
# Prérequis : Node.js (npx). Réseau requis au premier lancement (téléchargement).
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

npx --yes @redocly/cli@latest lint
