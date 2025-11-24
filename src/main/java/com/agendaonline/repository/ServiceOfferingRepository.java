package com.agendaonline.repository;

import com.agendaonline.domain.model.ServiceOffering;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {
    List<ServiceOffering> findByProfessionalIdAndActiveTrue(Long professionalId);
}
