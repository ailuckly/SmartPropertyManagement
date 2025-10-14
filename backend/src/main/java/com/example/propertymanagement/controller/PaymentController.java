package com.example.propertymanagement.controller;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.payment.PaymentDto;
import com.example.propertymanagement.dto.payment.PaymentRequest;
import com.example.propertymanagement.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rental payment endpoints. Reads are visible to the lease tenant and owner; writes restricted to owner/admin.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Lists all payments visible to the current user.
     * Optionally filter by leaseId.
     * - Admin sees all payments
     * - Owner sees their properties' payments
     * - Tenant sees their leases' payments
     * 
     * @param leaseId Optional lease ID to filter by
     * @param pageable Pagination parameters
     */
    @GetMapping
    public ResponseEntity<PageResponse<PaymentDto>> getAllPayments(
            @RequestParam(required = false) Long leaseId,
            @PageableDefault Pageable pageable) {
        if (leaseId != null) {
            return ResponseEntity.ok(paymentService.getPaymentsByLease(leaseId, pageable));
        }
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    /**
     * Records a payment entry for the selected lease.
     */
    @PostMapping
    public ResponseEntity<PaymentDto> recordPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentDto dto = paymentService.recordPayment(request);
        return ResponseEntity.status(201).body(dto);
    }
}
