# ADR-007 — L'événement PortalCredentialProvisioned se passe de l'outbox transactionnel

- Statut : accepté
- Date : 2026-08-13

## Contexte

L'issue #164 constatait qu'après le retrait légitime du jeton ADMIN de
démonstration versionné (issue #124), il n'existait plus aucun chemin de
PRODUCTION pour créer un `portal_credential` — seule option restante, un
INSERT SQL manuel bypassant `TokenHasher`, risquant un jeton en clair en base
et sans aucune traçabilité. `ProvisionPortalCredentialUseCase` (module
`identity`) comble ce vide et publie un événement public
`PortalCredentialProvisioned`, consommable par le module `audit` selon le
même principe que `AvailabilityUpdated` (`ADR-006`).

`docs/architecture/ARCHITECTURE.md` §10 exige que « les événements critiques
utilisent un mécanisme de publication fiable », et §1 exige un ADR approuvé
pour toute dérogation. `AvailabilityUpdated` est le seul précédent
d'événement intermodule du dépôt, et il utilise un outbox transactionnel
(`OutboxRelay`, `@Scheduled`, table `_outbox`, reprise perpétuelle en cas
d'échec de publication). `PortalCredentialProvisioned` s'en écarte
délibérément : il est publié directement via
`ApplicationEventPublisher.publishEvent(...)`, dans la même transaction que
la persistance du credential, sans passage par une table outbox ni relais
planifié.

## Décision

1. `IdentityService.provision(...)` publie `PortalCredentialProvisioned`
   directement (synchrone, in-process) à l'intérieur du même
   `TransactionPort.inTransaction(...)` qui persiste le credential — pas
   d'outbox, pas de `@Scheduled` relais.
2. Cet écart est justifié par une différence de nature entre les deux
   producteurs, pas par un raccourci de confort :
   - `AvailabilityUpdated` est émis par le serveur HTTP long-lived, à haute
     fréquence relative (chaque mise à jour de disponibilité) — un
     `@Scheduled` relais a le temps et l'occasion de rattraper un échec de
     publication transitoire avant le prochain événement.
   - `PortalCredentialProvisioned` est émis par un processus **jetable**
     (`ProvisionPortalCredentialRunner`, `--spring.main.web-application-type=none`),
     qui se termine (`ConfigurableApplicationContext#close()`) immédiatement
     après avoir provisionné — l'action est rare, ponctuelle, déclenchée à la
     main. Un `@Scheduled` relais n'aurait structurellement pas l'occasion de
     tourner avant que le processus ne se ferme : l'infrastructure outbox
     serait présente mais inopérante pour ce producteur précis, un faux
     sentiment de fiabilité plutôt qu'une réelle garantie.
   - Répliquer l'infrastructure complète (table, adaptateur de relais, job
     planifié) pour un producteur qui ne peut structurellement pas
     bénéficier du relais est disproportionné pour une action admin rare.
3. Le prix assumé de ce choix : la fenêtre de perte possible est celle d'une
   dispatch synchrone in-process (le listener audit, à construire par un
   futur suivi hors du périmètre de #164, tourne en
   `@TransactionalEventListener(phase = AFTER_COMMIT)` — si le processus est
   tué entre le commit et l'exécution du listener, par exemple par la
   `mem_limit` du VPS, l'événement d'audit est perdu silencieusement, sans
   ligne outbox pour détecter ou rejouer l'écart). C'est une fenêtre bien
   plus étroite que celle de l'outbox lui-même (qui couvre un délai de poll
   pouvant aller jusqu'à `availability.outbox.relay-delay-ms`), mais non
   nulle — une dette explicite, pas une régression silencieuse.
4. Cette décision ne couvre QUE `PortalCredentialProvisioned`. Un futur
   événement émis par un producteur lui aussi structurellement jetable/rare
   peut suivre le même raisonnement sans nouvel ADR à lui seul ; un
   événement émis par un producteur long-lived à fréquence significative
   suit `AvailabilityUpdated` (outbox) par défaut.

## Conséquences

- Aucune ligne `_outbox` n'existe pour `PortalCredentialProvisioned` : un
  échec de publication (JVM tuée entre le commit et le listener) n'est ni
  détecté ni rejouable automatiquement — seule la ligne `portal_credential`
  elle-même reste la source de vérité que le provisioning a bien eu lieu.
- Si ce module gagne un jour un producteur d'événement long-lived à
  fréquence significative (hors provisioning ponctuel), il devra suivre le
  schéma outbox d'`AvailabilityUpdated`, pas celui-ci.
- Le listener côté `audit` consommant `PortalCredentialProvisioned` reste à
  construire (hors périmètre `backend/modules/identity/**` de #164) ; ce
  document s'applique dès sa construction, sans ADR supplémentaire requis
  pour le principe déjà couvert ici.
