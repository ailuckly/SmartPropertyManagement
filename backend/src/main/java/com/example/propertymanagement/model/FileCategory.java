package com.example.propertymanagement.model;

/**
 * 文件分类枚举
 * 定义系统中支持的文件类型分类
 * 
 * @author Development Team
 * @since 2025-10-14
 */
public enum FileCategory {
    
    /**
     * 物业图片
     * 用于物业展示的图片（室内、室外、设施等）
     */
    PROPERTY_IMAGE("物业图片"),
    
    /**
     * 租约合同
     * 租赁合同文件（通常为PDF格式）
     */
    LEASE_CONTRACT("租约合同"),
    
    /**
     * 维修图片
     * 维修请求相关的图片（问题照片、维修前后对比等）
     */
    MAINTENANCE_IMAGE("维修图片"),
    
    /**
     * 用户头像
     * 用户个人头像图片
     */
    USER_AVATAR("用户头像"),
    
    /**
     * 其他文件
     * 其他类型的文件
     */
    OTHER("其他");
    
    /**
     * 分类显示名称
     */
    private final String displayName;
    
    /**
     * 构造函数
     * @param displayName 显示名称
     */
    FileCategory(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
}
