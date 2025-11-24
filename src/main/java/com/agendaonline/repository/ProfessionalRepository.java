package com.agendaonline.repository;

import com.agendaonline.domain.model.Professional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
    Optional<Professional> findByPublicSlug(String slug);

    Optional<Professional> findByUserId(Long userId);
}
