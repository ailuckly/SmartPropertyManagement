package com.example.propertymanagement.dto.lease;

import com.example.propertymanagement.model.LeaseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record LeaseDto(
    Long id,
    Long propertyId,
    String propertyAddress,
    Long tenantId,
    String tenantUsername,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal rentAmount,
    LeaseStatus status,
    Instant createdAt
) {
}
