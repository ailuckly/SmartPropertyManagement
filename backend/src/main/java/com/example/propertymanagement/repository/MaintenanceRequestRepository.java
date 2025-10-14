package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.MaintenanceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    Page<MaintenanceRequest> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<MaintenanceRequest> findAllByPropertyOwnerId(Long ownerId, Pageable pageable);
    
    // 统计查询方法
    Long countByStatus(MaintenanceRequest.Status status);
    
    Long countByProperty_OwnerIdAndStatus(Long ownerId, MaintenanceRequest.Status status);
    
    Long countByRequesterId(Long requesterId);
    
    @EntityGraph(attributePaths = {"property"})
    List<MaintenanceRequest> findByProperty_OwnerId(Long ownerId);
    
    @EntityGraph(attributePaths = {"property"})
    List<MaintenanceRequest> findByRequesterId(Long requesterId);
    
    @EntityGraph(attributePaths = {"property"})
    List<MaintenanceRequest> findTop5ByOrderByIdDesc();
    
    @EntityGraph(attributePaths = {"property"})
    List<MaintenanceRequest> findTop5ByProperty_OwnerIdOrderByIdDesc(Long ownerId);
    
    @EntityGraph(attributePaths = {"property"})
    List<MaintenanceRequest> findTop5ByRequesterIdOrderByIdDesc(Long requesterId);
}
