package com.example.propertymanagement.controller;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.lease.LeaseDto;
import com.example.propertymanagement.dto.lease.LeaseRequest;
import com.example.propertymanagement.service.ExcelExportService;
import com.example.propertymanagement.service.LeaseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lease management endpoints for owners, tenants and administrators.
 */
@RestController
@RequestMapping("/api/leases")
public class LeaseController {

    private final LeaseService leaseService;
    private final ExcelExportService excelExportService;

    public LeaseController(LeaseService leaseService, ExcelExportService excelExportService) {
        this.leaseService = leaseService;
        this.excelExportService = excelExportService;
    }

    /**
     * Lists leases scoped to the current authenticated user.
     * @param pageable 分页参数
     * @param keyword 搜索关键词（可选）
     */
    @GetMapping
    public ResponseEntity<PageResponse<LeaseDto>> listLeases(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(leaseService.searchLeases(pageable, keyword));
        }
        return ResponseEntity.ok(leaseService.getLeases(pageable));
    }

    /**
     * Returns lease details when the caller is authorised to view them.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeaseDto> getLease(@PathVariable Long id) {
        return ResponseEntity.ok(leaseService.getLease(id));
    }

    /**
     * Creates a lease (owners for their own properties, admins for any).
     */
    @PostMapping
    public ResponseEntity<LeaseDto> createLease(@Valid @RequestBody LeaseRequest request) {
        LeaseDto dto = leaseService.createLease(request);
        return ResponseEntity.status(201).body(dto);
    }

    /**
     * Updates lease data. Same access restrictions as creation apply.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeaseDto> updateLease(@PathVariable Long id,
                                                @Valid @RequestBody LeaseRequest request) {
        return ResponseEntity.ok(leaseService.updateLease(id, request));
    }

    /**
     * Deletes a lease and resets the linked property status.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLease(@PathVariable Long id) {
        leaseService.deleteLease(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exports leases to Excel file.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportLeases() throws IOException {
        // 获取所有租约数据（不分页）
        PageResponse<LeaseDto> leases = leaseService.getLeases(Pageable.unpaged());
        
        // 定义表头
        List<String> headers = Arrays.asList(
            "租约ID", "物业ID", "物业地址", "租户ID", "租户用户名",
            "开始日期", "结束日期", "租金", "状态", "创建时间"
        );
        
        // 转换数据为行列表
        List<List<Object>> dataRows = new ArrayList<>();
        for (LeaseDto lease : leases.content()) {
            List<Object> row = Arrays.asList(
                lease.id(),
                lease.propertyId(),
                lease.propertyAddress(),
                lease.tenantId(),
                lease.tenantUsername(),
                lease.startDate(),
                lease.endDate(),
                lease.rentAmount(),
                lease.status() != null ? lease.status().name() : "",
                lease.createdAt()
            );
            dataRows.add(row);
        }
        
        // 生成 Excel
        byte[] excelBytes = excelExportService.generateExcel("租约列表", headers, dataRows);
        
        // 生成文件名
        String filename = excelExportService.generateFileName("租约列表");
        
        // 返回文件
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(excelBytes);
    }
}
