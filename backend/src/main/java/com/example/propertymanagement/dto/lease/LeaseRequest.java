package com.example.propertymanagement.dto.lease;

import com.example.propertymanagement.model.LeaseStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LeaseRequest(
    @NotNull(message = "物业ID不能为空")
    Long propertyId,

    @NotNull(message = "租客ID不能为空")
    Long tenantId,

    @NotNull(message = "租期开始日期不能为空")
    LocalDate startDate,

    @NotNull(message = "租期结束日期不能为空")
    LocalDate endDate,

    @NotNull(message = "租金不能为空")
    @PositiveOrZero(message = "租金不能为负数")
    BigDecimal rentAmount,

    LeaseStatus status
) {
}
