package com.example.propertymanagement.mapper;

import com.example.propertymanagement.dto.lease.LeaseDto;
import com.example.propertymanagement.model.Lease;

import java.time.ZoneOffset;

public final class LeaseMapper {

    private LeaseMapper() {
    }

    public static LeaseDto toDto(Lease lease) {
        return new LeaseDto(
            lease.getId(),
            lease.getPropertyId(),
            lease.getPropertyAddress(),
            lease.getTenantId(),
            lease.getTenantUsername(),
            lease.getStartDate(),
            lease.getEndDate(),
            lease.getRentAmount(),
            lease.getStatus(),
            lease.getGmtCreate().toInstant(ZoneOffset.UTC)
        );
    }
}
