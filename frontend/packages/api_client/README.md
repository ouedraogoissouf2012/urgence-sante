# api_client

Client REST **généré** depuis la spécification OpenAPI
[`docs/api/openapi.yaml`](../../../docs/api/openapi.yaml) (voir
[ADR-004](../../../docs/decisions/ADR-004-contract-first-api.md)).

## Génération (reproductible)

```bash
bash scripts/generate-api-client.sh
```

- Générateur : `openapi-generator` **dart**, version figée dans
  [`openapitools.json`](../../../openapitools.json) (7.23.0).
- Sortie : le contenu de `lib/` (models, api, client HTTP) est **entièrement
  généré** — ne pas l'éditer à la main (en-tête `AUTO-GENERATED`).
- Fichiers manuels préservés lors de la régénération via
  [`.openapi-generator-ignore`](.openapi-generator-ignore) : ce `README.md`, le
  `pubspec.yaml` et `analysis_options.yaml`.

## Isolation

Le code généré n'est pas soumis aux lints manuels stricts ni à la limite des
300 lignes (contrôles exclus pour le code généré). Toute évolution passe par la
spécification OpenAPI, jamais par une retouche du code généré.
