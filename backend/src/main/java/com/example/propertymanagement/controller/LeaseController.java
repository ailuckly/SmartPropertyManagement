package com.example.propertymanagement.controller;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.lease.LeaseDto;
import com.example.propertymanagement.dto.lease.LeaseRequest;
import com.example.propertymanagement.service.LeaseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lease management endpoints for owners, tenants and administrators.
 */
@RestController
@RequestMapping("/api/leases")
public class LeaseController {

    private final LeaseService leaseService;

    public LeaseController(LeaseService leaseService) {
        this.leaseService = leaseService;
    }

    /**
     * Lists leases scoped to the current authenticated user.
     */
    @GetMapping
    public ResponseEntity<PageResponse<LeaseDto>> listLeases(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(leaseService.getLeases(pageable));
    }

    /**
     * Returns lease details when the caller is authorised to view them.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeaseDto> getLease(@PathVariable Long id) {
        return ResponseEntity.ok(leaseService.getLease(id));
    }

    /**
     * Creates a lease (owners for their own properties, admins for any).
     */
    @PostMapping
    public ResponseEntity<LeaseDto> createLease(@Valid @RequestBody LeaseRequest request) {
        LeaseDto dto = leaseService.createLease(request);
        return ResponseEntity.status(201).body(dto);
    }

    /**
     * Updates lease data. Same access restrictions as creation apply.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeaseDto> updateLease(@PathVariable Long id,
                                                @Valid @RequestBody LeaseRequest request) {
        return ResponseEntity.ok(leaseService.updateLease(id, request));
    }

    /**
     * Deletes a lease and resets the linked property status.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLease(@PathVariable Long id) {
        leaseService.deleteLease(id);
        return ResponseEntity.noContent().build();
    }
}
