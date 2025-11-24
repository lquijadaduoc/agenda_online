package com.agendaonline.repository;

import com.agendaonline.domain.model.ProfessionalSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalSettingsRepository extends JpaRepository<ProfessionalSettings, Long> {
    Optional<ProfessionalSettings> findByProfessionalId(Long professionalId);
}
