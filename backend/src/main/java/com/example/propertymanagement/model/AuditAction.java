package com.example.propertymanagement.model;

/**
 * 审计操作类型枚举
 */
public enum AuditAction {
    LOGIN("登录"),
    LOGOUT("登出"),
    CREATE("创建"),
    UPDATE("更新"),
    DELETE("删除"),
    VIEW("查看"),
    SEARCH("搜索"),
    EXPORT("导出"),
    BATCH_DELETE("批量删除"),
    BATCH_UPDATE("批量更新"),
    UPLOAD("上传"),
    DOWNLOAD("下载");
    
    private final String description;
    
    AuditAction(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}