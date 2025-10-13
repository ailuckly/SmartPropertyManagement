package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.MaintenanceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    Page<MaintenanceRequest> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<MaintenanceRequest> findAllByPropertyOwnerId(Long ownerId, Pageable pageable);
}
