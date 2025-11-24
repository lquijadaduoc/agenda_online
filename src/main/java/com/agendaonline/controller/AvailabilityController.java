package com.agendaonline.controller;

import com.agendaonline.dto.availability.AvailableSlotResponse;
import com.agendaonline.dto.availability.AvailabilityBlockRequest;
import com.agendaonline.dto.availability.AvailabilityBlockResponse;
import com.agendaonline.service.AvailabilityService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
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
    public ResponseEntity<List<AvailabilityBlockResponse>> listBlocks() {
        List<AvailabilityBlockResponse> blocks = availabilityService.listBlocks().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(blocks);
    }

    @PostMapping("/blocks")
    public ResponseEntity<AvailabilityBlockResponse> createBlock(@Valid @RequestBody AvailabilityBlockRequest request) {
        return ResponseEntity.ok(toResponse(availabilityService.createBlock(request)));
    }

    @PutMapping("/blocks/{id}")
    public ResponseEntity<AvailabilityBlockResponse> updateBlock(@PathVariable Long id,
                                                                 @Valid @RequestBody AvailabilityBlockRequest request) {
        return ResponseEntity.ok(toResponse(availabilityService.updateBlock(id, request)));
    }

    @DeleteMapping("/blocks/{id}")
    public ResponseEntity<Void> deleteBlock(@PathVariable Long id) {
        availabilityService.deleteBlock(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/slots")
    public ResponseEntity<List<AvailableSlotResponse>> getSlots(@RequestParam Long serviceId,
                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.getAvailableSlots(serviceId, date));
    }

    private AvailabilityBlockResponse toResponse(com.agendaonline.domain.model.AvailabilityBlock block) {
        AvailabilityBlockResponse resp = new AvailabilityBlockResponse();
        resp.setId(block.getId());
        resp.setWeekday(block.getWeekday());
        resp.setSpecificDate(block.getSpecificDate());
        resp.setStartTime(block.getStartTime());
        resp.setEndTime(block.getEndTime());
        resp.setRecurring(block.isRecurring());
        return resp;
    }
}
