package com.example.propertymanagement.controller;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.maintenance.MaintenanceRequestCreate;
import com.example.propertymanagement.dto.maintenance.MaintenanceRequestDto;
import com.example.propertymanagement.dto.maintenance.MaintenanceStatusUpdate;
import com.example.propertymanagement.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenance-requests")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;

    public MaintenanceRequestController(MaintenanceRequestService maintenanceRequestService) {
        this.maintenanceRequestService = maintenanceRequestService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<MaintenanceRequestDto>> listRequests(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(maintenanceRequestService.getRequests(pageable));
    }

    @PostMapping
    public ResponseEntity<MaintenanceRequestDto> createRequest(
        @Valid @RequestBody MaintenanceRequestCreate request) {
        MaintenanceRequestDto dto = maintenanceRequestService.createRequest(request);
        return ResponseEntity.status(201).body(dto);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MaintenanceRequestDto> updateStatus(@PathVariable Long id,
                                                              @Valid @RequestBody MaintenanceStatusUpdate request) {
        return ResponseEntity.ok(maintenanceRequestService.updateStatus(id, request));
    }
}
