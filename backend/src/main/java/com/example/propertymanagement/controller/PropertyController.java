package com.example.propertymanagement.controller;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.property.PropertyDto;
import com.example.propertymanagement.dto.property.PropertyRequest;
import com.example.propertymanagement.service.PropertyService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes property CRUD endpoints for the SPA. The heavy business logic is handled by {@link PropertyService}.
 */
@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    /**
     * Returns a paginated property dataset, optionally filtered by owner.
     */
    @GetMapping
    public ResponseEntity<PageResponse<PropertyDto>> getProperties(
        @PageableDefault Pageable pageable,
        @RequestParam(required = false) Long ownerId
    ) {
        return ResponseEntity.ok(propertyService.getProperties(pageable, ownerId));
    }

    /**
     * Returns the property details by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PropertyDto> getProperty(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getProperty(id));
    }

    /**
     * Creates a new property record.
     */
    @PostMapping
    public ResponseEntity<PropertyDto> createProperty(@Valid @RequestBody PropertyRequest request) {
        PropertyDto property = propertyService.createProperty(request);
        return ResponseEntity.status(201).body(property);
    }

    /**
     * Updates the selected property.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PropertyDto> updateProperty(@PathVariable Long id,
                                                      @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(propertyService.updateProperty(id, request));
    }

    /**
     * Deletes a property by id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }
}
