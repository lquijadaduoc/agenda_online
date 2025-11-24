package com.agendaonline.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/professionals/{slug}")
public class PublicBookingController {

    @PostMapping("/appointments")
    public ResponseEntity<Void> createPublicAppointment(@PathVariable String slug, @RequestBody Object body) {
        return ResponseEntity.noContent().build();
    }
}
