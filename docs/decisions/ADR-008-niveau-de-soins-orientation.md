# ADR-008 — Niveau de soins : seuls les 2e et 3e niveaux sont proposés en urgence

- Statut : accepté
- Date : 2026-10-24

## Contexte

Le client a transmis, pour Port-Bouët, un tableau classant chaque
établissement par **niveau de la pyramide sanitaire** avec une consigne
explicite :

| Niveau | Consigne client |
|---|---|
| 1er niveau (centres de santé de proximité) | **ne pas afficher** — ne traite pas les urgences |
| 2e niveau (hôpitaux généraux) | afficher — traite certaines urgences |
| 3e niveau (CHU, établissements de référence) | afficher — traite toutes les urgences |

Avant cette décision, aucun champ ne portait ce niveau. L'orientation proposait
tout établissement offrant au moins un des services liés au symptôme
(`FacilitySpringRepository`, filtre `service_code IN (...)`). Or « offre un
service » n'est pas « prend en charge l'urgence » : un centre de santé avec une
maternité de proximité aurait été proposé pour une urgence obstétricale. La
consigne n'était respectée que **par hasard**, l'annuaire de production (5
centres) ne contenant aucun 1er niveau.

## Décision

1. **Le niveau de soins est une donnée obligatoire de l'annuaire**
   (`facility.care_level`, migration V15) : `PRIMARY`, `SECONDARY` ou
   `TERTIARY`, sans valeur par défaut, contrainte `CHECK`.
2. **La règle vit dans le domaine** : `CareLevel.isEmergencyCapable()` (module
   `facility`) est faux pour `PRIMARY` et vrai pour `SECONDARY`/`TERTIARY`.
   Aucune autre couche ne recopie cette règle.
3. **L'orientation ne reçoit que des centres accueillant les urgences** :
   `FacilityFacade.findEmergencyCapableNearby` remplace `findNearbyOfferingAny`
   (et la variante mono-service `findNearbyOffering`, sans appelant). Le nom
   rend la sémantique visible à l'appel ; aucune méthode existante ne change
   de comportement en silence.
4. **Le filtre s'applique dans le SQL, avant le `LIMIT`** : le vivier de
   candidats de l'orientation est borné (30) ; un filtrage après coup laisserait
   des centres de 1er niveau proches évincer tous les hôpitaux.
5. **L'annuaire public reste complet** : `GET /api/v1/facilities` (portail
   hospitalier) liste tous les niveaux ; seule l'orientation d'urgence écarte
   le 1er niveau.
6. **Les centres de 1er niveau restent importables** : ils sont conservés en
   base (information utile à un futur usage non urgent), simplement jamais
   proposés pour une urgence.
7. **Import strict** : un `careLevel` absent ou inconnu rejette la ligne avec un
   motif au rapport d'import — jamais affichée ni masquée en silence, et sans
   interrompre le reste du lot.
8. La distinction « certaines » / « toutes » les urgences (2e vs 3e niveau)
   **n'est pas** portée par le niveau : elle l'est déjà par les services offerts
   (un centre n'est proposé que pour un besoin couvert par ses services).

## Migration des données existantes (V15)

- Les 5 centres du jeu de démarrage réel reçoivent leur niveau explicitement
  (CHU : `TERTIARY` ; hôpitaux généraux : `SECONDARY`), conforme au tableau
  client pour le CHU de Treichville et l'HG de Port-Bouët. Le réimport au
  démarrage (profil production) réaffirme ensuite ces valeurs depuis le JSON :
  l'upsert met à jour `care_level`, un reclassement s'applique donc au rejeu.
- Tout autre établissement (aucun attendu) est classé `PRIMARY` — choix
  conservateur : jamais proposé pour une urgence tant qu'il n'est pas reclassé —
  et **signalé** par un `RAISE WARNING` dans le journal de migration.

## Conséquences

- Le respect de la consigne client ne dépend plus de la vigilance de la saisie
  à chaque import.
- La cellule annuaire doit renseigner le niveau de chaque nouvel établissement
  (colonne « niveau » à ajouter au fichier de collecte du client).
- Le contrat OpenAPI documente la règle sur `/orientation` et `/facilities` ;
  le niveau n'est **pas** exposé dans les réponses (aucun besoin d'affichage
  exprimé) — à ajouter, avec régénération du client Dart, si un écran le
  demande.
- Risque résiduel : si un import ne contenait que des centres de 1er niveau,
  l'orientation ne proposerait plus aucun centre (les gardes de démarrage ne
  vérifient que la présence d'établissements non-démo). Le jeu de démarrage
  actuel n'expose pas ce cas.
