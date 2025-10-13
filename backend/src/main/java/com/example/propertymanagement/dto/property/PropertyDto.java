package com.example.propertymanagement.dto.property;

import com.example.propertymanagement.model.PropertyStatus;
import com.example.propertymanagement.model.PropertyType;

import java.math.BigDecimal;
import java.time.Instant;

public record PropertyDto(
    Long id,
    Long ownerId,
    String ownerUsername,
    String address,
    String city,
    String state,
    String zipCode,
    PropertyType propertyType,
    Integer bedrooms,
    BigDecimal bathrooms,
    Integer squareFootage,
    PropertyStatus status,
    BigDecimal rentAmount,
    Instant createdAt
) {
}
