package com.example.propertymanagement.model;

/**
 * 通知类型枚举
 */
public enum NotificationType {
    /**
     * 租约相关通知
     */
    LEASE_CREATED("租约创建"),
    LEASE_UPDATED("租约更新"),
    LEASE_EXPIRING_SOON("租约即将到期"),
    LEASE_EXPIRED("租约已到期"),
    
    /**
     * 维修相关通知
     */
    MAINTENANCE_CREATED("维修请求已提交"),
    MAINTENANCE_IN_PROGRESS("维修处理中"),
    MAINTENANCE_COMPLETED("维修已完成"),
    MAINTENANCE_CANCELLED("维修已取消"),
    
    /**
     * 支付相关通知
     */
    PAYMENT_RECEIVED("收到支付"),
    PAYMENT_DUE("支付到期提醒"),
    
    /**
     * 系统通知
     */
    SYSTEM_ANNOUNCEMENT("系统公告"),
    PROPERTY_STATUS_CHANGED("物业状态变更");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
