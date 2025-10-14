<template>
  <div class="register-container">
    <el-card class="register-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon :size="28" color="#409EFF"><Edit /></el-icon>
          <h2>注册新账号</h2>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        size="large"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入3-50个字符"
            :prefix-icon="User"
            autocomplete="username"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="form.email"
            type="email"
            placeholder="name@example.com"
            :prefix-icon="Message"
            autocomplete="email"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入至少6个字符"
            :prefix-icon="Lock"
            autocomplete="new-password"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="姓氏" prop="lastName">
          <el-input
            v-model="form.lastName"
            placeholder="请输入姓氏（可选）"
            clearable
          />
        </el-form-item>

        <el-form-item label="名字" prop="firstName">
          <el-input
            v-model="form.firstName"
            placeholder="请输入名字（可选）"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="phoneNumber">
          <el-input
            v-model="form.phoneNumber"
            placeholder="请输入电话号码（可选）"
            :prefix-icon="Phone"
            autocomplete="tel"
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
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { User, Lock, Message, Phone, Edit } from '@element-plus/icons-vue';
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
  if (value && !/^[+0-9\-\s]{0,20}$/.test(value)) {
    callback(new Error('电话号码仅允许数字、+、-和空格'));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为 3-50 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, validator: validateEmail, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少为 6 个字符', trigger: 'blur' }
  ],
  phoneNumber: [
    { validator: validatePhone, trigger: 'blur' }
  ]
};

const error = ref('');

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    error.value = '';
    try {
      const payload = { ...form };
      await authStore.register(payload);
      ElMessage.success('注册成功！');
      router.replace('/');
    } catch (err) {
      // 显示后端详细校验信息
      const details = err.response?.data?.details;
      if (details && typeof details === 'object') {
        // 将后端错误信息显示在表单字段上
        const fieldMap = {
          username: 'username',
          email: 'email',
          password: 'password',
          phoneNumber: 'phoneNumber'
        };
        
        for (const [key, field] of Object.entries(fieldMap)) {
          if (details[key] && formRef.value && formRef.value.fields) {
            const fieldItem = formRef.value.fields.find(f => f.prop === field);
            if (fieldItem) {
              fieldItem.validateState = 'error';
              fieldItem.validateMessage = String(details[key]);
            }
          }
        }
      }
      error.value = err.response?.data?.message ?? '注册失败，请稍后再试';
    }
  });
};
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  padding: 40px 20px;
}

.register-card {
  width: 100%;
  max-width: 560px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.card-header h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
  font-weight: 500;
}

.footer-links {
  text-align: center;
  color: #606266;
  font-size: 14px;
}

.footer-links span {
  margin-right: 8px;
}

:deep(.el-card__header) {
  padding: 20px;
  background-color: #ffffff;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 30px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}
</style>
