package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"lease", "lease.property", "lease.tenant"})
    Page<Payment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"lease", "lease.property", "lease.tenant"})
    Page<Payment> findAllByLeaseId(Long leaseId, Pageable pageable);

    @EntityGraph(attributePaths = {"lease", "lease.property", "lease.tenant"})
    Page<Payment> findAllByLeasePropertyOwnerId(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"lease", "lease.property", "lease.tenant"})
    Page<Payment> findAllByLeaseTenantId(Long tenantId, Pageable pageable);
    
    // 统计查询方法
    @EntityGraph(attributePaths = {"lease"})
    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    @EntityGraph(attributePaths = {"lease", "lease.property"})
    List<Payment> findByLease_Property_OwnerIdAndPaymentDateBetween(Long ownerId, LocalDate startDate, LocalDate endDate);
}
