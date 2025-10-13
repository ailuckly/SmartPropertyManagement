package com.example.propertymanagement.dto.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PaymentDto(
    Long id,
    Long leaseId,
    BigDecimal amount,
    LocalDate paymentDate,
    String paymentMethod,
    Instant createdAt
) {
}
