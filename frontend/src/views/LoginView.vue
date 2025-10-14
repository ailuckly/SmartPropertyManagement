<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon :size="28" color="#409EFF"><UserFilled /></el-icon>
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
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { User, Lock, UserFilled } from '@element-plus/icons-vue';
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
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为 3-50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少为 6 个字符', trigger: 'blur' }
  ]
};

const error = ref('');

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    error.value = '';
    try {
      await authStore.login(form);
      ElMessage.success('登录成功！');
      const redirect = route.query.redirect ?? '/';
      router.replace(redirect);
    } catch (err) {
      error.value = err.response?.data?.message ?? '登录失败，请检查账号信息';
    }
  });
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
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
  margin-bottom: 22px;
}
</style>
