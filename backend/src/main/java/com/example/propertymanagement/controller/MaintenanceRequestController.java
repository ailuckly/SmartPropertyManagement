package com.example.propertymanagement.controller;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.maintenance.MaintenanceRequestCreate;
import com.example.propertymanagement.dto.maintenance.MaintenanceRequestDto;
import com.example.propertymanagement.dto.maintenance.MaintenanceStatusUpdate;
import com.example.propertymanagement.model.MaintenanceStatus;
import com.example.propertymanagement.service.ExcelExportService;
import com.example.propertymanagement.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
 * Endpoints for tenant maintenance submissions and owner/admin progress tracking.
 */
@RestController
@RequestMapping("/api/maintenance-requests")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;
    private final ExcelExportService excelExportService;

    public MaintenanceRequestController(MaintenanceRequestService maintenanceRequestService, 
                                       ExcelExportService excelExportService) {
        this.maintenanceRequestService = maintenanceRequestService;
        this.excelExportService = excelExportService;
    }

    /**
     * Lists maintenance requests visible to the caller.
     * @param pageable 分页参数
     * @param keyword 搜索关键词（可选）
     * @param status 状态筛选（可选）
     * @param propertyId 物业ID筛选（可选）
     */
    @GetMapping
    public ResponseEntity<PageResponse<MaintenanceRequestDto>> listRequests(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) Long propertyId) {
        // 如果有关键词搜索，使用搜索方法
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(maintenanceRequestService.searchRequests(pageable, keyword));
        }
        // 如果有状态或物业ID筛选，使用筛选方法
        if (status != null || propertyId != null) {
            return ResponseEntity.ok(maintenanceRequestService.getRequestsWithFilters(pageable, status, propertyId));
        }
        // 否则返回所有数据
        return ResponseEntity.ok(maintenanceRequestService.getRequests(pageable));
    }

    /**
     * Allows tenants to raise a new maintenance ticket.
     */
    @PostMapping
    public ResponseEntity<MaintenanceRequestDto> createRequest(
        @Valid @RequestBody MaintenanceRequestCreate request) {
        MaintenanceRequestDto dto = maintenanceRequestService.createRequest(request);
        return ResponseEntity.status(201).body(dto);
    }

    /**
     * Updates the maintenance status (owner/admin only).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<MaintenanceRequestDto> updateStatus(@PathVariable Long id,
                                                              @Valid @RequestBody MaintenanceStatusUpdate request) {
        return ResponseEntity.ok(maintenanceRequestService.updateStatus(id, request));
    }

    /**
     * Exports maintenance requests to Excel file.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportMaintenanceRequests() throws IOException {
        // 获取所有维修记录数据（不分页）
        PageResponse<MaintenanceRequestDto> requests = maintenanceRequestService.getRequests(Pageable.unpaged());
        
        // 定义表头
        List<String> headers = Arrays.asList(
            "维修ID", "物业ID", "物业地址", "租户ID", "租户用户名",
            "描述", "状态", "报修时间", "完成时间"
        );
        
        // 转换数据为行列表
        List<List<Object>> dataRows = new ArrayList<>();
        for (MaintenanceRequestDto request : requests.content()) {
            List<Object> row = Arrays.asList(
                request.id(),
                request.propertyId(),
                request.propertyAddress(),
                request.tenantId(),
                request.tenantUsername(),
                request.description(),
                request.status() != null ? request.status().name() : "",
                request.reportedAt(),
                request.completedAt()
            );
            dataRows.add(row);
        }
        
        // 生成 Excel
        byte[] excelBytes = excelExportService.generateExcel("维修记录", headers, dataRows);
        
        // 生成文件名
        String filename = excelExportService.generateFileName("维修记录");
        
        // 返回文件
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(excelBytes);
    }
}
