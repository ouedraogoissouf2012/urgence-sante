package com.urgencesante.patient.internal.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Empreinte SHA-256 (hex) d'un jeton de session. Le jeton en clair n'est jamais
 * persisté ni journalisé : seule l'empreinte sert au stockage et à la recherche.
 * SHA-256 (sans sel) convient ici car le jeton est aléatoire à haute entropie —
 * contrairement à un mot de passe choisi par l'humain, qui exige BCrypt.
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
            throw new IllegalStateException("SHA-256 indisponible", exception);
        }
    }
}
