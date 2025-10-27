package com.example.propertymanagement.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    
    @NotBlank(message = "消息不能为空")
    private String message;
    
    private List<ChatMessage> history;
    
    @Data
    public static class ChatMessage {
        private String role; // user, assistant
        private String content;
    }
}
