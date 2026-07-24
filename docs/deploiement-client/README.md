# Déployer Urgence Santé sur VOTRE domaine et VOTRE serveur

> Guide d'installation complet et autonome. À l'issue : le site, l'API et
> l'application Android fonctionnent sous **votre** nom de domaine, sur
> **votre** serveur, en HTTPS — sans dépendre d'aucune infrastructure tierce.

## Ce qu'il vous faut

| Prérequis | Détail |
|---|---|
| Un serveur (VPS) | Ubuntu 22.04/24.04, **4 Go de RAM recommandés**, accès SSH |
| Un nom de domaine | et l'accès à sa gestion DNS (registrar) |
| Un compte GitHub | gratuit — pour construire votre APK sans installer Flutter |

Le code source est public : `https://github.com/ouedraogoissouf2012/urgence-sante`

Dans tout ce guide, remplacez `VOTRE-DOMAINE` par votre domaine réel
(ex. `sante.exemple.ci`) et `IP-SERVEUR` par l'adresse IP de votre serveur.

---

## Étape 1 — DNS

Chez votre registrar, créez l'enregistrement :

```
Type A | Nom : VOTRE-DOMAINE | Valeur : IP-SERVEUR
```

## Étape 2 — Préparer le serveur (une fois, en SSH)

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-v2 git nginx certbot python3-certbot-nginx
sudo usermod -aG docker $USER   # puis se déconnecter/reconnecter
```

## Étape 3 — Récupérer le code

```bash
git clone https://github.com/ouedraogoissouf2012/urgence-sante.git ~/urgence-sante
cd ~/urgence-sante
```

## Étape 4 — Lancer le backend (API + base de données)

```bash
echo "DB_PASSWORD=$(openssl rand -hex 24)" > deploy/.env && chmod 600 deploy/.env
docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

Première construction : 5 à 10 minutes. Vérification :
`curl http://localhost:8086/actuator/health` → doit répondre `"UP"`.
Les 15 établissements de démonstration sont chargés automatiquement.

## Étape 5 — Lancer le site vitrine

```bash
docker run -d --restart unless-stopped --name urgence-site \
  -p 127.0.0.1:8085:80 \
  -v ~/urgence-sante/site:/usr/share/nginx/html:ro \
  nginx:alpine
```

## Étape 6 — Votre domaine + HTTPS (une commande)

```bash
sudo bash deploy/setup-domaine.sh VOTRE-DOMAINE
```

Le script configure nginx (site + API derrière votre domaine) puis active le
certificat HTTPS gratuit (Let's Encrypt, renouvelé automatiquement). S'il
affiche `DNS-PAS-ENCORE-PROPAGE`, attendez quelques minutes et relancez-le.

## Étape 7 — VOTRE application Android (APK à votre domaine)

L'URL de l'API est **compilée dans l'APK** : il vous faut votre propre build.
Sans rien installer, via GitHub :

1. **Forkez** le dépôt (bouton « Fork » sur GitHub).
2. Dans votre fork : **Actions** → activez les workflows → **Release APK** →
   **Run workflow** → renseignez `api_base_url` :
   `https://VOTRE-DOMAINE/api/v1` → lancez.
3. ~7 minutes plus tard, l'APK est publié dans **Releases** de votre fork
   (`mvp-demo-apk`, créée automatiquement).
4. Sur votre serveur, servez-le depuis le site :

```bash
wget "https://github.com/VOTRE-COMPTE/urgence-sante/releases/download/mvp-demo-apk/app-debug.apk" \
  -O ~/urgence-sante/site/urgence-sante.apk
```

*(Alternative sans GitHub : `flutter build apk --debug -t lib/main_development.dart
--dart-define=API_BASE_URL=https://VOTRE-DOMAINE/api/v1` depuis
`frontend/apps/patient_mobile`, avec Flutter 3.38 installé.)*

## Étape 8 — Le QR code à votre domaine

```bash
curl -s "https://api.qrserver.com/v1/create-qr-code/?size=280x280&margin=8&data=https%3A%2F%2FVOTRE-DOMAINE%2Furgence-sante.apk" \
  -o ~/urgence-sante/site/img/qr-apk.png
```

## Vérifications finales

| Test | Attendu |
|---|---|
| `https://VOTRE-DOMAINE` | le site s'affiche (cadenas 🔒) |
| `https://VOTRE-DOMAINE/api/v1/medical-services` | la liste JSON des besoins |
| `https://VOTRE-DOMAINE/swagger-ui/index.html` | la documentation d'API |
| Scan du QR sur le site | téléchargement direct de l'APK |
| Installation sur Android | l'app affiche les centres |

## Mise à jour (quand le code évolue)

```bash
cd ~/urgence-sante && git pull \
  && docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

Et relancez le workflow **Release APK** de votre fork si l'application a changé
(puis l'Étape 7.4 pour rafraîchir l'APK servi).

## Notes importantes

- **Données de démonstration** : les 15 établissements sont simulés (marqués
  « [DÉMO] »). L'intégration de données réelles est une étape produit séparée.
- La base de données n'est **pas exposée sur internet** (réseau Docker interne).
- Les écritures (portail hospitalier) exigent un jeton d'authentification.
