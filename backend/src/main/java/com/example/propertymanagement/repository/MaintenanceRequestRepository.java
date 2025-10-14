package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.MaintenanceRequest;
import com.example.propertymanagement.model.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    Page<MaintenanceRequest> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<MaintenanceRequest> findAllByPropertyOwnerId(Long ownerId, Pageable pageable);
    
    // 统计查询方法
    Long countByStatus(MaintenanceStatus status);
    
    Long countByProperty_OwnerIdAndStatus(Long ownerId, MaintenanceStatus status);
    
    Long countByTenantId(Long tenantId);
    
    @EntityGraph(attributePaths = {"property", "tenant"})
    List<MaintenanceRequest> findByProperty_OwnerId(Long ownerId);
    
    @EntityGraph(attributePaths = {"property", "tenant"})
    List<MaintenanceRequest> findByTenantId(Long tenantId);
    
    @EntityGraph(attributePaths = {"property", "tenant"})
    List<MaintenanceRequest> findTop5ByOrderByIdDesc();
    
    @EntityGraph(attributePaths = {"property", "tenant"})
    List<MaintenanceRequest> findTop5ByProperty_OwnerIdOrderByIdDesc(Long ownerId);
    
    @EntityGraph(attributePaths = {"property", "tenant"})
    List<MaintenanceRequest> findTop5ByTenantIdOrderByIdDesc(Long tenantId);
    
    /**
     * 搜索维修请求（支持描述关键词搜索）
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 匹配的维修请求列表
     */
    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    @Query("SELECT m FROM MaintenanceRequest m " +
           "WHERE LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<MaintenanceRequest> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 添加按状态筛选的方法（带 @EntityGraph）
    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<MaintenanceRequest> findAllByStatus(MaintenanceStatus status, Pageable pageable);
    
    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<MaintenanceRequest> findAllByPropertyOwnerIdAndStatus(Long ownerId, MaintenanceStatus status, Pageable pageable);
    
    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<MaintenanceRequest> findAllByTenantIdAndStatus(Long tenantId, MaintenanceStatus status, Pageable pageable);
    
    // 添加按物业ID筛选的方法（带 @EntityGraph）
    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<MaintenanceRequest> findAllByPropertyId(Long propertyId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<MaintenanceRequest> findAllByPropertyOwnerIdAndPropertyId(Long ownerId, Long propertyId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<MaintenanceRequest> findAllByTenantIdAndPropertyId(Long tenantId, Long propertyId, Pageable pageable);
    
    // 添加同时按状态和物业ID筛选的方法
    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<MaintenanceRequest> findAllByStatusAndPropertyId(MaintenanceStatus status, Long propertyId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<MaintenanceRequest> findAllByPropertyOwnerIdAndStatusAndPropertyId(Long ownerId, MaintenanceStatus status, Long propertyId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<MaintenanceRequest> findAllByTenantIdAndStatusAndPropertyId(Long tenantId, MaintenanceStatus status, Long propertyId, Pageable pageable);
}
