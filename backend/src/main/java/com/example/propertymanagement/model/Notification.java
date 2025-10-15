package com.example.propertymanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 通知实体类
 * 用于存储系统通知信息
 */
@Entity
@Table(name = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 通知类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    /**
     * 通知标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 通知内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 接收通知的用户ID（遵循阿里规范，不使用物理外键）
     */
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;
    
    /**
     * 接收通知的用户名（冗余字段，避免关联查询）
     */
    @Column(name = "recipient_username", length = 50)
    private String recipientUsername;

    /**
     * 是否已读
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * 关联的实体类型（如 lease, maintenance, payment 等）
     */
    @Column(length = 50)
    private String relatedEntityType;

    /**
     * 关联实体的 ID
     */
    @Column
    private Long relatedEntityId;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 阅读时间
     */
    @Column
    private LocalDateTime readAt;
}
