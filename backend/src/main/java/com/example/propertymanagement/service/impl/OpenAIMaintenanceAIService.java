package com.example.propertymanagement.service.impl;

import com.example.propertymanagement.config.AIProperties;
import com.example.propertymanagement.dto.ai.MaintenanceAnalysisResult;
import com.example.propertymanagement.service.AIService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
public class OpenAIMaintenanceAIService implements AIService {

    private final OpenAiService openAiService;
    private final AIProperties aiProperties;

    public OpenAIMaintenanceAIService(OpenAiService openAiService, AIProperties aiProperties) {
        this.openAiService = openAiService;
        this.aiProperties = aiProperties;
    }

    @Override
    public MaintenanceAnalysisResult analyzeMaintenanceRequest(String description, String propertyAddress) {
        try {
            String systemPrompt = "你是一个物业维修调度专家。请对用户的报修进行结构化分析，严格输出纯JSON格式数据，不得包含任何Markdown标记符号（如反引号、星号等）。JSON字段包含: category(类别: 水电/家电/家具/门窗/清洁/其他), urgencyLevel(低/中/高/紧急), solution(简要维修建议，使用纯文本描述，不使用Markdown语法), estimatedCost(数字, 元)。所有字段值必须是纯文本，不得使用任何Markdown格式标记。";
            String userPrompt = String.format("报修描述: %s\n物业地址: %s\n请仅输出JSON，不要有多余文字。", description, propertyAddress == null ? "" : propertyAddress);

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(aiProperties.getModel())
                    .temperature(aiProperties.getTemperature())
                    .maxTokens(aiProperties.getMaxTokens())
                    .messages(List.of(
                            new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
                            new ChatMessage(ChatMessageRole.USER.value(), userPrompt)
                    ))
                    .build();

            String content = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();

            // 简单解析JSON（避免引入额外依赖），做容错
            return parseResult(content);
        } catch (Exception e) {
            log.error("AI分析失败", e);
            return MaintenanceAnalysisResult.builder()
                    .success(false)
                    .errorMessage("AI分析失败: " + e.getMessage())
                    .build();
        }
    }

    private MaintenanceAnalysisResult parseResult(String content) {
        try {
            // 去除可能的代码块标记
            String json = content
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            String category = extract(json, "category");
            String urgency = extract(json, "urgencyLevel");
            String solution = extract(json, "solution");
            Double estimated = null;
            try {
                String estimateStr = extract(json, "estimatedCost");
                if (estimateStr != null && !estimateStr.isEmpty()) {
                    estimated = Double.parseDouble(estimateStr.replaceAll("[^0-9.]", ""));
                }
            } catch (Exception ignore) {}

            return MaintenanceAnalysisResult.builder()
                    .category(category)
                    .urgencyLevel(urgency)
                    .solution(solution)
                    .estimatedCost(estimated)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.warn("AI返回无法解析: {}", content);
            return MaintenanceAnalysisResult.builder()
                    .success(false)
                    .errorMessage("无法解析AI返回")
                    .build();
        }
    }

    private String extract(String json, String key) {
        // 极简解析器：匹配 "key" : "value" 或 "key": value
        String pattern = String.format("\"%s\"\s*:\s*", key);
        String after = json.split(pattern, 2).length > 1 ? json.split(pattern, 2)[1] : null;
        if (after == null) return null;
        after = after.trim();
        if (after.startsWith("\"")) {
            // 字符串
            String[] parts = after.substring(1).split("\"", 2);
            return parts.length > 0 ? parts[0] : null;
        }
        // 非字符串，读到逗号或右花括号
        String[] parts = after.split(",|}");
        return parts.length > 0 ? parts[0].trim() : null;
    }
}
