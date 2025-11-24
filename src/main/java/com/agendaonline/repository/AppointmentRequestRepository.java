package com.agendaonline.repository;

import com.agendaonline.domain.enums.AppointmentRequestStatus;
import com.agendaonline.domain.model.AppointmentRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRequestRepository extends JpaRepository<AppointmentRequest, Long> {
    List<AppointmentRequest> findByProfessionalIdAndStatus(Long professionalId, AppointmentRequestStatus status);
}
