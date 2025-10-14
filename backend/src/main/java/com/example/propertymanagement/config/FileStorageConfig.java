package com.example.propertymanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * 文件存储配置类
 * 从application.properties读取文件存储相关配置
 * 
 * @author Development Team
 * @since 2025-10-14
 */
@Configuration
@ConfigurationProperties(prefix = "file.storage")
@Data
public class FileStorageConfig {
    
    /**
     * 文件上传根目录
     * 默认值: uploads/
     */
    private String uploadDir = "uploads";
    
    /**
     * 最大文件大小（字节）
     * 默认: 10MB
     */
    private long maxFileSize = 10 * 1024 * 1024;
    
    /**
     * 允许的图片文件类型
     */
    private String[] allowedImageTypes = {
        "image/jpeg", 
        "image/jpg", 
        "image/png", 
        "image/gif", 
        "image/webp"
    };
    
    /**
     * 允许的文档文件类型
     */
    private String[] allowedDocumentTypes = {
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };
    
    /**
     * 图片文件最大大小（字节）
     * 默认: 5MB
     */
    private long maxImageSize = 5 * 1024 * 1024;
    
    /**
     * 文档文件最大大小（字节）
     * 默认: 10MB
     */
    private long maxDocumentSize = 10 * 1024 * 1024;
}
