# Point d'avancement — Application « Urgence Santé »

**Pour la Direction — NAPCOR GROUP · 19 juillet 2026**

---

## L'essentiel en 30 secondes

> **Le cœur de l'application est construit et fonctionne.** Une personne en
> urgence à Abidjan peut être orientée vers le centre de santé adapté le plus
> proche : carte, itinéraire, temps de trajet, et **appel direct du SAMU (185)
> ou des Pompiers (180)** — même sans connexion internet. Le parcours complet
> est démontrable de bout en bout.

Ce qui était promis pour le premier mois (Lot 1) est **livré**, avec un niveau
de qualité « production » — pas un prototype jetable.

---

## Où en est-on ? Les 9 fonctionnalités du Lot 1

| Fonctionnalité promise | État |
|---|:--:|
| Écran d'accueil, conditions, autorisation de localisation | ✅ Fait |
| Carte avec la position de l'utilisateur | ✅ Fait |
| Annuaire géolocalisé des centres de santé | ✅ Fait |
| Recherche automatique du centre le plus proche | ✅ Fait |
| Itinéraire et temps de trajet | ✅ Fait |
| Filtre par type de besoin (urgences, maternité, pédiatrie…) | ✅ Fait |
| Boutons d'appel d'urgence 185 / 180 | ✅ Fait |
| Fonctionnement de base sans connexion | ✅ Fait |
| Socle technique de production (fiabilité, sécurité, tests) | ✅ Fait |

**→ Les 9 engagements fermes du Lot 1 sont couverts.**

---

## Pourquoi c'est du solide (et pas juste « ça marche à la démo »)

- **C'est testé automatiquement.** Chaque livraison passe une batterie de
  contrôles ; la dernière est **entièrement au vert**.
- **C'est sécurisé.** Accès protégés, données validées, secrets hors du code.
- **C'est robuste.** L'application reste utile même quand le réseau est mauvais
  ou absent — un point critique en Côte d'Ivoire.
- **C'est réutilisable.** Le code est organisé pour accueillir les phases
  suivantes sans tout réécrire.

---

## Ce qui est volontairement reporté (et pourquoi)

| Reporté | Raison |
|---|---|
| **Disponibilité « en temps réel »** des hôpitaux | Cette donnée **n'existe pas** de façon exploitable en Côte d'Ivoire. La livrer suppose des **partenariats hôpitaux** et un outil de saisie — c'est la phase suivante. Afficher un faux « temps réel » en situation d'urgence serait dangereux. |
| Notifications, SMS/USSD, annuaire national | Phases ultérieures, non essentielles à la démonstration. |

**Bonne nouvelle :** le **portail de saisie pour les hôpitaux** (brique clé de
la phase suivante) est **déjà amorcé** — nous avons de l'avance sur la V2.

---

## Points à surveiller

1. **Fiabilisation des données d'Abidjan** (coordonnées + services réels des
   centres) : c'est le poste le plus long. Volume cible : 150 à 300 centres.
2. **Démarche réglementaire ARTCI** (données de santé) : à lancer en parallèle,
   son délai est administratif et indépendant du développement.
3. **Génération du fichier d'installation Android (APK)** : automatisée sur
   serveur, à confirmer avant chaque remise au client.

---

## En résumé pour la décision

| Question | Réponse |
|---|---|
| Le Lot 1 est-il tenu ? | **Oui**, les 9 fonctionnalités sont livrées. |
| Est-ce démontrable au client ? | **Oui**, parcours complet de bout en bout. |
| Est-ce de la qualité durable ? | **Oui**, code de production, testé et sécurisé. |
| Le « temps réel » est-il là ? | **Non** — phase suivante, par choix assumé. |
| Sommes-nous en avance quelque part ? | **Oui**, le portail hôpital de la V2 est déjà amorcé. |

---

*Un rapport technique détaillé et mesuré est disponible pour l'équipe de
développement : `reports/RAPPORT_AVANCEMENT.md`.*
