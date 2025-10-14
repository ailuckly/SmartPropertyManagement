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
            request.getProperty() != null ? request.getProperty().getId() : null,
            request.getProperty() != null ? request.getProperty().getAddress() : null,
            request.getTenant() != null ? request.getTenant().getId() : null,
            request.getTenant() != null ? request.getTenant().getUsername() : null,
            request.getDescription(),
            request.getStatus(),
            request.getReportedAt() != null ? request.getReportedAt().toInstant(ZoneOffset.UTC) : null,
            request.getCompletedAt() != null ? request.getCompletedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}
