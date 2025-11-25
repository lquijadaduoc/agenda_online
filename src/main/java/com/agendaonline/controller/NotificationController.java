package com.agendaonline.controller;

import com.agendaonline.domain.model.Notification;
import com.agendaonline.dto.notification.NotificationResponse;
import com.agendaonline.security.CurrentUserService;
import com.agendaonline.service.NotificationService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@PreAuthorize("hasAnyRole('PROFESSIONAL','ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    public NotificationController(NotificationService notificationService, CurrentUserService currentUserService) {
        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list() {
        Long professionalId = currentUserService.getCurrentProfessional().getId();
        List<NotificationResponse> result = notificationService.listByProfessional(professionalId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse resp = new NotificationResponse();
        resp.setId(notification.getId());
        if (notification.getProfessional() != null) {
            resp.setProfessionalId(notification.getProfessional().getId());
        }
        if (notification.getAppointment() != null) {
            resp.setAppointmentId(notification.getAppointment().getId());
        }
        resp.setChannel(notification.getChannel());
        resp.setType(notification.getType());
        resp.setRecipient(notification.getRecipient());
        resp.setStatus(notification.getStatus());
        resp.setSentAt(notification.getSentAt());
        resp.setErrorMessage(notification.getErrorMessage());
        return resp;
    }
}
