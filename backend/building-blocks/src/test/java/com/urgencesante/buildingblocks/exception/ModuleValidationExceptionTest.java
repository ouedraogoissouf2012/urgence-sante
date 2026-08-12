package com.urgencesante.buildingblocks.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ModuleValidationExceptionTest {

    @Test
    void expose_le_message_transmis() {
        final ModuleValidationException exception = new ModuleValidationException("donnée invalide");

        assertThat(exception).hasMessage("donnée invalide").isInstanceOf(RuntimeException.class);
    }
}
