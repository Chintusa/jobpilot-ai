import { cn } from '@/utils/cn';
import type { ReactNode } from 'react';
import { InboxIcon, AlertTriangle, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Spinner } from '@/components/ui';

// ============================================================
// EmptyState
// ============================================================
interface EmptyStateProps {
  icon?: ReactNode;
  title: string;
  description?: string;
  action?: { label: string; onClick: () => void };
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const emptySizes = {
  sm: { icon: 32, title: 'text-base', desc: 'text-sm', wrapper: 'py-8' },
  md: { icon: 48, title: 'text-lg', desc: 'text-sm', wrapper: 'py-12' },
  lg: { icon: 64, title: 'text-xl', desc: 'text-base', wrapper: 'py-16' },
};

export function EmptyState({ icon, title, description, action, size = 'md', className }: EmptyStateProps) {
  const s = emptySizes[size];
  return (
    <div className={cn('flex flex-col items-center justify-center text-center gap-3', s.wrapper, className)}>
      <div className="text-[#64748B] opacity-60">
        {icon || <InboxIcon size={s.icon} strokeWidth={1.5} />}
      </div>
      <div>
        <h3 className={cn('font-semibold text-[#F1F5F9] mb-1', s.title)}>{title}</h3>
        {description && (
          <p className={cn('text-[#64748B] max-w-sm', s.desc)}>{description}</p>
        )}
      </div>
      {action && (
        <Button variant="secondary" size="md" onClick={action.onClick}>
          {action.label}
        </Button>
      )}
    </div>
  );
}

// ============================================================
// LoadingState
// ============================================================
interface LoadingStateProps {
  message?: string;
  className?: string;
}

export function LoadingState({ message = 'Loading...', className }: LoadingStateProps) {
  return (
    <div className={cn('flex flex-col items-center justify-center gap-3 py-12', className)}>
      <Spinner size="lg" />
      <p className="text-sm text-[#64748B]">{message}</p>
    </div>
  );
}

// ============================================================
// Skeleton
// ============================================================
interface SkeletonProps {
  className?: string;
  lines?: number;
}

export function Skeleton({ className }: { className?: string }) {
  return (
    <div
      className={cn(
        'bg-[#243047] rounded animate-pulse',
        className
      )}
    />
  );
}

export function SkeletonCard({ lines = 3 }: SkeletonProps) {
  return (
    <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-xl p-6 space-y-3">
      <Skeleton className="h-5 w-1/3" />
      <Skeleton className="h-4 w-1/2" />
      {Array.from({ length: lines }).map((_, i) => (
        <Skeleton key={i} className={`h-3 w-${i === lines - 1 ? '2/3' : 'full'}`} />
      ))}
    </div>
  );
}

// ============================================================
// ErrorState
// ============================================================
interface ErrorStateProps {
  title?: string;
  description?: string;
  onRetry?: () => void;
  className?: string;
}

export function ErrorState({
  title = 'Something went wrong',
  description = 'We could not load this content. Please try again.',
  onRetry,
  className,
}: ErrorStateProps) {
  return (
    <div className={cn('flex flex-col items-center justify-center gap-4 py-12 text-center', className)}>
      <div className="w-12 h-12 rounded-full bg-[rgba(239,68,68,0.12)] flex items-center justify-center">
        <AlertTriangle size={24} className="text-[#EF4444]" />
      </div>
      <div>
        <h3 className="font-semibold text-[#F1F5F9] mb-1">{title}</h3>
        <p className="text-sm text-[#64748B] max-w-sm">{description}</p>
      </div>
      {onRetry && (
        <Button
          variant="secondary"
          size="md"
          leftIcon={<RefreshCw size={14} />}
          onClick={onRetry}
        >
          Retry
        </Button>
      )}
    </div>
  );
}
