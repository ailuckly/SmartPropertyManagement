<template>
  <div class="app-container">
    <header class="app-header">
      <div class="logo-group">
        <img src="@/assets/logo.svg" alt="logo" />
        <span>智慧物业管理平台</span>
      </div>
      <nav>
        <RouterLink v-if="isAuthenticated" to="/" class="nav-link">仪表盘</RouterLink>
        <RouterLink v-if="isAuthenticated" to="/properties" class="nav-link">物业管理</RouterLink>
        <RouterLink v-if="isTenant" to="/maintenance" class="nav-link">维修申请</RouterLink>
        <RouterLink v-if="isOwnerOrAdmin" to="/maintenance" class="nav-link">维修工单</RouterLink>
        <RouterLink v-if="isAuthenticated" to="/leases" class="nav-link">租约</RouterLink>
        <RouterLink v-if="isOwnerOrAdmin" to="/payments" class="nav-link">收支记录</RouterLink>
      </nav>
      <div class="header-actions">
        <template v-if="isAuthenticated">
          <span class="welcome">你好，{{ user?.username }}</span>
          <button class="btn-primary" @click="logout">退出</button>
        </template>
        <template v-else>
          <RouterLink to="/login" class="nav-link">登录</RouterLink>
          <RouterLink to="/register" class="nav-link">注册</RouterLink>
        </template>
      </div>
    </header>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { RouterLink, RouterView } from 'vue-router';
import { useAuthStore } from './stores/auth';

const authStore = useAuthStore();

const user = computed(() => authStore.user);
const isAuthenticated = computed(() => authStore.isAuthenticated);
const isOwnerOrAdmin = computed(() => authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']));
const isTenant = computed(() => authStore.hasAnyRole(['ROLE_TENANT']));

const logout = () => {
  authStore.logout();
};
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  height: 64px;
  background: #0f172a;
  color: #fff;
}

.logo-group {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 700;
  font-size: 18px;
}

.logo-group img {
  width: 32px;
  height: 32px;
}

nav {
  display: flex;
  gap: 16px;
}

.nav-link {
  color: #e2e8f0;
  font-weight: 500;
  transition: color 0.2s ease;
}

.nav-link:hover {
  color: #38bdf8;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome {
  font-size: 14px;
  color: #cbd5f5;
}

.app-main {
  flex: 1;
  padding: 24px;
  background: #f1f5f9;
}
</style>
