package com.agendaonline.service;

import com.agendaonline.domain.enums.AppointmentStatus;
import com.agendaonline.domain.model.Appointment;
import com.agendaonline.domain.model.Client;
import com.agendaonline.domain.model.Professional;
import com.agendaonline.domain.model.ProfessionalSettings;
import com.agendaonline.domain.model.ServiceOffering;
import com.agendaonline.dto.appointment.AppointmentCreateRequest;
import com.agendaonline.dto.appointment.PublicAppointmentRequest;
import com.agendaonline.dto.appointment.AppointmentUpdateRequest;
import com.agendaonline.repository.AppointmentRepository;
import com.agendaonline.repository.ClientRepository;
import com.agendaonline.repository.ProfessionalRepository;
import com.agendaonline.repository.ProfessionalSettingsRepository;
import com.agendaonline.repository.ServiceOfferingRepository;
import com.agendaonline.security.CurrentUserService;
import com.agendaonline.domain.enums.DefaultAppointmentStatus;
import com.agendaonline.domain.enums.NotificationChannel;
import com.agendaonline.domain.enums.NotificationStatus;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    public static final EnumSet<AppointmentStatus> ACTIVE_STATUSES = EnumSet.of(
        AppointmentStatus.PENDING,
        AppointmentStatus.CONFIRMED
    );

    private final AppointmentRepository appointmentRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ClientRepository clientRepository;
    private final ProfessionalRepository professionalRepository;
    private final ProfessionalSettingsRepository professionalSettingsRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              ServiceOfferingRepository serviceOfferingRepository,
                              ClientRepository clientRepository,
                              ProfessionalRepository professionalRepository,
                              ProfessionalSettingsRepository professionalSettingsRepository,
                              CurrentUserService currentUserService,
                              NotificationService notificationService,
                              AuditLogService auditLogService) {
        this.appointmentRepository = appointmentRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.clientRepository = clientRepository;
        this.professionalRepository = professionalRepository;
        this.professionalSettingsRepository = professionalSettingsRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<Appointment> list() {
        Professional professional = currentUserService.getCurrentProfessional();
        return appointmentRepository.findByProfessionalId(professional.getId());
    }

    @Transactional
    public Appointment create(AppointmentCreateRequest request) {
        Professional professional = currentUserService.getCurrentProfessional();
        if (request.getStartDateTime().isAfter(request.getEndDateTime()) || request.getStartDateTime().isEqual(request.getEndDateTime())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la de fin");
        }
        ServiceOffering service = serviceOfferingRepository.findById(request.getServiceId())
            .filter(s -> s.getProfessional().getId().equals(professional.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                .filter(c -> c.getProfessional().getId().equals(professional.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        }

        ensureNoOverlap(professional.getId(), request.getStartDateTime(), request.getEndDateTime());

        Appointment appointment = new Appointment();
        appointment.setProfessional(professional);
        appointment.setService(service);
        appointment.setClient(client);
        appointment.setStartDateTime(request.getStartDateTime());
        appointment.setEndDateTime(request.getEndDateTime());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(resolveDefaultStatus(professional));
        appointment.setCancellationToken(UUID.randomUUID().toString());
        Appointment saved = appointmentRepository.save(appointment);
        auditLogService.log(currentUserService.getCurrentUser(), "CREATE_APPOINTMENT", "Appointment",
            saved.getId(), null);
        sendNotification(professional, saved, "APPOINTMENT_CREATED", client);
        recordReminderIfConfigured(professional, saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Appointment get(Long id) {
        Professional professional = currentUserService.getCurrentProfessional();
        return appointmentRepository.findById(id)
            .filter(a -> a.getProfessional().getId().equals(professional.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado"));
    }

    @Transactional
    public Appointment update(Long id, AppointmentUpdateRequest request) {
        Appointment appointment = get(id);
        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }
        Appointment saved = appointmentRepository.save(appointment);
        auditLogService.log(currentUserService.getCurrentUser(), "UPDATE_APPOINTMENT", "Appointment",
            saved.getId(), null);
        return saved;
    }

    @Transactional
    public void cancel(Long id) {
        Appointment appointment = get(id);
        appointment.setStatus(AppointmentStatus.CANCELLED_BY_PROFESSIONAL);
        enforceCancellationPolicy(appointment);
        appointmentRepository.save(appointment);
        auditLogService.log(currentUserService.getCurrentUser(), "CANCEL_APPOINTMENT", "Appointment",
            appointment.getId(), null);
        sendNotification(appointment.getProfessional(), appointment, "APPOINTMENT_CANCELLED", appointment.getClient());
    }

    @Transactional
    public Appointment createPublic(String slug, PublicAppointmentRequest request) {
        Professional professional = professionalRepository.findByPublicSlug(slug)
            .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));
        ensurePublicBookingAllowed(professional.getId());
        if (request.getStartDateTime() == null) {
            throw new IllegalArgumentException("Fecha de inicio requerida");
        }
        ServiceOffering service = serviceOfferingRepository.findById(request.getServiceId())
            .filter(s -> s.getProfessional().getId().equals(professional.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        Client client = null;
        if (request.getClientEmail() != null) {
            client = clientRepository.findByProfessionalIdAndEmail(professional.getId(), request.getClientEmail())
                .orElse(null);
        }
        if (client == null) {
            client = new Client();
            client.setProfessional(professional);
            client.setName(request.getClientName());
            client.setEmail(request.getClientEmail());
            client.setPhone(request.getClientPhone());
            clientRepository.save(client);
        }

        Appointment appointment = new Appointment();
        appointment.setProfessional(professional);
        appointment.setService(service);
        appointment.setClient(client);
        var end = request.getStartDateTime().plusMinutes(service.getDurationMinutes());
        ensureNoOverlap(professional.getId(), request.getStartDateTime(), end);
        appointment.setStartDateTime(request.getStartDateTime());
        appointment.setEndDateTime(end);
        appointment.setStatus(resolveDefaultStatus(professional));
        appointment.setNotes(request.getNotes());
        appointment.setCreatedFromPublicLink(true);
        appointment.setCancellationToken(UUID.randomUUID().toString());
        Appointment saved = appointmentRepository.save(appointment);
        sendNotification(professional, saved, "PUBLIC_APPOINTMENT_CREATED", client);
        recordReminderIfConfigured(professional, saved);
        return saved;
    }

    @Transactional
    public void cancelByToken(String token) {
        Appointment appointment = appointmentRepository.findByCancellationToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Token inválido"));
        enforceCancellationPolicy(appointment);
        appointment.setStatus(AppointmentStatus.CANCELLED_BY_CLIENT);
        appointmentRepository.save(appointment);
        sendNotification(appointment.getProfessional(), appointment, "APPOINTMENT_CANCELLED_BY_CLIENT", appointment.getClient());
    }

    private void ensureNoOverlap(Long professionalId, java.time.OffsetDateTime start, java.time.OffsetDateTime end) {
        boolean exists = appointmentRepository.existsByProfessionalIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            professionalId, ACTIVE_STATUSES, end, start);
        if (exists) {
            throw new IllegalArgumentException("El turno se superpone con otro existente");
        }
    }

    private AppointmentStatus resolveDefaultStatus(Professional professional) {
        ProfessionalSettings settings = professionalSettingsRepository.findByProfessionalId(professional.getId())
            .orElse(null);
        if (settings == null || settings.getDefaultAppointmentStatus() == null) {
            return AppointmentStatus.PENDING;
        }
        return settings.getDefaultAppointmentStatus() == DefaultAppointmentStatus.CONFIRMED
            ? AppointmentStatus.CONFIRMED
            : AppointmentStatus.PENDING;
    }

    private void ensurePublicBookingAllowed(Long professionalId) {
        ProfessionalSettings settings = professionalSettingsRepository.findByProfessionalId(professionalId).orElse(null);
        if (settings != null && !settings.isAllowPublicBooking()) {
            throw new IllegalArgumentException("Las reservas públicas están deshabilitadas");
        }
    }

    private void enforceCancellationPolicy(Appointment appointment) {
        ProfessionalSettings settings = professionalSettingsRepository.findByProfessionalId(
            appointment.getProfessional().getId()).orElse(null);
        if (settings == null || settings.getCancellationPolicyHours() == null) {
            return;
        }
        OffsetDateTime deadline = appointment.getStartDateTime().minusHours(settings.getCancellationPolicyHours());
        if (OffsetDateTime.now().isAfter(deadline)) {
            throw new IllegalArgumentException("No se puede cancelar dentro de la ventana restringida");
        }
    }

    private void sendNotification(Professional professional, Appointment appointment, String type, Client client) {
        String recipient = client != null ? client.getEmail() : null;
        notificationService.send(professional, appointment, NotificationChannel.EMAIL, type, recipient,
            "{\"appointmentId\":" + appointment.getId() + "}");
    }

    private void recordReminderIfConfigured(Professional professional, Appointment appointment) {
        ProfessionalSettings settings = professionalSettingsRepository.findByProfessionalId(professional.getId())
            .orElse(null);
        if (settings != null && settings.getReminderTimeBeforeAppointmentMinutes() != null) {
            notificationService.record(professional, appointment, NotificationChannel.EMAIL, "REMINDER_SCHEDULED",
                null, "{\"minutes_before\":" + settings.getReminderTimeBeforeAppointmentMinutes() + "}",
                NotificationStatus.SENT, null);
        }
    }
}
