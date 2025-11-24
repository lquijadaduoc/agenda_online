package com.agendaonline.service;

import com.agendaonline.domain.enums.AppointmentStatus;
import com.agendaonline.repository.AppointmentRepository;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {

    private static final Set<AppointmentStatus> ACTIVE_STATUSES = EnumSet.of(
        AppointmentStatus.PENDING,
        AppointmentStatus.CONFIRMED
    );

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public void ensureNoOverlap(Long professionalId, OffsetDateTime start, OffsetDateTime end) {
        boolean exists = appointmentRepository
            .existsByProfessionalIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                professionalId, ACTIVE_STATUSES, end, start);
        if (exists) {
            throw new IllegalStateException("Ya existe una cita en ese horario");
        }
    }
}
