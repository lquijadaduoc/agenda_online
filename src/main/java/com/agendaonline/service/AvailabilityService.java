package com.agendaonline.service;

import com.agendaonline.domain.enums.AppointmentStatus;
import com.agendaonline.domain.model.Appointment;
import com.agendaonline.domain.model.AvailabilityBlock;
import com.agendaonline.domain.model.Professional;
import com.agendaonline.domain.model.ProfessionalSettings;
import com.agendaonline.domain.model.ServiceOffering;
import com.agendaonline.dto.availability.AvailableSlotResponse;
import com.agendaonline.dto.availability.AvailabilityBlockRequest;
import com.agendaonline.repository.AppointmentRepository;
import com.agendaonline.repository.AvailabilityBlockRepository;
import com.agendaonline.repository.ProfessionalRepository;
import com.agendaonline.repository.ProfessionalSettingsRepository;
import com.agendaonline.repository.ServiceOfferingRepository;
import com.agendaonline.security.CurrentUserService;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvailabilityService {

    private static final Set<AppointmentStatus> ACTIVE_STATUSES = EnumSet.of(
        AppointmentStatus.PENDING,
        AppointmentStatus.CONFIRMED
    );

    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ProfessionalRepository professionalRepository;
    private final ProfessionalSettingsRepository settingsRepository;
    private final AppointmentRepository appointmentRepository;
    private final CurrentUserService currentUserService;

    public AvailabilityService(AvailabilityBlockRepository availabilityBlockRepository,
                               ServiceOfferingRepository serviceOfferingRepository,
                               ProfessionalRepository professionalRepository,
                               ProfessionalSettingsRepository settingsRepository,
                               AppointmentRepository appointmentRepository,
                               CurrentUserService currentUserService) {
        this.availabilityBlockRepository = availabilityBlockRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.professionalRepository = professionalRepository;
        this.settingsRepository = settingsRepository;
        this.appointmentRepository = appointmentRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityBlock> listBlocks() {
        Professional professional = currentUserService.getCurrentProfessional();
        return availabilityBlockRepository.findByProfessionalId(professional.getId());
    }

    @Transactional
    public AvailabilityBlock createBlock(AvailabilityBlockRequest request) {
        Professional professional = currentUserService.getCurrentProfessional();
        AvailabilityBlock block = new AvailabilityBlock();
        block.setProfessional(professional);
        applyBlock(block, request);
        return availabilityBlockRepository.save(block);
    }

    @Transactional
    public AvailabilityBlock updateBlock(Long id, AvailabilityBlockRequest request) {
        Professional professional = currentUserService.getCurrentProfessional();
        AvailabilityBlock block = availabilityBlockRepository.findById(id)
            .filter(b -> b.getProfessional().getId().equals(professional.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Bloque no encontrado"));
        applyBlock(block, request);
        return availabilityBlockRepository.save(block);
    }

    @Transactional
    public void deleteBlock(Long id) {
        Professional professional = currentUserService.getCurrentProfessional();
        AvailabilityBlock block = availabilityBlockRepository.findById(id)
            .filter(b -> b.getProfessional().getId().equals(professional.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Bloque no encontrado"));
        availabilityBlockRepository.delete(block);
    }

    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> getAvailableSlots(Long serviceId, LocalDate date) {
        ServiceOffering service = serviceOfferingRepository.findById(serviceId)
            .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        Professional professional = service.getProfessional();

        ProfessionalSettings settings = settingsRepository.findByProfessionalId(professional.getId())
            .orElse(null);
        if (settings != null && settings.getBookingAdvanceDays() != null) {
            LocalDate maxDate = LocalDate.now().plusDays(settings.getBookingAdvanceDays());
            if (date.isAfter(maxDate)) {
                return List.of();
            }
        }

        ZoneId zoneId = resolveZone(professional.getTimezone());
        OffsetDateTime dayStart = date.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime dayEnd = date.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();

        List<AvailabilityBlock> blocks = new ArrayList<>();
        blocks.addAll(availabilityBlockRepository.findByProfessionalIdAndSpecificDate(professional.getId(), date));
        blocks.addAll(availabilityBlockRepository.findByProfessionalIdAndWeekday(professional.getId(), date.getDayOfWeek().getValue() % 7));
        if (blocks.isEmpty()) {
            return List.of();
        }

        List<Appointment> appointments = appointmentRepository
            .findByProfessionalIdAndStatusInAndStartDateTimeBetween(professional.getId(), ACTIVE_STATUSES, dayStart, dayEnd);

        List<AvailableSlotResponse> slots = new ArrayList<>();
        int duration = service.getDurationMinutes();
        for (AvailabilityBlock block : blocks) {
            slots.addAll(generateSlotsForBlock(block, date, duration, zoneId, appointments));
        }

        slots.sort(Comparator.comparing(AvailableSlotResponse::getStart));
        return slots;
    }

    private List<AvailableSlotResponse> generateSlotsForBlock(AvailabilityBlock block,
                                                              LocalDate date,
                                                              int durationMinutes,
                                                              ZoneId zoneId,
                                                              List<Appointment> appointments) {
        List<AvailableSlotResponse> slots = new ArrayList<>();
        LocalTime start = block.getStartTime();
        LocalTime end = block.getEndTime();
        LocalTime cursor = start;
        while (!cursor.plusMinutes(durationMinutes).isAfter(end)) {
            OffsetDateTime slotStart = date.atTime(cursor).atZone(zoneId).toOffsetDateTime();
            OffsetDateTime slotEnd = slotStart.plusMinutes(durationMinutes);
            boolean conflicts = appointments.stream().anyMatch(a -> overlaps(a, slotStart, slotEnd));
            if (!conflicts) {
                slots.add(new AvailableSlotResponse(slotStart, slotEnd));
            }
            cursor = cursor.plusMinutes(durationMinutes);
        }
        return slots;
    }

    private boolean overlaps(Appointment appointment, OffsetDateTime start, OffsetDateTime end) {
        return appointment.getStartDateTime().isBefore(end) && appointment.getEndDateTime().isAfter(start);
    }

    private ZoneId resolveZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneId.of("UTC");
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException ex) {
            return ZoneId.of("UTC");
        }
    }

    private void applyBlock(AvailabilityBlock block, AvailabilityBlockRequest request) {
        block.setWeekday(request.getWeekday());
        block.setSpecificDate(request.getSpecificDate());
        block.setStartTime(request.getStartTime());
        block.setEndTime(request.getEndTime());
        block.setRecurring(Boolean.TRUE.equals(request.getRecurring()));
    }
}
