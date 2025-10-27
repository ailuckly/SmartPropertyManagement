package com.example.propertymanagement.dto.maintenance;

import com.example.propertymanagement.model.MaintenanceStatus;

import java.time.Instant;

public record MaintenanceRequestDto(
    Long id,
    Long propertyId,
    String propertyAddress,
    Long tenantId,
    String tenantUsername,
    String description,
    MaintenanceStatus status,
    Instant reportedAt,
    Instant completedAt,
    // AI分析结果
    String aiCategory,
    String aiUrgencyLevel,
    String aiSolution,
    Double aiEstimatedCost
) {
}
