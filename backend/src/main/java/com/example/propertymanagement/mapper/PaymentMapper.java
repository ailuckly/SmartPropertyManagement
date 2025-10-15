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
            payment.getLeaseId(),
            payment.getPropertyAddress(),
            payment.getTenantUsername(),
            payment.getAmount(),
            payment.getPaymentDate(),
            payment.getPaymentMethod(),
            payment.getGmtCreate().toInstant(ZoneOffset.UTC)
        );
    }
}
