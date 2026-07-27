# Déploiement complet sur Contabo — de zéro jusqu'à votre domaine

> **Objectif** : partir de rien (pas de serveur) et arriver à une plateforme
> Urgence Santé complète en ligne — site, API, application Android — sous
> votre nom de domaine, en HTTPS, **sans assistance**.
>
> **Durée totale** : ~2 h la première fois (dont ~30 min d'attentes).
> **Poste de travail** : Windows. Les commandes se collent dans **PowerShell**
> sauf mention contraire. Règle d'or : coller les commandes **exactement**.

---

## Sommaire

- [Phase 0 — Ce qu'il faut avant de commencer](#phase-0)
- [Phase 1 — Commander le VPS Contabo](#phase-1)
- [Phase 2 — Première connexion au serveur](#phase-2)
- [Phase 3 — Accès sans mot de passe (clé SSH)](#phase-3)
- [Phase 4 — Préparer le serveur](#phase-4)
- [Phase 5 — Récupérer le code (git)](#phase-5)
- [Phase 6 — Lancer le backend (API + base)](#phase-6)
- [Phase 7 — Lancer le site vitrine](#phase-7)
- [Phase 8 — Le nom de domaine (DNS)](#phase-8)
- [Phase 9 — Domaine + HTTPS sur le serveur](#phase-9)
- [Phase 10 — L'application Android à votre domaine](#phase-10)
- [Phase 11 — Le QR code](#phase-11)
- [Phase 12 — Vérifications finales](#phase-12)
- [Entretien : mises à jour](#entretien)
- [Dépannage : les pannes réellement rencontrées](#depannage)

---

<a name="phase-0"></a>
## Phase 0 — Ce qu'il faut avant de commencer

| Prérequis | Détail |
|---|---|
| Un compte **Contabo** | contabo.com (ou tout VPS Ubuntu équivalent) |
| Un **nom de domaine** | + l'accès à sa gestion DNS (registrar : Porkbun, Namecheap, OVH…) |
| Un compte **GitHub** | le dépôt du projet : `github.com/ouedraogoissouf2012/urgence-sante` |
| Sur le PC : **ssh** | inclus dans Windows 10/11 — vérifier : `ssh -V` dans PowerShell |

**Convention du document** : remplacez partout
`IP-SERVEUR` (l'adresse IP du VPS), `VOTRE-DOMAINE` (ex. `sante.exemple.ci`)
et `MOT-DE-PASSE-VPS` par vos valeurs. Ne collez jamais un mot de passe dans
un chat ou un document.

---

<a name="phase-1"></a>
## Phase 1 — Commander le VPS Contabo (~10 min + attente e-mail)

1. Sur **contabo.com** → *VPS* → choisir au minimum **VPS S** (4 Go de RAM
   suffisent ; 8 Go et plus = confortable).
2. Image / OS : **Ubuntu 24.04** (ou 22.04).
3. Région : la plus proche de vos utilisateurs (Europe fonctionne bien pour
   l'Afrique de l'Ouest).
4. Valider la commande. Contabo envoie un **e-mail « Your login data »**
   contenant : l'**adresse IP** du serveur et le **mot de passe root**
   (parfois un utilisateur dédié). Gardez cet e-mail précieusement.

> ✅ **Point de contrôle** : vous avez une IP (ex. `194.163.161.169`) et un
> mot de passe.

---

<a name="phase-2"></a>
## Phase 2 — Première connexion au serveur (~5 min)

Dans **PowerShell** :

```powershell
ssh root@IP-SERVEUR
```

1. Première fois : question `Are you sure you want to continue connecting?`
   → tapez `yes` + Entrée.
2. `password:` → tapez le mot de passe de l'e-mail Contabo + Entrée
   *(rien ne s'affiche pendant la frappe : c'est normal, tapez en aveugle)*.

**Créez tout de suite un utilisateur de travail** (ne travaillez pas en root) :

```bash
adduser deploy            # choisissez un mot de passe fort — c'est votre futur mot de passe sudo
usermod -aG sudo deploy
exit
```

> ✅ **Point de contrôle** : `ssh deploy@IP-SERVEUR` fonctionne avec le
> mot de passe choisi.

---

<a name="phase-3"></a>
## Phase 3 — Accès sans mot de passe (clé SSH) (~5 min)

Sur le **PC**, dans PowerShell :

```powershell
ssh-keygen -t ed25519 -f "$env:USERPROFILE\.ssh\vps_ed25519" -N '""'
type "$env:USERPROFILE\.ssh\vps_ed25519.pub" | ssh deploy@IP-SERVEUR "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys && echo CLE-INSTALLEE"
```

(Mot de passe demandé une dernière fois → doit afficher `CLE-INSTALLEE`.)

Créez un **alias** pour taper court. Ouvrez le fichier
`C:\Users\VOTRE-SESSION\.ssh\config` (créez-le s'il n'existe pas) et ajoutez :

```
Host mon-vps
    HostName IP-SERVEUR
    User deploy
    IdentityFile "C:/Users/VOTRE-SESSION/.ssh/vps_ed25519"
    IdentitiesOnly yes
```

> ✅ **Point de contrôle** : `ssh mon-vps` ouvre le serveur **sans mot de passe**.
> ⚠️ Piège vécu : si une commande `type "$env:USERPROFILE..."` affiche
> « La syntaxe du nom de fichier est incorrecte », vous êtes dans **cmd**,
> pas dans PowerShell — utilisez le chemin complet en dur.

---

<a name="phase-4"></a>
## Phase 4 — Préparer le serveur (~5 min)

Connecté au serveur (`ssh mon-vps`) :

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-v2 git nginx certbot python3-certbot-nginx
sudo usermod -aG docker $USER
exit
```

⚠️ **Le `exit` est obligatoire** : le droit Docker ne prend effet qu'à la
**reconnexion**. Reconnectez-vous (`ssh mon-vps`) et vérifiez :

```bash
docker ps        # doit répondre sans « permission denied »
```

> ✅ **Point de contrôle** : `docker ps` répond (liste vide = normal).

---

<a name="phase-5"></a>
## Phase 5 — Récupérer le code (~2 min)

Sur le serveur :

```bash
git clone https://github.com/ouedraogoissouf2012/urgence-sante.git ~/urgence-sante
cd ~/urgence-sante
```

> ✅ **Point de contrôle** : `ls deploy/` montre `Dockerfile`,
> `docker-compose.yml`, `setup-domaine.sh`.

---

<a name="phase-6"></a>
## Phase 6 — Lancer le backend (API + base de données) (~10 min)

```bash
cd ~/urgence-sante
echo "DB_PASSWORD=$(openssl rand -hex 24)" > deploy/.env && chmod 600 deploy/.env
docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

La **première construction dure 5 à 10 min** (Maven télécharge). Suivre :

```bash
docker compose -f deploy/docker-compose.yml --env-file deploy/.env logs -f api
# Ctrl+C pour quitter le suivi quand vous voyez « Started UrgenceSanteApplication »
```

> ✅ **Point de contrôle** : `curl http://localhost:8086/actuator/health`
> répond `{"status":"UP"...}`. Les **15 établissements de démonstration se
> chargent automatiquement** au premier démarrage (aucun script à lancer).
> La base de données n'est **pas** exposée sur internet (réseau Docker interne).

---

<a name="phase-7"></a>
## Phase 7 — Lancer le site vitrine (~1 min)

```bash
docker run -d --restart unless-stopped --name urgence-site \
  -p 127.0.0.1:8085:80 \
  -v ~/urgence-sante/site:/usr/share/nginx/html:ro \
  nginx:alpine
```

> ✅ **Point de contrôle** : `curl -s http://localhost:8085 | head -3`
> montre du HTML (`<!DOCTYPE html>`).

---

<a name="phase-8"></a>
## Phase 8 — Le nom de domaine (DNS) (~5 min + propagation)

Chez votre **registrar** (là où le domaine a été acheté — Porkbun : *Domain
Management* → *DNS* ; même logique ailleurs), créez :

```
Type A  |  Nom/Hôte : le sous-domaine choisi (ex. « sante »)  |  Valeur : IP-SERVEUR
```

Exemple : domaine `exemple.ci` + hôte `sante` → la plateforme vivra sur
`https://sante.exemple.ci`.

**Vérifier la propagation** (depuis le PC, PowerShell) :

```powershell
nslookup VOTRE-DOMAINE
```

> ✅ **Point de contrôle** : la réponse contient `IP-SERVEUR`.
> La propagation prend de 2 minutes à 1 heure selon le registrar.

---

<a name="phase-9"></a>
## Phase 9 — Domaine + HTTPS sur le serveur (~3 min)

Sur le serveur :

```bash
sudo bash ~/urgence-sante/deploy/setup-domaine.sh VOTRE-DOMAINE
```

Le script : configure nginx (site + API derrière votre domaine), vérifie le
DNS, puis obtient le **certificat HTTPS gratuit** (Let's Encrypt) avec
**renouvellement automatique**.

- Fin attendue : **`HTTPS-OK : https://VOTRE-DOMAINE`**
- S'il affiche `DNS-PAS-ENCORE-PROPAGE` : attendez, puis **relancez la même
  commande** (le script est relançable sans risque).

> ✅ **Point de contrôle** : `https://VOTRE-DOMAINE` s'ouvre dans le
> navigateur avec le cadenas 🔒.

---

<a name="phase-10"></a>
## Phase 10 — L'application Android à votre domaine (~10 min)

L'adresse de l'API est **compilée dans l'APK** : il faut construire un APK
qui pointe votre domaine. **Sans rien installer**, via GitHub :

1. Sur GitHub, dépôt `urgence-sante` → onglet **Actions** →
   workflow **« Release APK »** → bouton **Run workflow**.
2. Champ `api_base_url` : `https://VOTRE-DOMAINE/api/v1` → **Run workflow**.
3. Attendre le ✅ vert (~7 min). L'APK est publié dans **Releases**
   (`mvp-demo-apk`).

Puis, sur le serveur, placez-le sur le site :

```bash
wget "https://github.com/ouedraogoissouf2012/urgence-sante/releases/download/mvp-demo-apk/app-debug.apk" \
  -O ~/urgence-sante/site/urgence-sante.apk
```

> 💡 **Automatisation (optionnelle)** : si le secret GitHub `VPS_SSH_KEY`
> (clé privée SSH d'accès au serveur) est configuré dans
> *Settings → Secrets and variables → Actions* du dépôt, le workflow dépose
> l'APK sur le serveur **tout seul** — l'étape `wget` devient inutile.

> ✅ **Point de contrôle** :
> `curl -sI https://VOTRE-DOMAINE/urgence-sante.apk | head -1` → `200`.

---

<a name="phase-11"></a>
## Phase 11 — Le QR code (~1 min)

Le QR du site doit encoder VOTRE domaine. Sur le serveur :

```bash
curl -s "https://api.qrserver.com/v1/create-qr-code/?size=280x280&margin=8&data=https%3A%2F%2FVOTRE-DOMAINE%2Furgence-sante.apk" \
  -o ~/urgence-sante/site/img/qr-apk.png
```

*(Remplacez `VOTRE-DOMAINE` dans l'URL — les `%2F` et `%3A` autour doivent
rester tels quels : c'est l'encodage de `https://`.)*

---

<a name="phase-12"></a>
## Phase 12 — Vérifications finales

| Test | Attendu |
|---|---|
| `https://VOTRE-DOMAINE` | le site s'affiche, cadenas 🔒 |
| `https://VOTRE-DOMAINE/api/v1/medical-services` | liste JSON des besoins médicaux |
| `https://VOTRE-DOMAINE/swagger-ui/index.html` | documentation interactive de l'API |
| `http://VOTRE-DOMAINE` (sans s) | redirige vers https |
| Scanner le QR du site avec un téléphone | téléchargement direct de l'APK |
| Installer l'app → choisir un besoin | **les centres s'affichent** |

Si les 6 lignes passent : **le déploiement est terminé.** 🎉

---

<a name="entretien"></a>
## Entretien : mises à jour

Quand le code évolue (nouveau merge sur `main`), sur le serveur :

```bash
cd ~/urgence-sante && git pull \
  && docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
```

Le site statique est à jour dès le `git pull` (rien à redémarrer). Si
**l'application mobile** a changé : relancer la Phase 10 (workflow + wget).

---

<a name="depannage"></a>
## Dépannage — les pannes réellement rencontrées sur ce projet

| Symptôme | Cause | Solution |
|---|---|---|
| `bash`, `WSL`, `ext4.vhdx`, `/bin/bash` en erreur sur le PC | commande bash lancée dans PowerShell/WSL cassé | ces commandes-là se lancent **sur le serveur** (`ssh mon-vps`), pas sur le PC |
| `La syntaxe du nom de fichier est incorrecte` | vous êtes dans **cmd**, pas PowerShell | ouvrir PowerShell, ou mettre le chemin complet entre guillemets |
| `Permission denied (publickey)` | mauvaise clé/alias SSH | vérifier le fichier `~/.ssh/config` (Host, IdentityFile) ; retaper la phase 3 |
| `permission denied` sur `docker ps` | groupe docker pas actif | se **déconnecter/reconnecter** du serveur (fin de Phase 4) |
| `Port 8085/8086 already in use` | conteneur déjà lancé | `docker ps` puis `docker rm -f <nom>` et relancer |
| `DNS-PAS-ENCORE-PROPAGE` | l'enregistrement DNS voyage encore | attendre 5-30 min, relancer le script (sans risque) |
| Le site affiche l'ancienne version | cache du navigateur | `Ctrl+F5` (PC) ou vider le cache (téléphone) |
| L'app affiche « Aucun centre trouvé » | backend arrêté OU utilisateur à > 100 km d'Abidjan | vérifier `https://VOTRE-DOMAINE/actuator/health` ; sinon « Continuer sans position précise » dans l'app |
| Page « Whitelabel Error » sur `/` de l'API | normal : l'API n'a pas de page d'accueil | utiliser les chemins complets (`/api/v1/...`, `/swagger-ui/index.html`) |

---

## Où sont les autres documents ?

| Document | Usage |
|---|---|
| **Ce guide** | déploiement complet de zéro, par vous-même |
| `docs/deploiement-client/README.md` | remis à un CLIENT qui déploie sur SON serveur |
| `deploy/README.md` | référence courte du backend Docker |
| `COMMANDES.md` | lancer l'application EN LOCAL sur le PC (développement) |
| `docs/local/DEMARRER_SANS_AIDE.md` | pas-à-pas local détaillé (développement) |
