package com.urgencesante.patient.internal.adapter.out.security;

import com.urgencesante.patient.internal.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Hachage BCrypt (adaptatif, salé) — {@code spring-security-crypto}, sans le
 * starter complet. Le coût (force) est configurable ; 12 est un bon compromis
 * sécurité/latence en 2026.
 */
public class BCryptPasswordHasher implements PasswordHasherPort {

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordHasher(int strength) {
        this.encoder = new BCryptPasswordEncoder(strength);
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return encoder.matches(rawPassword, passwordHash);
    }
}
