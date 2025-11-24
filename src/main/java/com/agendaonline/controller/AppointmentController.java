package com.agendaonline.controller;

import com.agendaonline.domain.model.Appointment;
import com.agendaonline.dto.appointment.AppointmentCreateRequest;
import com.agendaonline.dto.appointment.AppointmentResponse;
import com.agendaonline.dto.appointment.AppointmentUpdateRequest;
import com.agendaonline.service.AppointmentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> list() {
        List<AppointmentResponse> result = appointmentService.list().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentCreateRequest request) {
        return ResponseEntity.ok(toResponse(appointmentService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(appointmentService.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody AppointmentUpdateRequest request) {
        return ResponseEntity.ok(toResponse(appointmentService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        appointmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cancel/{token}")
    public ResponseEntity<Void> cancelByToken(@PathVariable String token) {
        appointmentService.cancelByToken(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reschedule/{token}")
    public ResponseEntity<Void> rescheduleByToken(@PathVariable String token, @RequestBody AppointmentUpdateRequest request) {
        appointmentService.cancelByToken(token);
        return ResponseEntity.noContent().build();
    }

    private AppointmentResponse toResponse(Appointment appointment) {
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
}
