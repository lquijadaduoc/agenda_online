package com.agendaonline.controller;

import com.agendaonline.domain.model.Professional;
import com.agendaonline.domain.model.Notification;
import com.agendaonline.dto.notification.NotificationResponse;
import com.agendaonline.dto.professional.ProfessionalResponse;
import com.agendaonline.service.AdminService;
import com.agendaonline.service.NotificationService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final NotificationService notificationService;

    public AdminController(AdminService adminService, NotificationService notificationService) {
        this.adminService = adminService;
        this.notificationService = notificationService;
    }

    @GetMapping("/professionals")
    public ResponseEntity<List<ProfessionalResponse>> listProfessionals() {
        List<ProfessionalResponse> result = adminService.listProfessionals().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/professionals/{id}")
    public ResponseEntity<ProfessionalResponse> getProfessional(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(adminService.getProfessional(id)));
    }

    @PutMapping("/professionals/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody StatusPayload payload) {
        adminService.updateStatus(id, payload.active());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> listNotifications() {
        List<NotificationResponse> result = notificationService.listAll().stream()
            .map(this::toNotificationResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<String> stats() {
        return ResponseEntity.ok("pending implementation");
    }

    private ProfessionalResponse toResponse(Professional professional) {
        ProfessionalResponse resp = new ProfessionalResponse();
        resp.setId(professional.getId());
        resp.setPublicSlug(professional.getPublicSlug());
        resp.setBusinessName(professional.getBusinessName());
        resp.setPhone(professional.getPhone());
        if (professional.getUser() != null) {
            resp.setName(professional.getUser().getName());
            resp.setEmail(professional.getUser().getEmail());
        }
        return resp;
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
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

    public record StatusPayload(boolean active) {
    }
}
