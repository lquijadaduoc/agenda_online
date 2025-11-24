package com.agendaonline.controller;

import com.agendaonline.domain.model.ServiceOffering;
import com.agendaonline.dto.service.ServiceOfferingRequest;
import com.agendaonline.dto.service.ServiceOfferingResponse;
import com.agendaonline.service.ServiceOfferingService;
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
@RequestMapping("/services")
public class ServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    public ServiceOfferingController(ServiceOfferingService serviceOfferingService) {
        this.serviceOfferingService = serviceOfferingService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceOfferingResponse>> list() {
        List<ServiceOfferingResponse> result = serviceOfferingService.list().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ServiceOfferingResponse> create(@Valid @RequestBody ServiceOfferingRequest request) {
        return ResponseEntity.ok(toResponse(serviceOfferingService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOfferingResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody ServiceOfferingRequest request) {
        return ResponseEntity.ok(toResponse(serviceOfferingService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serviceOfferingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ServiceOfferingResponse toResponse(ServiceOffering s) {
        ServiceOfferingResponse resp = new ServiceOfferingResponse();
        resp.setId(s.getId());
        resp.setName(s.getName());
        resp.setDescription(s.getDescription());
        resp.setDurationMinutes(s.getDurationMinutes());
        resp.setPrice(s.getPrice());
        resp.setActive(s.isActive());
        return resp;
    }
}
