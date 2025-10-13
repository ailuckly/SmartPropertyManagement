package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Lease;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaseRepository extends JpaRepository<Lease, Long> {

    Page<Lease> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<Lease> findAllByPropertyOwnerId(Long ownerId, Pageable pageable);
}
