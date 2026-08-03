/**
 * Module Patient — comptes patients (inscription et authentification).
 *
 * <p>Ce package constitue l'API publique du module. L'implémentation vit dans le
 * sous-package {@code internal} (domaine, application, adaptateurs) et n'est pas
 * accessible aux autres modules. Un module tiers ne dépend que de cette API.
 *
 * <p>Identifiant : numéro de téléphone. Authentification : mot de passe (haché
 * BCrypt) contre un jeton de session porteur (opaque, haché SHA-256 au repos).
 * Les données médicales du patient NE transitent PAS par ce module : elles
 * restent sur l'appareil (choix produit — aucune donnée de santé côté serveur).
 */
package com.urgencesante.patient;
