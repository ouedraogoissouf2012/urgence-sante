package com.urgencesante.buildingblocks.security;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Génère un jeton porteur opaque, haute entropie (32 octets {@link SecureRandom},
 * encodage URL-safe sans padding) — même schéma que {@link TokenHasher} pour
 * son hachage. Réutilisable partout où une session/un identifiant a besoin
 * d'un secret aléatoire côté serveur (jamais dérivé d'une donnée métier).
 * {@link SecureRandom} est sûr pour un usage concurrent (voir sa Javadoc) :
 * l'instance partagée ci-dessous ne requiert aucune synchronisation côté
 * appelant.
 */
public final class OpaqueTokenGenerator {

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private OpaqueTokenGenerator() {
    }

    public static String generate() {
        final byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
