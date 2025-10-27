<template>
  <transition name="ai-fade">
    <div v-if="visible" class="ai-assistant">
      <!-- AI头像 -->
      <div class="ai-avatar">
        <el-icon class="ai-icon" :class="{ 'ai-thinking': analyzing }">
          <ChatDotRound />
        </el-icon>
      </div>

      <!-- AI对话气泡 -->
      <div class="ai-bubble">
        <!-- 分析中状态 -->
        <div v-if="analyzing" class="ai-loading">
          <div class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <div class="ai-text">AI正在分析中...</div>
        </div>

        <!-- 分析结果 -->
        <div v-else-if="result && result.success" class="ai-result">
          <div class="ai-title">
            <el-icon><MagicStick /></el-icon>
            <span>智能分析结果</span>
          </div>

          <div class="result-items">
            <!-- 问题分类 -->
            <div class="result-item">
              <div class="item-label">
                <el-icon><Collection /></el-icon>
                问题分类
              </div>
              <el-tag :type="getCategoryColor(result.category)" size="small" effect="light">
                {{ result.category }}
              </el-tag>
            </div>

            <!-- 紧急程度 -->
            <div class="result-item">
              <div class="item-label">
                <el-icon><Warning /></el-icon>
                紧急程度
              </div>
              <el-tag :type="getUrgencyColor(result.urgencyLevel)" size="small" effect="dark">
                {{ result.urgencyLevel }}
              </el-tag>
            </div>

            <!-- 预估费用 -->
            <div v-if="result.estimatedCost" class="result-item">
              <div class="item-label">
                <el-icon><Money /></el-icon>
                预估费用
              </div>
              <span class="cost-value">¥{{ result.estimatedCost }}</span>
            </div>

            <!-- 维修建议 -->
            <div v-if="result.solution" class="result-item solution">
              <div class="item-label">
                <el-icon><Lightning /></el-icon>
                维修建议
              </div>
              <div class="solution-text">{{ result.solution }}</div>
            </div>
          </div>
        </div>

        <!-- 分析失败 -->
        <div v-else-if="result && !result.success" class="ai-error">
          <el-icon><WarningFilled /></el-icon>
          <span>{{ result.errorMessage || 'AI分析失败' }}</span>
        </div>

        <!-- 关闭按钮 -->
        <el-button
          v-if="!analyzing"
          class="close-btn"
          :icon="Close"
          circle
          size="small"
          @click="handleClose"
        />
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed } from 'vue';
import {
  ChatDotRound,
  MagicStick,
  Collection,
  Warning,
  Money,
  Lightning,
  WarningFilled,
  Close
} from '@element-plus/icons-vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  analyzing: {
    type: Boolean,
    default: false
  },
  result: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['close']);

const handleClose = () => {
  emit('close');
};

// 分类颜色映射
const getCategoryColor = (category) => {
  const colorMap = {
    '水电': 'primary',
    '家电': 'success',
    '家具': 'warning',
    '门窗': 'info',
    '清洁': '',
    '其他': 'info'
  };
  return colorMap[category] || 'info';
};

// 紧急程度颜色映射
const getUrgencyColor = (level) => {
  const colorMap = {
    '低': 'info',
    '中': 'warning',
    '高': 'danger',
    '紧急': 'danger'
  };
  return colorMap[level] || 'info';
};
</script>

<style scoped>
.ai-assistant {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-top: 16px;
  animation: slideInUp 0.4s ease-out;
}

.ai-avatar {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.ai-icon {
  font-size: 24px;
  color: white;
}

.ai-thinking {
  animation: pulse 1.5s ease-in-out infinite;
}

.ai-bubble {
  position: relative;
  flex: 1;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  max-width: 500px;
}

.ai-bubble::before {
  content: '';
  position: absolute;
  left: -8px;
  top: 16px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 8px 8px 8px 0;
  border-color: transparent #f5f7fa transparent transparent;
}

/* 加载状态 */
.ai-loading {
  display: flex;
  align-items: center;
  gap: 12px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

.ai-text {
  color: #606266;
  font-size: 14px;
}

/* 分析结果 */
.ai-result {
  position: relative;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.ai-title .el-icon {
  color: #667eea;
  font-size: 18px;
}

.result-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.result-item.solution {
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}

.item-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.item-label .el-icon {
  color: #909399;
  font-size: 16px;
}

.cost-value {
  color: #f56c6c;
  font-size: 16px;
  font-weight: 600;
}

.solution-text {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  background: rgba(255, 255, 255, 0.6);
  padding: 12px;
  border-radius: 8px;
  width: 100%;
}

/* 错误状态 */
.ai-error {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f56c6c;
  font-size: 14px;
}

.ai-error .el-icon {
  font-size: 18px;
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.8);
  border: none;
}

.close-btn:hover {
  background: white;
}

/* 动画 */
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.7;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.ai-fade-enter-active,
.ai-fade-leave-active {
  transition: all 0.3s ease;
}

.ai-fade-enter-from,
.ai-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 响应式 */
@media (max-width: 768px) {
  .ai-bubble {
    max-width: 100%;
  }
  
  .result-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}
</style>
