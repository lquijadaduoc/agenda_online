package com.agendaonline.service;

import com.agendaonline.domain.enums.AppointmentStatus;
import com.agendaonline.domain.model.Appointment;
import com.agendaonline.domain.model.Client;
import com.agendaonline.domain.model.Professional;
import com.agendaonline.domain.model.ServiceOffering;
import com.agendaonline.dto.appointment.AppointmentCreateRequest;
import com.agendaonline.dto.appointment.PublicAppointmentRequest;
import com.agendaonline.dto.appointment.AppointmentUpdateRequest;
import com.agendaonline.repository.AppointmentRepository;
import com.agendaonline.repository.ClientRepository;
import com.agendaonline.repository.ProfessionalRepository;
import com.agendaonline.repository.ServiceOfferingRepository;
import com.agendaonline.security.CurrentUserService;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private static final EnumSet<AppointmentStatus> ACTIVE_STATUSES = EnumSet.of(
        AppointmentStatus.PENDING,
        AppointmentStatus.CONFIRMED
    );

    private final AppointmentRepository appointmentRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ClientRepository clientRepository;
    private final ProfessionalRepository professionalRepository;
    private final CurrentUserService currentUserService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              ServiceOfferingRepository serviceOfferingRepository,
                              ClientRepository clientRepository,
                              ProfessionalRepository professionalRepository,
                              CurrentUserService currentUserService) {
        this.appointmentRepository = appointmentRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.clientRepository = clientRepository;
        this.professionalRepository = professionalRepository;
        this.currentUserService = currentUserService;
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
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCancellationToken(UUID.randomUUID().toString());
        return appointmentRepository.save(appointment);
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
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancel(Long id) {
        Appointment appointment = get(id);
        appointment.setStatus(AppointmentStatus.CANCELLED_BY_PROFESSIONAL);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment createPublic(String slug, PublicAppointmentRequest request) {
        Professional professional = professionalRepository.findByPublicSlug(slug)
            .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));
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
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setNotes(request.getNotes());
        appointment.setCreatedFromPublicLink(true);
        appointment.setCancellationToken(UUID.randomUUID().toString());
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancelByToken(String token) {
        Appointment appointment = appointmentRepository.findByCancellationToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Token inválido"));
        appointment.setStatus(AppointmentStatus.CANCELLED_BY_CLIENT);
        appointmentRepository.save(appointment);
    }

    private void ensureNoOverlap(Long professionalId, java.time.OffsetDateTime start, java.time.OffsetDateTime end) {
        boolean exists = appointmentRepository.existsByProfessionalIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            professionalId, ACTIVE_STATUSES, end, start);
        if (exists) {
            throw new IllegalArgumentException("El turno se superpone con otro existente");
        }
    }
}
