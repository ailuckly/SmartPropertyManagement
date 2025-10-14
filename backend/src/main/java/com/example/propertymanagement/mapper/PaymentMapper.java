package com.example.propertymanagement.mapper;

import com.example.propertymanagement.dto.payment.PaymentDto;
import com.example.propertymanagement.model.Payment;

import java.time.ZoneOffset;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentDto toDto(Payment payment) {
        return new PaymentDto(
            payment.getId(),
            payment.getLease() != null ? payment.getLease().getId() : null,
            payment.getLease() != null && payment.getLease().getProperty() != null 
                ? payment.getLease().getProperty().getAddress() : null,
            payment.getLease() != null && payment.getLease().getTenant() != null 
                ? payment.getLease().getTenant().getUsername() : null,
            payment.getAmount(),
            payment.getPaymentDate(),
            payment.getPaymentMethod(),
            payment.getGmtCreate().toInstant(ZoneOffset.UTC)
        );
    }
}
