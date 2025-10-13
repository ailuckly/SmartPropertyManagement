package com.example.propertymanagement.dto.maintenance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MaintenanceRequestCreate(
    @NotNull(message = "物业ID不能为空")
    Long propertyId,

    @NotBlank(message = "问题描述不能为空")
    String description
) {
}
