package com.example.propertymanagement.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
    @NotNull(message = "租约ID不能为空")
    Long leaseId,

    @NotNull(message = "支付金额不能为空")
    @PositiveOrZero(message = "支付金额不能为负数")
    BigDecimal amount,

    @NotNull(message = "支付日期不能为空")
    LocalDate paymentDate,

    String paymentMethod
) {
}
