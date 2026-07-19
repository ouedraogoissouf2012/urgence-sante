# Guide de démonstration MVP — Urgence Santé

> ⚠️ **Toutes les données de cette démonstration sont SIMULÉES.** Les 15
> établissements portent `data_status = DEMO` en base (suffixe « [DÉMO] »,
> téléphones et statuts fictifs, coordonnées approximatives) et le jeton du
> portail est un jeton de démonstration. Aucune donnée réelle. Ces lignes
> **ne peuvent pas être chargées en production** : l'import les refuse et le
> démarrage en profil `production` échoue si des lignes `DEMO` existent
> (issue #41).

## Reproduire la démo et sa PREUVE en une commande

```bash
bash scripts/e2e-smoke.sh    # base vierge → migrations → seed → backend → parcours API mesuré
```

Le script part d'une **base PostGIS neuve**, applique les migrations (Flyway),
charge le jeu de démonstration, démarre le backend puis exécute un parcours API
**mesuré** (besoins, recommandation avec position et temps qualifié,
disponibilité, sécurité du portail 401/200, itinéraire, readiness). Il écrit un
**rapport horodaté** dans [`reports/e2e/`](../../reports/e2e/) (commit,
environnement, latences). C'est la preuve automatisée conservée exigée par
l'issue #48.

## Prérequis

- Docker en marche ; `infrastructure/.env` créé depuis `.env.example`
  (choisir un `POSTGRES_PORT` libre, ex. `55432`).
- JDK 21 (le jar est construit automatiquement si absent).
- Flutter 3.38+ pour les applications (facultatif pour la partie API).

## Démarrer / arrêter

```bash
bash scripts/demo-up.sh      # base + backend (port 8090) + 15 établissements + statuts
bash scripts/demo-down.sh    # arrêt et nettoyage complet
```

`demo-up.sh` est **reproductible** : le jeu de données est vidé puis réinséré à
chaque exécution, et les statuts initiaux sont posés via l'API (historique réel).

## Scénario A — Patient (le cœur du produit)

**Situation** : au Plateau (5.349, −4.008), besoin d'une **maternité**.

```bash
curl "http://localhost:8090/api/v1/orientation?lat=5.349&lon=-4.008&service=maternity"
```

**Résultat attendu (classement, meilleure recommandation en premier)** :
1. **CHU de Cocody [DÉMO]** — `AVAILABLE`, ~3,8 km : « service disponible » ;
2. **Clinique d'Adjamé [DÉMO]** — `SATURATED`, plus proche (~2 km) mais saturée ;
3. les centres **sans statut** apparaissent « disponibilité non confirmée » ;
4. **Hôpital d'Anyama [DÉMO]** — `CLOSED` : **exclu** de la liste.

Autres vérifications :
```bash
# Urgences depuis Marcory : Treichville AVAILABLE devant PISAM LIMITED.
curl "http://localhost:8090/api/v1/orientation?lat=5.301&lon=-3.982&service=emergency"
# Erreurs contrôlées :
curl -i "http://localhost:8090/api/v1/orientation?lat=5.3&lon=-4.0&service=inconnu"   # 400 problem+json
curl -i "http://localhost:8090/api/v1/facilities/00000000-0000-0000-0000-000000000000" # 404 problem+json
```

**Application mobile** (Android, SDK requis) — l'URL d'API se règle **au build**
via `--dart-define=API_BASE_URL=…` selon la cible :

| Cible | Commande (depuis `frontend/apps/patient_mobile`) |
|---|---|
| Android **Emulator** | `flutter run -t lib/main_development.dart --dart-define=API_BASE_URL=http://10.0.2.2:8090/api/v1` |
| Appareil **physique** (même Wi-Fi) | `flutter run -t lib/main_development.dart --dart-define=API_BASE_URL=http://<IP-du-poste>:8090/api/v1` |
| Poste Windows (défaut dev) | `flutter run -t lib/main_development.dart` *(localhost:8090 implicite)* |

Parcours : choix du besoin → carte + position → centres recommandés avec
raison → boutons **SAMU 185 / Pompiers 180** permanents.

> Sur l'émulateur Android, `localhost` désigne l'émulateur lui-même : utilisez
> **10.0.2.2** pour joindre la machine hôte.

## Scénario B — Agent hospitalier

**Portail web** : `cd frontend/apps/hospital_portal && flutter run -d chrome -t lib/main_development.dart`
*(le backend de démo autorise les origines `http://localhost:*` — CORS configuré
par `demo-up.sh` via `CORS_ALLOWED_ORIGINS` ; en production, seuls des domaines
explicites sont acceptés, les motifs génériques font échouer le démarrage).*

La mise à jour exige désormais une **authentification** (issue #42). Le jeu de
démo fournit un jeton **ADMIN** de régulation : `demo-samu-admin-2026`.

1. tableau des services : la **maternité** est `Disponible` avec son horodatage ;
2. passer la maternité à **Saturé** → mise à jour horodatée immédiate ;
3. ouvrir l'**historique** : `SATURATED` puis `AVAILABLE`, plus récent d'abord ;
4. relancer le scénario A : le CHU de Cocody est **déclassé** derrière un centre
   mieux disponible — la boucle complète agent → patient est démontrée.

Équivalent API (jeton porteur requis ; sans jeton → `401`, hors périmètre → `403`) :
```bash
TOKEN=demo-samu-admin-2026
curl -X PUT "http://localhost:8090/api/v1/facilities/11111111-0000-0000-0000-000000000001/availability/maternity" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"status":"SATURATED"}'
curl "http://localhost:8090/api/v1/facilities/11111111-0000-0000-0000-000000000001/availability/maternity/history"
```

## Performances et robustesse (mesurées — voir `reports/e2e/`)

Protocole : `scripts/e2e-smoke.sh`, base neuve, poste de développement
mono-instance, OSRM public, sans préchauffage. Latences **indicatives** (le
rapport horodaté fait foi) :

- `GET /orientation` : **premier appel** dominé par la latence OSRM publique
  (mesuré ~4,3 s au démarrage à froid) ; **un seul appel groupé** OSRM Table
  couvre tous les candidats (issue #44), la latence ne croît donc pas avec le
  nombre de centres ; en mode dégradé (circuit ouvert) la réponse est immédiate
  (temps estimé par distance, qualifié `ESTIMATED`) ;
- lectures (catalogue, disponibilité, santé) : quelques dizaines de ms ;
- recherche PostGIS (`ST_DWithin` + index GIST) : quelques millisecondes ;
- erreurs au contrat : `400`/`404` en `application/problem+json` (RFC 9457) ;
- statut vieilli (> 60 min) automatiquement présenté « non confirmé ».

Le **parcours d'interface** complet (besoin → localisation → recommandation →
itinéraire/appel → **panne réseau/hors ligne**) est prouvé par
`test/parcours_complet_test.dart` (patient_mobile).

## APK Android

**APK de démonstration téléchargeable** (build debug, cible `main_development`) :
[Release `mvp-demo-apk`](https://github.com/ouedraogoissouf2012/urgence-sante/releases/tag/mvp-demo-apk)
→ `urgence-sante-patient-mvp-debug.apk` (~151 Mo).

> ⚠️ Debug + données simulées : à installer sur un appareil de test, avec le
> backend de démo joignable (régler `API_BASE_URL` au build pour un appareil
> physique — voir le tableau du scénario A).

Reconstruire localement (SDK Android requis), depuis `frontend/apps/patient_mobile` :
```bash
flutter build apk --debug -t lib/main_development.dart
```

> **Windows — chemin non-ASCII :** si le chemin du dépôt contient un caractère
> accentué (ici « à » dans « propre à moi »), le compilateur de shaders Flutter
> échoue. Construire depuis un **chemin ASCII** (ex. un `git worktree` sous
> `C:\tmp\...`). Le job CI **« APK Android »** publie aussi l'artefact quand les
> runners sont disponibles.

## Limites assumées du MVP

- Authentification du portail par **jeton porteur** (démo : jeton ADMIN fourni) ;
  un fournisseur d'identité complet reste une évolution ;
- disponibilité saisie manuellement par les agents (pas d'interconnexion SI) ;
- OSRM public : soumis à sa disponibilité — le mode dégradé prend le relais ;
- limitation de débit **en mémoire** (mono-instance) — un limiteur partagé
  (Redis) sera requis en multi-instances.

## Inventaire des données simulées

| Donnée | Nature | Isolation |
|---|---|---|
| 15 établissements « [DÉMO] » | fictifs (`data_status = DEMO`) | refus à l'import en prod + garde de démarrage |
| Téléphones `+22501000000xx` | fictifs | idem |
| Jeton `demo-samu-admin-2026` | identifiant de démonstration | seed démo uniquement |
| Statuts initiaux | posés par `demo-up.sh` | rechargés à chaque exécution |

Le **jeu de démarrage réel** (`infrastructure/directory/abidjan-starter.json`)
est distinct : établissements publics, marqués `PROVISIONAL` (à vérifier), avec
provenance — voir [ADR-005](../decisions/ADR-005-annuaire-perimetre.md).
