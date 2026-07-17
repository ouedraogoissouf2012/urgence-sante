# Application Urgence Santé — Analyse de faisabilité technique & Cahier des charges
**Projet : NAPCOR GROUP — APPLI SANTÉ**
Marché cible : Côte d'Ivoire / Afrique de l'Ouest francophone · Stack : Flutter · Version : 1.0 (2026-05)

> Ce document remplace/complète le modèle Codeur.com par un cahier des charges **technique** réel. Le volet budget est volontairement écarté pour se concentrer sur la **faisabilité** et les **vrais besoins techniques**.

---

## 0. Résumé exécutif (le retour)

**L'idée est bonne, le besoin est réel, et le cœur technique de l'app (géoloc + annuaire des centres de santé + itinéraire vers le plus proche) est parfaitement faisable.**

**MAIS** la fonctionnalité que tu présentes comme centrale — *« la disponibilité en temps réel du service du centre médical »* — est, en l'état, **le point le plus dur et le moins faisable du projet**. Ce n'est pas un problème de code : c'est un problème de **source de données**. En Côte d'Ivoire, cette donnée **n'existe pas** sous forme exploitable : pas d'API hospitalière, pas de système de gestion de lits interconnecté, taux d'informatisation des hôpitaux très faible. Même en France, le pays le plus avancé sur le sujet (SI-SAMU), la disponibilité temps réel repose sur de la **saisie humaine par les services**, pas sur un flux automatique.

**Conclusion stratégique :** il faut **découper le projet**. Livrer d'abord un MVP qui sauve déjà des vies sans dépendre du temps réel (orientation vers le bon centre + appel SAMU/pompiers + itinéraire), puis construire la donnée de disponibilité **progressivement**, hôpital par hôpital, via un back-office simple. Vouloir le temps réel dès la v1 = risque d'échec garanti.

| Brique | Faisabilité technique | Difficulté réelle |
|---|---|---|
| Géolocalisation utilisateur + carte | ✅ Élevée | Faible |
| Annuaire géolocalisé des centres de santé | ✅ Élevée | Moyenne (qualité donnée à construire) |
| Tri par proximité + itinéraire/temps de trajet | ✅ Élevée | Faible |
| Affichage n° SAMU (185) / Pompiers (180) + appel | ✅ Élevée | Très faible |
| Inscription / auth / RGPD-like | ✅ Élevée | Faible |
| **Disponibilité « temps réel » des services** | ⚠️ **Faible en v1** | **Très élevée (donnée inexistante)** |
| Mode hors-ligne / réseau faible | ✅ Moyenne | Moyenne |

---

## 1. Le point dur : la donnée de disponibilité « temps réel »

### 1.1 Pourquoi c'est le vrai sujet
Ton énoncé du problème (slide 2) est : *« Indiquer à une personne en urgence le centre médical le plus proche **qui est en mesure de traiter son cas**, en prenant en compte la disponibilité en temps réel du service. »*

Le « qui est en mesure de traiter son cas » + « temps réel » est exactement ce qui manquait dans ton histoire d'Abidjan (hôpital débordé, transfert à 30 min). C'est la valeur ajoutée… et c'est aussi la donnée la plus difficile à obtenir.

### 1.2 Ce qui existe en Côte d'Ivoire (vérifié — meilleure nouvelle du projet)
Bonne surprise : l'**annuaire statique** est bien plus riche que prévu. Plusieurs sources exploitables pour bâtir la base :
- **data.gouv.ci** — jeu « Hôpitaux, cliniques, cliniques vétérinaires, cabinets dentaires et pharmacies » : **plus de 1 300 établissements géolocalisés** (open data officiel, téléchargeable).
- **cartesanitaire.ci** (Carte Sanitaire nationale, Ministère de la Santé) — **le plus précieux** : géolocalisation + **services offerts par établissement** (hospitalisation, **urgences**, médecine, **chirurgie**, **pédiatrie**, **gynéco-obstétrique**), plus source d'énergie, accès à l'eau, équipement de communication. C'est exactement la donnée « quel centre peut traiter quel cas ».
- **Ministère de la Santé / annuaire.gouv.ci** : ~**2 500 établissements** au total ; liste des hôpitaux publics.
- **Humanitarian Data Exchange (HDX)** et **healthsites.io / OpenStreetMap** : jeux géolocalisés (CSV/GeoJSON/API), contribuables, pour recoupement et complétion.
- **Liste Wikipédia des établissements d'Abidjan** + OMS (profil CIV) pour vérification.

