package com.urgencesante.medicalservice;

import java.util.Optional;

/**
 * Vue publique d'un service médical, exposée aux autres modules. Ne révèle
 * aucun type interne.
 */
public record MedicalServiceView(String code, String label, Optional<String> category) {
}
