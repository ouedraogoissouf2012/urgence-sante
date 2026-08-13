# Déploiement du backend sur le VPS

> Base PostGIS (privée) + API publique sur le port **8086**, en conteneurs
> Docker — sans sudo, sans toucher aux autres projets du serveur.
> Profil **production** explicite (`SPRING_PROFILES_ACTIVE`, non
> surchargeable ici — voir docker-compose.yml) : l'API sert l'annuaire RÉEL
> importé au démarrage, jamais de données de démonstration (issue #123). Le
> Swagger reste accessible sur `/swagger-ui/index.html`.

## Première installation (sur le VPS)

```bash
# 1. Cloner (déjà fait si le site est déployé)
git clone https://github.com/ouedraogoissouf2012/urgence-sante.git ~/urgence-sante

# 2. Secrets et configuration requise (jamais commités)
cd ~/urgence-sante
{
  echo "DB_PASSWORD=$(openssl rand -hex 24)"
  # Instance OSRM auto-hébergée ou hôte de production — voir
  # backend/bootstrap/src/main/resources/application-production.yml. AUCUN
  # défaut : le démarrage refuse le serveur de démo public (sans SLA, throttlé).
  echo "OSRM_BASE_URL=https://<votre-hote-osrm>"
} > deploy/.env

# 3. Construire et lancer (5-10 min la première fois : Maven télécharge)
docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

Sans `DB_PASSWORD` ou `OSRM_BASE_URL` dans `deploy/.env`, `docker compose up`
refuse de démarrer le conteneur (garde dure, voir docker-compose.yml) plutôt
que de retomber silencieusement sur une configuration non sûre.

Vérification : `curl http://localhost:8086/actuator/health` → `"UP"`.

## Mise à jour (méthode git)

```bash
cd ~/urgence-sante && git pull \
  && docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

## Ce qui est exposé / protégé

| Élément | Exposition |
|---|---|
| API (`:8086/api/v1`) | publique — validation d'entrées + limites de débit intégrées |
| Swagger (`:8086/swagger-ui/index.html`) | publique (contrat en lecture) |
| Base PostGIS | **réseau interne Docker uniquement** (aucun port publié) |
| Écriture disponibilité (PUT) | jeton porteur requis (401 sinon) |
| Mémoire | API plafonnée à 1,5 Go (protège les autres projets du VPS) |

## Données

Profil `production` : au démarrage, `FacilityImportRunner` importe l'annuaire
RÉEL livré avec l'image (`infrastructure/directory/abidjan-starter.json`,
statut `PROVISIONAL` — à vérifier par la cellule annuaire, pas fictif) puis
purge toute donnée de démonstration résiduelle — mais SEULEMENT si l'import a
réussi, pour ne jamais vider l'annuaire sur un import cassé. La garde
`DemoDataProductionGuard` refuse ensuite le démarrage si une donnée `DEMO`
subsiste malgré tout (défense en profondeur, issue #41).

Mettre à jour l'annuaire : éditer `infrastructure/directory/abidjan-starter.json`
(ou fournir un autre fichier via `FACILITY_IMPORT_FILE`), puis redéployer —
l'import est idempotent (upsert par clé naturelle `source`/`external_ref`),
rejouable sans risque à chaque démarrage.

Le profil `local` (développement uniquement, jamais utilisé par ce
docker-compose) reste seedé avec 15 établissements **[DÉMO]** fictifs.
