import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/app/authStore';
import { ROUTES } from '@/routes/routes';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, accessToken } = useAuthStore();
  const location = useLocation();

  // Allow authenticated users or active token
  const isAuthed = isAuthenticated || !!accessToken || !!localStorage.getItem('jobpilot_access_token');

  if (!isAuthed) {
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <>{children}</>;
};
