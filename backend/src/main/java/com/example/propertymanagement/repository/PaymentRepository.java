package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findAllByLeaseId(Long leaseId, Pageable pageable);
}
