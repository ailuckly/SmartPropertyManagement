<template>
  <section class="auth-card card">
    <h2>注册新账号</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-field">
        <label for="username">用户名</label>
        <input id="username" v-model="form.username" required type="text" autocomplete="username" placeholder="3-50个字符" />
        <small v-if="fieldErrors.username" class="field-error">{{ fieldErrors.username }}</small>
      </div>
      <div class="form-field">
        <label for="email">邮箱</label>
        <input id="email" v-model="form.email" required type="email" autocomplete="email" placeholder="name@example.com" />
        <small v-if="fieldErrors.email" class="field-error">{{ fieldErrors.email }}</small>
      </div>
      <div class="form-field">
        <label for="password">密码</label>
        <input
          id="password"
          v-model="form.password"
          required
          type="password"
          autocomplete="new-password"
          placeholder="至少6个字符"
        />
        <small v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</small>
      </div>
      <div class="form-field">
        <label for="firstName">名字</label>
        <input id="firstName" v-model="form.firstName" type="text" autocomplete="given-name" placeholder="可选" />
      </div>
      <div class="form-field">
        <label for="lastName">姓氏</label>
        <input id="lastName" v-model="form.lastName" type="text" autocomplete="family-name" placeholder="可选" />
      </div>
      <div class="form-field">
        <label for="phoneNumber">联系电话</label>
        <input id="phoneNumber" v-model="form.phoneNumber" type="tel" autocomplete="tel" placeholder="可选" />
        <small v-if="fieldErrors.phoneNumber" class="field-error">{{ fieldErrors.phoneNumber }}</small>
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
  phoneNumber: ''
});

const fieldErrors = reactive({
  username: '',
  email: '',
  password: '',
  phoneNumber: ''
});

const error = ref('');

const validate = () => {
  fieldErrors.username = '';
  fieldErrors.email = '';
  fieldErrors.password = '';
  fieldErrors.phoneNumber = '';

  let ok = true;
  if (!form.username || form.username.length < 3) {
    fieldErrors.username = '用户名至少 3 个字符';
    ok = false;
  }
  // 简单邮箱校验，后端仍会进行严格验证
  if (!form.email || !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(form.email)) {
    fieldErrors.email = '请输入有效邮箱地址';
    ok = false;
  }
  if (!form.password || form.password.length < 6) {
    fieldErrors.password = '密码至少 6 个字符';
    ok = false;
  }
  if (form.phoneNumber && !/^[+0-9\-\s]{0,20}$/.test(form.phoneNumber)) {
    fieldErrors.phoneNumber = '联系电话仅允许 +、数字、空格和连字符';
    ok = false;
  }
  return ok;
};

const handleSubmit = async () => {
  error.value = '';
  if (!validate()) return;
  try {
    const payload = { ...form };
    await authStore.register(payload);
    router.replace('/');
  } catch (err) {
    // 显示后端详细校验信息（details 是一个字段->提示 的映射）
    const details = err.response?.data?.details;
    if (details && typeof details === 'object') {
      for (const key of Object.keys(fieldErrors)) {
        if (details[key]) fieldErrors[key] = String(details[key]);
      }
    }
    error.value = err.response?.data?.message ?? '注册失败，请稍后再试';
  }
};
</script>

<style scoped>
.auth-card {
  max-width: 560px;
  margin: 56px auto;
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
.field-error { color: #ef4444; font-size: 12px; }
</style>
