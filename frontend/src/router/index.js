import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';

/**
 * Central application router. Each route declares the roles required to access it;
 * the global guard consults the auth store to enforce those constraints.
 */
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { requiresAuth: true, title: '仪表盘' }
    },
    {
      path: '/properties',
      name: 'properties',
      component: () => import('../views/PropertiesView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_OWNER', 'ROLE_ADMIN', 'ROLE_TENANT'], title: '物业管理' }
    },
    {
      path: '/maintenance',
      name: 'maintenance',
      component: () => import('../views/MaintenanceView.vue'),
      meta: { requiresAuth: true, title: '维修' }
    },
    {
      path: '/leases',
      name: 'leases',
      component: () => import('../views/LeasesView.vue'),
      meta: { requiresAuth: true, title: '租约' }
    },
    {
      path: '/payments',
      name: 'payments',
      component: () => import('../views/PaymentsView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_OWNER', 'ROLE_ADMIN'], title: '收支记录' }
    },
    {
      path: '/notifications',
      name: 'notifications',
      component: () => import('../views/NotificationsView.vue'),
      meta: { requiresAuth: true, title: '通知中心' }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true, title: '个人资料' }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { guestOnly: true, title: '登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { guestOnly: true, title: '注册' }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
      meta: { title: '未找到' }
    }
  ]
});

/**
 * Ensures the user is loaded before navigating and applies guest/auth and role-based route protection.
 */
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  // 仅在访问受保护页面时阻塞等待会话恢复；
  // 对于登录/注册等 guestOnly 页面，不阻塞导航，避免初次进入时的空白等待
  if (!authStore.initialized) {
    if (to.meta.requiresAuth) {
      try {
        await authStore.fetchCurrentUser();
      } catch (_) {
        // 忽略错误，继续根据 isAuthenticated 判定
      }
    } else {
      // 非阻塞预取（不影响导航）
      authStore.fetchCurrentUser().catch(() => {});
    }
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return next({ name: 'login', query: { redirect: to.fullPath } });
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return next({ name: 'dashboard' });
  }

  if (to.meta.roles && !authStore.hasAnyRole(to.meta.roles)) {
    return next({ name: 'dashboard' });
  }

  return next();
});

export default router;
