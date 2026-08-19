import { cn } from '@/utils/cn';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useStore } from '@/app/store';
import { AIAgentStatus } from '@/components/ai';
import { Avatar } from '@/components/ui';
import {
  LayoutDashboard, Bot, Search, FileText,
  User, BarChart3, Settings, ChevronLeft, ChevronRight, Zap,
  ChevronDown, BriefcaseBusiness
} from 'lucide-react';
import { ROUTES } from '@/routes/routes';
import { useState } from 'react';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  path: string;
  badge?: number;
  children?: { label: string; path: string }[];
}

const navItems: NavItem[] = [
  { label: 'Dashboard', icon: <LayoutDashboard size={18} />, path: ROUTES.DASHBOARD },
  { label: 'AI Job Agent', icon: <Bot size={18} />, path: ROUTES.AGENT },
  { label: 'Discover Jobs', icon: <Search size={18} />, path: ROUTES.JOBS },
  {
    label: 'Applications', icon: <BriefcaseBusiness size={18} />, path: ROUTES.APPLICATIONS,
    children: [
      { label: 'All Applications', path: ROUTES.APPLICATIONS },
      { label: 'Interventions', path: ROUTES.INTERVENTIONS },
    ],
  },
  { label: 'Resume', icon: <FileText size={18} />, path: ROUTES.RESUME },
  { label: 'Candidate Profile', icon: <User size={18} />, path: ROUTES.PROFILE },
  { label: 'Analytics', icon: <BarChart3 size={18} />, path: ROUTES.ANALYTICS },
];

interface SidebarProps {
  mobileOpen?: boolean;
  onMobileClose?: () => void;
}

