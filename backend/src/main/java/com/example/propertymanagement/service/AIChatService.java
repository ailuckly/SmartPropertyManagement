package com.example.propertymanagement.service;

import com.example.propertymanagement.dto.ai.ChatRequest;

import java.util.List;

/**
 * AI聊天服务接口
 */
public interface AIChatService {
    
    /**
     * 聊天对话
     * 
     * @param message 用户消息
     * @param history 历史对话记录
     * @return AI回复
     */
    String chat(String message, List<ChatRequest.ChatMessage> history);
}
