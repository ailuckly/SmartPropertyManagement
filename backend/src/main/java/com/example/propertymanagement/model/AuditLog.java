package com.example.propertymanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 审计日志实体，记录系统中的重要操作
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 执行操作的用户ID
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 执行操作的用户名
     */
    @Column(name = "username", length = 100)
    private String username;
    
    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;
    
    /**
     * 操作的资源类型
     */
    @Column(name = "resource_type", length = 50)
    private String resourceType;
    
    /**
     * 操作的资源ID
     */
    @Column(name = "resource_id")
    private String resourceId;
    
    /**
     * 操作详情描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 客户端IP地址
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /**
     * User-Agent
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    /**
     * 操作时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 操作结果（成功/失败）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 20)
    private AuditResult result;
    
    /**
     * 错误信息（如果操作失败）
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    // 构造函数
    public AuditLog() {
        this.createdAt = LocalDateTime.now();
        this.result = AuditResult.SUCCESS;
    }
    
    // Builder pattern
    public static AuditLog.Builder builder() {
        return new AuditLog.Builder();
    }
    
    public static class Builder {
        private AuditLog auditLog;
        
        public Builder() {
            this.auditLog = new AuditLog();
        }
        
        public Builder userId(Long userId) {
            this.auditLog.userId = userId;
            return this;
        }
        
        public Builder username(String username) {
            this.auditLog.username = username;
            return this;
        }
        
        public Builder action(AuditAction action) {
            this.auditLog.action = action;
            return this;
        }
        
        public Builder resourceType(String resourceType) {
            this.auditLog.resourceType = resourceType;
            return this;
        }
        
        public Builder resourceId(String resourceId) {
            this.auditLog.resourceId = resourceId;
            return this;
        }
        
        public Builder description(String description) {
            this.auditLog.description = description;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            this.auditLog.ipAddress = ipAddress;
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            this.auditLog.userAgent = userAgent;
            return this;
        }
        
        public Builder result(AuditResult result) {
            this.auditLog.result = result;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.auditLog.errorMessage = errorMessage;
            return this;
        }
        
        public AuditLog build() {
            return this.auditLog;
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }
    
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public AuditResult getResult() { return result; }
    public void setResult(AuditResult result) { this.result = result; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}