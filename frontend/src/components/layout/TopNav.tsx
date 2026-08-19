import { cn } from '@/utils/cn';
import { useStore } from '@/app/store';
import { AIAgentStatus } from '@/components/ai';
import { Avatar } from '@/components/ui';
import { Input } from '@/components/ui/Input';
import { Bell, Menu, Search } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@/routes/routes';

interface TopNavProps {
  onMenuClick?: () => void;
}

export function TopNav({ onMenuClick }: TopNavProps) {
  const { agentStatus, user, sidebarCollapsed } = useStore();
  const [notificationCount] = useState(2);
  const navigate = useNavigate();

  return (
    <header
      className={cn(
        'fixed top-0 right-0 z-20 flex items-center justify-between gap-4',
        'h-16 px-4 sm:px-6',
        'bg-[#111827] border-b border-[rgba(255,255,255,0.06)] backdrop-blur-md',
        'transition-all duration-300',
        'left-0',
        sidebarCollapsed ? 'lg:left-16' : 'lg:left-60'
      )}
    >
      {/* Left: Mobile hamburger & Search bar */}
      <div className="flex items-center gap-3 flex-1 max-w-xl">
        <button
          className="lg:hidden flex items-center justify-center w-9 h-9 rounded-lg text-[#94A3B8] hover:text-[#F1F5F9] hover:bg-[rgba(255,255,255,0.06)] transition-all flex-shrink-0 cursor-pointer"
          onClick={onMenuClick}
          aria-label="Open navigation menu"
        >
          <Menu size={20} />
        </button>

        <div className="w-full">
          <Input
            variant="filled"
            placeholder="Search for jobs, skills, companies..."
            leftIcon={<Search size={15} />}
            className="h-9 text-xs sm:text-sm bg-[#1E293B] border-[rgba(148,163,184,0.12)]"
          />
        </div>
      </div>

      {/* Right section: notifications, agent status pill, user avatar */}
      <div className="flex items-center gap-2.5 sm:gap-3.5 ml-auto flex-shrink-0">
        {/* Notification bell */}
        <button
          onClick={() => navigate(ROUTES.INTERVENTIONS)}
          className="relative flex items-center justify-center w-9 h-9 rounded-lg text-[#94A3B8] hover:text-[#F1F5F9] hover:bg-[rgba(255,255,255,0.06)] transition-all cursor-pointer"
          title="Notifications & Interventions"
        >
          <Bell size={18} />
          {notificationCount > 0 && (
            <span className="absolute 1 top-1.5 right-1.5 w-2 h-2 bg-[#EF4444] rounded-full shadow-[0_0_8px_#EF4444]" />
          )}
        </button>

        {/* AI Agent Status Pill (clickable to go to control center) */}
        <button
          onClick={() => navigate(ROUTES.AGENT)}
          className="cursor-pointer hover:opacity-90 transition-opacity"
          title="AI Agent Control Center"
        >
          <AIAgentStatus status={agentStatus} variant="pill" showLabel={true} className="hidden sm:inline-flex" />
          <AIAgentStatus status={agentStatus} variant="dot" className="sm:hidden" />
        </button>

        {/* Avatar */}
        {user && (
          <button
            onClick={() => navigate(ROUTES.PROFILE)}
            className="flex items-center gap-2 rounded-full hover:ring-2 hover:ring-[#2563EB] p-0.5 transition-all cursor-pointer"
            title="Candidate Profile"
          >
            <Avatar name={user.name} size="sm" />
          </button>
        )}
      </div>
    </header>
  );
}
