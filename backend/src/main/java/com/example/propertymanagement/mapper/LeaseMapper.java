package com.example.propertymanagement.mapper;

import com.example.propertymanagement.dto.lease.LeaseDto;
import com.example.propertymanagement.model.Lease;

public final class LeaseMapper {

    private LeaseMapper() {
    }

    public static LeaseDto toDto(Lease lease) {
        return new LeaseDto(
            lease.getId(),
            lease.getProperty() != null ? lease.getProperty().getId() : null,
            lease.getProperty() != null ? lease.getProperty().getAddress() : null,
            lease.getTenant() != null ? lease.getTenant().getId() : null,
            lease.getTenant() != null ? lease.getTenant().getUsername() : null,
            lease.getStartDate(),
            lease.getEndDate(),
            lease.getRentAmount(),
            lease.getStatus(),
            lease.getCreatedAt()
        );
    }
}
