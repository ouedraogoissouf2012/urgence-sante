# ADR-009 — Élargissement du rayon d'orientation jusqu'à 70 km

- Statut : accepté
- Date : 2026-10-27

## Contexte

L'orientation cherchait les centres dans un rayon fixe de 15 km. Une analyse
du moteur réel depuis les centres du tableau client (Port-Bouët, 10 types
d'urgence) a montré des réponses VIDES : depuis Gonzagueville, aucun centre
dans 15 km pour une urgence gynéco-obstétricale ou psychiatrique — l'app
n'affichait alors que SAMU et Pompiers. En urgence, un centre éloigné vaut
mieux qu'un écran vide.

## Décision

1. Sans rayon imposé par l'appelant (cas de l'application), la recherche se
   fait à **15 km**, puis s'élargit à **30, 50 puis 70 km** tant qu'AUCUN
   centre ne répond au besoin, et s'arrête au premier palier utile.
2. « Ne répond pas au besoin » = aucun candidat dans le rayon, OU uniquement
   des centres inéligibles (service signalé fermé).
3. L'élargissement vit **côté serveur** (`OrientationService`) : toutes les
   versions de l'application en bénéficient sans mise à jour.
4. Un rayon **imposé** (`radiusMeters`) est respecté tel quel, jamais élargi.
5. Les paliers sont configurables (`orientation.widening-radii-meters`),
   validés au démarrage (strictement croissants, ≤ 100 km).
6. Le classement à l'intérieur d'un palier est inchangé (disponibilité puis
   proximité, ADR-008 pour l'éligibilité par niveau de soins).

## Conséquences

- Au-delà de 70 km : aucun centre, l'application oriente vers SAMU (185) et
  Pompiers (180) — comportement existant.
- Coût : jusqu'à 4 recherches successives quand rien n'est trouvé ; chacune
  est bornée (vivier de 30 candidats, un appel d'itinéraire groupé), et un
  palier sans candidat ne déclenche ni itinéraire ni disponibilité.
- La distance affichée sur chaque centre rend l'élargissement visible au
  patient ; aucun changement de contrat de réponse.
