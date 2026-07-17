#!/usr/bin/env bash
#
# Régénère le client Dart depuis le contrat OpenAPI, de façon reproductible.
#
# - Spécification    : docs/api/openapi.yaml
# - Générateur       : openapi-generator « dart », version figée dans
#                      openapitools.json.
# - Sortie           : frontend/packages/api_client/lib (fichiers générés).
# - Fichiers manuels : préservés via
#                      frontend/packages/api_client/.openapi-generator-ignore.
#
# Prérequis : Node.js (npx) et un JDK (le générateur est en Java).
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

npx --yes @openapitools/openapi-generator-cli generate \
  --input-spec docs/api/openapi.yaml \
  --generator-name dart \
  --output frontend/packages/api_client \
  --additional-properties=pubName=api_client,pubVersion=1.0.0

# Retire l'override « // @dart=2.18 » des fichiers générés : il est incompatible
# avec le pub workspace (qui exige un langage >= 3.5) et inutile ici (le code
# généré fonctionne sous le SDK du package).
find frontend/packages/api_client/lib -name '*.dart' -type f \
  -exec sed -i '/^\/\/ @dart=/d' {} +

echo "OK : client Dart régénéré dans frontend/packages/api_client/lib"
