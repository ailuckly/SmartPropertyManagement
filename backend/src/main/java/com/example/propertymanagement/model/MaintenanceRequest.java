package com.example.propertymanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"property", "tenant"})
@Entity
@Table(name = "maintenance_request")
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 物业ID（遵循阿里规范，不使用物理外键）
     */
    @Column(name = "property_id", nullable = false)
    private Long propertyId;
    
    /**
     * 物业地址（冗余字段，避免关联查询）
     */
    @Column(name = "property_address", length = 255)
    private String propertyAddress;

    /**
     * 租户ID（遵循阿里规范，不使用物理外键）
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    /**
     * 租户用户名（冗余字段，避免关联查询）
     */
    @Column(name = "tenant_username", length = 50)
    private String tenantUsername;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.PENDING;
    
    /**
     * AI分析 - 问题分类
     */
    @Column(name = "ai_category", length = 50)
    private String aiCategory;
    
    /**
     * AI分析 - 紧急程度
     */
    @Column(name = "ai_urgency_level", length = 20)
    private String aiUrgencyLevel;
    
    /**
     * AI分析 - 维修建议
     */
    @Column(name = "ai_solution", columnDefinition = "TEXT")
    private String aiSolution;
    
    /**
     * AI分析 - 预估费用
     */
    @Column(name = "ai_estimated_cost")
    private Double aiEstimatedCost;

    @CreationTimestamp
    @Column(name = "reported_at", nullable = false, updatable = false)
    private LocalDateTime reportedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "gmt_create", updatable = false, nullable = false)
    private LocalDateTime gmtCreate;

    @UpdateTimestamp
    @Column(name = "gmt_modified", nullable = false)
    private LocalDateTime gmtModified;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Integer isDeleted = 0;
}
