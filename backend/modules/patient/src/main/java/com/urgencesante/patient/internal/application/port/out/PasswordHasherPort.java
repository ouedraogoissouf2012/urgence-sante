package com.urgencesante.patient.internal.application.port.out;

/**
 * Hachage et vérification des mots de passe (port sortant).
 *
 * <p>Abstrait l'algorithme : l'implémentation de production utilise BCrypt, les
 * tests peuvent substituer un faux déterministe. Le mot de passe en clair ne
 * quitte jamais cette frontière.
 */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
