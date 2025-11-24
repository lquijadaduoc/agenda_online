package com.agendaonline.controller;

import com.agendaonline.dto.availability.AvailableSlotResponse;
import com.agendaonline.service.AvailabilityService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/blocks")
    public ResponseEntity<String> listBlocks() {
        return ResponseEntity.ok("blocks");
    }

    @PostMapping("/blocks")
    public ResponseEntity<Void> createBlock(@RequestBody Object body) {
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/blocks/{id}")
    public ResponseEntity<Void> updateBlock(@PathVariable Long id, @RequestBody Object body) {
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/blocks/{id}")
    public ResponseEntity<Void> deleteBlock(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/slots")
    public ResponseEntity<List<AvailableSlotResponse>> getSlots(@RequestParam Long serviceId,
                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.getAvailableSlots(serviceId, date));
    }
}
