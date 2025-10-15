package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findAll(Pageable pageable);

    Page<Payment> findAllByLeaseId(Long leaseId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.leaseId IN (SELECT l.id FROM Lease l WHERE l.ownerId = :ownerId)")
    Page<Payment> findAllByLeasePropertyOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    Page<Payment> findAllByTenantId(Long tenantId, Pageable pageable);
    
    // 统计查询方法
    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.leaseId IN (SELECT l.id FROM Lease l WHERE l.ownerId = :ownerId) AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByLeasePropertyOwnerIdAndPaymentDateBetween(@Param("ownerId") Long ownerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
