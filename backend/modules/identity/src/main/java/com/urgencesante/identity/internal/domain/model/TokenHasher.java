package com.urgencesante.identity.internal.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Empreinte SHA-256 (hex) d'un jeton. Le jeton en clair n'est jamais persisté
 * ni journalisé : seule l'empreinte sert au stockage et à la recherche.
 */
public final class TokenHasher {

    private TokenHasher() {
    }

    public static String sha256Hex(String rawToken) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            // SHA-256 est garanti par la plateforme Java.
            throw new IllegalStateException("SHA-256 indisponible", exception);
        }
    }
}
