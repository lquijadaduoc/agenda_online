package com.agendaonline;

import com.agendaonline.domain.enums.AppointmentStatus;
import com.agendaonline.domain.enums.Role;
import com.agendaonline.domain.model.Appointment;
import com.agendaonline.domain.model.AvailabilityBlock;
import com.agendaonline.domain.model.Professional;
import com.agendaonline.domain.model.ServiceOffering;
import com.agendaonline.domain.model.User;
import com.agendaonline.dto.availability.AvailableSlotResponse;
import com.agendaonline.repository.AppointmentRepository;
import com.agendaonline.repository.AvailabilityBlockRepository;
import com.agendaonline.repository.ProfessionalRepository;
import com.agendaonline.repository.ServiceOfferingRepository;
import com.agendaonline.repository.UserRepository;
import com.agendaonline.service.AvailabilityService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AvailabilityServiceTest {

    @Autowired
    private AvailabilityService availabilityService;
    @Autowired
    private AvailabilityBlockRepository availabilityBlockRepository;
    @Autowired
    private ServiceOfferingRepository serviceOfferingRepository;
    @Autowired
    private ProfessionalRepository professionalRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;

    private ServiceOffering service;
    private Professional professional;
    private LocalDate monday;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        availabilityBlockRepository.deleteAll();
        serviceOfferingRepository.deleteAll();
        professionalRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Pro User");
        user.setEmail("pro@example.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.PROFESSIONAL);
        user.setActive(true);
        userRepository.save(user);

        professional = new Professional();
        professional.setUser(user);
        professional.setPublicSlug("pro-slug");
        professional.setBusinessName("Pro Biz");
        professionalRepository.save(professional);

        service = new ServiceOffering();
        service.setProfessional(professional);
        service.setName("Sesión");
        service.setDurationMinutes(60);
        serviceOfferingRepository.save(service);

        AvailabilityBlock block = new AvailabilityBlock();
        block.setProfessional(professional);
        block.setWeekday(1); // Monday
        block.setStartTime(LocalTime.of(9, 0));
        block.setEndTime(LocalTime.of(12, 0));
        block.setRecurring(true);
        availabilityBlockRepository.save(block);

        monday = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        Appointment appointment = new Appointment();
        appointment.setProfessional(professional);
        appointment.setService(service);
        OffsetDateTime start = monday.atTime(10, 0).atZone(ZoneOffset.UTC).toOffsetDateTime();
        appointment.setStartDateTime(start);
        appointment.setEndDateTime(start.plusHours(1));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
    }

    @Test
    @Transactional
    void shouldReturnSlotsExcludingOverlaps() {
        List<AvailableSlotResponse> slots = availabilityService.getAvailableSlots(service.getId(), monday);
        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).getStart().toLocalTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(slots.get(1).getStart().toLocalTime()).isEqualTo(LocalTime.of(11, 0));
    }
}
