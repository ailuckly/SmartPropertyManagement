<template>
  <section class="auth-card card">
    <h2>注册新账号</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-field">
        <label for="username">用户名</label>
        <input id="username" v-model="form.username" required placeholder="3-50个字符" />
      </div>
      <div class="form-field">
        <label for="email">邮箱</label>
        <input id="email" v-model="form.email" required type="email" placeholder="name@example.com" />
      </div>
      <div class="form-field">
        <label for="password">密码</label>
        <input
          id="password"
          v-model="form.password"
          required
          type="password"
          placeholder="至少6个字符"
        />
      </div>
      <div class="form-field">
        <label for="firstName">名字</label>
        <input id="firstName" v-model="form.firstName" placeholder="可选" />
      </div>
      <div class="form-field">
        <label for="lastName">姓氏</label>
        <input id="lastName" v-model="form.lastName" placeholder="可选" />
      </div>
      <div class="form-field">
        <label for="phoneNumber">联系电话</label>
        <input id="phoneNumber" v-model="form.phoneNumber" placeholder="可选" />
      </div>
      <div class="form-field">
        <label for="role">角色</label>
        <select id="role" v-model="form.role">
          <option value="">租户</option>
          <option value="ROLE_OWNER">业主</option>
          <option value="ROLE_ADMIN">管理员</option>
        </select>
      </div>
      <p v-if="error" class="error-msg">{{ error }}</p>
      <button class="btn-primary" type="submit" :disabled="authStore.loading">
        {{ authStore.loading ? '注册中...' : '注册并登录' }}
      </button>
    </form>
    <p class="link-row">
      已有账号？
      <RouterLink to="/login">去登录</RouterLink>
    </p>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

/**
 * Registration screen. Mirrors the backend validation requirements and logs in the user upon success.
 */
const authStore = useAuthStore();
const router = useRouter();

const form = reactive({
  username: '',
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  phoneNumber: '',
  role: ''
});

const error = ref('');

const handleSubmit = async () => {
  error.value = '';
  try {
    const payload = {
      ...form,
      role: form.role || null
    };
    await authStore.register(payload);
    router.replace('/');
  } catch (err) {
    error.value = err.response?.data?.message ?? '注册失败，请稍后再试';
  }
};
</script>

<style scoped>
.auth-card {
  max-width: 520px;
  margin: 40px auto;
}

.auth-card h2 {
  margin-top: 0;
  margin-bottom: 16px;
}

.error-msg {
  color: #ef4444;
  margin-bottom: 12px;
}

.link-row {
  margin-top: 16px;
  text-align: center;
  color: #1e293b;
}

.link-row a {
  color: #2563eb;
}
</style>
