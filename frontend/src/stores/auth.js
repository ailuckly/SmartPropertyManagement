import { defineStore } from 'pinia';
import api from '../api/http';
import { notify } from '../utils/notify';

/**
 * Global authentication state. Keeps track of the logged-in user and hides API details from components.
 * The store also restores sessions on refresh and provides helpers for role-based checks.
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    initialized: false,
    loading: false
  }),
  getters: {
    isAuthenticated: (state) => !!state.user,
    roles: (state) => state.user?.roles ?? [],
    hasRole() {
      return (role) => this.roles.includes(role);
    },
    hasAnyRole() {
      return (roles = []) => roles.some((role) => this.roles.includes(role));
    }
  },
  actions: {
    /**
     * Attempts username/password login and stores the returned user profile on success.
     */
    async login(payload) {
      this.loading = true;
      try {
        const { data } = await api.post('/auth/login', payload);
        this.user = data.user;
        this.initialized = true;
        notify('登录成功', 'success');
        return data;
      } finally {
        this.loading = false;
      }
    },
    /**
     * Creates a new account and logs in immediately after successful registration.
     */
    async register(payload) {
      this.loading = true;
      try {
        const { data } = await api.post('/auth/register', payload);
        this.user = data.user;
        this.initialized = true;
        notify('注册成功，已自动登录', 'success');
        return data;
      } finally {
        this.loading = false;
      }
    },
    /**
     * Bootstrap helper used by the router guard to resume sessions via the /auth/me endpoint.
     */
    async fetchCurrentUser() {
      try {
        const { data } = await api.get('/auth/me');
        this.user = data;
      } catch (error) {
        this.user = null;
      } finally {
        this.initialized = true;
      }
    },
    /**
     * Clears backend cookies and resets local state. Network failures are swallowed to keep UI consistent.
     */
    async logout() {
      try {
        await api.post('/auth/logout');
        notify('您已退出登录', 'info');
      } catch (error) {
        // ignore network errors during logout
        console.warn('Logout request failed, but clearing local state anyway:', error);
      } finally {
        this.user = null;
        this.initialized = true;
        
        // Force a page reload to completely clear any cached state
        setTimeout(() => {
          window.location.reload();
        }, 100);
      }
    }
  }
});
