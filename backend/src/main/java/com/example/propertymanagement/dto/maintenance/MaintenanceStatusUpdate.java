package com.example.propertymanagement.dto.maintenance;

import com.example.propertymanagement.model.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;

public record MaintenanceStatusUpdate(
    @NotNull(message = "状态不能为空")
    MaintenanceStatus status
) {
}
