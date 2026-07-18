# Guide de démonstration MVP — Urgence Santé

> ⚠️ **Toutes les données de cette démonstration sont SIMULÉES** (établissements
> suffixés « [DÉMO] », téléphones et statuts fictifs, coordonnées approximatives).
> Elles ne représentent aucune information médicale réelle.

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

1. Écran « **Accès agent — démonstration** » : choisir *CHU de Cocody [DÉMO]*
   (authentification réelle : phase ultérieure, module identity) ;
2. tableau des services : la **maternité** est `Disponible` avec son horodatage ;
3. passer la maternité à **Saturé** → mise à jour horodatée immédiate ;
4. ouvrir l'**historique** : `SATURATED` puis `AVAILABLE`, plus récent d'abord ;
5. relancer le scénario A : le CHU de Cocody est **déclassé** derrière un centre
   mieux disponible — la boucle complète agent → patient est démontrée.

Équivalent API :
```bash
curl -X PUT "http://localhost:8090/api/v1/facilities/11111111-0000-0000-0000-000000000001/availability/maternity" \
  -H "Content-Type: application/json" -d '{"status":"SATURATED"}'
curl "http://localhost:8090/api/v1/facilities/11111111-0000-0000-0000-000000000001/availability/maternity/history"
```

## Performances et robustesse (constatées lors de la validation)

- `GET /orientation` (15 établissements, OSRM public joignable) : **≈ 3 s**
  (mesuré 2,7 s — un appel OSRM par candidat ; mise en parallèle candidate à
  une itération ultérieure) ; en mode dégradé sans OSRM, temps estimé par
  distance et réponse bien plus rapide ;
- recherche PostGIS (`ST_DWithin` + index GIST) : quelques millisecondes ;
- erreurs au contrat : `400`/`404` en `application/problem+json` (RFC 9457) ;
- statut vieilli (> 60 min) automatiquement présenté « non confirmé ».

## Limites assumées du MVP

- Aucune authentification (accès portail en mode démo explicite) ;
- disponibilité saisie manuellement par les agents (pas d'interconnexion SI) ;
- OSRM public : soumis à sa disponibilité — le mode dégradé prend le relais.
