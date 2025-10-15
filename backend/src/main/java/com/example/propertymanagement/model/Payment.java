package com.example.propertymanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "lease")
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 租约ID（遵循阿里规范，不使用物理外键）
     */
    @Column(name = "lease_id", nullable = false)
    private Long leaseId;
    
    /**
     * 租户ID（冗余字段，避免关联查询）
     */
    @Column(name = "tenant_id")
    private Long tenantId;
    
    /**
     * 租户用户名（冗余字段，避免关联查询）
     */
    @Column(name = "tenant_username", length = 50)
    private String tenantUsername;
    
    /**
     * 物业ID（冗余字段，避免关联查询）
     */
    @Column(name = "property_id")
    private Long propertyId;
    
    /**
     * 物业地址（冗余字段，避免关联查询）
     */
    @Column(name = "property_address", length = 255)
    private String propertyAddress;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

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
