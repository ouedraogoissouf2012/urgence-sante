package com.urgencesante.medicalservice.internal.adapter.out.persistence.repository;

import com.urgencesante.medicalservice.internal.adapter.out.persistence.entity.MedicalServiceJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository Spring Data du catalogue (détail de persistance). */
public interface MedicalServiceSpringRepository
        extends JpaRepository<MedicalServiceJpaEntity, String> {

    List<MedicalServiceJpaEntity> findAllByOrderByLabelAsc();

    List<MedicalServiceJpaEntity> findByCategoryOrderByLabelAsc(String category);
}
