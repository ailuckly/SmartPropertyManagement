package com.example.propertymanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件实体类
 * 用于存储系统中的各类文件信息（图片、PDF等）
 * 
 * @author Development Team
 * @since 2025-10-14
 */
@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {
    
    /**
     * 文件ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 原始文件名
     */
    @Column(nullable = false)
    private String originalFileName;
    
    /**
     * 存储文件名（系统生成的唯一文件名）
     */
    @Column(nullable = false, unique = true)
    private String storedFileName;
    
    /**
     * 文件存储路径（相对路径）
     */
    @Column(nullable = false)
    private String filePath;
    
    /**
     * 文件类型（MIME类型）
     * 例如: image/jpeg, image/png, application/pdf
     */
    @Column(nullable = false)
    private String fileType;
    
    /**
     * 文件大小（字节）
     */
    @Column(nullable = false)
    private Long fileSize;
    
    /**
     * 文件分类
     * PROPERTY_IMAGE: 物业图片
     * LEASE_CONTRACT: 租约合同
     * MAINTENANCE_IMAGE: 维修图片
     * OTHER: 其他
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileCategory category;
    
    /**
     * 关联实体ID
     * 根据category不同，可能关联不同的实体
     * 例如: 物业ID、租约ID、维修请求ID等
     */
    @Column(name = "entity_id")
    private Long entityId;
    
    /**
     * 是否为封面图片（用于物业图片）
     */
    @Column(name = "is_cover")
    private Boolean isCover = false;
    
    /**
     * 文件描述（可选）
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 上传用户ID
     */
    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;
    
    /**
     * 上传时间
     */
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 在持久化之前自动设置上传时间
     */
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (isCover == null) {
            isCover = false;
        }
    }
    
    /**
     * 在更新之前自动设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
