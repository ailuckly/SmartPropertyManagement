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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "owner")
@Entity
@Table(name = "property")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 业主ID（遵循阿里规范，不使用物理外键）
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    /**
     * 业主用户名（冗余字段，避免关联查询）
     */
    @Column(name = "owner_username", length = 50)
    private String ownerUsername;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false, length = 20)
    private PropertyType propertyType;

    private Integer bedrooms;

    @Column(precision = 3, scale = 1)
    private BigDecimal bathrooms;

    @Column(name = "square_footage")
    private Integer squareFootage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private PropertyStatus status = PropertyStatus.AVAILABLE;

    @Column(name = "rent_amount", precision = 10, scale = 2)
    private BigDecimal rentAmount;

    @Column(name = "cover_image_path", length = 255)
    private String coverImagePath;

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
