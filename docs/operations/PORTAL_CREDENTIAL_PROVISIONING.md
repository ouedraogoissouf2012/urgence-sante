# Provisionnement d'un credential portail

Contexte : après le retrait du jeton ADMIN de démonstration versionné
(issue #124), il n'existait plus aucun chemin de PRODUCTION pour créer un
`portal_credential` — seule option restante, un INSERT SQL manuel bypassant
`TokenHasher`, risquant un jeton en clair en base et sans aucune traçabilité
(issue #164). Ce document décrit le chemin officiel, qui remplace cette
pratique.

## Comment ça marche

Le provisionnement s'exécute comme un processus **jetable** du même jar que
le serveur (`--spring.main.web-application-type=none`, aucun port HTTP
ouvert), gardé par DEUX conditions cumulatives sur
`ProvisionPortalCredentialRunner` (module `identity`) : la propriété
`identity.provision.enabled=true` ET l'absence de serveur web
(`@ConditionalOnNotWebApplication`) — une garde structurelle délibérée : si
cette propriété était un jour laissée par erreur dans la configuration d'un
serveur normal (web), elle resterait totalement inerte, au lieu de fermer le
serveur en production juste après son démarrage. Concrètement :

1. un jeton opaque de 32 octets aléatoires (`SecureRandom`,
   `OpaqueTokenGenerator`) est généré ;
2. seule son **empreinte SHA-256** (`TokenHasher`, la même logique que
   l'authentification du portail) est persistée — jamais le jeton en clair ;
3. le credential et un événement d'audit (`PortalCredentialProvisioned`) sont
   écrits dans la **même transaction** ;
4. le jeton en clair est imprimé **une seule fois** sur stdout — il n'est ni
   journalisé, ni ré-affichable ensuite ;
5. le contexte Spring est fermé explicitement
   (`ConfigurableApplicationContext#close()`, jamais `System.exit`) — laissé
   vivant, le processus resterait bloqué indéfiniment à cause des threads
   non-démons des tâches planifiées du serveur (`@EnableScheduling`). Un
   échec (validation refusée, propriété CLI malformée...) n'a besoin
   d'aucun traitement particulier : Spring Boot ferme déjà le contexte de
   lui-même avant de faire échouer le lancement.

Pourquoi un processus CLI plutôt qu'un endpoint HTTP protégé par
`PortalRole.ADMIN` (l'autre option envisagée pour cette issue) : un endpoint
admin ne peut par construction pas créer le **premier** credential ADMIN —
il n'existe alors aucun jeton pour s'y authentifier (problème de démarrage).
Le CLI, lui, fonctionne aussi bien pour le tout premier credential que pour
les suivants, sans exposer de surface HTTP supplémentaire pour une action
rare et sensible. Un endpoint HTTP `POST /api/v1/portal/credentials` reste
une extension possible plus tard (le cas usuel — équipe déjà en place, un
admin en crée d'autres) ; ce runbook ne le couvre pas.

## Local (poste de développement)

Prérequis : base PostGIS locale démarrée (`scripts/local-up.sh` ou
équivalent), `infrastructure/.env` présent.

> `scripts/local-up.sh` insère déjà, pour son propre confort, UN credential
> ADMIN de démo par SQL direct (jeton par défaut `demo-samu-admin-2026`,
> surchargeable via `PORTAL_TOKEN`) — c'est justement l'anti-pattern que cette
> issue corrige pour la PRODUCTION ; en local, ce raccourci reste acceptable
> et n'est pas retiré. Le script ci-dessous sert à provisionner des
> credentials SUPPLÉMENTAIRES (un opérateur d'établissement, par exemple) ou
> à répéter localement le chemin de production avant de l'exécuter pour de
> vrai.

```bash
scripts/provision-portal-credential.sh --label "Régulation SAMU" --role ADMIN
scripts/provision-portal-credential.sh --label "Hôpital X" --role FACILITY_OPERATOR --facility-id <uuid>
```

Le jeton s'affiche sur la ligne `TOKEN=...` de la sortie. Testez-le
immédiatement :

```bash
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8090/api/v1/facilities/<id>/availability/<code>
```

## Production (VPS)

La base n'est joignable que depuis le réseau Docker interne défini par
`deploy/docker-compose.yml` (pas de port publié) — un `java -jar` lancé
directement sur l'hôte ne peut pas s'y connecter. On lance donc un conteneur
jetable qui partage la même image, le même réseau et le même fichier d'env
que le service `api` déjà déployé :

```bash
cd deploy
docker compose --env-file .env run --rm api \
  --spring.main.web-application-type=none \
  --identity.provision.enabled=true \
  --identity.provision.label="Régulation SAMU" \
  --identity.provision.role=ADMIN
```

Pour un opérateur d'établissement, ajoutez
`--identity.provision.facility-id=<uuid>` et `--identity.provision.role=FACILITY_OPERATOR`.

Sans `--spring.profiles.active=production` délibérément : `DemoDataProductionGuard`
(module `facility`) est un autre `ApplicationRunner`, actif uniquement sous ce
profil, sans ordre défini par rapport à celui-ci — les combiner dans la même
invocation risquerait une interaction (l'un empêchant l'autre de s'exécuter)
sans bénéfice réel ici : ce processus jetable n'ouvre jamais de port HTTP et
n'a donc rien à voir avec les données DEMO que ce garde protège.

Le jeton affiché (ligne `TOKEN=...`) doit être transmis à son destinataire
par un canal sécurisé (jamais par email en clair, jamais collé dans un
ticket ou un chat non chiffré), puis l'historique du terminal doit être
effacé (`history -c` ou fermeture du terminal).

`docker compose run --rm` supprime le conteneur jetable en fin d'exécution ;
le service `api` déployé en continu n'est ni arrêté ni redémarré.

## Validation appliquée

Reflète la contrainte `chk_operator_scope` de la base
(`V7__create_portal_credential.sql`), vérifiée aussi côté application
(`PortalCredential.provision`, erreur claire avant d'atteindre la base) :

- `FACILITY_OPERATOR` **requiert** `--identity.provision.facility-id` ;
- `ADMIN` **refuse** `--identity.provision.facility-id`.

## Traçabilité

Chaque provisionnement publie un événement `PortalCredentialProvisioned`
(package public `com.urgencesante.identity`), consommable par le module
`audit` selon le même schéma que `AvailabilityUpdatedAuditListener`
(`ADR-006`). **Le listener côté audit n'est pas construit par cette issue**
(hors du périmètre `backend/modules/identity/**`) — l'événement existe et est
publié, prêt à être consommé par un suivi dédié, à l'image de
`AvailabilityUpdated` avant l'issue #162.

Contrairement à `AvailabilityUpdated`, cet événement est publié SANS outbox
transactionnel — voir `ADR-007` pour la justification complète de cet écart
documenté au document d'architecture.
