package com.agendaonline.controller;

import com.agendaonline.dto.appointment.PublicAppointmentRequest;
import com.agendaonline.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/professionals/{slug}")
public class PublicBookingController {

    private final AppointmentService appointmentService;

    public PublicBookingController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/appointments")
    public ResponseEntity<Void> createPublicAppointment(@PathVariable String slug,
                                                        @Valid @RequestBody PublicAppointmentRequest request) {
        appointmentService.createPublic(slug, request);
        return ResponseEntity.noContent().build();
    }
}
