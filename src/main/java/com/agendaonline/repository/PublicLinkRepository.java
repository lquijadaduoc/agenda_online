package com.agendaonline.repository;

import com.agendaonline.domain.model.PublicLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicLinkRepository extends JpaRepository<PublicLink, Long> {
    Optional<PublicLink> findByToken(String token);
}
