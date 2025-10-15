package com.example.propertymanagement.controller;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.property.BatchStatusUpdateRequest;
import com.example.propertymanagement.dto.property.PropertyDto;
import com.example.propertymanagement.dto.property.PropertyFilterRequest;
import com.example.propertymanagement.dto.property.PropertyRequest;
import com.example.propertymanagement.service.ExcelExportService;
import com.example.propertymanagement.service.PropertyService;
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
 * Exposes property CRUD endpoints for the SPA. The heavy business logic is handled by {@link PropertyService}.
 */
@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final ExcelExportService excelExportService;

    public PropertyController(PropertyService propertyService, ExcelExportService excelExportService) {
        this.propertyService = propertyService;
        this.excelExportService = excelExportService;
    }

    /**
     * Returns a paginated property dataset with advanced filtering support.
     * @param pageable 分页参数
     * @param ownerId 业主ID（可选）
     * @param keyword 搜索关键词（可选）
     * @param status 物业状态（可选）
     * @param propertyType 物业类型（可选）
     * @param minRent 最低租金（可选）
     * @param maxRent 最高租金（可选）
     * @param minBedrooms 最少卧室数（可选）
     * @param maxBedrooms 最多卧室数（可选）
     * @param city 城市（可选）
     */
    @GetMapping
    public ResponseEntity<PageResponse<PropertyDto>> getProperties(
        @PageableDefault Pageable pageable,
        @RequestParam(required = false) Long ownerId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String propertyType,
        @RequestParam(required = false) Double minRent,
        @RequestParam(required = false) Double maxRent,
        @RequestParam(required = false) Integer minBedrooms,
        @RequestParam(required = false) Integer maxBedrooms,
        @RequestParam(required = false) String city
    ) {
        PropertyFilterRequest filterRequest = PropertyFilterRequest.builder()
            .ownerId(ownerId)
            .keyword(keyword)
            .status(status)
            .propertyType(propertyType)
            .minRent(minRent)
            .maxRent(maxRent)
            .minBedrooms(minBedrooms)
            .maxBedrooms(maxBedrooms)
            .city(city)
            .build();
            
        return ResponseEntity.ok(propertyService.getPropertiesWithFilters(pageable, filterRequest));
    }

    /**
     * Returns the property details by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PropertyDto> getProperty(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getProperty(id));
    }

    /**
     * Creates a new property record.
     */
    @PostMapping
    public ResponseEntity<PropertyDto> createProperty(@Valid @RequestBody PropertyRequest request) {
        PropertyDto property = propertyService.createProperty(request);
        return ResponseEntity.status(201).body(property);
    }

    /**
     * Updates the selected property.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PropertyDto> updateProperty(@PathVariable Long id,
                                                      @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(propertyService.updateProperty(id, request));
    }

    /**
     * Deletes a property by id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Batch delete properties by ids.
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Void> batchDeleteProperties(@RequestBody List<Long> ids) {
        propertyService.batchDeleteProperties(ids);
        return ResponseEntity.noContent().build();
    }

    /**
     * Batch update property status.
     */
    @PutMapping("/batch/status")
    public ResponseEntity<Void> batchUpdateStatus(@RequestBody BatchStatusUpdateRequest request) {
        propertyService.batchUpdateStatus(request.getIds(), request.getStatus());
        return ResponseEntity.noContent().build();
    }

    /**
     * Exports properties to Excel file.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportProperties(@RequestParam(required = false) Long ownerId) throws IOException {
        // 获取所有物业数据（不分页）
        PageResponse<PropertyDto> properties = propertyService.getProperties(Pageable.unpaged(), ownerId);
        
        // 定义表头
        List<String> headers = Arrays.asList(
            "物业ID", "业主ID", "业主用户名", "地址", "城市", "州/省", "邮编",
            "物业类型", "卧室数", "浴室数", "面积(平方英尺)", "状态", "租金", "创建时间"
        );
        
        // 转换数据为行列表
        List<List<Object>> dataRows = new ArrayList<>();
        for (PropertyDto property : properties.content()) {
            List<Object> row = Arrays.asList(
                property.id(),
                property.ownerId(),
                property.ownerUsername(),
                property.address(),
                property.city(),
                property.state(),
                property.zipCode(),
                property.propertyType() != null ? property.propertyType().name() : "",
                property.bedrooms(),
                property.bathrooms(),
                property.squareFootage(),
                property.status() != null ? property.status().name() : "",
                property.rentAmount(),
                property.createdAt()
            );
            dataRows.add(row);
        }
        
        // 生成 Excel
        byte[] excelBytes = excelExportService.generateExcel("物业列表", headers, dataRows);
        
        // 生成文件名
        String filename = excelExportService.generateFileName("物业列表");
        
        // 返回文件
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(excelBytes);
    }
}
