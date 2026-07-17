<div align="center">

# LOT 1 — PÉRIMÈTRE DE LIVRAISON À 1 MOIS

## Application Mobile « Urgence Santé »

### Fonctionnalités livrables en un mois — Document d'aide à la décision

---

**NAPCOR GROUP**

| | |
|---|---|
| **Projet** | Application Urgence Santé (APPLI SANTÉ) |
| **Objet** | Fonctionnalités réalisables en 1 mois (≈ 20 jours ouvrés) |
| **Marché** | Côte d'Ivoire — Grand Abidjan (périmètre Lot 1) |
| **Stack** | Flutter (mobile) · Spring Boot + PostgreSQL/PostGIS (back-end) |
| **Version** | 1.0 |
| **Date** | Mai 2026 |
| **Destinataire** | Direction / Client — pour décision |

</div>

---

## 1. En une phrase

> **En un mois, nous livrons une application Android fonctionnelle qui oriente une personne en situation d'urgence vers le centre de santé adapté le plus proche d'Abidjan — avec itinéraire, temps de trajet et appel direct des secours.**

C'est **le cœur du projet**, celui qui répond directement au problème vécu (être orienté vers le bon hôpital sans perdre de temps). La fonction de **disponibilité « en temps réel »** ne fait **pas** partie de ce lot : elle nécessite une phase ultérieure (voir §5).

---

## 2. Hypothèse de réalisation

| Paramètre | Valeur retenue |
|:---|:---|
| Durée | 1 mois ≈ **20 jours ouvrés** |
| Équipe | **1 développeur Flutter + 1 développeur Spring Boot**, à plein temps, + appui design à temps partiel |
| Plateforme livrée | **Android** (application installable / démonstrable) |
| Couverture géographique | **Grand Abidjan** (≈ 150 à 300 établissements fiabilisés) |
| Niveau de qualité | **Version production** (architecture en couches, tests, sécurité) — pas un prototype jetable |

