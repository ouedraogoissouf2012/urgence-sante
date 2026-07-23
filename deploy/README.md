# Déploiement du backend sur le VPS

> Base PostGIS (privée) + API publique sur le port **8086**, en conteneurs
> Docker — sans sudo, sans toucher aux autres projets du serveur.
> L'API sert les 15 établissements de démonstration (seed automatique) et
> le Swagger sur `/swagger-ui/index.html`.

## Première installation (sur le VPS)

```bash
# 1. Cloner (déjà fait si le site est déployé)
git clone https://github.com/ouedraogoissouf2012/urgence-sante.git ~/urgence-sante

# 2. Mot de passe de la base (jamais commité)
cd ~/urgence-sante
echo "DB_PASSWORD=$(openssl rand -hex 24)" > deploy/.env

# 3. Construire et lancer (5-10 min la première fois : Maven télécharge)
docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

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

Profil `local` (défaut) : les 15 établissements **[DÉMO]** sont insérés
automatiquement par Flyway au premier démarrage. Données simulées, assumées
comme telles — la garde `DemoDataProductionGuard` interdit ce jeu en profil
`production`.
