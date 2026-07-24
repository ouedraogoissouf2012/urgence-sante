# Déployer le site d'accueil sur un VPS Contabo

> Le site est **100 % statique** (un dossier `site/` : HTML + polices + image).
> Aucune base de données, aucun runtime — n'importe quel serveur web le sert.

## Ce qu'il faut

- Un VPS Contabo (Ubuntu/Debian) avec son adresse IP et l'accès SSH
  (utilisateur + mot de passe reçus par e-mail de Contabo).
- Le dossier `site/` de ce dépôt.

## Étape 1 — Installer nginx sur le VPS (une seule fois)

Connecté en SSH au VPS :

```bash
apt update && apt install -y nginx
```

Vérification : ouvre `http://<IP-du-VPS>` dans un navigateur → page « Welcome to nginx ».

## Étape 2 — Envoyer le site sur le VPS

Depuis le PC (PowerShell), à la racine du projet :

```powershell
scp -r site/* root@<IP-du-VPS>:/var/www/html/
```

*(Remplace `<IP-du-VPS>` par l'adresse IP Contabo. Alternative sans commande :
WinSCP ou FileZilla, glisser-déposer le contenu de `site/` vers `/var/www/html/`.)*

## Étape 3 — C'est en ligne

Ouvre `http://<IP-du-VPS>` : le site Urgence Santé s'affiche, le bouton et le
QR code téléchargent l'APK (hébergé sur la release GitHub — rien d'autre à
configurer).

## Mise à jour du site

Refaire l'étape 2 (le `scp`) après chaque modification de `site/`.

## Plus tard (optionnel)

- **Nom de domaine** : chez un registrar, faire pointer un domaine (ex.
  `urgence-sante.ci`) vers l'IP du VPS (enregistrement DNS « A »).
- **HTTPS** : `apt install -y certbot python3-certbot-nginx` puis
  `certbot --nginx` (gratuit, à faire après le domaine).
- **Backend en ligne** : le même VPS pourra héberger le backend (Java +
  PostGIS) — c'est le chantier qui rendra l'APK utilisable partout, pas
  seulement sur le Wi-Fi de développement.

## L'APK servi par le site

Le fichier `site/urgence-sante.apk` est servi directement par le serveur
(téléchargement immédiat au scan du QR, sans passer par GitHub). Il n'est
**pas versionné** (voir .gitignore) : c'est le VPS qui le télécharge depuis
la release GitHub. Pour le mettre à jour après une nouvelle release :

```bash
wget "https://github.com/ouedraogoissouf2012/urgence-sante/releases/download/mvp-demo-apk/app-debug.apk" \
  -O ~/urgence-sante/site/urgence-sante.apk
```
