package com.agendaonline.controller;

import com.agendaonline.domain.model.Professional;
import com.agendaonline.dto.professional.ProfessionalProfileUpdateRequest;
import com.agendaonline.dto.professional.ProfessionalResponse;
import com.agendaonline.service.ProfessionalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfessionalResponse> me() {
        Professional prof = professionalService.me();
        return ResponseEntity.ok(toResponse(prof));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody ProfessionalProfileUpdateRequest request) {
        professionalService.updateProfile(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/professionals/{slug}/public")
    public ResponseEntity<ProfessionalResponse> publicProfile(@PathVariable String slug) {
        Professional prof = professionalService.publicBySlug(slug);
        return ResponseEntity.ok(toResponse(prof));
    }

    private ProfessionalResponse toResponse(Professional professional) {
        ProfessionalResponse resp = new ProfessionalResponse();
        resp.setId(professional.getId());
        resp.setPublicSlug(professional.getPublicSlug());
        resp.setBusinessName(professional.getBusinessName());
        resp.setPhone(professional.getPhone());
        resp.setTimezone(professional.getTimezone());
        resp.setBio(professional.getBio());
        resp.setAddress(professional.getAddress());
        if (professional.getUser() != null) {
            resp.setName(professional.getUser().getName());
            resp.setEmail(professional.getUser().getEmail());
        }
        return resp;
    }
}
