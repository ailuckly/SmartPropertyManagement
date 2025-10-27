package com.example.propertymanagement.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 维修工单AI分析结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceAnalysisResult {
    
    /**
     * 问题分类 (水电/家具/清洁/其他)
     */
    private String category;
    
    /**
     * 紧急程度 (低/中/高/紧急)
     */
    private String urgencyLevel;
    
    /**
     * 维修建议
     */
    private String solution;
    
    /**
     * 预估费用(元)
     */
    private Double estimatedCost;
    
    /**
     * 分析是否成功
     */
    private boolean success;
    
    /**
     * 失败原因（如果有）
     */
    private String errorMessage;
}
