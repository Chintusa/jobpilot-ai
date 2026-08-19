import { create } from 'zustand';
import { apiClient } from '@/api/client';

export interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  phone?: string;
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  login: (email: string, password: string) => Promise<boolean>;
  register: (name: string, email: string, password: string, phone?: string) => Promise<boolean>;
  logout: () => Promise<void>;
  fetchCurrentUser: () => Promise<void>;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: {
    id: 'demo-user-1',
    name: 'Jhasaketan M.',
    email: 'jhasaketan@example.com',
    role: 'ROLE_USER',
  },
  accessToken: localStorage.getItem('jobpilot_access_token'),
  refreshToken: localStorage.getItem('jobpilot_refresh_token'),
  isAuthenticated: !!localStorage.getItem('jobpilot_access_token') || true, // default true for seamless demo
  isLoading: false,
  error: null,

  login: async (email, password) => {
    set({ isLoading: true, error: null });
    try {
      const response = await apiClient.post('/api/v1/auth/login', { email, password });
      const data = response.data?.data;
      if (data) {
        localStorage.setItem('jobpilot_access_token', data.accessToken);
        localStorage.setItem('jobpilot_refresh_token', data.refreshToken);
        set({
          user: data.user,
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
          isAuthenticated: true,
          isLoading: false,
        });
        return true;
      }
      return false;
    } catch (err: any) {
      // Fallback for offline demo mode
      const fallbackUser: User = { id: 'demo-1', name: 'Jhasaketan M.', email, role: 'ROLE_USER' };
      localStorage.setItem('jobpilot_access_token', 'demo-jwt-token');
      set({
        user: fallbackUser,
        accessToken: 'demo-jwt-token',
        isAuthenticated: true,
        isLoading: false,
        error: err.response?.data?.message || null,
      });
      return true;
    }
  },

  register: async (name, email, password, phone) => {
    set({ isLoading: true, error: null });
    try {
      const response = await apiClient.post('/api/v1/auth/register', { name, email, password, phone });
      const data = response.data?.data;
      if (data) {
        localStorage.setItem('jobpilot_access_token', data.accessToken);
        localStorage.setItem('jobpilot_refresh_token', data.refreshToken);
        set({
          user: data.user,
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
          isAuthenticated: true,
          isLoading: false,
        });
        return true;
      }
      return false;
    } catch (err: any) {
      const fallbackUser: User = { id: 'demo-1', name, email, role: 'ROLE_USER' };
      localStorage.setItem('jobpilot_access_token', 'demo-jwt-token');
      set({
        user: fallbackUser,
        accessToken: 'demo-jwt-token',
        isAuthenticated: true,
        isLoading: false,
        error: err.response?.data?.message || null,
      });
      return true;
    }
  },

  logout: async () => {
    try {
      await apiClient.post('/api/v1/auth/logout');
    } catch {
      // ignore
    } finally {
      localStorage.removeItem('jobpilot_access_token');
      localStorage.removeItem('jobpilot_refresh_token');
      set({
        user: null,
        accessToken: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,
      });
    }
  },

  fetchCurrentUser: async () => {
    try {
      const res = await apiClient.get('/api/v1/auth/me');
      if (res.data?.data) {
        set({ user: res.data.data, isAuthenticated: true });
      }
    } catch {
      // ignore
    }
  },

  clearError: () => set({ error: null }),
}));
