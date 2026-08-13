package com.urgencesante.buildingblocks.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PageRequestTest {

    @Test
    void accepte_les_bornes_valides() {
        assertThat(PageRequest.of(0, 1).offset()).isZero();
        assertThat(PageRequest.of(PageRequest.MAX_PAGE, PageRequest.MAX_SIZE).page())
                .isEqualTo(PageRequest.MAX_PAGE);
    }

    @Test
    void refuse_une_page_negative() {
        assertThatThrownBy(() -> PageRequest.of(-1, 20))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_une_page_au_dela_du_plafond() {
        // Audit P3 #140 : avant ce plafond, un `page` arbitrairement grand
        // (ex. Integer.MAX_VALUE) était accepté et produisait un OFFSET SQL
        // arbitrairement grand — pagination profonde non bornée.
        assertThatThrownBy(() -> PageRequest.of(PageRequest.MAX_PAGE + 1, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(PageRequest.MAX_PAGE));

        assertThatThrownBy(() -> PageRequest.of(Integer.MAX_VALUE, 20))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_une_taille_hors_bornes() {
        assertThatThrownBy(() -> PageRequest.of(0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PageRequest.of(0, PageRequest.MAX_SIZE + 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void l_offset_borne_reste_dans_un_ordre_de_grandeur_raisonnable() {
        // Plafond du OFFSET maximal atteignable, pour tracer explicitement
        // l'ordre de grandeur accepté (voir Javadoc de PageRequest#MAX_PAGE).
        final long maxOffset = PageRequest.of(PageRequest.MAX_PAGE, PageRequest.MAX_SIZE).offset();
        assertThat(maxOffset).isEqualTo(1_000_000L);
    }
}
