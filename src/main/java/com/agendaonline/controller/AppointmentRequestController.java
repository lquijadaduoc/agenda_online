package com.agendaonline.controller;

import com.agendaonline.domain.model.Appointment;
import com.agendaonline.domain.model.AppointmentRequest;
import com.agendaonline.dto.appointment.AppointmentRequestResponse;
import com.agendaonline.dto.appointment.AppointmentResponse;
import com.agendaonline.service.AppointmentRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointment-requests")
@PreAuthorize("hasAnyRole('PROFESSIONAL','ADMIN')")
public class AppointmentRequestController {

    private final AppointmentRequestService appointmentRequestService;

    public AppointmentRequestController(AppointmentRequestService appointmentRequestService) {
        this.appointmentRequestService = appointmentRequestService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentRequestResponse>> listPending() {
        List<AppointmentRequestResponse> result = appointmentRequestService.listPendingForCurrentProfessional()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<AppointmentResponse> accept(@PathVariable Long id) {
        Appointment appointment = appointmentRequestService.accept(id);
        return ResponseEntity.ok(toAppointmentResponse(appointment));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @Valid @RequestBody(required = false) RejectPayload payload) {
        String reason = payload != null ? payload.reason() : null;
        appointmentRequestService.reject(id, reason);
        return ResponseEntity.noContent().build();
    }

    private AppointmentRequestResponse toResponse(AppointmentRequest request) {
        AppointmentRequestResponse resp = new AppointmentRequestResponse();
        resp.setId(request.getId());
        if (request.getProfessional() != null) {
            resp.setProfessionalId(request.getProfessional().getId());
        }
        if (request.getService() != null) {
            resp.setServiceId(request.getService().getId());
        }
        resp.setClientName(request.getClientName());
        resp.setClientEmail(request.getClientEmail());
        resp.setClientPhone(request.getClientPhone());
        resp.setRequestedDateTime(request.getRequestedDateTime());
        resp.setStatus(request.getStatus());
        return resp;
    }

    private AppointmentResponse toAppointmentResponse(Appointment appointment) {
        AppointmentResponse resp = new AppointmentResponse();
        resp.setId(appointment.getId());
        if (appointment.getService() != null) {
            resp.setServiceId(appointment.getService().getId());
        }
        if (appointment.getClient() != null) {
            resp.setClientId(appointment.getClient().getId());
        }
        resp.setStartDateTime(appointment.getStartDateTime());
        resp.setEndDateTime(appointment.getEndDateTime());
        resp.setStatus(appointment.getStatus());
        resp.setNotes(appointment.getNotes());
        resp.setCancellationToken(appointment.getCancellationToken());
        return resp;
    }

    public record RejectPayload(@NotBlank(message = "Se requiere un motivo para rechazar") String reason) {
    }
}
