# Provenance de l'annuaire réel (`abidjan-starter.json`)

Chaque établissement reste `PROVISIONAL` tant que la cellule annuaire ne l'a
pas vérifié sur le terrain ([ADR-005](../../docs/decisions/ADR-005-annuaire-perimetre.md)).
Le niveau de soins suit la consigne du client ([ADR-008](../../docs/decisions/ADR-008-niveau-de-soins-orientation.md)).
Recherches du 27/10/2026.

## Centres du tableau client (Port-Bouët)

| Établissement | Niveau | Position | Téléphone | Services retenus | Sources |
|---|---|---|---|---|---|
| Hôpital Général de Port-Bouët | 2e | 5.2629, -3.9621 — emprise du bâtiment cartographiée (5.2616–5.2641, -3.9634–-3.9609) ; ancienne valeur (-3.9264) située **dans l'aéroport**, corrigée | +225 27 21 27 15 00 | urgences, chirurgie, pédiatrie, maternité, gynéco-obstétrique, ophtalmologie | OpenStreetMap (way « Hôpital général de Port-Bouët ») ; repère « Groupe Scolaire Selmer Hôpital » à 340 m ; vfmatch.org (adresse) ; Ministère de la Santé ; linfodrome.com (pédiatrie, 15 lits, néonatologie) ; construction21.org (≈ 10 000 accouchements/an, 12 000 urgences/an) |
| CHU de Treichville | 3e | inchangée | inchangé | + gynéco-obstétrique, maternité (service rouvert, 70 lits), psychiatrie (service de neuro-psychiatrie) | sante.gouv.ci (ressources du CHU-T) ; afrique-sur7.fr / pressecotedivoire.fr (réouverture gynéco-obstétrique) |
| Clinique Sainte Marie Port-Bouët | 3e | 5.2588, -3.9672 | +225 27 21 58 30 48 | urgences **seulement** (seul service attesté ; établissement « pluridisciplinaire » sans détail publié) | pharmacies-de-garde.ci et pratik-ci.com (positions concordantes à 50 m) ; ⚠️ la « Clinique Sainte Marie » d'annuaireci.com est un AUTRE établissement (Adjamé) |
| Centre Médical Victoria | 1er | 5.2394, -3.8966 — **approximative** : « carrefour de Gonzagueville, après le corridor » | +225 01 42 03 21 56 | aucun (jamais proposé en urgence) | mupemenetci.org (adresse, téléphone) ; OpenStreetMap (Corridor de Gonzagueville) |
| Centre de Santé Urbain Communautaire de Gonzagueville | 1er | 5.2402, -3.8961 | — (numéro publié à l'ancien format 8 chiffres, non converti) | aucun | OpenStreetMap |
| Médico-scolaire de Port-Bouët | 1er | 5.2511, -3.9567 | — | aucun | OpenStreetMap (« Medico Scolaire ») |
| Centre de santé rural CNRA de Port-Bouët | 1er | 5.2428, -3.8865 — **approximative** : rue Marc Delorme (station CNRA de Port-Bouët) | — | aucun | abidjan.net (station Marc Delorme du CNRA, Port-Bouët) ; OpenStreetMap (rue Marc Delorme) |

Le tableau client cite deux fois le CSU Communautaire de Gonzagueville : une
seule entrée est créée.

## À confirmer sur le terrain (un appel suffit)

- **Clinique Sainte Marie** (27 21 58 30 48) : liste des services d'urgence
  réellement assurés (maternité ? pédiatrie ? chirurgie ? réanimation ?) — le
  client la dit « 3e niveau, toutes les urgences », non attesté publiquement.
- **Centre Médical Victoria** et **CSR CNRA** : position exacte.
