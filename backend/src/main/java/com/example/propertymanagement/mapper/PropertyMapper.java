package com.example.propertymanagement.mapper;

import com.example.propertymanagement.dto.property.PropertyDto;
import com.example.propertymanagement.model.Property;

import java.time.ZoneOffset;

public final class PropertyMapper {

    private PropertyMapper() {
    }

    public static PropertyDto toDto(Property property) {
        return new PropertyDto(
            property.getId(),
            property.getOwner() != null ? property.getOwner().getId() : null,
            property.getOwner() != null ? property.getOwner().getUsername() : null,
            property.getAddress(),
            property.getCity(),
            property.getState(),
            property.getZipCode(),
            property.getPropertyType(),
            property.getBedrooms(),
            property.getBathrooms(),
            property.getSquareFootage(),
            property.getStatus(),
            property.getRentAmount(),
            property.getGmtCreate().toInstant(ZoneOffset.UTC)
        );
    }
}
