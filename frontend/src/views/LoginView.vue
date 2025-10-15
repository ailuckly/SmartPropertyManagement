<template>
  <div class="login-container">
    <!-- 左侧装饰区域 -->
    <div class="decoration-section">
      <div class="brand-info">
        <div class="brand-logo">
          <el-icon :size="48" color="#ffffff"><OfficeBuilding /></el-icon>
        </div>
        <h1 class="brand-title">智慧物业管理平台</h1>
        <p class="brand-subtitle">让物业管理更简单、更高效</p>
        
        <div class="features-list">
          <div class="feature-item">
            <el-icon color="#67C23A"><House /></el-icon>
            <span>智能物业管理</span>
          </div>
          <div class="feature-item">
            <el-icon color="#E6A23C"><User /></el-icon>
            <span>租户服务系统</span>
          </div>
          <div class="feature-item">
            <el-icon color="#F56C6C"><Tools /></el-icon>
            <span>维修工单管理</span>
          </div>
          <div class="feature-item">
            <el-icon color="#909399"><Wallet /></el-icon>
            <span>财务数据统计</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 右侧登录区域 -->
    <div class="login-section">
      <el-card class="login-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon :size="24" color="#409EFF"><UserFilled /></el-icon>
            <h2>用户登录</h2>
          </div>
        </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            autocomplete="username"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            autocomplete="current-password"
            show-password
            clearable
          />
        </el-form-item>

        <el-alert
          v-if="error"
          :title="error"
          type="error"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="authStore.loading"
            style="width: 100%"
            size="large"
          >
            {{ authStore.loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <div class="footer-links">
        <span>还没有账户？</span>
        <el-link type="primary" :underline="false" @click="router.push('/register')">
          立即注册
        </el-link>
      </div>
    </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { User, Lock, UserFilled, OfficeBuilding, House, Tools, Wallet } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

/**
 * Login form backed by the auth store. Successful login redirects to the original target (when present).
 */
const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const formRef = ref(null);

const form = reactive({
  username: '',
  password: ''
});

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: ['blur', 'change'] },
    { min: 3, max: 50, message: '用户名长度为 3-50 个字符', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: ['blur', 'change'] },
    { min: 6, message: '密码至少为 6 个字符', trigger: ['blur', 'change'] }
  ]
};

const error = ref('');

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  // 清除之前的错误信息
  error.value = '';
  
  try {
    // 验证表单
    await formRef.value.validate();
    
    // 验证成功，执行登录
    await authStore.login(form);
    ElMessage.success('登录成功！');
    const redirect = route.query.redirect ?? '/';
    router.replace(redirect);
  } catch (err) {
    // 区分表单验证错误和API请求错误
    if (err?.message?.includes('validation')) {
      // 表单验证失败，错误已由Element Plus显示，无需额外处理
      return;
    }
    
    // API请求失败，显示错误信息
    const errorMsg = err.response?.data?.message || err.message || '登录失败，请检查网络连接';
    error.value = errorMsg;
  }
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  padding: 0;
  box-sizing: border-box;
}

.decoration-section {
  flex: 0 0 60%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.brand-info {
  text-align: center;
  color: white;
}

.brand-logo {
  margin-bottom: 20px;
}

.brand-title {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.brand-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 40px 0;
  font-weight: 300;
}

.features-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  max-width: 300px;
  margin: 0 auto;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  opacity: 0.95;
}

.login-section {
  flex: 0 0 40%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f6f8fa;
  padding: 40px 30px;
}

.login-card {
  width: 100%;
  max-width: 380px;
  border: 1px solid #e8e9eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  transition: all 0.3s ease;
  background: #ffffff;
  overflow: hidden;
}

.login-card:hover {
  border-color: #d0d7de;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #24292f;
  font-weight: 600;
  letter-spacing: -0.025em;
}

.footer-links {
  text-align: center;
  color: #656d76;
  font-size: 14px;
  line-height: 1.45;
}

.footer-links span {
  margin-right: 4px;
}

:deep(.el-link) {
  color: #0969da;
  font-weight: 500;
}

:deep(.el-link:hover) {
  color: #0550ae;
}

:deep(.el-card__header) {
  padding: 24px 24px 16px 24px;
  background-color: #ffffff;
  border-bottom: 1px solid #e8e9eb;
  margin: 0;
}

:deep(.el-card__body) {
  padding: 20px 24px 24px 24px;
  background-color: #ffffff;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

:deep(.el-input__wrapper) {
  padding: 8px 12px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  background-color: #ffffff;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #959da5;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.12);
}

:deep(.el-input__inner) {
  font-size: 14px;
  line-height: 1.45;
  color: #24292f;
}

:deep(.el-input) {
  width: 100%;
}

:deep(.el-button--primary) {
  background-color: #0969da;
  border-color: #0969da;
  border-radius: 6px;
  font-weight: 500;
  font-size: 14px;
  padding: 8px 16px;
  transition: all 0.2s ease;
}

:deep(.el-button--primary:hover) {
  background-color: #0550ae;
  border-color: #0550ae;
}

:deep(.el-button--primary:active) {
  background-color: #0a3069;
  border-color: #0a3069;
}
</style>
