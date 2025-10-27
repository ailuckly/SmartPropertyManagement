<template>
  <div class="global-ai-chat">
    <!-- 聊天窗口 -->
    <transition name="chat-slide">
      <div v-show="isOpen" class="chat-window">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="header-left">
            <div class="ai-avatar-small">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="header-info">
              <div class="header-title">AI智能助手</div>
              <div class="header-status">
                <span class="status-dot"></span>
                在线服务
              </div>
            </div>
          </div>
          <div class="header-actions">
            <el-button :icon="Close" circle size="small" @click="toggleChat" />
          </div>
        </div>

        <!-- 消息列表 -->
        <div ref="messageListRef" class="message-list">
          <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
            <!-- AI消息 -->
            <div v-if="msg.role === 'assistant'" class="message-wrapper">
              <div class="message-avatar ai-avatar">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="message-bubble ai-bubble">
                <div v-if="msg.typing" class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
                <div v-else class="message-content" v-html="formatMessage(msg.content)"></div>
              </div>
            </div>

            <!-- 用户消息 -->
            <div v-else class="message-wrapper user-wrapper">
              <div class="message-bubble user-bubble">
                <div class="message-content">{{ msg.content }}</div>
              </div>
              <div class="message-avatar user-avatar">
                <el-icon><User /></el-icon>
              </div>
            </div>
          </div>

          <!-- 快捷问题 -->
          <div v-if="messages.length === 1 && !isTyping" class="quick-questions">
            <div class="quick-title">常见问题：</div>
            <el-button
              v-for="(q, idx) in quickQuestions"
              :key="idx"
              size="small"
              plain
              @click="sendQuickQuestion(q)"
            >
              {{ q }}
            </el-button>
          </div>
        </div>

        <!-- 输入框 -->
        <div class="chat-input-area">
          <el-input
            v-model="inputText"
            :disabled="isTyping"
            placeholder="输入消息..."
            @keyup.enter="sendMessage"
          >
            <template #append>
              <el-button :icon="Promotion" :loading="isTyping" @click="sendMessage" />
            </template>
          </el-input>
        </div>
      </div>
    </transition>

    <!-- 悬浮按钮 -->
    <transition name="fab-bounce">
      <div v-show="!isOpen" class="chat-fab" @click="toggleChat">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0">
          <div class="fab-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
        </el-badge>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue';
import { ChatDotRound, Close, User, Promotion } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import api from '@/api/http';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const isOpen = ref(false);
const inputText = ref('');
const messages = ref([
  {
    role: 'assistant',
    content: '你好！我是AI智能助手。\n\n我可以帮助您：\n- 📋 解答物业管理相关问题\n- 🔧 指导维修工单提交\n- 💰 查询费用和租约信息\n- 📊 介绍系统功能使用\n\n有什么可以帮您的吗？',
    timestamp: new Date()
  }
]);
const isTyping = ref(false);
const messageListRef = ref(null);
const unreadCount = ref(0);

// 快捷问题
const quickQuestions = [
  '如何提交维修申请？',
  '如何查看我的租约？',
  '如何联系物业管理员？',
  '系统有哪些功能？'
];

// 切换聊天窗口
const toggleChat = () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    unreadCount.value = 0;
    nextTick(() => {
      scrollToBottom();
    });
  }
};

// 发送消息
const sendMessage = async () => {
  const text = inputText.value.trim();
  if (!text || isTyping.value) return;

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: text,
    timestamp: new Date()
  });
  inputText.value = '';

  // 显示AI输入中
  isTyping.value = true;
  messages.value.push({
    role: 'assistant',
    typing: true,
    timestamp: new Date()
  });

  scrollToBottom();

  try {
    // 调用后端AI接口
    const { data } = await api.post('/ai/chat', {
      message: text,
      history: messages.value
        .filter(m => !m.typing)
        .slice(-10) // 只发送最近10条
        .map(m => ({ role: m.role, content: m.content }))
    });

    // 移除typing消息
    messages.value = messages.value.filter(m => !m.typing);

    // 添加AI回复
    messages.value.push({
      role: 'assistant',
      content: data.reply || '抱歉，我现在无法回答这个问题。',
      timestamp: new Date()
    });

    // 如果窗口关闭，增加未读数
    if (!isOpen.value) {
      unreadCount.value++;
    }
  } catch (error) {
    // 移除typing消息
    messages.value = messages.value.filter(m => !m.typing);

    // 显示错误消息
    messages.value.push({
      role: 'assistant',
      content: getErrorReply(error),
      timestamp: new Date()
    });
  } finally {
    isTyping.value = false;
    scrollToBottom();
  }
};

// 快捷问题
const sendQuickQuestion = (question) => {
  inputText.value = question;
  sendMessage();
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
    }
  });
};

// 格式化消息（支持换行和Markdown）
const formatMessage = (text) => {
  if (!text) return '';
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>');
};

// 错误回复
const getErrorReply = (error) => {
  if (error.response?.status === 401) {
    return '请先登录后再使用AI助手功能。';
  } else if (error.response?.status === 503) {
    return '抱歉，AI服务暂时不可用。请稍后再试。';
  } else {
    return '抱歉，我遇到了一些问题。请稍后再试或联系管理员。';
  }
};
</script>

<style scoped>
.global-ai-chat {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
}

/* 聊天窗口 */
.chat-window {
  width: 380px;
  height: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar-small {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar-small .el-icon {
  font-size: 22px;
  color: white;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.header-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  opacity: 0.9;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #67c23a;
  border-radius: 50%;
  animation: pulse-dot 2s ease-in-out infinite;
}

.header-actions .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.header-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
  scroll-behavior: smooth;
}

.message-item {
  margin-bottom: 16px;
}

.message-wrapper {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.user-wrapper {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.user-avatar {
  background: #409eff;
  color: white;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
}

.ai-bubble {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-bubble {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
}

.message-content :deep(strong) {
  font-weight: 600;
}

.message-content :deep(code) {
  background: rgba(0, 0, 0, 0.06);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
}

.user-bubble .message-content :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

/* 输入中动画 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: typing-bounce 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

/* 快捷问题 */
.quick-questions {
  margin-top: 16px;
  padding: 12px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.quick-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.quick-questions .el-button {
  margin: 4px 4px 4px 0;
  font-size: 13px;
}

/* 输入区域 */
.chat-input-area {
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.chat-input-area :deep(.el-input-group__append) {
  background: #409eff;
  border: none;
  padding: 0 12px;
}

.chat-input-area :deep(.el-input-group__append .el-button) {
  color: white;
  background: transparent;
  border: none;
}

/* 悬浮按钮 */
.chat-fab {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.chat-fab:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.5);
}

.fab-icon {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fab-icon .el-icon {
  font-size: 28px;
  color: white;
}

/* 动画 */
.chat-slide-enter-active,
.chat-slide-leave-active {
  transition: all 0.3s ease;
}

.chat-slide-enter-from,
.chat-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

.fab-bounce-enter-active {
  animation: bounce-in 0.5s;
}

.fab-bounce-leave-active {
  animation: bounce-out 0.3s;
}

@keyframes bounce-in {
  0% {
    opacity: 0;
    transform: scale(0);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes bounce-out {
  to {
    opacity: 0;
    transform: scale(0);
  }
}

@keyframes typing-bounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.7;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-window {
    width: calc(100vw - 32px);
    height: calc(100vh - 100px);
    bottom: 16px;
    right: 16px;
  }

  .global-ai-chat {
    bottom: 16px;
    right: 16px;
  }
}

/* 滚动条样式 */
.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: #909399;
}
</style>
