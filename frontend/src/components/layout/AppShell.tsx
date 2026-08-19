import { cn } from '@/utils/cn';
import { useState, useEffect, type ReactNode } from 'react';
import { useLocation } from 'react-router-dom';
import { useStore } from '@/app/store';
import { Sidebar } from './Sidebar';
import { TopNav } from './TopNav';

interface AppShellProps {
  children: ReactNode;
}

export function AppShell({ children }: AppShellProps) {
  const { sidebarCollapsed } = useStore();
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);
  const location = useLocation();

  // Automatically close mobile sidebar on route change
  useEffect(() => {
    setMobileSidebarOpen(false);
  }, [location.pathname]);

  return (
    <div className="min-h-screen bg-[#0A0F1E]">
      <Sidebar
        mobileOpen={mobileSidebarOpen}
        onMobileClose={() => setMobileSidebarOpen(false)}
      />

      {/* Main content — offset by sidebar width ONLY on desktop (lg:) */}
      <div
        className={cn(
          'min-h-screen flex flex-col transition-all duration-300',
          sidebarCollapsed ? 'lg:pl-16' : 'lg:pl-60',
          'pl-0'
        )}
      >
        <TopNav onMenuClick={() => setMobileSidebarOpen(true)} />

        {/* Page content below topnav */}
        <main className="flex-1 pt-16 page-bg">
          <div className="p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto animate-[fade-in_0.25s_ease-in-out]">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}
