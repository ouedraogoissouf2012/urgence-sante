# Commandes — Urgence Santé (local)

> Terminal : **PowerShell**, depuis la racine du projet
> (`cd "C:\Users\USER PC\Documents\propre à moi\nouveauxAPPs"`).
> Prérequis : Docker Desktop démarré (baleine 🐳 visible).

## Lancer

```powershell
# db (base de données PostGIS)
docker compose -f infrastructure/docker-compose.yml --env-file infrastructure/.env up -d

# api (backend — données de test chargées automatiquement)
java -jar backend/bootstrap/target/bootstrap-0.1.0-SNAPSHOT-exec.jar

# mobile (app patient) — depuis frontend/apps/patient_mobile
flutter run -d chrome

# portail (agent hôpital) — depuis frontend/apps/hospital_portal
flutter run -d chrome
```

Ordre : **db → api → mobile**. Chaque commande garde son terminal ouvert
(`api`, `mobile`) : ouvre un nouveau terminal pour la suivante.

## Voir

| Quoi | Lien |
|---|---|
| Swagger (les API) | http://localhost:8090/swagger-ui/index.html |
| Santé du backend | http://localhost:8090/actuator/health |
| Exemple d'API | http://localhost:8090/api/v1/orientation?lat=5.349&lon=-4.008&service=maternity |
| App patient / portail | Chrome s'ouvre tout seul |

Jeton agent (portail, démo) : `demo-samu-admin-2026`

## Arrêter

```powershell
# api / mobile / portail : Ctrl+C (ou touche q pour flutter) dans leur terminal
# db :
docker compose -f infrastructure/docker-compose.yml --env-file infrastructure/.env down -v
```

## Reconstruire le jar (après un changement backend)

```powershell
cd backend ; .\mvnw.cmd -pl bootstrap -am package -DskipTests ; cd ..
```

## Si ça ne marche pas

| Symptôme | Cause | Solution |
|---|---|---|
| `Cannot connect to Docker` | Docker éteint | Ouvrir Docker Desktop, attendre 🐳 |
| `Port 8090 already in use` | ancien backend actif | Ctrl+C dans son terminal, ou redémarrer le poste |
| Connexion DB refusée | mot de passe `.env` ≠ défaut | `$env:DB_PASSWORD="ton-mot-de-passe"` puis relancer `api` |
| `flutter` non reconnu | PATH pas rechargé | ouvrir un NOUVEAU terminal |
| « Aucun centre trouvé » | backend pas lancé | relancer `api`, vérifier le lien Santé |

> Note : les statuts de disponibilité (Disponible/Saturé) ne sont pas posés par
> le chargement automatique — l'app affiche « disponibilité non confirmée ».
> Pour les poser comme en démo complète : `bash scripts/local-up.sh` (Git Bash).
