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

// Clean up any stale dummy token from previous demo runs
const existingToken = localStorage.getItem('jobpilot_access_token');
const validInitialToken = existingToken && existingToken !== 'demo-jwt-token' ? existingToken : null;
if (existingToken === 'demo-jwt-token') {
  localStorage.removeItem('jobpilot_access_token');
  localStorage.removeItem('jobpilot_refresh_token');
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: validInitialToken,
  refreshToken: localStorage.getItem('jobpilot_refresh_token'),
  isAuthenticated: !!validInitialToken,
  isLoading: false,
  error: null,

  login: async (email, password) => {
    set({ isLoading: true, error: null });
    try {
      const response = await apiClient.post('/api/v1/auth/login', { email, password });
      const data = response.data?.data;
      if (data && data.accessToken) {
        localStorage.setItem('jobpilot_access_token', data.accessToken);
        if (data.refreshToken) {
          localStorage.setItem('jobpilot_refresh_token', data.refreshToken);
        }
        set({
          user: data.user,
          accessToken: data.accessToken,
          refreshToken: data.refreshToken || null,
          isAuthenticated: true,
          isLoading: false,
          error: null,
        });
        return true;
      }
      set({
        isLoading: false,
        error: 'Login failed: no access token returned by server',
      });
      return false;
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Invalid email or password';
      set({
        isLoading: false,
        isAuthenticated: false,
        accessToken: null,
        error: msg,
      });
      localStorage.removeItem('jobpilot_access_token');
      localStorage.removeItem('jobpilot_refresh_token');
      return false;
    }
  },

  register: async (name, email, password, phone) => {
    set({ isLoading: true, error: null });
    try {
      const response = await apiClient.post('/api/v1/auth/register', { name, email, password, phone });
      const data = response.data?.data;
      if (data && data.accessToken) {
        localStorage.setItem('jobpilot_access_token', data.accessToken);
        if (data.refreshToken) {
          localStorage.setItem('jobpilot_refresh_token', data.refreshToken);
        }
        set({
          user: data.user,
          accessToken: data.accessToken,
          refreshToken: data.refreshToken || null,
          isAuthenticated: true,
          isLoading: false,
          error: null,
        });
        return true;
      }
      set({
        isLoading: false,
        error: 'Registration succeeded but no token was provided',
      });
      return false;
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Registration failed';
      set({
        isLoading: false,
        isAuthenticated: false,
        accessToken: null,
        error: msg,
      });
      localStorage.removeItem('jobpilot_access_token');
      localStorage.removeItem('jobpilot_refresh_token');
      return false;
    }
  },

  logout: async () => {
    try {
      await apiClient.post('/api/v1/auth/logout');
    } catch {
      // ignore network errors on logout
    } finally {
      localStorage.removeItem('jobpilot_access_token');
      localStorage.removeItem('jobpilot_refresh_token');
      set({
        user: null,
        accessToken: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      });
    }
  },

  fetchCurrentUser: async () => {
    const token = localStorage.getItem('jobpilot_access_token');
    if (!token || token === 'demo-jwt-token') {
      set({ isAuthenticated: false, user: null });
      return;
    }
    try {
      const res = await apiClient.get('/api/v1/auth/me');
      if (res.data?.data) {
        set({ user: res.data.data, isAuthenticated: true });
      }
    } catch {
      // Invalid or expired token
      localStorage.removeItem('jobpilot_access_token');
      localStorage.removeItem('jobpilot_refresh_token');
      set({ user: null, accessToken: null, isAuthenticated: false });
    }
  },

  clearError: () => set({ error: null }),
}));
