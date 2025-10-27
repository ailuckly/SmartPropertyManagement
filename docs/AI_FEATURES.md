# AI智能功能 - 完整配置指南

## 🎯 功能概览

本平台集成了两大AI智能功能：

### 1. 全局AI聊天助手
- 💬 固定在页面右下角的智能对话机器人
- 🎯 解答系统使用问题，提供操作指导
- 🔄 支持多轮对话，理解上下文
- 📱 响应式设计，适配桌面和移动端

### 2. 维修工单智能分析
- 🏷️ 自动分类问题类型（水电/家电/家具/门窗/清洁/其他）
- ⚠️ 评估紧急程度（低/中/高/紧急）
- 💡 生成维修建议
- 💰 预估维修费用

## 🚀 快速配置（使用七牛云AI）

### 步骤1：获取API Key
1. 登录七牛云AI平台
2. 进入 **API Key 管理** 页面
3. 复制你的 API Key（格式：`sk-xxxxx`）

### 步骤2：配置后端

**开发环境（直接修改配置文件）**

编辑 `backend/src/main/resources/application.properties`：

```properties
# 启用AI功能
app.ai.enabled=true

# 七牛云API Key（必填）
app.ai.api-key=你的七牛云API-Key

# 七牛云接口地址
app.ai.base-url=https://openai.qiniu.com/v1

# 七牛云模型ID
app.ai.model=gpt-oss-120b
```

**生产环境（使用环境变量）**

Windows PowerShell:
```powershell
$env:AI_ENABLED="true"
$env:OPENAI_API_KEY="你的七牛云API-Key"
$env:OPENAI_BASE_URL="https://openai.qiniu.com/v1"
$env:OPENAI_MODEL="gpt-oss-120b"
```

Linux/Mac:
```bash
export AI_ENABLED=true
export OPENAI_API_KEY=你的七牛云API-Key
export OPENAI_BASE_URL=https://openai.qiniu.com/v1
export OPENAI_MODEL=gpt-oss-120b
```

### 步骤3：启动服务
```bash
cd backend
mvn spring-boot:run
```

启动后查看日志，应看到：
```
初始化OpenAI服务: baseUrl=https://openai.qiniu.com/v1, model=gpt-oss-120b
```

## 📋 配置参数说明

| 参数 | 默认值 | 说明 |
|-----|--------|------|
| `app.ai.enabled` | `false` | 是否启用AI功能 |
| `app.ai.api-key` | - | API密钥（必填） |
| `app.ai.base-url` | `https://openai.qiniu.com/v1` | API服务地址 |
| `app.ai.model` | `gpt-oss-120b` | 使用的模型 |
| `app.ai.timeout` | `60` | 请求超时（秒） |
| `app.ai.temperature` | `0.7` | 输出随机性（0-2） |
| `app.ai.max-tokens` | `800` | 最大返回token数 |

### 备用地址

如主地址不可用，可切换到备用地址：
```properties
app.ai.base-url=https://api.qnaigc.com/v1
```

## ✅ 功能测试

### 测试全局AI聊天
1. 登录系统
2. 查看右下角紫色悬浮球 💬
3. 点击展开聊天窗口
4. 发送消息测试

**示例对话**：
```
用户: 系统有哪些功能？
AI:   【系统主要功能】
      📋 物业管理 - 查看和管理物业信息
      📄 租约管理 - 创建和跟踪租约
      🔧 维修工单 - 提交和处理维修请求
      💰 财务管理 - 记录租金收支
      👥 用户管理 - 管理系统用户（管理员）
```

### 测试维修工单分析
1. 进入"维修申请"页面
2. 填写工单信息：
   - 物业ID: 1
   - 问题描述: "厕所马桶漏水，水一直流"
3. 点击"提交申请"
4. 查看AI分析结果（分类、紧急度、建议、费用）

