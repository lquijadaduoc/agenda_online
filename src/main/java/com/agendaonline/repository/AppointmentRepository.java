package com.agendaonline.repository;

import com.agendaonline.domain.enums.AppointmentStatus;
import com.agendaonline.domain.model.Appointment;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByProfessionalIdAndStartDateTimeBetween(Long professionalId, OffsetDateTime from, OffsetDateTime to);

    List<Appointment> findByProfessionalIdAndStatus(Long professionalId, AppointmentStatus status);

    Optional<Appointment> findByCancellationToken(String token);

    boolean existsByProfessionalIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
        Long professionalId, Iterable<AppointmentStatus> statuses, OffsetDateTime start, OffsetDateTime end);

    List<Appointment> findByProfessionalIdAndStatusInAndStartDateTimeBetween(
        Long professionalId, Iterable<AppointmentStatus> statuses, OffsetDateTime from, OffsetDateTime to);
}
