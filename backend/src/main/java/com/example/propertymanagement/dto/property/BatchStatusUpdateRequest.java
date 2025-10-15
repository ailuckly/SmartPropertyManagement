package com.example.propertymanagement.dto.property;

import com.example.propertymanagement.model.PropertyStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for batch updating property status.
 */
public class BatchStatusUpdateRequest {
    
    @NotEmpty(message = "物业ID列表不能为空")
    private List<Long> ids;
    
    @NotNull(message = "状态不能为空")
    private PropertyStatus status;
    
    // Constructors
    public BatchStatusUpdateRequest() {}
    
    public BatchStatusUpdateRequest(List<Long> ids, PropertyStatus status) {
        this.ids = ids;
        this.status = status;
    }
    
    // Getters and Setters
    public List<Long> getIds() {
        return ids;
    }
    
    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
    
    public PropertyStatus getStatus() {
        return status;
    }
    
    public void setStatus(PropertyStatus status) {
        this.status = status;
    }
}