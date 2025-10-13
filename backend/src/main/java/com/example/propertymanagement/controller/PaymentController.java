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

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<PaymentDto>> getPayments(@RequestParam Long leaseId,
                                                                @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPayments(leaseId, pageable));
    }

    @PostMapping
    public ResponseEntity<PaymentDto> recordPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentDto dto = paymentService.recordPayment(request);
        return ResponseEntity.status(201).body(dto);
    }
}
