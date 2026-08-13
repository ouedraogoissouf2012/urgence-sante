# ADR-006 — Le module Audit consomme les événements publics des modules métier

- Statut : accepté
- Date : 2026-08-12

## Contexte

L'issue #136 constatait que les modules `audit` et `notification` étaient des
coquilles vides : aucun `@EventListener`/`@ApplicationModuleListener` n'existe
dans le dépôt, alors que `OutboxRelay` (module Availability) publie déjà
`AvailabilityUpdated` via `ApplicationEventPublisher`. Aucune trace
persistante ne relie un fait métier ou de sécurité à un acteur, une action,
une ressource et un instant — inacceptable pour une application manipulant
des données de santé.

`docs/architecture/MODULE_DEPENDENCIES.md` anticipait déjà cette relation
(chaque module métier porte la mention « événements » dans sa colonne
`audit ») mais ne documentait pas la flèche inverse réellement nécessaire :
pour écouter `AvailabilityUpdated`, le module `audit` doit dépendre, à la
compilation, de l'API publique du module `availability` (import du type
d'événement). Cette flèche `audit → availability` est nouvelle et exige, par
la règle 6 de cette même matrice, une mise à jour du document et un ADR.

## Décision

1. `audit` dépend de l'API publique (racine, hors `internal`) des modules
   métier dont il consomme les événements — aujourd'hui uniquement
   `availability` (`AvailabilityUpdated`). Cette dépendance Maven est
   strictement descendante : aucun module métier ne dépend d'`audit`, et
   `audit` n'appelle jamais l'API d'un autre module de façon synchrone
   (règle 2 de la matrice, inchangée).
2. Le consommateur est un `@TransactionalEventListener(phase = AFTER_COMMIT)`
   (`AvailabilityUpdatedAuditListener`), pas un `@ApplicationModuleListener` de
   Spring Modulith : ce dernier introduirait un nouveau paradigme (registre de
   publication, retards asynchrones) sans précédent dans le dépôt, alors que
   la sémantique recherchée — n'agir qu'après le commit durable du relais
   outbox source — est déjà couverte par l'API `@TransactionalEventListener`
   standard de Spring.
3. L'identité d'une ligne d'audit est celle de l'événement source
   (`source_event_id`), pas un identifiant généré séparément : une ligne par
   événement, et la contrainte de clé primaire rend l'insertion idempotente
   face à la livraison « au moins une fois » de l'outbox.
4. L'acteur (`actor_id`/`actor_label`) reste **nullable** : aucun événement
   actuel ne porte l'acteur ayant déclenché le fait (voir auto-critique de la
   PR #136 pour le détail du chaînon manquant côté `PortalSecurityInterceptor`
   → `AvailabilityController`). Combler ce point exige une modification du
   module `availability` (et potentiellement `identity`), hors périmètre de
   cette PR.
5. `notification` reste une coquille vide : aucun besoin métier concret ne
   justifie aujourd'hui d'y construire une infrastructure de notification.
6. `audit` s'écarte de l'anatomie canonique de `docs/architecture/
   ARCHITECTURE.md` §6 sur deux points, tous deux volontaires : (a) aucun
   `ModuleFacade`/`ModuleView`/`ModulePublicEvent` public à la racine du
   module — cohérent avec sa ligne dans `MODULE_DEPENDENCIES.md` (« API
   publique : événements publics ») et la règle 2 (« les modules métier ne
   l'appellent pas directement ») : `audit` n'a rien à exposer synchrone
   tant qu'aucun consommateur de SES propres événements/lectures n'existe ;
   (b) un nouveau sous-dossier `internal/adapter/in/event/` (au lieu de
   `in/web/`) — même principe que `in/web/` (adaptateur entrant), seule la
   technologie change. Les futurs modules purement réactifs (`notification`,
   le jour où un besoin réel apparaît) suivent le même schéma.

## Conséquences

- `docs/architecture/MODULE_DEPENDENCIES.md` : la case (audit, availability)
  passe de « non » à « événements », avec une clarification ajoutée sur le
  sens des cases « événements » (flux, pas dépendance de compilation — sans
  cela, les cases symétriques `availability`↔`audit` se lisent à tort comme
  un cycle).
- Toute future dépendance d'`audit` vers un autre module producteur
  d'événements (facility, identity, …) suit le même schéma et n'exige pas un
  nouvel ADR à elle seule — celui-ci couvre le principe général, y compris
  la dérogation à l'anatomie canonique (point 6).
- La corrélation acteur → fait reste une dette explicite, pas une régression :
  elle nécessite un changement dans un module hors du périmètre d'#136.
- **Aucune reprise automatique** si l'écriture d'`audit` échoue après que
  l'événement source a été marqué publié : contrairement à `OutboxRelay`
  (`attempts` + reprise perpétuelle côté `availability`), un échec transitoire
  de `AuditPersistenceAdapter` (pool épuisé, coupure DB brève) perd la ligne
  d'audit correspondante de façon définitive et silencieuse (seul un
  `LOG.error` est émis). Accepté comme dette explicite pour cette PR — un
  vrai mécanisme de reprise (deuxième outbox, ou métrique/alerte a minima)
  reste à faire si la fiabilité de la trace devient critique.
- **Piège transactionnel à connaître pour TOUT futur `@TransactionalEventListener`
  écrivant en base, dans N'IMPORTE QUEL module** (pas seulement `audit`) :
  1. `phase = AFTER_COMMIT` s'exécute alors que les ressources de la
     transaction déclenchante sont encore liées au thread (le nettoyage
     Spring n'intervient qu'après `afterCommit()`). Un appel `@Transactional`
     imbriqué SANS `propagation = REQUIRES_NEW` y « participe » silencieusement
     au lieu d'ouvrir une nouvelle transaction : l'écriture s'exécute sans
     lever d'exception mais n'est **jamais committée**. Constaté en test réel
     (`AuditIntegrationTest`), pas une précaution théorique.
  2. Avec `REQUIRES_NEW` seul, si l'entité JPA a un identifiant assigné
     manuellement (pas de `@GeneratedValue`), Spring Data route l'écriture
     vers `entityManager.merge(...)` plutôt que `persist(...)` : l'INSERT réel
     est différé jusqu'au flush (donc jusqu'au commit), ce qui rend inopérant
     tout `catch` local d'une violation de contrainte. `saveAndFlush(...)`
     (pas `save(...)`) est nécessaire pour forcer l'exécution synchrone de
     l'INSERT — voir `AuditPersistenceAdapter`.
