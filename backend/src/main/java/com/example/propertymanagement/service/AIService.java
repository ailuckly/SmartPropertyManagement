package com.example.propertymanagement.service;

import com.example.propertymanagement.dto.ai.MaintenanceAnalysisResult;

/**
 * AI智能分析服务接口
 */
public interface AIService {
    
    /**
     * 分析维修工单描述，返回智能建议
     * 
     * @param description 用户描述的问题
     * @param propertyAddress 物业地址（可选，用于更精准的分析）
     * @return 分析结果
     */
    MaintenanceAnalysisResult analyzeMaintenanceRequest(String description, String propertyAddress);
}
