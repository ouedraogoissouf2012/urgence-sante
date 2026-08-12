#!/usr/bin/env bash
#
# Garde anti-« skip silencieux » : vérifie, à partir des rapports Surefire du
# dernier `mvn verify`, que TOUTES les classes de test d'intégration PostGIS
# (celles qui étendent AbstractPostgisIntegrationTest — base réelle, voir
# docs/architecture/TEST_STRATEGY.md) ont bien été EXÉCUTÉES et non ignorées.
#
# La liste des classes est découverte dynamiquement (grep sur le code source)
# plutôt que codée en dur, pour qu'une nouvelle classe d'intégration soit
# automatiquement couverte sans devoir se souvenir de mettre à jour ce script.
#
# Sans cette garde, un `mvn verify` sans Docker joignable laisse ces tests
# passer en « Skipped » (profil développeur rapide) : le hook pre-push
# resterait vert alors qu'aucune requête n'a touché une vraie base — c'est
# exactement le trou qui a laissé passer la collision de migration Flyway du
# 10 août jusqu'à la CI (issue #154).
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT"

mapfile -t IT_SOURCE_FILES < <(
  grep -rlE 'extends[[:space:]]+AbstractPostgisIntegrationTest\b' \
      --include='*.java' backend 2>/dev/null | sort
)

if (( ${#IT_SOURCE_FILES[@]} == 0 )); then
  echo "ÉCHEC : aucune classe « extends AbstractPostgisIntegrationTest » détectée." >&2
  echo "Le mécanisme de découverte est cassé (chemin déplacé ? base renommée ?)." >&2
  echo "Corrigez ce script — ne le contournez pas, la garde anti-skip serait aveugle." >&2
  exit 1
fi

missing_classes=()
for src in "${IT_SOURCE_FILES[@]}"; do
  fqcn="$(sed -E 's#.*/src/test/java/##; s#/#.#g; s/\.java$//' <<< "$src")"
  module_root="${src%%/src/test/java/*}"
  report="$module_root/target/surefire-reports/$fqcn.txt"
  if [[ ! -f "$report" ]]; then
    missing_classes+=("$fqcn — rapport Surefire introuvable ($report)")
  elif ! grep -qE '^Tests run: [1-9]' "$report" || ! grep -q "Skipped: 0" "$report"; then
    summary="$(grep -h 'Tests run:' "$report" | tail -1)"
    missing_classes+=("$fqcn — ${summary:-rapport vide, aucun test recensé}")
  fi
done

if (( ${#missing_classes[@]} > 0 )); then
  echo "" >&2
  echo "ÉCHEC : ${#missing_classes[@]}/${#IT_SOURCE_FILES[@]} classe(s) d'intégration PostGIS ignorée(s) ou introuvable(s) :" >&2
  for entry in "${missing_classes[@]}"; do
    echo "  ✗ $entry" >&2
  done
  echo "" >&2
  echo "Docker a déjà été validé plus haut dans ce hook : la cause probable n'est PAS Docker," >&2
  echo "mais une classe renommée/déplacée ou une régression réelle. Ces tests doivent tourner" >&2
  echo "sur une base réelle à chaque push (issue #154) — ne contournez pas la garde." >&2
  exit 1
fi

echo "OK : ${#IT_SOURCE_FILES[@]} classes d'intégration PostGIS exécutées (Skipped: 0)."
