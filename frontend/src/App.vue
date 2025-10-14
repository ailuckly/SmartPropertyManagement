<template>
  <el-container class="app-container">
    <!-- Top header -->
    <el-header class="app-header">
      <div class="logo-group">
        <img src="@/assets/logo.svg" alt="logo" />
        <span>智慧物业管理平台</span>
      </div>
      <div class="header-actions">
        <template v-if="isAuthenticated">
          <el-input
            v-model="searchText"
            placeholder="搜索（预留）"
            :prefix-icon="Search"
            style="width: 200px"
            clearable
          />
          <NotificationBell ref="notificationBell" />
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-icon><User /></el-icon>
              <span>{{ user?.username }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- Sidebar for authenticated users -->
      <el-aside v-if="isAuthenticated" width="200px" class="app-aside">
        <el-menu
          :default-active="activeMenu"
          :router="true"
          background-color="#ffffff"
          text-color="#303133"
          active-text-color="#409EFF"
        >
          <!-- 通用菜单 -->
          <el-menu-item index="/">
            <el-icon><DataLine /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-divider style="margin: 8px 0" />
          
          <!-- 管理员菜单 -->
          <template v-if="isAdmin">
            <div class="menu-group-title">系统管理</div>
            <el-menu-item index="/properties">
              <el-icon><OfficeBuilding /></el-icon>
              <span>物业管理</span>
            </el-menu-item>
            <el-menu-item index="/leases">
              <el-icon><Document /></el-icon>
              <span>租约管理</span>
            </el-menu-item>
            <el-menu-item index="/maintenance">
              <el-icon><Tools /></el-icon>
              <span>维修管理</span>
            </el-menu-item>
            <el-menu-item index="/payments">
              <el-icon><Wallet /></el-icon>
              <span>财务管理</span>
            </el-menu-item>
            <el-menu-item index="/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
          </template>
          
          <!-- 业主菜单 -->
          <template v-else-if="isOwner">
            <div class="menu-group-title">物业运营</div>
            <el-menu-item index="/properties">
              <el-icon><OfficeBuilding /></el-icon>
              <span>我的物业</span>
            </el-menu-item>
            <el-menu-item index="/leases">
              <el-icon><Document /></el-icon>
              <span>租约管理</span>
            </el-menu-item>
            <el-menu-item index="/maintenance">
              <el-icon><Tools /></el-icon>
              <span>维修工单</span>
            </el-menu-item>
            <el-menu-item index="/payments">
              <el-icon><Wallet /></el-icon>
              <span>收入记录</span>
            </el-menu-item>
          </template>
          
          <!-- 租户菜单 -->
          <template v-else-if="isTenant">
            <div class="menu-group-title">租房服务</div>
            <el-menu-item index="/properties">
              <el-icon><OfficeBuilding /></el-icon>
              <span>浏览物业</span>
            </el-menu-item>
            <el-menu-item index="/leases">
              <el-icon><Document /></el-icon>
              <span>我的租约</span>
            </el-menu-item>
            <el-menu-item index="/maintenance">
              <el-icon><Tools /></el-icon>
              <span>维修申请</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- Main content -->
      <el-main class="app-main">
        <div v-if="isAuthenticated" class="page-header">
          <el-breadcrumb :separator-icon="ArrowRight">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="pageTitle">{{ pageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
          <h1 class="page-title">{{ pageTitle }}</h1>
        </div>
        <NotifyHost />
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue';
import { RouterView, useRoute, useRouter } from 'vue-router';
import { useAuthStore } from './stores/auth';
import NotifyHost from './components/NotifyHost.vue';
import NotificationBell from './components/NotificationBell.vue';
import {
  Search,
  User,
  ArrowDown,
  SwitchButton,
  DataLine,
  OfficeBuilding,
  Tools,
  Document,
  Wallet,
  ArrowRight
} from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

const searchText = ref('');
const notificationBell = ref(null);
const user = computed(() => authStore.user);
const isAuthenticated = computed(() => authStore.isAuthenticated);
const isAdmin = computed(() => authStore.hasAnyRole(['ROLE_ADMIN']));
const isOwner = computed(() => authStore.hasAnyRole(['ROLE_OWNER']));
const isTenant = computed(() => authStore.hasAnyRole(['ROLE_TENANT']));
const isOwnerOrAdmin = computed(() => authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']));
const pageTitle = computed(() => route.meta?.title ?? '');
const activeMenu = computed(() => route.path);

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile');
  } else if (command === 'logout') {
    authStore.logout();
    ElMessage.success('已退出登录');
    router.push('/login');
  }
};
</script>

<style scoped>
.app-container {
  min-height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.logo-group {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}

.logo-group img {
  width: 32px;
  height: 32px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
  color: #606266;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.main-container {
  height: calc(100vh - 60px);
}

.app-aside {
  background-color: #ffffff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
}

.app-main {
  background-color: #f5f7fa;
  overflow-y: auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 12px 0 0 0;
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
}

:deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
}

.menu-group-title {
  padding: 12px 20px 8px;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
</style>
