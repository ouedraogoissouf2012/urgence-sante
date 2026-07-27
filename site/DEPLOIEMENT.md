# Déploiement du site

> 📘 **Le guide de référence est ailleurs** : pour un déploiement complet
> (serveur Contabo, backend, site, domaine, HTTPS, APK), suivre
> **[docs/DEPLOIEMENT_COMPLET_CONTABO.md](../docs/DEPLOIEMENT_COMPLET_CONTABO.md)**.
> Pour un déploiement remis à un client : `docs/deploiement-client/README.md`.

Ce fichier ne conserve que la note spécifique au dossier `site/`.

## L'APK servi par le site

Le fichier `site/urgence-sante.apk` est servi directement par le serveur
(téléchargement immédiat au scan du QR, sans passer par GitHub). Il n'est
**pas versionné** (voir .gitignore) : c'est le serveur qui le télécharge
depuis la release GitHub — ou le workflow « Release APK » qui l'y dépose
automatiquement. Pour le rafraîchir à la main :

```bash
wget "https://github.com/ouedraogoissouf2012/urgence-sante/releases/download/mvp-demo-apk/app-debug.apk" \
  -O ~/urgence-sante/site/urgence-sante.apk
```

Le QR code (`site/img/qr-apk.png`) doit encoder l'URL publique de ce fichier
sur le domaine en service — voir la Phase 11 du guide complet.