➡️ **Ces sources donnent : nom, type, localisation, et même les services *théoriquement* offerts. Ce qu'elles ne donnent JAMAIS : la disponibilité *instantanée* (service ouvert maintenant, file d'attente, lits libres).** C'est la nuance clé : on peut savoir qu'un hôpital *a* un service d'urgence ; on ne sait pas s'il est *saturé là, maintenant*.

### 1.3 Comment les autres font (et leurs limites)
- **SI-SAMU / « Mes Urgences » (France)** : le système de référence. *Vérifié* : l'app « Mes Urgences » (Île-de-France, ton concurrent `fr.sesan.urgences`) affiche l'affluence temps réel **uniquement parce qu'elle exploite les logiciels métier des services d'urgence** (Résumés de Passage aux Urgences – RPU). Autrement dit, le temps réel n'existe que parce qu'**en amont chaque hôpital a un logiciel qui produit la donnée**. Pas de logiciel hospitalier = pas de temps réel. C'est toute la difficulté en CI.
- **Tableaux de bord COVID (lits de réa)** : ont prouvé qu'un suivi national de capacité est possible — mais au prix d'une remontée **manuelle quotidienne** imposée par l'État, en situation de crise.
- **Helium Health** (Nigeria/Afrique de l'Ouest) : déploie des **EMR/dossiers patients** dans les hôpitaux. C'est la brique qui, à terme, pourrait alimenter un flux — mais l'adoption est partielle et il n'expose pas d'API publique de disponibilité d'urgence.
- **mPharma / Field Intelligence (Shelf Life)** : excellents sur la **chaîne pharmaceutique / stock**, pas sur la capacité d'accueil des urgences.

**Constat clé (recherche confirmée) :** la santé numérique africaine souffre d'une **fragmentation et d'une très faible interopérabilité** ; il n'existe **aucun flux temps réel prêt à brancher**. Il faut **créer la donnée**, pas la consommer.

### 1.4 Modèles réalistes pour amorcer la donnée (par ordre de réalisme)
1. **Back-office hôpital ultra-simple (recommandé)** — une web-app (ou un écran dédié dans l'app) où un agent d'accueil/infirmier de garde met à jour un **statut simple** : `Urgences : DISPONIBLE / SATURÉ / FERMÉ` + cases « services ouverts » (chirurgie, maternité, pédiatrie, réa…). Mise à jour en 10 secondes. C'est ce qui transforme l'app en service vivant.
2. **Statut par USSD/SMS (zones à faible connectivité)** — l'hôpital envoie un code court (`*XXX*1#` = urgences saturées) sans smartphone ni data. Robuste, mais à intégrer via un agrégateur SMS/USSD.
3. **Appel de régulation (fallback humain)** — l'app n'affiche pas un statut mais **met en relation** avec un centre de régulation (type SAMU 185) qui, lui, connaît l'état du terrain. C'est le modèle le plus fiable à court terme.
4. **Statut déclaratif + horodatage de fraîcheur** — toujours afficher « mis à jour il y a X min » pour ne jamais mentir à l'utilisateur sur une info périmée.

> **Recommandation :** v1 sans temps réel (juste horaires/services théoriques + appel SAMU). v2 = back-office hôpital (modèle 1) déployé sur un nombre limité d'établissements partenaires à Abidjan, avec statut « fraîcheur » visible. **Ne jamais afficher un statut « temps réel » que l'app ne peut pas garantir** — en contexte d'urgence vitale, une fausse info peut tuer (enjeu de responsabilité).

---

## 2. Géolocalisation & cartographie

### 2.1 Couverture cartographique en CI / Abidjan
- **Google Maps** : meilleure couverture des POI et du routage à Abidjan, géocodage le plus robuste sur un adressage informel (peu de noms de rues/numéros fiables en CI).
- **OpenStreetMap (OSM)** : couverture honnête et **gratuite**, en amélioration, contribuable (on peut y ajouter soi-même les centres de santé via healthsites.io).
- **Mapbox** : bon compromis design/coût, données OSM + routage propre.

### 2.2 Options Flutter
| Plugin | Fond de carte | Routage / temps trajet | Coût | Verdict |
|---|---|---|---|---|
| `google_maps_flutter` + Directions/Routes API | Google | Oui (Routes API) | **Payant à l'usage** (voir 2.3) | Meilleure qualité, à utiliser ciblé |
| `flutter_map` (+ tuiles OSM) | OSM | via OSRM/GraphHopper externe | **Gratuit** (tuiles OSM, respecter usage) | Idéal MVP économe |
| `mapbox_maps_flutter` | Mapbox/OSM | Mapbox Directions | Free tier généreux puis payant | Bon compromis |

### 2.3 ⚠️ Piège coût — Google Maps a changé sa tarification (mars 2025)
Le crédit mensuel forfaitaire de **200 $/mois a été supprimé** et remplacé par un **quota d'appels gratuits par API/SKU** (paliers Essentials/Pro/Enterprise). Conséquence : il faut **architecturer pour limiter les appels** (mettre en cache les résultats, ne pas recharger une carte/un géocodage à chaque frame). Sinon la facture grimpe vite.

### 2.4 Recommandation cartographie
- **MVP : `flutter_map` + tuiles OSM + routage OSRM/GraphHopper** (coût ~0, suffisant pour « centre le plus proche + itinéraire »).
- **Calcul du « plus proche » côté serveur** (PostGIS, voir §3), pas seulement à vol d'oiseau côté client.
- **Géocodage** : éviter de dépendre des adresses textuelles (peu fiables en CI). S'appuyer sur les **coordonnées GPS** des établissements (qu'on saisit une fois) et la **position GPS de l'utilisateur**.

---

## 3. Architecture & back-end

### 3.1 Recommandation d'architecture
```
[ App Flutter ]  ──HTTPS/REST(JSON)──►  [ API back-end ]  ──►  [ PostgreSQL + PostGIS ]
   - géoloc GPS                            (Node/NestJS ou         - établissements (geom POINT)
   - carte (flutter_map/OSM)                Django ou Spring)       - services & statuts dispo
   - cache offline (Isar/Drift)            - logique "plus proche"  - utilisateurs, demandes
   - FCM (notifications)                    - auth (JWT)            - audit / logs
        │                                        │
        └────────── [ Firebase: Auth + FCM + Crashlytics ] (en complément)
   [ Back-office hôpital : web (Flutter Web/React) → met à jour les statuts ]
```

### 3.2 Firebase vs back-end custom
- **Firebase seul** : rapide à démarrer (Auth, Firestore, FCM, Crashlytics), mais **Firestore est faible pour les requêtes géospatiales** (« les N plus proches » nécessite des bricolages geohash) et l'app est un **service vital** → besoin de contrôle, d'audit, et d'hébergement maîtrisé des données de santé.
- **Recommandation : approche hybride.**
  - **Back-end custom** (NestJS/Node *ou* Django *ou* Spring — selon compétences équipe) + **PostgreSQL/PostGIS** pour le métier géospatial et les données sensibles.
  - **Firebase** uniquement pour : **FCM (push)**, **Crashlytics** (suivi des crashs), éventuellement **Auth** au démarrage.

### 3.3 PostGIS = la bonne brique pour « le plus proche »
Requête « les 5 centres ouverts les plus proches » en une ligne, performante avec index spatial :
```sql
-- index : CREATE INDEX idx_etab_geom ON etablissements USING GIST (geom);
SELECT id, nom, type,
       ST_Distance(geom::geography, ST_MakePoint(:lng, :lat)::geography) AS distance_m
FROM etablissements
WHERE service_urgence = true            -- filtrage métier (service requis)
ORDER BY geom <-> ST_MakePoint(:lng, :lat)::geometry   -- KNN, ultra rapide
LIMIT 5;
```
Le distance « à vol d'oiseau » sert au **tri** ; le **temps de trajet réel** est ensuite calculé via le routage (OSRM/Routes API) sur ces 5 candidats seulement (économie d'appels).

