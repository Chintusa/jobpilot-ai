import { Navigate } from 'react-router-dom';
import { useAuthStore } from '@/app/authStore';
import { ROUTES } from '@/routes/routes';

export function AdminRoute({ children }: { children: React.ReactNode }) {
  const { user, isAuthenticated } = useAuthStore();

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  // Check if user has ADMIN role
  if (user?.role !== 'ROLE_ADMIN') {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  return <>{children}</>;
}
