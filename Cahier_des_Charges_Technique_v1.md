<div align="center">

# CAHIER DES CHARGES TECHNIQUE

## Application Mobile « Urgence Santé »

### Orientation des patients vers le centre de santé adapté le plus proche

---

**NAPCOR GROUP**
*Leader technologique — Développement d'applications de gestion & Gouvernance de la sécurité*

---

| | |
|---|---|
| **Projet** | Application Urgence Santé (APPLI SANTÉ) |
| **Marché cible** | Côte d'Ivoire — extension Afrique de l'Ouest francophone |
| **Document** | Cahier des charges technique |
| **Version** | 1.0 |
| **Date** | Mai 2026 |
| **Statut** | Pour transmission prestataire |
| **Contact** | mongnenou.derou@napcor-group.com |
| **Confidentialité** | Diffusion restreinte |

</div>

<div style="page-break-after: always;"></div>

---

## Contrôle du document

| Version | Date | Auteur | Description |
|:---:|:---:|:---|:---|
| 0.1 | Mai 2026 | NAPCOR GROUP | Trame initiale (cahier des charges fonctionnel) |
| **1.0** | **Mai 2026** | **NAPCOR GROUP** | **Cahier des charges technique — étude de faisabilité, architecture Spring Boot, périmètre MVP/V2/V3** |

**Validation**

| Rôle | Nom | Date | Visa |
|:---|:---|:---:|:---:|
| Maître d'ouvrage (MOA) | NAPCOR GROUP | | |
| Maître d'œuvre (MOE) | *Prestataire* | | |
| Référent technique | | | |

---

## Sommaire

