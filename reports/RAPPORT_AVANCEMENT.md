# Rapport d'avancement — Urgence Santé (Lot 1)

| | |
|---|---|
| **Projet** | Application « Urgence Santé » — orientation d'urgence, Grand Abidjan |
| **Commanditaire** | NAPCOR GROUP |
| **Périmètre** | Lot 1 (MVP Android, sans temps réel) |
| **Date du rapport** | 2026-07-19 |
| **Branche de référence** | `main` |
| **État de fusion** | Dernier commit `main` **✅ VALIDE** (rapport de validation interne du 2026-07-19T19-55) |

> Ce rapport est établi à partir du **code et de l'historique Git réels**, pas de
> la documentation d'intention. Les chiffres sont mesurés sur le dépôt.

---

## 1. En une phrase

Le cœur du Lot 1 est **construit et vérifié** : une application patient Android
qui oriente vers le centre de santé adapté le plus proche d'Abidjan (carte,
tri par proximité, itinéraire, appel des secours, mode hors-ligne de base),
adossée à un back-end Spring Boot de production avec base PostGIS.

---

## 2. Ce qui est fait (mesuré)

| Indicateur | Valeur |
|---|---|
| Commits sur `main` | **41** |
| Modules back-end livrés | **8** (`facility`, `medical-service`, `availability`, `routing`, `orientation`, `identity`, `audit`, `notification`) |
| Fichiers source Java (hors tests/build) | **~186** |
| Classes de test Java (unit + intégration) | **40** |
| Fichiers source Dart (hors généré/build) | **~113** |
| Fichiers de test Dart | **20** |
| Migrations base de données (Flyway) | **V1 → V8** |
| Contrat d'API | OpenAPI v1 (`docs/api/openapi.yaml`) + client Dart généré |
| Applications Flutter | **2** (`patient_mobile`, `hospital_portal`) |

---

## 3. Fonctionnalités du Lot 1 — état par exigence

