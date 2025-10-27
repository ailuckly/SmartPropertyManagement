package com.example.propertymanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI模块配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.ai")
public class AIProperties {
    
    /**
     * 是否启用AI功能
     */
    private boolean enabled = false;
    
    /**
     * OpenAI API Key
     */
    private String apiKey;
    
    /**
     * OpenAI API Base URL (可选，支持兼容OpenAI协议的第三方服务)
     */
    private String baseUrl = "https://api.openai.com/";
    
    /**
     * 使用的模型名称
     */
    private String model = "gpt-3.5-turbo";
    
    /**
     * 请求超时时间(秒)
     */
    private int timeout = 30;
    
    /**
     * 温度参数(0-2)，控制输出随机性
     */
    private double temperature = 0.7;
    
    /**
     * 最大token数
     */
    private int maxTokens = 500;
}