### 3.4 Notifications push
- **FCM (Firebase Cloud Messaging)** : standard, gratuit, fonctionne Android/iOS via Flutter (`firebase_messaging`). Usages : confirmation de demande, mise à jour de statut d'un centre suivi, messages de prévention.

### 3.5 Mode hors-ligne / réseau faible (critique en CI)
La connectivité data est **inégale** (zones 2G/3G, coupures). En contexte d'urgence, l'app **doit rester utile sans réseau**.
- **Offline-first** : embarquer/mettre en cache **l'annuaire des centres + leurs coordonnées + numéros d'urgence** localement (base locale **Isar** ou **Drift/SQLite**).
- **Dégradé maîtrisé** : sans réseau → afficher quand même la carte (tuiles en cache), la liste des centres proches connus, et **les numéros 185/180** cliquables (l'appel téléphonique GSM marche même sans data).
- **Sync** : récupérer/rafraîchir l'annuaire et les statuts quand le réseau revient ; horodater la fraîcheur.

### 3.6 Hébergement
- Pas de région cloud « hyperscaler » **en** Côte d'Ivoire. Options à latence raisonnable : **AWS Cape Town**, **Azure South Africa**, ou **Europe (Paris/Francfort)** — souvent la latence Europe↔Abidjan est correcte et l'offre plus riche.
- **Contrainte data santé (voir §4)** : privilégier un hébergeur permettant la **localisation/contrôle** des données et le chiffrement ; documenter le pays d'hébergement (exigence ARTCI possible).

---

## 4. Conformité — données de santé & sécurité

### 4.1 Cadre légal ivoirien (vérifié)
- **Loi n° 2013-450 du 19 juin 2013** relative à la protection des données à caractère personnel.
- Autorité : **ARTCI** (Autorité de Régulation des Télécommunications/TIC).
- Les **données de santé = données sensibles** → généralement **autorisation préalable de l'ARTCI** (et non simple déclaration) avant traitement. **Action requise : faire une démarche ARTCI** avant la mise en production.
- Logiques proches du **RGPD** : consentement, finalité, minimisation, droit d'accès/rectification/suppression, sécurité.

### 4.2 Ce que ça impose au projet
- **Minimiser les données collectées.** Ton CDC actuel demande Mail/téléphone + Nom/Prénom + Ville + GPS. Pour une app d'urgence, le **strict nécessaire** suffit (un n° de téléphone + position au moment de la demande). Plus on collecte de données de santé, plus la charge légale est lourde.
- **Distinguer** données d'identité (gérables) et données de santé (très encadrées). Si possible, **ne pas stocker** l'état de santé/le motif médical, ou seulement de façon anonyme/agrégée.
- **Consentement explicite** + écran CGU (déjà prévu, slide 9) + politique de confidentialité.
- **Chiffrement** : TLS en transit (obligatoire), chiffrement au repos de la base, secrets hors du code.

### 4.3 Sécurité mobile — OWASP MASVS
Référentiel à suivre (OWASP **MASVS** + guide de test **MASTG**) :
- Stockage sécurisé des données sensibles sur l'appareil (pas de données santé en clair ; usage du Keychain/Keystore).
- Pas de secrets/API keys en dur dans l'APK.
- TLS strict + (idéalement) **certificate pinning**.
- Auth robuste (le CDC demande « mot de passe complexe » + « mot de passe oublié » — OK), hash des mots de passe (bcrypt/argon2), JWT à durée limitée.
- Masquage des données sensibles à l'affichage (déjà demandé dans ton CDC — bon réflexe).

### 4.4 ⚠️ Responsabilité (point non-technique mais vital)
Une app qui oriente une urgence vitale engage une **responsabilité**. Il faut :
- Des **CGU/avertissements** clairs (« en cas d'urgence vitale, appelez le 185/180 »).
- Ne **jamais** présenter une info de disponibilité comme certaine si elle ne l'est pas (horodatage de fraîcheur obligatoire).
- Garder l'**appel direct aux secours** comme action toujours accessible (filet de sécurité).

---

## 5. Périmètre recommandé : MVP vs V2 vs V3

### MVP (v1) — « Sauver des vies sans dépendre du temps réel »
**Faisable, robuste, livrable.**
1. Onboarding : CGU + autorisation géoloc + inscription minimale.
2. Carte + **ma position**.
3. **Annuaire géolocalisé** des centres de santé (amorcé via HDX/healthsites/sante.gouv.ci, nettoyé manuellement pour Abidjan).
4. **Tri par proximité** (PostGIS) + **itinéraire & temps de trajet** vers les centres proches.
5. **Filtre par type de besoin** (urgences générales, maternité, pédiatrie…) basé sur les services *déclarés* du centre (statique).
6. **Boutons d'appel direct 185 (SAMU) / 180 (Pompiers)** — toujours visibles.
7. **Mode hors-ligne** : annuaire + numéros en cache.

### V2 — « Introduire la disponibilité, proprement »
8. **Back-office hôpital** (web) : statut `DISPONIBLE/SATURÉ/FERMÉ` par service + horodatage.
9. Affichage du statut dans l'app **avec fraîcheur** (« mis à jour il y a X min »).
10. Notifications FCM (suivi d'un centre, confirmation de demande).
11. Déploiement pilote sur quelques hôpitaux partenaires d'Abidjan.

### V3 — « Passer à l'échelle »
12. USSD/SMS pour hôpitaux peu connectés.
13. Intégration éventuelle avec EMR (Helium Health-like) / régulation SAMU.
14. Multilingue, tablette, analytics, mode « ambulance/transport ».

---

## 6. Corrections / compléments au cahier des charges initial

| Section du CDC Codeur | Ce qui manque / à corriger |
|---|---|
| Objectifs **quantitatifs** (slide vide) | À définir : nb de centres référencés à Abidjan (ex. 100), nb de villes couvertes, temps de réponse cible de l'app, taux de fraîcheur des statuts. |
| **Contraintes techniques** (page vide) | À remplir : Android 8+ / iOS 13+, fonctionnement en réseau 3G/hors-ligne, version min, langues (FR), accessibilité (gros boutons, mode urgence). |
| **Arborescence** (sommaire) | Décrire le « user case » de la demande (mentionné mais absent) : écran demande → choix du besoin → liste triée → fiche centre → itinéraire/appel. |
| **Type d'application** (aucune case cochée) | C'est un **Utilitaire** (pas e-commerce/jeu/réseau social). Le pictogramme « Paiement » n'a pas lieu d'être en v1. |
| **Données collectées** | Réduire au minimum (RGPD/ARTCI). Justifier chaque champ. |
| **Disponibilité temps réel** | Reformuler : « statut déclaratif horodaté », pas « temps réel » garanti. |
| **Responsabilité / secours** | Ajouter avertissement légal + appel direct secours comme filet. |

---

## 7. Stack technique recommandée (synthèse)

- **Mobile** : Flutter (Dart) — `flutter_map` + tuiles OSM (MVP), `firebase_messaging` (FCM), `geolocator` (GPS), `isar`/`drift` (offline), `dio` (HTTP).
- **Back-end** : NestJS (Node/TS) *ou* Django *ou* Spring Boot — selon compétences NAPCOR.
- **Base** : **PostgreSQL + PostGIS** (requêtes géospatiales) ; Redis (cache) optionnel.
- **Routage** : OSRM/GraphHopper auto-hébergé (économe) ou Mapbox/Google Directions (ciblé).
- **Services managés** : Firebase (FCM, Crashlytics, Auth optionnel).
- **Back-office** : Flutter Web ou React (saisie statut hôpital).
- **Hébergement** : AWS Cape Town / Azure South Africa / Europe, chiffrement + conformité ARTCI.
- **Sécurité** : OWASP MASVS, TLS, hash argon2/bcrypt, secrets hors code, démarche ARTCI données santé.

---

## 8. Risques majeurs (à garder en tête)

1. **Donnée de disponibilité** : sans partenariats hôpitaux, pas de temps réel. → commencer par l'annuaire + appel SAMU.
2. **Qualité/fraîcheur de l'annuaire** : données publiques incomplètes → nettoyage manuel initial (Abidjan d'abord).
3. **Connectivité** : concevoir offline-first dès le départ (pas après coup).
4. **Coûts API cartes** : maîtriser via OSM + cache (Google Maps n'est plus « gratuit »).
5. **Juridique** : données de santé = autorisation ARTCI + responsabilité en cas d'urgence → cadrer tôt.
6. **Concurrence/modèle** : « Mes urgences » (France) existe mais le contexte CI est différent → l'avantage se gagne sur la **donnée locale** et les **partenariats**, pas sur la techno.

---

### Sources principales consultées
- Ministère de la Santé CI — sante.gouv.ci (carte sanitaire / établissements)
- Humanitarian Data Exchange — « Health centers in Ivory Coast » (data.humdata.org)
- healthsites.io / OpenStreetMap ; OMS (profil CIV)
- ARTCI — Loi n° 2013-450 du 19/06/2013 (données personnelles)
- Numéros d'urgence CI : SAMU **185**, Pompiers/GSPM **180**, Police 170/111
- Google Maps Platform — changement de tarification 2025 (suppression du crédit 200 $/mois)
- OWASP MASVS / MASTG ; PostGIS (KNN `<->`, ST_Distance)
- Écosystème santé Afrique : Helium Health (EMR), mPharma, Field Intelligence ; littérature sur la fragmentation/interopérabilité (Frontiers, Brookings, WHO)
- Modèle de référence disponibilité urgences : SI-SAMU (France)