| # | Exigence Lot 1 | État | Preuve (commit / module) |
|:--:|---|:--:|---|
| L1 | Accueil, conditions, consentement localisation | ✅ | `feat(frontend) issue #38` — feature `onboarding` |
| L2 | Carte + position utilisateur (OSM) | ✅ | `feat(app) issue #39` — feature `orientation` |
| L3 | Annuaire géolocalisé des centres | ✅ | module `facility` (import traçable, idempotent — #41) |
| L4 | Recherche du plus proche (moteur géospatial) | ✅ | `facility` — recherche PostGIS (KNN) — #9 |
| L5 | Itinéraire & temps de trajet | ✅ | module `routing` + adaptateur OSRM — #12 / #44 |
| L6 | Filtre par besoin (urgences, maternité…) | ✅ | modules `medical-service` + `orientation` — #10 / #13 |
| L7 | Appel direct SAMU 185 / Pompiers 180 | ✅ | `core/calls` (patient_mobile) |
| L8 | Mode hors-ligne de base | ✅ | `feat(frontend) issue #40` — `core/storage` |
| L9 | Socle technique de production | ✅ | voir §4 |

**Bilan : les 9 exigences fermes du Lot 1 sont couvertes par du code livré.**

---

## 4. Socle technique de production (L9) — détail

Au-delà du simple CRUD, le socle porte des garanties de niveau production :

- **Architecture** : monolithe modulaire **hexagonal**, sens de dépendance
  `adapter → application → domain` imposé par des **gardes exécutables**
  (Spring Modulith + ArchUnit) qui échouent la build en cas de violation.
- **Base de données** : PostgreSQL + **PostGIS**, migrations **Flyway V1→V8**,
  intégrité référentielle (#37), tests d'intégration exécutés sur **base
  éphémère vierge** avec contrôle **anti-skip** (aucun test silencieusement sauté).
- **Résilience routing** : appel groupé OSRM, **circuit breaker**, temps de
  trajet qualifié (#44).
- **Fiabilité événementielle** : **outbox transactionnel** pour la publication
  fiable des changements de disponibilité (#43).
- **Validation** : bornes serveur, rejet des valeurs `NaN`/`Infini` sur toutes
  les requêtes (#45).
- **Sécurité portail** : jeton porteur (JWT), portée (scope), **limitation de
  débit** (#42).
- **Observabilité** : endpoint de santé, corrélation des requêtes, logs
  structurés, métriques (#46).
- **Qualité imposée** : aucun fichier manuel > **300 lignes** (vérifié en CI locale).

---

## 5. Au-delà du strict Lot 1 (déjà amorcé)

- **Portail hospitalier** (`hospital_portal`) : application de saisie de
  disponibilité pour agents — brique de la V2, déjà présente et sécurisée (#15, #42).
- **Module `availability`** : statuts, fraîcheur, historique, événements —
  le socle du « statut déclaratif horodaté » de la V2 est posé.
- **Démonstration bout-en-bout** : scénario MVP automatisé et **mesuré**,
  tests e2e du parcours patient **et** du parcours agent dans le navigateur
  (Chrome), APK de démonstration téléchargeable (#48, #16).

> Note : `audit` et `notification` sont encore à l'état de squelette
> (1 fichier chacun) — conformes à leur statut « hors périmètre / phase
> ultérieure » du Lot 1.

---

## 6. Qualité & processus de validation

- **Source de vérité** : `scripts/verify-all.sh` (la CI cloud GitHub Actions
  est en place mais non financée pour l'instant).
- Chaque contrôle produit un **rapport daté** dans `reports/verification/`.
- **Règle de fusion** : une branche ne rejoint `main` qu'avec un rapport
  **✅ VALIDE** couvrant son commit de tête.
- **Dernière validation complète (2026-07-19)** — tous les contrôles au vert :
  - Limite 300 lignes ✅
  - Contrat OpenAPI valide ✅
  - Back-end + tests PostGIS réels (`mvn verify`) → **BUILD SUCCESS** ✅
  - Anti-skip PostGIS (3 classes, `Skipped: 0`) ✅
  - Flutter analyze → **No issues found** ✅
  - Tous les tests Flutter (5 workspaces) → **All tests passed** ✅
  - APK Android : ⚪ N/A local (chemin de dépôt non-ASCII → build délégué à la CI Linux)

---

## 7. Points d'attention / limites assumées

1. **Temps réel** : hors périmètre Lot 1 par décision stratégique (donnée
   inexistante en CI) — ce n'est pas un retard, c'est un choix documenté.
2. **APK en local** : le build APK ne passe pas sur cette machine à cause du
   chemin non-ASCII du dépôt (`propre à moi`) ; il est délégué à un job CI Linux.
   → **À surveiller** avant toute livraison binaire.
3. **CI cloud non financée** : la garde de fusion repose actuellement sur la
   validation locale `verify-all.sh`. À rebrancher sur GitHub Actions dès que
   le compte est débloqué.
4. **Données Abidjan** : la fiabilisation manuelle des établissements
   (coordonnées + services réels) reste le poste le plus long — à confirmer sur
   volume et fraîcheur.
5. **Le `README.md` racine est périmé** : il indique « aucun code métier » alors
   que 8 modules sont livrés. → à mettre à jour.

---

## 8. Prochaines étapes suggérées

- [ ] Corriger le `README.md` racine (refléter l'état réel).
- [ ] Sécuriser le build APK (CI Linux) et produire un binaire de démo à jour.
- [ ] Fiabiliser le jeu de données Abidjan (150–300 centres) et mesurer la couverture.
- [ ] Rebrancher la CI cloud comme garde de fusion officielle.
- [ ] Cadrer la V2 (statut de disponibilité déclaratif) à partir du module `availability` existant.

---

*Rapport généré à partir de l'historique Git et du code source du dépôt.
Les affirmations « ✅ » correspondent à du code livré et validé, pas à des intentions.*
