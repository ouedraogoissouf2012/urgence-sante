package com.urgencesante.medicalservice.internal.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.medicalservice.internal.application.port.out.LoadMedicalServicePort;
import com.urgencesante.medicalservice.internal.domain.model.MedicalService;
import com.urgencesante.medicalservice.internal.domain.model.MedicalServiceCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MedicalServiceCatalogServiceTest {

    private Optional<String> receivedCategory;

    /** Faux port en mémoire, substituable au vrai adaptateur. */
    private final LoadMedicalServicePort fakePort = new LoadMedicalServicePort() {
        @Override
        public List<MedicalService> findAll(Optional<String> category) {
            receivedCategory = category;
            return List.of(MedicalService.of(MedicalServiceCode.of("maternity"), "Maternité", "emergency"));
        }

        @Override
        public Optional<MedicalService> findByCode(MedicalServiceCode code) {
            return Optional.empty();
        }
    };
    private final MedicalServiceCatalogService service = new MedicalServiceCatalogService(fakePort);

    @Test
    void liste_le_catalogue() {
        final List<MedicalService> result = service.list(Optional.empty());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).label()).isEqualTo("Maternité");
    }

    @Test
    void transmet_le_filtre_de_categorie_au_port() {
        service.list(Optional.of("emergency"));

        assertThat(receivedCategory).contains("emergency");
    }

    @Test
    void tolere_une_categorie_nulle() {
        service.list(null);

        assertThat(receivedCategory).isEmpty();
    }
}
