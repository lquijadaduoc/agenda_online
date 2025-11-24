package com.agendaonline.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/professionals")
    public ResponseEntity<String> listProfessionals() {
        return ResponseEntity.ok("professionals");
    }

    @GetMapping("/professionals/{id}")
    public ResponseEntity<String> getProfessional(@PathVariable Long id) {
        return ResponseEntity.ok("professional " + id);
    }

    @PutMapping("/professionals/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<String> stats() {
        return ResponseEntity.ok("stats");
    }
}
