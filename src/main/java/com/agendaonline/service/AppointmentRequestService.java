package com.agendaonline.service;

import com.agendaonline.domain.enums.AppointmentRequestStatus;
import com.agendaonline.domain.enums.AppointmentStatus;
import com.agendaonline.domain.enums.DefaultAppointmentStatus;
import com.agendaonline.domain.enums.NotificationChannel;
import com.agendaonline.domain.enums.NotificationStatus;
import com.agendaonline.domain.model.Appointment;
import com.agendaonline.domain.model.AppointmentRequest;
import com.agendaonline.domain.model.Client;
import com.agendaonline.domain.model.Professional;
import com.agendaonline.domain.model.ProfessionalSettings;
import com.agendaonline.domain.model.ServiceOffering;
import com.agendaonline.dto.appointment.AppointmentRequestCreateRequest;
import com.agendaonline.repository.AppointmentRepository;
import com.agendaonline.repository.AppointmentRequestRepository;
import com.agendaonline.repository.ClientRepository;
import com.agendaonline.repository.ProfessionalRepository;
import com.agendaonline.repository.ProfessionalSettingsRepository;
import com.agendaonline.repository.ServiceOfferingRepository;
import com.agendaonline.security.CurrentUserService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentRequestService {

    private final AppointmentRequestRepository appointmentRequestRepository;
    private final ProfessionalRepository professionalRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ClientRepository clientRepository;
    private final ProfessionalSettingsRepository settingsRepository;
    private final AppointmentRepository appointmentRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public AppointmentRequestService(AppointmentRequestRepository appointmentRequestRepository,
                                     ProfessionalRepository professionalRepository,
                                     ServiceOfferingRepository serviceOfferingRepository,
                                     ClientRepository clientRepository,
                                     ProfessionalSettingsRepository settingsRepository,
                                     AppointmentRepository appointmentRepository,
                                     CurrentUserService currentUserService,
                                     NotificationService notificationService,
                                     AuditLogService auditLogService) {
        this.appointmentRequestRepository = appointmentRequestRepository;
        this.professionalRepository = professionalRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.clientRepository = clientRepository;
        this.settingsRepository = settingsRepository;
        this.appointmentRepository = appointmentRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AppointmentRequest createPublic(String slug, AppointmentRequestCreateRequest request) {
        Professional professional = professionalRepository.findByPublicSlug(slug)
            .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));
        ensurePublicBookingAllowed(professional.getId());
        ServiceOffering service = serviceOfferingRepository.findById(request.getServiceId())
            .filter(s -> s.getProfessional().getId().equals(professional.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setProfessional(professional);
        appointmentRequest.setService(service);
        appointmentRequest.setClientName(request.getClientName());
        appointmentRequest.setClientEmail(request.getClientEmail());
        appointmentRequest.setClientPhone(request.getClientPhone());
        appointmentRequest.setRequestedDateTime(request.getRequestedDateTime());
        appointmentRequest.setStatus(AppointmentRequestStatus.PENDING);
        return appointmentRequestRepository.save(appointmentRequest);
    }

    @Transactional(readOnly = true)
    public List<AppointmentRequest> listPendingForCurrentProfessional() {
        Professional professional = currentUserService.getCurrentProfessional();
        return appointmentRequestRepository.findByProfessionalIdAndStatus(
            professional.getId(), AppointmentRequestStatus.PENDING);
    }

    @Transactional
    public Appointment accept(Long requestId) {
        AppointmentRequest appointmentRequest = loadOwnedRequest(requestId);
        if (appointmentRequest.getStatus() != AppointmentRequestStatus.PENDING) {
            throw new IllegalArgumentException("La solicitud ya fue procesada");
        }
        Professional professional = appointmentRequest.getProfessional();
        ServiceOffering service = appointmentRequest.getService();

        Client client = null;
        if (appointmentRequest.getClientEmail() != null) {
            client = clientRepository.findByProfessionalIdAndEmail(
                    professional.getId(), appointmentRequest.getClientEmail())
                .orElse(null);
        }
        if (client == null) {
            client = new Client();
            client.setProfessional(professional);
            client.setName(appointmentRequest.getClientName());
            client.setEmail(appointmentRequest.getClientEmail());
            client.setPhone(appointmentRequest.getClientPhone());
            clientRepository.save(client);
        }

        OffsetDateTime start = appointmentRequest.getRequestedDateTime();
        OffsetDateTime end = start.plusMinutes(service.getDurationMinutes());
        boolean overlap = appointmentRepository.existsByProfessionalIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            professional.getId(), AppointmentService.ACTIVE_STATUSES, end, start);
        if (overlap) {
            throw new IllegalArgumentException("El turno se superpone con otro existente");
        }

        Appointment appointment = new Appointment();
        appointment.setProfessional(professional);
        appointment.setService(service);
        appointment.setClient(client);
        appointment.setStartDateTime(start);
        appointment.setEndDateTime(end);
        appointment.setNotes(null);
        appointment.setCancellationToken(UUID.randomUUID().toString());
        appointment.setStatus(resolveDefaultStatus(professional));
        Appointment saved = appointmentRepository.save(appointment);

        appointmentRequest.setStatus(AppointmentRequestStatus.ACCEPTED);
        appointmentRequestRepository.save(appointmentRequest);

        notificationService.record(professional, saved, NotificationChannel.EMAIL, "APPOINTMENT_ACCEPTED",
            client.getEmail(), "{\"source\":\"request\"}", NotificationStatus.SENT, null);
        auditLogService.log(currentUserService.getCurrentUser(), "ACCEPT_APPOINTMENT_REQUEST", "AppointmentRequest",
            appointmentRequest.getId(), null);
        return saved;
    }

    @Transactional
    public void reject(Long requestId, String reason) {
        AppointmentRequest appointmentRequest = loadOwnedRequest(requestId);
        if (appointmentRequest.getStatus() != AppointmentRequestStatus.PENDING) {
            throw new IllegalArgumentException("La solicitud ya fue procesada");
        }
        appointmentRequest.setStatus(AppointmentRequestStatus.REJECTED);
        appointmentRequestRepository.save(appointmentRequest);
        auditLogService.log(currentUserService.getCurrentUser(), "REJECT_APPOINTMENT_REQUEST", "AppointmentRequest",
            appointmentRequest.getId(), reason);
    }

    private AppointmentRequest loadOwnedRequest(Long id) {
        Professional professional = currentUserService.getCurrentProfessional();
        return appointmentRequestRepository.findById(id)
            .filter(req -> req.getProfessional().getId().equals(professional.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
    }

    private AppointmentStatus resolveDefaultStatus(Professional professional) {
        ProfessionalSettings settings = settingsRepository.findByProfessionalId(professional.getId()).orElse(null);
        if (settings == null || settings.getDefaultAppointmentStatus() == null) {
            return AppointmentStatus.PENDING;
        }
        return settings.getDefaultAppointmentStatus() == DefaultAppointmentStatus.CONFIRMED
            ? AppointmentStatus.CONFIRMED
            : AppointmentStatus.PENDING;
    }

    private void ensurePublicBookingAllowed(Long professionalId) {
        ProfessionalSettings settings = settingsRepository.findByProfessionalId(professionalId).orElse(null);
        if (settings != null && !settings.isAllowPublicBooking()) {
            throw new IllegalArgumentException("Las reservas públicas están deshabilitadas");
        }
    }
}
