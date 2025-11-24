package com.agendaonline.repository;

import com.agendaonline.domain.model.Client;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByProfessionalId(Long professionalId);

    Optional<Client> findByProfessionalIdAndEmail(Long professionalId, String email);
}
