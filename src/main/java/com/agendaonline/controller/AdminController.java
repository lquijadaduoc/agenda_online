package com.agendaonline.controller;

import com.agendaonline.domain.model.Professional;
import com.agendaonline.dto.professional.ProfessionalResponse;
import com.agendaonline.service.AdminService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
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

    public record StatusPayload(boolean active) {
    }
}
