package com.example.propertymanagement.service.impl;

import com.example.propertymanagement.config.AIProperties;
import com.example.propertymanagement.dto.ai.ChatRequest;
import com.example.propertymanagement.service.AIChatService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
public class OpenAIChatService implements AIChatService {

    private final OpenAiService openAiService;
    private final AIProperties aiProperties;

    // 系统提示词 - 定义AI助手的角色和能力
    private static final String SYSTEM_PROMPT = """
            你是"智慧物业管理平台"的AI智能助手。你的职责是帮助用户解决物业管理相关问题。
            
            系统功能包括：
            1. **物业管理**：查看、添加、编辑物业信息
            2. **租约管理**：创建、查看、更新租约状态
            3. **维修工单**：租户提交维修申请，业主/管理员处理工单
            4. **财务管理**：记录和查询租金收支
            5. **用户管理**：管理员可以管理系统用户
            
            用户角色：
            - **管理员（ADMIN）**：拥有所有权限
            - **业主（OWNER）**：管理自己的物业和租约
            - **租户（TENANT）**：查看租约、提交维修申请
            
            回答要求：
            - 简洁明了，重点突出
            - 提供具体操作步骤
            - 友好、专业的语气
            - 如果不确定，建议联系管理员
            - 严格要求：必须使用纯文本格式回复，不得使用任何Markdown语法标记，包括但不限于星号(*)、下划线(_)、井号(#)、反引号(`)、方括号([])、尖括号(<>)等标记符号
            - 强调要点时使用数字编号、短横线或自然语言描述，避免使用任何特殊符号标记
            - 使用自然的中文语言表达，以清晰易懂的方式组织信息，保持专业且友好的语气
            """;

    public OpenAIChatService(OpenAiService openAiService, AIProperties aiProperties) {
        this.openAiService = openAiService;
        this.aiProperties = aiProperties;
    }

    @Override
    public String chat(String message, List<ChatRequest.ChatMessage> history) {
        try {
            // 构建消息列表
            List<ChatMessage> messages = new ArrayList<>();
            
            // 添加系统提示
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), SYSTEM_PROMPT));
            
            // 添加历史对话（限制数量避免token超限）
            if (history != null && !history.isEmpty()) {
                int startIndex = Math.max(0, history.size() - 10); // 最多10轮对话
                for (int i = startIndex; i < history.size(); i++) {
                    ChatRequest.ChatMessage msg = history.get(i);
                    messages.add(new ChatMessage(msg.getRole(), msg.getContent()));
                }
            }
            
            // 添加当前用户消息
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), message));
            
            // 调用OpenAI API
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(aiProperties.getModel())
                    .messages(messages)
                    .temperature(aiProperties.getTemperature())
                    .maxTokens(aiProperties.getMaxTokens())
                    .build();
            
            String reply = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();
            
            log.info("AI聊天成功 - 用户消息: {}, 回复长度: {}", message, reply.length());
            return reply;
            
        } catch (Exception e) {
            log.error("AI聊天失败", e);
            return getFallbackReply(message);
        }
    }
    
    /**
     * 降级回复 - AI不可用时的默认回复
     */
    private String getFallbackReply(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("维修") || lowerMessage.contains("报修")) {
            return "提交维修申请步骤：\n\n" +
                   "1. 进入维修申请页面\n" +
                   "2. 填写物业ID和问题描述\n" +
                   "3. 点击提交申请按钮\n" +
                   "4. 等待业主或管理员处理\n\n" +
                   "温馨提示：详细描述问题有助于更快解决！";
        }
        
        if (lowerMessage.contains("租约") || lowerMessage.contains("租金")) {
            return "查看租约信息：\n\n" +
                   "1. 进入租约管理页面\n" +
                   "2. 可以查看租约详情、租金和租期\n" +
                   "3. 租户可以查看自己的租约\n" +
                   "4. 业主可以管理物业的所有租约\n\n" +
                   "如需帮助，请联系管理员。";
        }
        
        if (lowerMessage.contains("功能") || lowerMessage.contains("使用")) {
            return "系统主要功能：\n\n" +
                   "1. 物业管理 - 查看和管理物业信息\n" +
                   "2. 租约管理 - 创建和跟踪租约\n" +
                   "3. 维修工单 - 提交和处理维修请求\n" +
                   "4. 财务管理 - 记录租金收支\n" +
                   "5. 用户管理 - 管理系统用户（管理员权限）\n\n" +
                   "具体问题欢迎详细咨询！";
        }
        
        return "抱歉，AI服务暂时不可用。\n\n" +
               "您可以：\n" +
               "- 浏览左侧菜单了解系统功能\n" +
               "- 查看各页面的帮助文档\n" +
               "- 联系系统管理员获取支持\n\n" +
               "感谢您的理解！";
    }
}
