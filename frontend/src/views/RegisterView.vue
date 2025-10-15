<template>
  <div class="register-container">
    <!-- 左侧注册区域 -->
    <div class="register-section">
      <el-card class="register-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon :size="24" color="#409EFF"><Edit /></el-icon>
            <h2>注册新账号</h2>
          </div>
        </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="default"
        style="width: 100%"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
            autocomplete="username"
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            type="email"
            placeholder="邮箱地址"
            :prefix-icon="Message"
            autocomplete="email"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            autocomplete="new-password"
            show-password
          />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item prop="lastName">
              <el-input
                v-model="form.lastName"
                placeholder="姓氏"
                :prefix-icon="UserFilled"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="firstName">
              <el-input
                v-model="form.firstName"
                placeholder="名字"
                :prefix-icon="UserFilled"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item prop="phoneNumber">
          <el-input
            v-model="form.phoneNumber"
            placeholder="手机号码"
            :prefix-icon="Phone"
            autocomplete="tel"
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
            :loading="authStore.loading"
            style="width: 100%"
            size="default"
            @click="handleSubmit"
          >
            {{ authStore.loading ? '注册中...' : '注册并登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <div class="footer-links">
        <span>已有账号？</span>
        <el-link type="primary" :underline="false" @click="router.push('/login')">
          立即登录
        </el-link>
      </div>
    </el-card>
    </div>
    
    <!-- 右侧信息展示区域 -->
    <div class="info-section">
      <div class="info-content">
        <div class="platform-intro">
          <div class="intro-icon">
            <el-icon :size="64" color="#ffffff"><OfficeBuilding /></el-icon>
          </div>
          <h1 class="platform-title">加入我们</h1>
          <p class="platform-desc">体验现代化物业管理的便捷与高效</p>
        </div>
        
        <div class="advantages-list">
          <div class="advantage-item">
            <div class="advantage-icon">
              <el-icon :size="20" color="#67C23A"><House /></el-icon>
            </div>
            <div class="advantage-text">
              <h4>专业物业管理</h4>
              <p>提供从房屋出租到维修管理的一站式服务</p>
            </div>
          </div>
          
          <div class="advantage-item">
            <div class="advantage-icon">
              <el-icon :size="20" color="#409EFF"><DataLine /></el-icon>
            </div>
            <div class="advantage-text">
              <h4>智能数据分析</h4>
              <p>实时监控租金收入、维修状态等关键指标</p>
            </div>
          </div>
          
          <div class="advantage-item">
            <div class="advantage-icon">
              <el-icon :size="20" color="#E6A23C"><Bell /></el-icon>
            </div>
            <div class="advantage-text">
              <h4>实时消息通知</h4>
              <p>第一时间了解租约变更、维修进展等信息</p>
            </div>
          </div>
          
          <div class="advantage-item">
            <div class="advantage-icon">
              <el-icon :size="20" color="#F56C6C"><Key /></el-icon>
            </div>
            <div class="advantage-text">
              <h4>安全可靠保障</h4>
              <p>企业级数据加密，保障您的个人信息安全</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { User, UserFilled, Lock, Message, Phone, Edit, OfficeBuilding, House, DataLine, Bell, Key } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

/**
 * Registration screen. Mirrors the backend validation requirements and logs in the user upon success.
 */
const authStore = useAuthStore();
const router = useRouter();
const formRef = ref(null);

const form = reactive({
  username: '',
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  phoneNumber: ''
});

// 自定义邮箱验证器
const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱地址'));
  } else if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(value)) {
    callback(new Error('请输入有效的邮箱地址'));
  } else {
    callback();
  }
};

// 自定义电话验证器
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号码'));
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的11位手机号码（如：13812345678）'));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: ['blur', 'change'] },
    { min: 3, max: 50, message: '用户名长度为 3-50 个字符', trigger: ['blur', 'change'] }
  ],
  email: [
    { required: true, validator: validateEmail, trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: ['blur', 'change'] },
    { min: 6, message: '密码至少为 6 个字符', trigger: ['blur', 'change'] }
  ],
  lastName: [
    { required: true, message: '请输入姓氏', trigger: ['blur', 'change'] },
    { min: 1, max: 20, message: '姓氏长度为 1-20 个字符', trigger: ['blur', 'change'] }
  ],
  firstName: [
    { required: true, message: '请输入名字', trigger: ['blur', 'change'] },
    { min: 1, max: 20, message: '名字长度为 1-20 个字符', trigger: ['blur', 'change'] }
  ],
  phoneNumber: [
    { required: true, message: '请输入手机号码', trigger: ['blur', 'change'] },
    { validator: validatePhone, trigger: ['blur', 'change'] }
  ]
};

const error = ref('');

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  // 清除之前的错误信息和验证状态
  error.value = '';
  formRef.value.clearValidate();
  
  try {
    // 验证表单
    await formRef.value.validate();
    
    // 构造符合后端API格式的请求数据
    const payload = {
      username: form.username.trim(),
      email: form.email.trim(),
      password: form.password,
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      phoneNumber: form.phoneNumber.trim()
    };
    
    await authStore.register(payload);
    ElMessage.success('注册成功，即将跳转至主页...');
    router.replace('/');
  } catch (err) {
    // 区分表单验证错误和API请求错误
    if (err?.message?.includes('validation')) {
      // 表单验证失败，错误已由Element Plus显示
      return;
    }
    
    // 处理后端返回的字段级错误
    const details = err.response?.data?.details;
    if (details && typeof details === 'object' && formRef.value) {
      // 使用setFields方法设置字段错误
      const fields = Object.entries(details)
        .filter(([key]) => ['username', 'email', 'password', 'phoneNumber', 'firstName', 'lastName'].includes(key))
        .map(([key, message]) => ({
          name: key,
          errors: [String(message)]
        }));
      
      if (fields.length > 0) {
        formRef.value.setFields(fields);
      }
    }
    
    // 显示顶部错误提示
    const errorMessage = err.response?.data?.message || err.message || '注册失败，请稍后再试';
    error.value = errorMessage;
  }
};
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  padding: 0;
  box-sizing: border-box;
}

.register-section {
  flex: 0 0 40%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f6f8fa;
  padding: 40px 30px;
}

.register-card {
  width: 100%;
  max-width: 420px;
  border: 1px solid #e8e9eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  transition: all 0.3s ease;
  background: #ffffff;
  overflow: hidden;
}

.register-card:hover {
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

.info-section {
  flex: 0 0 60%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: white;
}

.info-content {
  max-width: 480px;
}

.platform-intro {
  text-align: center;
  margin-bottom: 50px;
}

.intro-icon {
  margin-bottom: 20px;
}

.platform-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 16px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.platform-desc {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
  font-weight: 300;
  line-height: 1.5;
}

.advantages-list {
  display: grid;
  gap: 24px;
}

.advantage-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.advantage-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.advantage-text h4 {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 500;
}

.advantage-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
  line-height: 1.4;
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
  margin-bottom: 14px;
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
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

:deep(.el-row) {
  margin-bottom: 0;
}

:deep(.el-col) {
  margin-bottom: 0;
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