**响应示例**：
```json
{
  "id": 1,
  "description": "厕所马桶漏水，水一直流",
  "aiCategory": "水电",
  "aiUrgencyLevel": "高",
  "aiSolution": "需要检查马桶水箱内部配件，可能是浮球阀或排水阀损坏，建议尽快联系专业水电工维修",
  "aiEstimatedCost": 150.0
}
```

## 🎨 全局聊天助手UI特性

### 视觉效果
- **主题色**：紫色渐变（#667eea → #764ba2）
- **悬浮球**：Hover放大效果
- **窗口**：滑入滑出动画
- **输入中**：跳动圆点动画
- **在线状态**：绿色脉搏灯

### 快捷问题按钮
首次打开提供常见问题：
- "如何提交维修申请？"
- "如何查看我的租约？"
- "如何联系物业管理员？"
- "系统有哪些功能？"

### 响应式设计
- **桌面端**：宽380px，高600px
- **移动端**：自适应全屏

## 🔧 技术架构

### 后端接口
```
POST /api/ai/chat
Content-Type: application/json

请求体：
{
  "message": "如何提交维修申请？",
  "history": [
    { "role": "user", "content": "..." },
    { "role": "assistant", "content": "..." }
  ]
}

响应：
{
  "reply": "AI的回复内容"
}
```

### 前端组件
- **组件位置**：`frontend/src/components/GlobalAIChat.vue`
- **集成方式**：在 `App.vue` 中全局挂载
- **状态管理**：组件内部管理对话历史（限制10轮）

### 技术流程
```
MaintenanceRequestController
    ↓
MaintenanceRequestService
    ↓ (条件调用)
AIService (接口)
    ↓
OpenAIMaintenanceAIService (实现)
    ↓
OpenAI API (七牛云兼容接口)
```

## 🐛 常见问题排查

### 问题1：AI功能不生效
**排查步骤**：
1. 确认 `app.ai.enabled=true`
2. 检查API Key是否正确
3. 查看后端日志是否有错误
4. 测试网络能否访问 `https://openai.qiniu.com`

### 问题2：悬浮球不显示
**原因**：
- 未登录
- AI未启用
- 前端缓存问题

**解决**：清除浏览器缓存（Ctrl + F5 或 Cmd + Shift + R）

### 问题3：API调用超时
**调整超时时间**：
```properties
app.ai.timeout=120
```

### 问题4：回复质量不佳
**优化建议**：
- 调整 `temperature` 参数（0.5-0.7）
- 增加 `max-tokens`（获取更详细回复）
- 完善系统提示词

## 🔒 安全建议

1. **不要硬编码API Key**
   - 使用环境变量
   - 使用密钥管理服务

2. **设置访问限制**
   - 仅登录用户可用
   - 限制调用频率

3. **监控异常调用**
   - 定期检查日志
   - 设置告警阈值

## 💰 费用说明

七牛云AI按实际调用量计费：
- 每次对话消耗token
- 历史对话越长，消耗越多
- 建议限制对话轮数（已限制10轮）
- 在七牛云控制台可查看调用次数和费用统计

## 📚 扩展开发

### 自定义AI分析逻辑
修改文件：
- `OpenAIMaintenanceAIService.java` - 修改提示词和解析逻辑
- `AIProperties.java` - 添加新的配置项
- `MaintenanceAnalysisResult.java` - 扩展返回字段

### 系统提示词
AI助手的角色定义位于后端服务中，可根据需要调整：
```
你是"智慧物业管理平台"的AI智能助手。
职责：帮助用户解决物业管理相关问题。

回答要求：
- 简洁明了，重点突出
- 提供具体操作步骤
- 友好、专业的语气
```

## 📊 监控与日志

### 查看AI调用日志
后端日志会输出：
```
AI聊天成功 - 用户消息: xxx, 回复长度: xxx
维修工单AI分析失败
```

### 统计API使用
七牛云控制台可以查看：
- 调用次数
- Token消耗
- 费用统计

---

**版本**：v1.0  
**更新日期**：2025-10-27  
**适用系统**：智慧物业管理平台
