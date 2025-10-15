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

    @Query("SELECT m FROM MaintenanceRequest m WHERE m.propertyId IN (SELECT p.id FROM Property p WHERE p.ownerId = :ownerId)")
    Page<MaintenanceRequest> findAllByPropertyOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);
    
    // 统计查询方法
    Long countByStatus(MaintenanceStatus status);
    
    @Query("SELECT COUNT(m) FROM MaintenanceRequest m WHERE m.propertyId IN (SELECT p.id FROM Property p WHERE p.ownerId = :ownerId) AND m.status = :status")
    Long countByPropertyOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") MaintenanceStatus status);
    
    Long countByTenantId(Long tenantId);
    
    @Query("SELECT m FROM MaintenanceRequest m WHERE m.propertyId IN (SELECT p.id FROM Property p WHERE p.ownerId = :ownerId)")
    List<MaintenanceRequest> findByPropertyOwnerId(@Param("ownerId") Long ownerId);
    
    List<MaintenanceRequest> findByTenantId(Long tenantId);
    
    List<MaintenanceRequest> findTop5ByOrderByIdDesc();
    
    @Query(value = "SELECT m.* FROM MaintenanceRequest m WHERE m.property_id IN (SELECT p.id FROM Property p WHERE p.owner_id = :ownerId) ORDER BY m.id DESC LIMIT 5", nativeQuery = true)
    List<MaintenanceRequest> findTop5ByPropertyOwnerIdOrderByIdDesc(@Param("ownerId") Long ownerId);
    
    List<MaintenanceRequest> findTop5ByTenantIdOrderByIdDesc(Long tenantId);
    
    /**
     * 搜索维修请求（支持描述关键词搜索）
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 匹配的维修请求列表
     */
    @Query("SELECT m FROM MaintenanceRequest m " +
           "WHERE LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<MaintenanceRequest> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 添加按状态筛选的方法
    Page<MaintenanceRequest> findAllByStatus(MaintenanceStatus status, Pageable pageable);
    
    @Query("SELECT m FROM MaintenanceRequest m WHERE m.propertyId IN (SELECT p.id FROM Property p WHERE p.ownerId = :ownerId) AND m.status = :status")
    Page<MaintenanceRequest> findAllByPropertyOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") MaintenanceStatus status, Pageable pageable);
    
    Page<MaintenanceRequest> findAllByTenantIdAndStatus(Long tenantId, MaintenanceStatus status, Pageable pageable);
    
    // 添加按物业ID筛选的方法
    Page<MaintenanceRequest> findAllByPropertyId(Long propertyId, Pageable pageable);
    
    @Query("SELECT m FROM MaintenanceRequest m WHERE m.propertyId = :propertyId AND m.propertyId IN (SELECT p.id FROM Property p WHERE p.ownerId = :ownerId)")
    Page<MaintenanceRequest> findAllByPropertyOwnerIdAndPropertyId(@Param("ownerId") Long ownerId, @Param("propertyId") Long propertyId, Pageable pageable);
    
    Page<MaintenanceRequest> findAllByTenantIdAndPropertyId(Long tenantId, Long propertyId, Pageable pageable);
    
    // 添加同时按状态和物业ID筛选的方法
    Page<MaintenanceRequest> findAllByStatusAndPropertyId(MaintenanceStatus status, Long propertyId, Pageable pageable);
    
    @Query("SELECT m FROM MaintenanceRequest m WHERE m.propertyId = :propertyId AND m.status = :status AND m.propertyId IN (SELECT p.id FROM Property p WHERE p.ownerId = :ownerId)")
    Page<MaintenanceRequest> findAllByPropertyOwnerIdAndStatusAndPropertyId(@Param("ownerId") Long ownerId, @Param("status") MaintenanceStatus status, @Param("propertyId") Long propertyId, Pageable pageable);
    
    Page<MaintenanceRequest> findAllByTenantIdAndStatusAndPropertyId(Long tenantId, MaintenanceStatus status, Long propertyId, Pageable pageable);
}
