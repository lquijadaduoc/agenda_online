package com.agendaonline.service;

import com.agendaonline.domain.enums.NotificationChannel;
import com.agendaonline.domain.model.Appointment;
import com.agendaonline.domain.model.ProfessionalSettings;
import com.agendaonline.repository.AppointmentRepository;
import com.agendaonline.repository.ProfessionalSettingsRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);
    private static final String REMINDER_TYPE = "APPOINTMENT_REMINDER";

    private final ProfessionalSettingsRepository settingsRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    public ReminderScheduler(ProfessionalSettingsRepository settingsRepository,
                             AppointmentRepository appointmentRepository,
                             NotificationService notificationService) {
        this.settingsRepository = settingsRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${reminders.scanDelayMs:300000}")
    @Transactional(readOnly = true)
    public void processReminders() {
        OffsetDateTime now = OffsetDateTime.now();
        List<ProfessionalSettings> settingsList = settingsRepository.findAll();
        for (ProfessionalSettings settings : settingsList) {
            if (settings.getReminderTimeBeforeAppointmentMinutes() == null
                || settings.getReminderTimeBeforeAppointmentMinutes() <= 0
                || settings.getProfessional() == null) {
                continue;
            }
            handleProfessional(settings, now);
        }
    }

    private void handleProfessional(ProfessionalSettings settings, OffsetDateTime now) {
        int minutes = settings.getReminderTimeBeforeAppointmentMinutes();
        OffsetDateTime windowEnd = now.plusMinutes(minutes);
        Long professionalId = settings.getProfessional().getId();

        List<Appointment> upcoming = appointmentRepository.findByProfessionalIdAndStatusInAndStartDateTimeBetween(
            professionalId, AppointmentService.ACTIVE_STATUSES, now, windowEnd);

        for (Appointment appointment : upcoming) {
            OffsetDateTime remindAt = appointment.getStartDateTime().minusMinutes(minutes);
            if (now.isBefore(remindAt)) {
                continue;
            }
            if (notificationService.existsByAppointmentAndType(appointment.getId(), REMINDER_TYPE)) {
                continue;
            }
            String recipient = appointment.getClient() != null ? appointment.getClient().getEmail() : null;
            notificationService.send(appointment.getProfessional(), appointment, NotificationChannel.EMAIL,
                REMINDER_TYPE, recipient,
                "{\"minutes_before\":" + minutes + "}");
            log.info("Reminder sent for appointment {}", appointment.getId());
        }
    }
}
