package com.agendaonline.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @GetMapping
    public ResponseEntity<String> list(@RequestParam(required = false) String from,
                                       @RequestParam(required = false) String to,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) Long clientId,
                                       @RequestParam(required = false) Long serviceId) {
        return ResponseEntity.ok("appointments");
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody Object body) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable Long id) {
        return ResponseEntity.ok("appointment " + id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Object body) {
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cancel/{token}")
    public ResponseEntity<Void> cancelByToken(@PathVariable String token) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reschedule/{token}")
    public ResponseEntity<Void> rescheduleByToken(@PathVariable String token, @RequestBody Object body) {
        return ResponseEntity.noContent().build();
    }
}
