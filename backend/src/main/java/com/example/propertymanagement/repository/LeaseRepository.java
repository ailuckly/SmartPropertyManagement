package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Lease;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaseRepository extends JpaRepository<Lease, Long> {

    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<Lease> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<Lease> findAllByTenantId(Long tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<Lease> findAllByPropertyOwnerId(Long ownerId, Pageable pageable);
}