> ⚠️ Tout changement de cette hypothèse (taille d'équipe, ajout de plateforme iOS, couverture nationale) modifie le périmètre livrable.

---

## 3. ✅ Fonctionnalités livrées — Engagement ferme (Lot 1)

| # | Fonctionnalité | Description |
|:--:|:---|:---|
| **L1** | **Démarrage & conditions** | Écran d'accueil, acceptation des conditions d'utilisation, demande d'autorisation de localisation. |
| **L2** | **Carte & position** | Affichage d'une carte avec la position de l'utilisateur en temps réel (OpenStreetMap). |
| **L3** | **Annuaire géolocalisé des centres** | Base des établissements de santé d'Abidjan (publics et privés), géolocalisés, avec leurs services. |
| **L4** | **Recherche du plus proche** | Tri automatique des centres adaptés par proximité réelle (moteur géospatial). |
| **L5** | **Itinéraire & temps de trajet** | Calcul du chemin et de la durée estimée vers le centre choisi. |
| **L6** | **Filtre par besoin** | Sélection du type de soin recherché (urgences, maternité, pédiatrie, chirurgie…). |
| **L7** | **Appel d'urgence direct** | Boutons d'appel **SAMU 185** et **Pompiers 180**, accessibles en permanence. |
| **L8** | **Fonctionnement hors-ligne (base)** | L'annuaire et les numéros d'urgence restent consultables sans connexion internet. |
| **L9** | **Socle technique de production** | Back-end **Spring Boot** structuré en couches, base **PostgreSQL/PostGIS**, **tests automatisés**, validation des données, gestion centralisée des erreurs, journalisation, secrets sécurisés. |

**Livrable concret remis au client :** une **application Android installable** démontrant le parcours complet « urgence → centre adapté le plus proche → itinéraire / appel » sur Abidjan.

---

## 4. 🟡 Fonctionnalités possibles si l'avance le permet (optionnelles)

À livrer **uniquement** si le rythme le permet, **sans compromettre** le Lot 1 :

| # | Fonctionnalité | Condition |
|:--:|:---|:---|
| O1 | **Comptes utilisateurs** (inscription / connexion sécurisée) | Le « mot de passe oublié » suppose un canal e-mail/SMS tiers à brancher. *Non indispensable à la démonstration.* |
| O2 | **Version iOS** (iPhone) | Nécessite un compte Apple Developer et un poste de build adapté. |
| O3 | **Extension de l'annuaire** au-delà d'Abidjan | Dépend du temps de fiabilisation des données. |

> 💡 **Recommandation :** pour la décision, l'application peut démarrer **sans création de compte**. Nous proposons donc de **sécuriser d'abord le Lot 1**, et de traiter ces options en complément.

---

## 5. 🔴 Hors périmètre du mois — et pourquoi (transparence)

| Élément exclu | Raison |
|:---|:---|
| **Disponibilité « en temps réel »** des services | Cette donnée **n'existe pas** de façon exploitable en Côte d'Ivoire : aucun système hospitalier ne la diffuse. Elle doit être **construite** via un outil de saisie pour les hôpitaux + des **partenariats** — impossible en un mois, quel que soit l'effort fourni. |
| **Outil de saisie pour les hôpitaux** (back-office) | Brique de la phase suivante : suppose l'adhésion d'hôpitaux pilotes et un déploiement sur le terrain. |
| **Notifications push** | Non essentielles à la démonstration ; reportées. |
| **SMS/USSD, interconnexion avec les systèmes hospitaliers, régulation SAMU** | Dépendances externes lourdes ; phases ultérieures. |
| **Annuaire national exhaustif + multilingue** | Le contrôle qualité de l'ensemble des ~2 500 établissements du pays ne tient pas en un mois ; le Lot 1 cible Abidjan. |

> Cette honnêteté est volontaire : présenter une disponibilité « temps réel » non fiable dans un contexte d'urgence vitale serait **dangereux et irresponsable**. Nous préférons livrer d'abord une valeur **solide et vérifiable**.

---

## 6. Déroulé indicatif des 4 semaines

| Semaine | Contenu principal |
|:--:|:---|
| **Semaine 1** | Fondations : socle Spring Boot en couches + base géospatiale, **import des données ouvertes**, squelette de l'application Flutter, mise en place des tests. |
| **Semaine 2** | Moteur de recherche du centre le plus proche (testé) ; carte, géolocalisation et liste côté mobile. |
| **Semaine 3** | Itinéraire et temps de trajet, filtre par besoin, boutons d'appel d'urgence, mode hors-ligne, liaison mobile ↔ serveur. |
| **Semaine 4** | **Renforcement** (sécurité, validation, robustesse), **fiabilisation manuelle des données d'Abidjan**, tests finaux, préparation du build de démonstration. |

---

## 7. Conditions de réussite (à valider avec le client)

1. **Périmètre figé** : le Lot 1 est arrêté ; tout ajout en cours de route décale d'autant le reste.
2. **Données** : la vérification manuelle des établissements d'Abidjan (coordonnées + services réels) est le poste le plus long → volume plafonné à ~150–300 centres.
3. **Accès fournis dès le départ** : serveur d'hébergement, compte store, (et fournisseur SMS si l'option comptes est retenue).
4. **Conformité** : la démarche réglementaire **ARTCI** (données de santé) doit être **lancée en parallèle** — son délai administratif est indépendant du développement.

---

## 8. Synthèse pour la décision

| Question du client | Réponse |
|:---|:---|
| Qu'aurons-nous au bout d'un mois ? | Une **application Android fonctionnelle** d'orientation vers les urgences à Abidjan. |
| Est-ce utile / démontrable ? | Oui : parcours complet **urgence → centre adapté le plus proche → itinéraire / appel**. |
| Le « temps réel » est-il inclus ? | **Non** — phase ultérieure (nécessite hôpitaux partenaires + outil de saisie). |
| Est-ce de la qualité jetable ? | **Non** — code de production (architecture, tests, sécurité), réutilisable pour les phases suivantes. |

---

<div align="center">

*Document établi par NAPCOR GROUP — Mai 2026 — Version 1.0 — Diffusion restreinte*

</div>
