package com.example.propertymanagement.mapper;

import com.example.propertymanagement.dto.payment.PaymentDto;
import com.example.propertymanagement.model.Payment;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentDto toDto(Payment payment) {
        return new PaymentDto(
            payment.getId(),
            payment.getLease() != null ? payment.getLease().getId() : null,
            payment.getAmount(),
            payment.getPaymentDate(),
            payment.getPaymentMethod(),
            payment.getCreatedAt()
        );
    }
}
