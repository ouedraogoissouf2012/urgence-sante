#!/usr/bin/env bash
#
# Vérifie qu'aucun fichier source écrit manuellement ne dépasse la limite de
# lignes fixée par docs/architecture/QUALITY_RULES.md.
#
# Exclusions (QUALITY_RULES.md) : code généré, artefacts de build, dépendances
# tierces. Les migrations SQL et les fichiers de verrouillage ne font pas partie
# des extensions scannées.
#
# Sortie non nulle si au moins un fichier dépasse la limite : la règle échoue de
# manière visible et ne se désactive pas silencieusement.
set -euo pipefail

MAX_LINES=300
EXTENSIONS=(java dart)

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT"

# Construit l'expression « -name '*.ext' [-o ...] » pour find.
name_expr=()
for i in "${!EXTENSIONS[@]}"; do
  (( i > 0 )) && name_expr+=(-o)
  name_expr+=(-name "*.${EXTENSIONS[$i]}")
done

violations=0
while IFS= read -r -d '' file; do
  lines=$(wc -l < "$file")
  if (( lines > MAX_LINES )); then
    printf '  %-72s %5d lignes\n' "${file#./}" "$lines"
    violations=$((violations + 1))
  fi
done < <(
  find . \
    \( -type d \( -name target -o -name build -o -name .dart_tool \
        -o -name node_modules -o -name .git -o -name generated \) -prune \) \
    -o \( -type f \( "${name_expr[@]}" \) -print0 \)
)

if (( violations > 0 )); then
  echo ""
  echo "ÉCHEC : ${violations} fichier(s) dépassent ${MAX_LINES} lignes."
  echo "Découpez le fichier (responsabilité unique) ; ne désactivez pas la règle."
  exit 1
fi

echo "OK : aucun fichier source (${EXTENSIONS[*]}) ne dépasse ${MAX_LINES} lignes."
