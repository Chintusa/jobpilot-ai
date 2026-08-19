import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ROUTES } from './routes';
import { LoadingState } from '@/components/feedback/index';
import { ProtectedRoute } from '@/components/layout/ProtectedRoute';
import { AdminRoute } from '@/components/layout/AdminRoute';
import LoginPage, { RegisterPage } from '@/pages/AuthPages';

const LandingPage = lazy(() => import('@/pages/LandingPage'));
const DashboardPage = lazy(() => import('@/pages/DashboardPage'));
const JobsPage = lazy(() => import('@/pages/JobsPage'));
const JobDetailPage = lazy(() => import('@/pages/JobDetailPage'));
const AgentControlCenterPage = lazy(() => import('@/pages/AgentControlCenterPage'));
const ApplicationAgentPage = lazy(() => import('@/pages/ApplicationAgentPage'));
const ApplicationsPage = lazy(() => import('@/pages/ApplicationsPage'));
const InterventionsPage = lazy(() => import('@/pages/InterventionsPage'));
const ProfilePage = lazy(() => import('@/pages/ProfilePage'));
const ResumePage = lazy(() => import('@/pages/ResumePage'));
const AnalyticsPage = lazy(() => import('@/pages/AnalyticsPage'));
const SettingsPage = lazy(() => import('@/pages/SettingsPage'));
const AdminDashboardPage = lazy(() => import('@/pages/admin/AdminDashboardPage'));

function SuspenseWrapper({ children }: { children: React.ReactNode }) {
  return (
    <Suspense fallback={<LoadingState message="Loading page..." className="min-h-screen" />}>
      {children}
    </Suspense>
  );
}

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Routes */}
        <Route path={ROUTES.LANDING} element={<SuspenseWrapper><LandingPage /></SuspenseWrapper>} />
        <Route path={ROUTES.LOGIN} element={<LoginPage />} />
        <Route path={ROUTES.REGISTER} element={<RegisterPage />} />

        {/* Protected Authenticated Routes */}
        <Route path={ROUTES.DASHBOARD} element={<ProtectedRoute><SuspenseWrapper><DashboardPage /></SuspenseWrapper></ProtectedRoute>} />
        <Route path={ROUTES.JOBS} element={<ProtectedRoute><SuspenseWrapper><JobsPage /></SuspenseWrapper></ProtectedRoute>} />
        <Route path={ROUTES.JOB_DETAIL} element={<ProtectedRoute><SuspenseWrapper><JobDetailPage /></SuspenseWrapper></ProtectedRoute>} />
        <Route path={ROUTES.AGENT} element={<ProtectedRoute><SuspenseWrapper><AgentControlCenterPage /></SuspenseWrapper></ProtectedRoute>} />
        <Route path={ROUTES.AGENT_APPLY} element={<ProtectedRoute><SuspenseWrapper><ApplicationAgentPage /></SuspenseWrapper></ProtectedRoute>} />
        <Route path={ROUTES.APPLICATIONS} element={<ProtectedRoute><SuspenseWrapper><ApplicationsPage /></SuspenseWrapper></ProtectedRoute>} />
        <Route path={ROUTES.INTERVENTIONS} element={<ProtectedRoute><SuspenseWrapper><InterventionsPage /></SuspenseWrapper></ProtectedRoute>} />
        <Route path={ROUTES.PROFILE} element={<ProtectedRoute><SuspenseWrapper><ProfilePage /></SuspenseWrapper></ProtectedRoute>} />
        <Route path={ROUTES.RESUME} element={<ProtectedRoute><SuspenseWrapper><ResumePage /></SuspenseWrapper></ProtectedRoute>} />
        <Route path={ROUTES.ANALYTICS} element={<ProtectedRoute><SuspenseWrapper><AnalyticsPage /></SuspenseWrapper></ProtectedRoute>} />
        <Route path={ROUTES.SETTINGS} element={<ProtectedRoute><SuspenseWrapper><SettingsPage /></SuspenseWrapper></ProtectedRoute>} />
        
        {/* Admin Route */}
        <Route path={ROUTES.ADMIN} element={<AdminRoute><SuspenseWrapper><AdminDashboardPage /></SuspenseWrapper></AdminRoute>} />

        {/* Fallback */}
        <Route path="*" element={<Navigate to={ROUTES.DASHBOARD} replace />} />
      </Routes>
    </BrowserRouter>
  );
}
