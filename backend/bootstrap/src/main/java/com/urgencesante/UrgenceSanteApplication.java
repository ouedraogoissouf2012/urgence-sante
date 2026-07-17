package com.urgencesante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'application Urgence Santé.
 *
 * <p>Placée au package racine {@code com.urgencesante}, cette classe permet à
 * Spring de découvrir la configuration de chaque module métier situé dans un
 * sous-package. L'assemblage vit ici ; aucune règle métier n'y figure.
 */
@SpringBootApplication
public class UrgenceSanteApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrgenceSanteApplication.class, args);
    }
}