export function Sidebar({ mobileOpen = false, onMobileClose }: SidebarProps) {
  const { sidebarCollapsed, toggleSidebar, agentStatus, user } = useStore();
  const location = useLocation();
  const navigate = useNavigate();
  const [expandedItem, setExpandedItem] = useState<string | null>('Applications');

  const isActive = (path: string) => {
    if (path === ROUTES.DASHBOARD) return location.pathname === path;
    return location.pathname.startsWith(path);
  };

  const handleNavClick = () => {
    if (onMobileClose) {
      onMobileClose();
    }
  };

  return (
    <>
      {/* Mobile backdrop */}
      {mobileOpen && (
        <div
          className="fixed inset-0 bg-[rgba(10,15,30,0.75)] backdrop-blur-sm z-30 lg:hidden"
          onClick={onMobileClose}
        />
      )}

      <aside
        className={cn(
          'fixed top-0 left-0 h-full z-40 flex flex-col',
          'bg-gradient-to-b from-[#0D1526] to-[#0A0F1E]',
          'border-r border-[rgba(255,255,255,0.06)]',
          'transition-all duration-300 ease-in-out',
          // Desktop collapsed/expanded
          'lg:translate-x-0',
          sidebarCollapsed ? 'lg:w-16' : 'lg:w-60',
          // Mobile: show/hide via translate
          mobileOpen ? 'translate-x-0 w-64 shadow-2xl' : '-translate-x-full lg:translate-x-0'
        )}
      >
        {/* Logo area */}
        <div className={cn(
          'flex items-center h-16 px-4 border-b border-[rgba(255,255,255,0.06)] flex-shrink-0',
          sidebarCollapsed ? 'justify-center' : 'justify-between'
        )}>
          {!sidebarCollapsed && (
            <div
              className="flex items-center gap-2.5 cursor-pointer"
              onClick={() => { navigate(ROUTES.DASHBOARD); handleNavClick(); }}
            >
              <div className="w-7 h-7 rounded-lg gradient-brand flex items-center justify-center flex-shrink-0 shadow-[0_0_12px_rgba(37,99,235,0.5)]">
                <Zap size={14} className="text-white" />
              </div>
              <span className="font-bold text-[15px] text-[#F1F5F9]">
                JobPilot <span className="text-[#3B82F6]">AI</span>
              </span>
            </div>
          )}
          {sidebarCollapsed && (
            <div
              className="w-7 h-7 rounded-lg gradient-brand flex items-center justify-center cursor-pointer shadow-[0_0_12px_rgba(37,99,235,0.5)]"
              onClick={() => { navigate(ROUTES.DASHBOARD); handleNavClick(); }}
            >
              <Zap size={14} className="text-white" />
            </div>
          )}
          {/* Toggle btn — desktop only */}
          <button
            onClick={toggleSidebar}
            className="hidden lg:flex items-center justify-center w-6 h-6 rounded-md text-[#64748B] hover:text-[#F1F5F9] hover:bg-[rgba(255,255,255,0.06)] transition-all flex-shrink-0 cursor-pointer"
            title={sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {sidebarCollapsed ? <ChevronRight size={14} /> : <ChevronLeft size={14} />}
          </button>
        </div>

        {/* Nav items */}
        <nav className="flex-1 overflow-y-auto py-4 px-2 space-y-1">
          {navItems.map((item) => {
            const active = isActive(item.path);
            const hasChildren = item.children && item.children.length > 0;
            const isExpanded = expandedItem === item.label;

            return (
              <div key={item.path}>
                {hasChildren && !sidebarCollapsed ? (
                  <button
                    onClick={() => setExpandedItem(isExpanded ? null : item.label)}
                    className={cn(
                      'w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium cursor-pointer',
                      'transition-all duration-120',
                      active
                        ? 'bg-[rgba(37,99,235,0.15)] text-[#F1F5F9] border-l-[3px] border-[#2563EB] pl-[calc(0.75rem-3px)]'
                        : 'text-[#94A3B8] hover:bg-[rgba(255,255,255,0.05)] hover:text-[#F1F5F9]'
                    )}
                  >
                    <span className={active ? 'text-[#2563EB]' : 'text-[#64748B]'}>
                      {item.icon}
                    </span>
                    <span className="flex-1 text-left">{item.label}</span>
                    <ChevronDown
                      size={14}
                      className={cn('transition-transform duration-200 text-[#64748B]', isExpanded && 'rotate-180')}
                    />
                  </button>
                ) : (
                  <NavLink
                    to={hasChildren ? item.children![0].path : item.path}
                    onClick={handleNavClick}
                    className={cn(
                      'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium',
                      'transition-all duration-120',
                      active
                        ? 'bg-[rgba(37,99,235,0.15)] text-[#F1F5F9] border-l-[3px] border-[#2563EB] pl-[calc(0.75rem-3px)]'
                        : 'text-[#94A3B8] hover:bg-[rgba(255,255,255,0.05)] hover:text-[#F1F5F9]',
                      sidebarCollapsed && 'justify-center px-0'
                    )}
                    title={sidebarCollapsed ? item.label : undefined}
                  >
                    <span className={active ? 'text-[#2563EB]' : 'text-[#64748B]'}>
                      {item.icon}
                    </span>
                    {!sidebarCollapsed && <span>{item.label}</span>}
                    {!sidebarCollapsed && item.badge && (
                      <span className="ml-auto bg-[#EF4444] text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">
                        {item.badge}
                      </span>
                    )}
                  </NavLink>
                )}

                {/* Sub items */}
                {hasChildren && isExpanded && !sidebarCollapsed && (
                  <div className="ml-8 mt-1 space-y-0.5 border-l border-[rgba(148,163,184,0.1)] pl-2">
                    {item.children!.map((child) => (
                      <NavLink
                        key={child.path}
                        to={child.path}
                        onClick={handleNavClick}
                        className={({ isActive }) =>
                          cn(
                            'block px-2.5 py-1.5 rounded-md text-xs font-medium transition-colors',
                            isActive
                              ? 'text-[#60A5FA] bg-[rgba(37,99,235,0.12)]'
                              : 'text-[#64748B] hover:text-[#94A3B8] hover:bg-[rgba(255,255,255,0.04)]'
                          )
                        }
                      >
                        {child.label}
                      </NavLink>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </nav>

        {/* Bottom: Settings + User */}
        <div className={cn(
          'border-t border-[rgba(255,255,255,0.06)] py-3 px-2 space-y-1 flex-shrink-0'
        )}>
          <NavLink
            to={ROUTES.SETTINGS}
            onClick={handleNavClick}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all',
                isActive
                  ? 'bg-[rgba(37,99,235,0.15)] text-[#F1F5F9]'
                  : 'text-[#94A3B8] hover:bg-[rgba(255,255,255,0.05)] hover:text-[#F1F5F9]',
                sidebarCollapsed && 'justify-center px-0'
              )
            }
          >
            <Settings size={18} className="text-[#64748B]" />
            {!sidebarCollapsed && <span>Settings</span>}
          </NavLink>

          {/* User area */}
          {!sidebarCollapsed && user && (
            <div
              onClick={() => { navigate(ROUTES.PROFILE); handleNavClick(); }}
              className="flex items-center gap-2.5 px-3 py-2.5 rounded-lg hover:bg-[rgba(255,255,255,0.04)] cursor-pointer transition-all"
            >
              <Avatar name={user.name} size="sm" />
              <div className="flex-1 min-w-0">
                <div className="text-xs font-medium text-[#F1F5F9] truncate">{user.name}</div>
                <AIAgentStatus status={agentStatus} variant="pill" showLabel={true}
                  className="mt-0.5 scale-90 origin-left" />
              </div>
            </div>
          )}
          {sidebarCollapsed && user && (
            <div
              className="flex justify-center py-1 cursor-pointer"
              onClick={() => { navigate(ROUTES.PROFILE); handleNavClick(); }}
            >
              <Avatar name={user.name} size="sm" />
            </div>
          )}
        </div>
      </aside>
    </>
  );
}
