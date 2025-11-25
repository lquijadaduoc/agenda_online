package com.agendaonline.service;

import com.agendaonline.domain.enums.NotificationChannel;
import com.agendaonline.domain.enums.NotificationStatus;
import com.agendaonline.domain.model.Appointment;
import com.agendaonline.domain.model.Notification;
import com.agendaonline.domain.model.Professional;
import com.agendaonline.repository.NotificationRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification send(Professional professional,
                             Appointment appointment,
                             NotificationChannel channel,
                             String type,
                             String recipient,
                             String payload) {
        NotificationStatus status = NotificationStatus.SENT;
        String error = null;
        try {
            log.info("Sending {} notification type {} to {}", channel, type, recipient);
            // Hook real provider here.
        } catch (Exception ex) {
            status = NotificationStatus.FAILED;
            error = ex.getMessage();
            log.error("Failed to send notification type {}", type, ex);
        }
        return record(professional, appointment, channel, type, recipient, payload, status, error);
    }

    public Notification record(Professional professional,
                               Appointment appointment,
                               NotificationChannel channel,
                               String type,
                               String recipient,
                               String payload,
                               NotificationStatus status,
                               String errorMessage) {
        Notification notification = new Notification();
        notification.setProfessional(professional);
        notification.setAppointment(appointment);
        notification.setChannel(channel);
        notification.setType(type);
        notification.setRecipient(recipient != null ? recipient : "unknown");
        notification.setPayload(payload);
        notification.setStatus(status);
        notification.setErrorMessage(errorMessage);
        if (status == NotificationStatus.SENT) {
            notification.setSentAt(OffsetDateTime.now());
        }
        return notificationRepository.save(notification);
    }

    public List<Notification> listByProfessional(Long professionalId) {
        return notificationRepository.findByProfessionalId(professionalId);
    }

    public List<Notification> listAll() {
        return notificationRepository.findAll();
    }

    public boolean existsByAppointmentAndType(Long appointmentId, String type) {
        return notificationRepository.existsByAppointmentIdAndType(appointmentId, type);
    }
}
