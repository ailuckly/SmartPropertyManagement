import { defineStore } from 'pinia';
import api from '../api/http';

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
    async login(payload) {
      this.loading = true;
      try {
        const { data } = await api.post('/auth/login', payload);
        this.user = data.user;
        this.initialized = true;
        return data;
      } finally {
        this.loading = false;
      }
    },
    async register(payload) {
      this.loading = true;
      try {
        const { data } = await api.post('/auth/register', payload);
        this.user = data.user;
        this.initialized = true;
        return data;
      } finally {
        this.loading = false;
      }
    },
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
    async logout() {
      try {
        await api.post('/auth/logout');
      } catch (error) {
        // ignore network errors during logout
      } finally {
        this.user = null;
        this.initialized = true;
      }
    }
  }
});
