import { create } from 'zustand';
import type { AgentStatus, UserProfile } from '@/types';

interface AppState {
  sidebarCollapsed: boolean;
  setSidebarCollapsed: (collapsed: boolean) => void;
  toggleSidebar: () => void;

  agentStatus: AgentStatus;
  setAgentStatus: (status: AgentStatus) => void;

  user: UserProfile | null;
  setUser: (user: UserProfile | null) => void;

  accessToken: string | null;
  setAccessToken: (token: string | null) => void;
}

export const useStore = create<AppState>((set) => ({
  sidebarCollapsed: false,
  setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }),
  toggleSidebar: () => set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),

  agentStatus: 'ACTIVE',
  setAgentStatus: (status) => set({ agentStatus: status }),

  // Mock user for development
  user: {
    id: '1',
    name: 'Jhasaketan',
    email: 'jhasaketan@example.com',
  },
  setUser: (user) => set({ user }),

  accessToken: 'mock-token',
  setAccessToken: (token) => set({ accessToken: token }),
}));
