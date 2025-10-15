package com.example.propertymanagement.mapper;

import com.example.propertymanagement.dto.maintenance.MaintenanceRequestDto;
import com.example.propertymanagement.model.MaintenanceRequest;

import java.time.ZoneOffset;

public final class MaintenanceMapper {

    private MaintenanceMapper() {
    }

    public static MaintenanceRequestDto toDto(MaintenanceRequest request) {
        return new MaintenanceRequestDto(
            request.getId(),
            request.getPropertyId(),
            request.getPropertyAddress(),
            request.getTenantId(),
            request.getTenantUsername(),
            request.getDescription(),
            request.getStatus(),
            request.getReportedAt() != null ? request.getReportedAt().toInstant(ZoneOffset.UTC) : null,
            request.getCompletedAt() != null ? request.getCompletedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}
