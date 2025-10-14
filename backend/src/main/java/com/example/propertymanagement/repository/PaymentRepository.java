package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"lease", "lease.property", "lease.tenant"})
    Page<Payment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"lease", "lease.property", "lease.tenant"})
    Page<Payment> findAllByLeaseId(Long leaseId, Pageable pageable);

    @EntityGraph(attributePaths = {"lease", "lease.property", "lease.tenant"})
    Page<Payment> findAllByLeasePropertyOwnerId(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"lease", "lease.property", "lease.tenant"})
    Page<Payment> findAllByLeaseTenantId(Long tenantId, Pageable pageable);
}