1. [Contexte et genèse du projet](#1-contexte-et-genèse-du-projet)
2. [Objectifs](#2-objectifs)
3. [Périmètre fonctionnel](#3-périmètre-fonctionnel)
4. [Analyse de faisabilité — le défi de la donnée temps réel](#4-analyse-de-faisabilité--le-défi-de-la-donnée-temps-réel)
5. [Architecture technique cible](#5-architecture-technique-cible)
6. [Exigences fonctionnelles détaillées](#6-exigences-fonctionnelles-détaillées)
7. [Exigences non fonctionnelles](#7-exigences-non-fonctionnelles)
8. [Données & cartographie](#8-données--cartographie)
9. [Sécurité & conformité réglementaire](#9-sécurité--conformité-réglementaire)
10. [Trajectoire de livraison (MVP / V2 / V3)](#10-trajectoire-de-livraison-mvp--v2--v3)
11. [Livrables attendus & critères d'acceptation](#11-livrables-attendus--critères-dacceptation)
12. [Risques et mesures de mitigation](#12-risques-et-mesures-de-mitigation)
13. [Glossaire](#13-glossaire)
14. [Annexes & sources](#14-annexes--sources)

<div style="page-break-after: always;"></div>

---

## 1. Contexte et genèse du projet

Le projet est né d'une expérience vécue à Abidjan : lors d'une crise d'asthme aiguë, une personne a été conduite à l'hôpital le plus proche (200 m), mais ce service, **débordé**, n'a pu la prendre en charge. Après l'attente d'une décharge administrative, il a fallu rejoindre un autre établissement à **30 minutes de route** pour obtenir des soins.

Cette situation illustre un problème structurel : **en situation d'urgence, le patient ne dispose d'aucun moyen de savoir, à l'avance, quel établissement proche est réellement en mesure de le prendre en charge.** La conséquence est une perte de temps qui peut être fatale.

L'application **Urgence Santé** vise à répondre à ce problème : orienter immédiatement l'utilisateur vers le centre de santé adapté le plus proche, et — à terme — tenir compte de la disponibilité réelle des services.

> **Énoncé du problème (référence backlog) :** *« Indiquer à une personne en situation d'urgence sanitaire le centre médical le plus proche qui est en mesure de traiter son cas, en prenant en compte la disponibilité en temps réel du service du centre médical. »*

**Positionnement.** Une application comparable existe en France (*Mes Urgences*, `fr.sesan.urgences`). Le contexte ivoirien étant très différent (données hospitalières peu informatisées, connectivité inégale), l'avantage concurrentiel se construira sur **la qualité de la donnée locale et les partenariats**, et non sur la seule technologie.

---

## 2. Objectifs

### 2.1 Objectifs qualitatifs
- **Sauver des vies** en réduisant le délai d'accès à un service de soins adapté.
- **Améliorer l'orientation** des patients en situation d'urgence.
- Offrir une expérience **simple, intuitive et fiable**, utilisable en situation de stress.
- Poser les fondations d'une **interconnexion progressive des systèmes de santé**.

### 2.2 Objectifs quantitatifs *(à confirmer par la MOA)*

| Indicateur | Cible MVP | Cible 12 mois |
|:---|:---:|:---:|
| Établissements référencés et géolocalisés | ≥ 300 (Grand Abidjan) | ≥ 1 500 (national) |
| Villes couvertes | 1 (Abidjan) | ≥ 5 |
| Temps d'affichage des centres proches | < 3 s | < 2 s |
| Disponibilité du service (uptime) | 99,5 % | 99,9 % |
| Hôpitaux partenaires alimentant un statut (V2) | — | ≥ 10 |

### 2.3 Cible utilisateur
Toute personne (et son entourage) confrontée à une urgence sanitaire et devant rejoindre un service d'urgence, ainsi que l'ensemble des centres de santé (publics comme privés).

---

## 3. Périmètre fonctionnel

| # | Fonctionnalité | MVP | V2 | V3 |
|:--:|:---|:--:|:--:|:--:|
| F1 | Onboarding (CGU, autorisation géolocalisation) | ✅ | | |
| F2 | Inscription / authentification sécurisée | ✅ | | |
| F3 | Carte + position de l'utilisateur | ✅ | | |
| F4 | Annuaire géolocalisé des centres de santé | ✅ | | |
| F5 | Tri par proximité + itinéraire & temps de trajet | ✅ | | |
| F6 | Filtre par type de besoin (urgences, maternité, pédiatrie…) | ✅ | | |
| F7 | Appel direct SAMU (185) / Pompiers (180) | ✅ | | |
| F8 | Mode hors-ligne (annuaire + numéros en cache) | ✅ | | |
| F9 | Statut de disponibilité déclaratif horodaté (back-office hôpital) | | ✅ | |
| F10 | Notifications push (FCM) | | ✅ | |
| F11 | Mise à jour de statut par USSD/SMS (zones peu connectées) | | | ✅ |
| F12 | Interconnexion EMR / régulation SAMU | | | ✅ |
| F13 | Multilingue, support tablette, statistiques | | | ✅ |

---

## 4. Analyse de faisabilité — le défi de la donnée temps réel

### 4.1 Synthèse de faisabilité

| Brique technique | Faisabilité | Difficulté réelle |
|:---|:---:|:---:|
| Géolocalisation utilisateur + carte | Élevée | Faible |
| Annuaire géolocalisé des centres | Élevée | Moyenne (qualité à fiabiliser) |
| Tri par proximité + itinéraire | Élevée | Faible |
| Affichage & appel des numéros d'urgence | Élevée | Très faible |
| Inscription / authentification / conformité | Élevée | Faible |
| Mode hors-ligne / réseau faible | Moyenne | Moyenne |
| **Disponibilité « temps réel » des services** | **Faible en V1** | **Très élevée** |

### 4.2 Le point dur : la disponibilité en temps réel

La fonctionnalité centrale — connaître la disponibilité **instantanée** d'un service — se heurte à une réalité : **cette donnée n'existe pas sous forme exploitable en Côte d'Ivoire.** Il n'y a ni API hospitalière, ni système interconnecté de gestion des lits ou des files d'attente.

**Enseignement du modèle de référence (France).** L'application *Mes Urgences* n'affiche l'affluence en temps réel **que parce que chaque service d'urgence dispose d'un logiciel métier produisant la donnée** (Résumés de Passage aux Urgences). **Sans logiciel hospitalier en amont, aucun temps réel n'est possible.**

**Conséquence directe.** La donnée de disponibilité doit être **créée**, et non consommée. Vouloir l'imposer dès la V1 expose le projet à un échec. La stratégie retenue consiste à **livrer d'abord la valeur qui ne dépend pas du temps réel**, puis à construire la disponibilité progressivement via un back-office hôpital simple (cf. §10).

> ⚠️ **Exigence de sûreté.** En contexte d'urgence vitale, une information de disponibilité erronée peut avoir des conséquences graves. Tout statut affiché **devra être horodaté** (« mis à jour il y a X min ») et l'**appel direct aux secours devra rester accessible en permanence** comme filet de sécurité.

### 4.3 Ce qui existe réellement (atout du projet)
La donnée d'**annuaire** est de bonne qualité et exploitable immédiatement :
- **data.gouv.ci** — plus de **1 300 établissements géolocalisés** (open data officiel).
- **cartesanitaire.ci** (Ministère de la Santé) — géolocalisation **et services offerts par établissement** (urgences, chirurgie, pédiatrie, gynéco-obstétrique, hospitalisation…).
- **annuaire.gouv.ci / Ministère** — ~**2 500 établissements** au total.
- **healthsites.io / OpenStreetMap**, **HDX** — jeux complémentaires (CSV/GeoJSON/API).

On peut donc savoir qu'un établissement **possède** un service donné (donnée théorique) ; ce qui manque est uniquement son **état instantané** (ouvert / saturé / fermé).

---

## 5. Architecture technique cible

### 5.1 Vue d'ensemble

```
┌─────────────────────────┐        HTTPS / REST (JSON)        ┌──────────────────────────────┐
│   APPLICATION MOBILE     │ ───────────────────────────────► │   API BACK-END (Spring Boot)  │
│   Flutter (Android/iOS)  │ ◄─────────────────────────────── │   - Spring Web (REST)         │
│                          │                                   │   - Spring Security (JWT)     │
│  • Géolocalisation GPS    │                                   │   - Spring Data JPA           │
│  • Carte (flutter_map/OSM)│                                   │   - Logique « plus proche »   │
│  • Cache offline (Isar)   │                                   │   - Validation, audit, logs   │
│  • Notifications (FCM)     │                                   └───────────────┬──────────────┘
└────────────┬─────────────┘                                                    │ JDBC
             │                                                                   ▼
             │                                                    ┌──────────────────────────────┐
             │                                                    │   PostgreSQL + PostGIS        │
             │                                                    │   - Établissements (geom)     │
             │  Firebase (FCM, Crashlytics)                        │   - Services & statuts        │
             └───────────────────────────────────────────────►    │   - Utilisateurs, demandes    │
                                                                  │   - Journaux d'audit          │
   ┌──────────────────────────────┐    met à jour les statuts     └──────────────────────────────┘
   │  BACK-OFFICE HÔPITAL (Web)    │ ─────────────────────────────────────────►  (API Spring Boot)
   │  Flutter Web / Angular         │   DISPONIBLE / SATURÉ / FERMÉ + horodatage
   └──────────────────────────────┘
```

### 5.2 Stack technique retenue

| Couche | Technologie | Justification |
|:---|:---|:---|
| **Application mobile** | **Flutter** (Dart) | Base de code unique Android + iOS, performances natives, riche écosystème cartographie/géoloc. |
| **Back-end** | **Spring Boot** (Java) | **Choix MOA.** Robuste, mature, adapté à un service critique ; écosystème Spring complet (Security, Data JPA, Validation, Actuator). |
| **Base de données** | **PostgreSQL + PostGIS** | Requêtes géospatiales natives et performantes (« N centres les plus proches »). |
| **Sécurité** | **Spring Security + JWT** | Authentification/autorisation standardisées, gestion des rôles (citoyen / agent hôpital / admin). |
| **Notifications** | **Firebase Cloud Messaging (FCM)** | Standard push Android/iOS, gratuit, intégration Flutter native. |
| **Cartographie (MVP)** | **flutter_map + tuiles OpenStreetMap** | Sans frais d'usage ; couverture honnête à Abidjan ; alternative Google Maps en option ciblée. |
| **Routage / temps de trajet** | **OSRM ou GraphHopper** (auto-hébergé) | Calcul d'itinéraire économe ; appels limités aux candidats proches. |
| **Persistance locale (offline)** | **Isar** ou **Drift (SQLite)** | Cache de l'annuaire et des numéros d'urgence pour usage hors-ligne. |
| **Back-office hôpital** | **Flutter Web** ou **Angular** | Interface légère de mise à jour des statuts. |
| **Hébergement** | Cloud régional (Afrique du Sud) **ou** Europe | Latence acceptable vers Abidjan ; conformité et chiffrement (cf. §9). |
| **Supervision** | **Spring Boot Actuator** + Crashlytics | Métriques santé du service, suivi des incidents. |

### 5.3 Module géospatial (cœur métier)

La sélection des établissements adaptés les plus proches s'appuie sur **PostGIS**. Exemple de requête (« 5 centres avec service d'urgence les plus proches ») :

```sql
-- Index spatial préalable :
-- CREATE INDEX idx_etab_geom ON etablissements USING GIST (geom);

SELECT id, nom, type,
       ST_Distance(geom::geography, ST_MakePoint(:lng, :lat)::geography) AS distance_m
FROM etablissements
WHERE service_urgence = TRUE                              -- filtrage métier
ORDER BY geom <-> ST_MakePoint(:lng, :lat)::geometry      -- KNN indexé, très rapide
LIMIT 5;
```

La distance « à vol d'oiseau » sert au **tri** ; le **temps de trajet réel** est ensuite calculé via le service de routage, **uniquement sur les 5 candidats** (économie de ressources et d'appels API).

### 5.4 Résilience réseau (offline-first)
La connectivité étant inégale (zones 2G/3G, coupures), l'application **doit rester utile sans réseau** :
- annuaire des centres + coordonnées + numéros d'urgence **mis en cache localement** ;
- en l'absence de réseau : affichage de la carte en cache, des centres connus à proximité, et des **numéros 185/180 cliquables** (l'appel GSM fonctionne sans data) ;
- **synchronisation** de l'annuaire et des statuts au retour du réseau, avec horodatage de fraîcheur.

---

## 6. Exigences fonctionnelles détaillées

**Parcours utilisateur principal (cas d'usage « demande d'urgence ») :**

1. **Lancement / Onboarding** — acceptation des CGU, autorisation de géolocalisation.
2. **Inscription** — données minimales (téléphone *ou* e-mail, nom/prénom, ville) ; mot de passe complexe ; gestion du mot de passe oublié ; masquage des données sensibles à l'affichage.
3. **Écran d'accueil (carte)** — carte centrée sur la position de l'utilisateur ; bouton flottant « **Initier une demande** » ; numéros d'urgence visibles en permanence (**SAMU 185**, **Pompiers 180**).
4. **Demande** — sélection du type de besoin (urgence générale, maternité, pédiatrie, chirurgie…).
5. **Résultats** — liste triée des centres adaptés les plus proches, avec distance, temps de trajet estimé et — en V2 — statut horodaté.
6. **Fiche établissement** — détails, services, contact, itinéraire.
7. **Action** — lancer l'itinéraire (navigation) ou appeler (centre / SAMU / pompiers).

| Réf. | Exigence | Priorité |
|:---:|:---|:---:|
| EF-01 | L'utilisateur accepte les CGU au premier lancement | Haute |
| EF-02 | L'application demande et utilise la géolocalisation GPS | Haute |
| EF-03 | L'utilisateur peut s'inscrire et se connecter de façon sécurisée | Haute |
| EF-04 | L'application affiche les centres adaptés triés par proximité | Haute |
| EF-05 | L'application calcule et affiche le temps de trajet | Haute |
| EF-06 | L'utilisateur peut filtrer par type de besoin médical | Haute |
| EF-07 | Les numéros SAMU (185) et Pompiers (180) sont appelables en 1 geste | Haute |
| EF-08 | L'application reste fonctionnelle (annuaire + appels) hors-ligne | Haute |
| EF-09 | *(V2)* Un agent hôpital met à jour le statut de son service | Moyenne |
| EF-10 | *(V2)* Le statut s'affiche avec son horodatage de fraîcheur | Moyenne |

---

## 7. Exigences non fonctionnelles

| Réf. | Catégorie | Exigence |
|:---:|:---|:---|
| ENF-01 | **Performance** | Affichage des centres proches en < 3 s sur réseau 3G. |
| ENF-02 | **Disponibilité** | Service critique : objectif ≥ 99,5 % (MVP), montée à 99,9 %. |
| ENF-03 | **Résilience réseau** | Fonctionnement dégradé maîtrisé hors-ligne (offline-first). |
| ENF-04 | **Compatibilité** | Android 8.0+ et iOS 13+ ; smartphones et tablettes. |
| ENF-05 | **Ergonomie** | Interface très intuitive ; gros boutons ; lisibilité en situation de stress ; « mode urgence ». |
| ENF-06 | **Accessibilité** | Contrastes élevés, tailles de police adaptables. |
| ENF-07 | **Sécurité** | Conformité OWASP MASVS ; chiffrement en transit et au repos. |
| ENF-08 | **Conformité** | Respect de la loi ivoirienne n° 2013-450 (autorisation ARTCI). |
| ENF-09 | **Maintenabilité** | Code documenté, architecture en couches, tests automatisés. |
| ENF-10 | **Scalabilité** | Architecture prête pour l'extension nationale puis régionale. |
| ENF-11 | **Observabilité** | Journaux, métriques (Actuator), suivi des crashs (Crashlytics). |
| ENF-12 | **Langue** | Français (MVP) ; architecture prête au multilingue. |

---

## 8. Données & cartographie

### 8.1 Constitution de la base d'établissements
- **Amorçage** à partir des sources ouvertes (data.gouv.ci, cartesanitaire.ci, healthsites.io/OSM, HDX).
- **Fiabilisation manuelle** prioritaire sur le Grand Abidjan (coordonnées GPS, services réels, horaires, contacts).
- Modèle de données minimal par établissement : `id, nom, type, géométrie (point), services[], horaires, téléphone, secteur (public/privé), source, date_maj`.

### 8.2 Cartographie
- **MVP : OpenStreetMap via `flutter_map`** (sans frais d'usage), routage **OSRM/GraphHopper**.
- ⚠️ **Google Maps n'est plus gratuit** : le crédit mensuel de 200 $ a été **supprimé en mars 2025**, remplacé par des quotas gratuits par service (10 000 requêtes/mois pour les SKU « Essentials » : cartes dynamiques, géocodage). En cas d'usage de Google Maps, **mettre en cache** agressivement pour maîtriser les coûts.
- **Géocodage** : s'appuyer en priorité sur les **coordonnées GPS** (l'adressage formel est faible en CI) plutôt que sur les adresses textuelles.

---

## 9. Sécurité & conformité réglementaire

### 9.1 Cadre légal ivoirien
- **Loi n° 2013-450 du 19 juin 2013** relative à la protection des données à caractère personnel ; autorité de contrôle : **ARTCI**.
- Les **données de santé sont des données sensibles** → **autorisation préalable de l'ARTCI** requise avant tout traitement (démarche à initier avant la mise en production).
- Principes alignés sur le RGPD : consentement, finalité, **minimisation**, droits d'accès/rectification/suppression.

### 9.2 Mesures techniques de sécurité (OWASP MASVS)
- **Minimisation des données** : ne collecter que le strict nécessaire ; éviter de stocker l'état de santé / le motif médical, ou le faire de manière anonymisée.
- **Chiffrement** : TLS strict en transit ; chiffrement de la base au repos ; secrets hors du code source.
- **Authentification** : mots de passe hachés (Argon2/bcrypt via Spring Security), JWT à durée limitée, gestion des rôles.
- **Stockage mobile** : pas de données sensibles en clair ; usage du Keychain (iOS) / Keystore (Android) ; masquage à l'affichage.
- **Bonnes pratiques** : certificate pinning, protection contre la rétro-ingénierie, journaux d'audit côté serveur.

### 9.3 Responsabilité
- CGU et avertissements explicites (« en cas d'urgence vitale, appelez le 185 / 180 »).
- Aucune information de disponibilité présentée comme certaine sans **horodatage de fraîcheur**.
- Accès permanent à l'appel direct des secours.

---

## 10. Trajectoire de livraison (MVP / V2 / V3)

### Phase 1 — MVP : « Sauver des vies sans dépendre du temps réel »
Livrable robuste et autonome, à forte valeur immédiate.
- Onboarding, inscription, carte + position.
- Annuaire géolocalisé (amorcé et fiabilisé sur Abidjan).
- Tri par proximité, itinéraire et temps de trajet.
- Filtre par type de besoin (services déclarés).
- Appels d'urgence 185 / 180.
- Mode hors-ligne.

### Phase 2 — V2 : « Introduire la disponibilité, proprement »
- **Back-office hôpital** (web) : mise à jour d'un statut simple par service — `DISPONIBLE / SATURÉ / FERMÉ` — en quelques secondes.
- Affichage du statut dans l'application **avec horodatage de fraîcheur**.
- Notifications FCM (confirmation de demande, suivi d'un centre).
- **Déploiement pilote** sur un nombre limité d'hôpitaux partenaires à Abidjan.

### Phase 3 — V3 : « Passer à l'échelle »
- Remontée de statut par **USSD/SMS** pour les établissements peu connectés.
- Interconnexion éventuelle avec des EMR (type Helium Health) ou la régulation SAMU.
- Multilingue, support tablette, tableau de bord analytique.

---

## 11. Livrables attendus & critères d'acceptation

### 11.1 Livrables prestataire
- Maquettes / wireframes et charte graphique (logo, couleurs, typographies — **à créer**).
- Application Flutter (Android + iOS), publiée sur les stores.
- API back-end **Spring Boot** + base PostgreSQL/PostGIS.
- Back-office hôpital (V2).
- Code source documenté + dépôt versionné.
- Documentation technique (architecture, API, déploiement) et manuel d'exploitation.
- Jeux de tests automatisés ; plan de maintenance et de dépannage.

### 11.2 Critères d'acceptation (extraits)
- Parcours « demande d'urgence » fonctionnel de bout en bout.
- Affichage correct des centres les plus proches et calcul d'itinéraire vérifiés sur Abidjan.
- Fonctionnement hors-ligne démontré (annuaire + appels).
- Tests de sécurité conformes aux exigences MASVS.
- Performances conformes aux ENF (cf. §7).

---

## 12. Risques et mesures de mitigation

| # | Risque | Impact | Mitigation |
|:--:|:---|:---:|:---|
| R1 | Donnée de disponibilité temps réel indisponible | Élevé | MVP sans temps réel ; back-office hôpital en V2 ; partenariats. |
| R2 | Qualité/fraîcheur incomplète de l'annuaire | Moyen | Fiabilisation manuelle Abidjan d'abord ; contributions OSM. |
| R3 | Connectivité réseau inégale | Élevé | Conception offline-first dès le départ. |
| R4 | Coûts d'API cartographiques | Moyen | OpenStreetMap + cache ; Google Maps en option ciblée. |
| R5 | Non-conformité réglementaire (données santé) | Élevé | Démarche ARTCI anticipée ; minimisation ; chiffrement. |
| R6 | Responsabilité en cas d'information erronée | Élevé | Horodatage ; avertissements ; appel secours toujours accessible. |
| R7 | Adoption par les hôpitaux (V2) | Moyen | Back-office ultra-simple ; accompagnement ; pilote restreint. |

---

## 13. Glossaire

| Terme | Définition |
|:---|:---|
| **MVP** | *Minimum Viable Product* — première version livrable apportant la valeur essentielle. |
| **PostGIS** | Extension géospatiale de PostgreSQL (requêtes de proximité, géométries). |
| **KNN** | *K-Nearest Neighbors* — recherche des K éléments les plus proches. |
| **FCM** | *Firebase Cloud Messaging* — service de notifications push. |
| **JWT** | *JSON Web Token* — jeton d'authentification. |
| **OSM** | OpenStreetMap — cartographie libre et ouverte. |
| **OSRM / GraphHopper** | Moteurs de calcul d'itinéraire open source. |
| **EMR** | *Electronic Medical Record* — dossier patient informatisé. |
| **ARTCI** | Autorité de Régulation des Télécommunications/TIC de Côte d'Ivoire (autorité de protection des données). |
| **OWASP MASVS** | Référentiel de sécurité des applications mobiles. |
| **RPU** | Résumé de Passage aux Urgences (donnée métier hospitalière, modèle FR). |
| **USSD** | Protocole de messagerie sur réseau GSM (codes courts, sans data). |

---

## 14. Annexes & sources

**Numéros d'urgence — Côte d'Ivoire**
- **SAMU : 185** (gratuit) — Service d'Aide Médicale Urgente, bases à Abidjan et Yamoussoukro.
- **Sapeurs-Pompiers (GSPM) : 180**
- **Police : 170 / 111**

**Sources de données — établissements de santé**
- data.gouv.ci — *Hôpitaux, cliniques et pharmacies de Côte d'Ivoire* (1 300+ établissements géolocalisés)
- cartesanitaire.ci — Carte Sanitaire nationale (géolocalisation + services par établissement)
- annuaire.gouv.ci — annuaire des services publics / hôpitaux
- healthsites.io & OpenStreetMap ; Humanitarian Data Exchange (HDX)

**Références techniques & réglementaires**
- Loi n° 2013-450 du 19 juin 2013 (ARTCI) — protection des données à caractère personnel
- OWASP MASVS / MASTG — sécurité des applications mobiles
- PostGIS — recherche du plus proche voisin (opérateur `<->`, `ST_Distance`)
- Google Maps Platform — évolution tarifaire de mars 2025 (suppression du crédit mensuel de 200 $)
- Modèle de référence : SI-SAMU / *Mes Urgences* (Île-de-France) — affluence temps réel issue des logiciels métier des urgences (RPU)
- Écosystème santé numérique africain : Helium Health (EMR, offline-first, HL7/FHIR), mPharma, Field Intelligence

---

<div align="center">

*Document établi par NAPCOR GROUP — Mai 2026 — Version 1.0*
*Diffusion restreinte — Reproduction soumise à autorisation*

</div>
