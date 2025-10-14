<template>
  <div class="app-container">
    <!-- Top header -->
    <header class="app-header">
      <div class="logo-group">
        <img src="@/assets/logo.svg" alt="logo" />
        <span>智慧物业管理平台</span>
      </div>
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

    <!-- Sidebar for authenticated users -->
    <aside v-if="isAuthenticated" class="sidebar">
      <div class="section-title">总览</div>
      <nav class="nav">
        <RouterLink to="/" class="nav-item">仪表盘</RouterLink>
      </nav>
      <div class="section-title">业务</div>
      <nav class="nav">
        <RouterLink to="/properties" class="nav-item">物业管理</RouterLink>
        <RouterLink v-if="isTenant" to="/maintenance" class="nav-item">维修申请</RouterLink>
        <RouterLink v-if="isOwnerOrAdmin" to="/maintenance" class="nav-item">维修工单</RouterLink>
        <RouterLink to="/leases" class="nav-item">租约</RouterLink>
        <RouterLink v-if="isOwnerOrAdmin" to="/payments" class="nav-item">收支记录</RouterLink>
      </nav>
    </aside>

    <!-- Main content -->
    <main :class="['app-main', isAuthenticated ? 'main-with-sidebar' : '']">
      <div v-if="isAuthenticated" class="container page-header">
        <nav aria-label="Breadcrumb">
          <RouterLink to="/" class="nav-link">首页</RouterLink>
          <span style="opacity:.6; padding: 0 6px;">/</span>
          <span>{{ pageTitle }}</span>
        </nav>
        <h1 style="margin:0; font-size:18px;">{{ pageTitle }}</h1>
      </div>
      <NotifyHost />
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { RouterLink, RouterView, useRoute } from 'vue-router';
import { useAuthStore } from './stores/auth';
import NotifyHost from './components/NotifyHost.vue';

const authStore = useAuthStore();
const route = useRoute();

const user = computed(() => authStore.user);
const isAuthenticated = computed(() => authStore.isAuthenticated);
const isOwnerOrAdmin = computed(() => authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']));
const isTenant = computed(() => authStore.hasAnyRole(['ROLE_TENANT']));
const pageTitle = computed(() => route.meta?.title ?? '');

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
  min-height: calc(100vh - 64px);
}
</style>
