package com.example.propertymanagement.model;

/**
 * 审计结果枚举
 */
public enum AuditResult {
    SUCCESS("成功"),
    FAILURE("失败");
    
    private final String description;
    
    AuditResult(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}