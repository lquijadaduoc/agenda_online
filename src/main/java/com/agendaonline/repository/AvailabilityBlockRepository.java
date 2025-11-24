package com.agendaonline.repository;

import com.agendaonline.domain.model.AvailabilityBlock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityBlockRepository extends JpaRepository<AvailabilityBlock, Long> {
    List<AvailabilityBlock> findByProfessionalId(Long professionalId);

    List<AvailabilityBlock> findByProfessionalIdAndSpecificDate(Long professionalId, LocalDate date);

    List<AvailabilityBlock> findByProfessionalIdAndWeekday(Long professionalId, Integer weekday);
}
