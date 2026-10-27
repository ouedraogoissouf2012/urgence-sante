# Provenance de l'annuaire réel (`abidjan-starter.json`)

**Règle : une donnée douteuse est retirée.** Une valeur n'est conservée que si
elle est attestée par une source fiable — pour les positions, l'emprise du
bâtiment cartographiée ; pour les téléphones et les services, au moins deux
sources concordantes. Sinon : téléphone supprimé, service retiré, ou
établissement retiré (migration dédiée, l'import ne supprimant jamais rien).

Chaque établissement reste `PROVISIONAL` tant que la cellule annuaire ne l'a
pas vérifié sur le terrain ([ADR-005](../../docs/decisions/ADR-005-annuaire-perimetre.md)).
Niveau de soins : consigne du client ([ADR-008](../../docs/decisions/ADR-008-niveau-de-soins-orientation.md)).
Vérifications du 27/10/2026.

## Établissements retenus

| Établissement | Niveau | Position | Téléphone | Services | Sources |
|---|---|---|---|---|---|
| CHU de Cocody | 3e | 5.3423, -3.9939 — centre de l'emprise OSM (way 327402561) ; ancienne valeur décalée de 1,0 km | +225 27 22 48 10 00 (valeur d'origine retrouvée dans une source publique) | inchangés (CHU de référence) | OpenStreetMap ; lepetitjournal / abidjan.net (annuaires) |
| CHU de Treichville | 3e | 5.2916, -4.0036 — emprise OSM (way 91274323) ; ancienne décalée de 1,2 km | +225 27 21 24 91 55 (l'ancien numéro n'apparaît nulle part) | + gynéco-obstétrique / maternité, psychiatrie | OSM ; annuaireci / abidjan.net pages jaunes ; afrique-sur7 & pressecotedivoire (réouverture gynéco-obstétrique) ; UVCI (service de psychiatrie du CHU-T) |
| CHU de Yopougon | 3e | 5.3584, -4.0898 — emprise OSM (way 91273250) ; ancienne décalée de 2,4 km | +225 27 23 53 75 75 (OSM + annuaires concordants) | inchangés (CHU de référence) | OSM ; chuyopougon.ci / annuaireci |
| Hôpital Général d'Abobo (Abobo-Sud) | 2e | 5.4237, -4.0164 — emprise OSM (way 785843158) ; ancienne décalée de 0,6 km | **supprimé** : aucune concordance (et deux « HG d'Abobo » existent) | urgences, maternité, pédiatrie, chirurgie (gynécologie retirée : non attestée) | OSM ; vfmatch.org / incotedivoire.net |
| Hôpital Général de Port-Bouët | 2e | 5.2629, -3.9621 — emprise OSM (way 785830346) ; l'ancienne valeur tombait dans l'aéroport | +225 27 21 27 85 00 (OSM + annuaires concordants ; l'ancien 27 21 27 **15** 00 était faux) | urgences, chirurgie, pédiatrie, maternité, obstétrique (ophtalmologie et gynécologie retirées : une seule source) | OSM ; construction21.org (≈ 10 000 accouchements/an, 12 000 urgences/an, blocs opératoires et obstétricaux) ; linfodrome.com & medpages (service de pédiatrie) |
| Centre de Santé Urbain Communautaire de Gonzagueville | 1er | 5.2402, -3.8961 (OSM, établissement typé « clinic ») | — (numéro publié à l'ancien format, non converti) | aucun (jamais proposé en urgence) | OSM ; page publique de l'établissement (existence) |

## Établissements retirés (doute non levé) — migration V16

| Établissement (tableau client) | Raison |
|---|---|
| Clinique Sainte Marie Port-Bouët | aucun service d'urgence attesté publiquement, alors qu'elle était proposée aux patients |
| Centre Médical Victoria | position approximative |
| Centre de santé rural CNRA de Port-Bouët | position approximative |
| Médico-scolaire de Port-Bouët | une seule source, sans type d'établissement |

Réintégration possible dès qu'une vérification (appel, visite) lève le doute.
