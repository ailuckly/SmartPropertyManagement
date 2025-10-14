package com.example.propertymanagement.controller;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.payment.PaymentDto;
import com.example.propertymanagement.dto.payment.PaymentRequest;
import com.example.propertymanagement.service.ExcelExportService;
import com.example.propertymanagement.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rental payment endpoints. Reads are visible to the lease tenant and owner; writes restricted to owner/admin.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final ExcelExportService excelExportService;

    public PaymentController(PaymentService paymentService, ExcelExportService excelExportService) {
        this.paymentService = paymentService;
        this.excelExportService = excelExportService;
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

    /**
     * Exports payments to Excel file.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPayments(@RequestParam(required = false) Long leaseId) throws IOException {
        // 获取所有收支数据（不分页）
        PageResponse<PaymentDto> payments;
        if (leaseId != null) {
            payments = paymentService.getPaymentsByLease(leaseId, Pageable.unpaged());
        } else {
            payments = paymentService.getAllPayments(Pageable.unpaged());
        }
        
        // 定义表头
        List<String> headers = Arrays.asList(
            "收支ID", "租约ID", "物业地址", "租户姓名",
            "金额", "支付日期", "支付方式", "创建时间"
        );
        
        // 转换数据为行列表
        List<List<Object>> dataRows = new ArrayList<>();
        for (PaymentDto payment : payments.content()) {
            List<Object> row = Arrays.asList(
                payment.id(),
                payment.leaseId(),
                payment.propertyAddress(),
                payment.tenantName(),
                payment.amount(),
                payment.paymentDate(),
                payment.paymentMethod(),
                payment.createdAt()
            );
            dataRows.add(row);
        }
        
        // 生成 Excel
        byte[] excelBytes = excelExportService.generateExcel("收支记录", headers, dataRows);
        
        // 生成文件名
        String filename = excelExportService.generateFileName("收支记录");
        
        // 返回文件
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(excelBytes);
    }
}
