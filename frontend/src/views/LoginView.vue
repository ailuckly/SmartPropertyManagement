<template>
  <section class="auth-card card">
    <h2>登录</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-field">
        <label for="username">用户名</label>
        <input id="username" v-model="form.username" required placeholder="请输入用户名" />
      </div>
      <div class="form-field">
        <label for="password">密码</label>
        <input
          id="password"
          v-model="form.password"
          required
          type="password"
          placeholder="请输入密码"
        />
      </div>
      <p v-if="error" class="error-msg">{{ error }}</p>
      <button type="submit" class="btn-primary" :disabled="authStore.loading">
        {{ authStore.loading ? '登录中...' : '登录' }}
      </button>
    </form>
    <p class="link-row">
      还没有账户？
      <RouterLink to="/register">去注册</RouterLink>
    </p>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter, useRoute, RouterLink } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const form = reactive({
  username: '',
  password: ''
});

const error = ref('');

const handleSubmit = async () => {
  error.value = '';
  try {
    await authStore.login(form);
    const redirect = route.query.redirect ?? '/';
    router.replace(redirect);
  } catch (err) {
    error.value = err.response?.data?.message ?? '登录失败，请检查账号信息';
  }
};
</script>

<style scoped>
.auth-card {
  max-width: 420px;
  margin: 40px auto;
}

.auth-card h2 {
  margin-top: 0;
  margin-bottom: 16px;
}

.link-row {
  margin-top: 16px;
  text-align: center;
  color: #1e293b;
}

.link-row a {
  color: #2563eb;
}

.error-msg {
  color: #ef4444;
  margin-bottom: 12px;
}
</style>
