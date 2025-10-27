package com.example.propertymanagement.controller;

import com.example.propertymanagement.dto.ai.ChatRequest;
import com.example.propertymanagement.dto.ai.ChatResponse;
import com.example.propertymanagement.service.AIChatService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI聊天接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
public class AIChatController {

    private final AIChatService aiChatService;

    public AIChatController(AIChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * 聊天对话接口
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("收到AI聊天请求: message={}, historySize={}", 
            request.getMessage(), 
            request.getHistory() != null ? request.getHistory().size() : 0);
        
        try {
            // 检查aiChatService是否注入
            if (aiChatService == null) {
                log.error("AIChatService未注入！请检查app.ai.enabled配置和Bean初始化");
                return ResponseEntity.ok(new ChatResponse(
                    "AI服务未启用或配置错误。请联系管理员。"
                ));
            }
            
            String reply = aiChatService.chat(request.getMessage(), request.getHistory());
            log.info("AI聊天响应成功，回复长度: {}", reply != null ? reply.length() : 0);
            return ResponseEntity.ok(new ChatResponse(reply));
            
        } catch (Exception e) {
            log.error("AI聊天失败 - 错误类型: {}, 错误信息: {}", 
                e.getClass().getName(), e.getMessage(), e);
            
            // 返回友好的错误消息
            return ResponseEntity.ok(new ChatResponse(
                "抱歉，AI服务暂时不可用。请稍后再试。\n\n" +
                "您可以：\n" +
                "- 浏览系统功能菜单\n" +
                "- 查看帮助文档\n" +
                "- 联系管理员获取支持"
            ));
        }
    }
}
