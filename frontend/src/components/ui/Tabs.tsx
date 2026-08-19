import { cn } from '@/utils/cn';
import type { ReactNode } from 'react';

interface TabsProps {
  tabs: { label: string; value: string; badge?: number }[];
  activeTab: string;
  onChange: (value: string) => void;
  variant?: 'line' | 'pill';
  className?: string;
}

export function Tabs({ tabs, activeTab, onChange, variant = 'line', className }: TabsProps) {
  return (
    <div
      className={cn(
        'flex',
        variant === 'line'
          ? 'border-b border-[rgba(148,163,184,0.08)] gap-0'
          : 'gap-1 p-1 bg-[#1E293B] rounded-lg',
        className
      )}
    >
      {tabs.map((tab) => (
        <button
          key={tab.value}
          onClick={() => onChange(tab.value)}
          className={cn(
            'flex items-center gap-2 text-sm font-medium transition-all duration-150 cursor-pointer',
            variant === 'line'
              ? cn(
                  'px-4 py-3 border-b-2 -mb-px',
                  activeTab === tab.value
                    ? 'border-[#2563EB] text-[#F1F5F9]'
                    : 'border-transparent text-[#64748B] hover:text-[#94A3B8]'
                )
              : cn(
                  'px-3 py-1.5 rounded-md',
                  activeTab === tab.value
                    ? 'bg-[#243047] text-[#F1F5F9]'
                    : 'text-[#64748B] hover:text-[#94A3B8]'
                )
          )}
        >
          {tab.label}
          {tab.badge !== undefined && tab.badge > 0 && (
            <span className="bg-[#2563EB] text-white text-[10px] font-semibold px-1.5 py-0.5 rounded-full min-w-[18px] text-center">
              {tab.badge}
            </span>
          )}
        </button>
      ))}
    </div>
  );
}

interface TabContentProps {
  children: ReactNode;
  className?: string;
}

export function TabContent({ children, className }: TabContentProps) {
  return (
    <div className={cn('animate-[fade-in_0.2s_ease-in-out]', className)}>
      {children}
    </div>
  );
}
