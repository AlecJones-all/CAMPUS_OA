import { defineStore } from 'pinia';
import { getProfile, logout as logoutRequest } from '../api/http';

function readStoredArray(key: string): string[] {
  try {
    const value = JSON.parse(localStorage.getItem(key) ?? '[]');
    return Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : [];
  } catch {
    localStorage.removeItem(key);
    return [];
  }
}

export const useAppStore = defineStore('app', {
  state: () => ({
    appName: 'Campus OA',
    token: localStorage.getItem('campus_oa_token') ?? '',
    currentUserName: localStorage.getItem('campus_oa_username') ?? '',
    roles: readStoredArray('campus_oa_roles'),
    menus: readStoredArray('campus_oa_menus'),
    permissions: readStoredArray('campus_oa_permissions'),
    pendingCount: 0,
    initialized: false
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },
  actions: {
    setAuth(payload: { token: string; currentUserName: string; roles: string[]; menus: string[]; permissions: string[] }) {
      this.token = payload.token;
      this.currentUserName = payload.currentUserName;
      this.roles = payload.roles;
      this.menus = payload.menus;
      this.permissions = payload.permissions;
      this.initialized = true;
      localStorage.setItem('campus_oa_token', payload.token);
      localStorage.setItem('campus_oa_username', payload.currentUserName);
      localStorage.setItem('campus_oa_roles', JSON.stringify(payload.roles));
      localStorage.setItem('campus_oa_menus', JSON.stringify(payload.menus));
      localStorage.setItem('campus_oa_permissions', JSON.stringify(payload.permissions));
    },
    async ensureAuth() {
      if (this.initialized) {
        return;
      }
      if (!this.token) {
        this.initialized = true;
        return;
      }

      try {
        const response = await getProfile();
        this.currentUserName = response.data.profile.realName;
        this.roles = response.data.profile.roles;
        this.menus = response.data.menus;
        this.permissions = response.data.permissions;
        localStorage.setItem('campus_oa_username', this.currentUserName);
        localStorage.setItem('campus_oa_roles', JSON.stringify(this.roles));
        localStorage.setItem('campus_oa_menus', JSON.stringify(this.menus));
        localStorage.setItem('campus_oa_permissions', JSON.stringify(this.permissions));
      } catch {
        this.clearAuth();
      } finally {
        this.initialized = true;
      }
    },
    clearAuth() {
      this.token = '';
      this.currentUserName = '';
      this.roles = [];
      this.menus = [];
      this.permissions = [];
      this.initialized = true;
      localStorage.removeItem('campus_oa_token');
      localStorage.removeItem('campus_oa_username');
      localStorage.removeItem('campus_oa_roles');
      localStorage.removeItem('campus_oa_menus');
      localStorage.removeItem('campus_oa_permissions');
    },
    async logout() {
      try {
        if (this.token) {
          await logoutRequest();
        }
      } finally {
        this.clearAuth();
      }
    }
  }
});
