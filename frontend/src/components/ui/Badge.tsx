import { cn } from '@/utils/cn';
import type { ApplicationStatus } from '@/types';
import type { ReactNode } from 'react';

type BadgeVariant = 'status' | 'skill' | 'match-strong' | 'match-good' | 'match-weak' | 'info' | 'label';

interface BadgeProps {
  variant?: BadgeVariant;
  status?: ApplicationStatus;
  children: ReactNode;
  icon?: ReactNode;
  className?: string;
}

const statusStyles: Record<ApplicationStatus, string> = {
  DISCOVERED: 'bg-[rgba(100,116,139,0.15)] text-[#94A3B8] border border-[rgba(100,116,139,0.3)]',
  MATCHED: 'bg-[rgba(16,185,129,0.15)] text-[#10B981] border border-[rgba(16,185,129,0.3)]',
  PREPARING: 'bg-[rgba(245,158,11,0.15)] text-[#F59E0B] border border-[rgba(245,158,11,0.3)]',
  PENDING_REVIEW: 'bg-[rgba(245,158,11,0.15)] text-[#F59E0B] border border-[rgba(245,158,11,0.3)]',
  SUBMITTED: 'bg-[rgba(37,99,235,0.15)] text-[#60A5FA] border border-[rgba(37,99,235,0.3)]',
  INTERVIEWING: 'bg-[rgba(139,92,246,0.15)] text-[#A78BFA] border border-[rgba(139,92,246,0.3)]',
  OFFERED: 'bg-[rgba(16,185,129,0.20)] text-[#34D399] border border-[rgba(16,185,129,0.4)]',
  REJECTED: 'bg-[rgba(239,68,68,0.15)] text-[#F87171] border border-[rgba(239,68,68,0.3)]',
  WITHDRAWN: 'bg-[rgba(100,116,139,0.15)] text-[#94A3B8] border border-[rgba(100,116,139,0.3)]',
};

const variantStyles: Record<BadgeVariant, string> = {
  skill: 'bg-[rgba(37,99,235,0.15)] text-[#93C5FD] border border-[rgba(37,99,235,0.25)]',
  'match-strong': 'bg-[rgba(16,185,129,0.15)] text-[#10B981] border border-[rgba(16,185,129,0.3)]',
  'match-good': 'bg-[rgba(59,130,246,0.15)] text-[#60A5FA] border border-[rgba(59,130,246,0.3)]',
  'match-weak': 'bg-[rgba(245,158,11,0.15)] text-[#F59E0B] border border-[rgba(245,158,11,0.3)]',
  info: 'bg-[rgba(6,182,212,0.15)] text-[#22D3EE] border border-[rgba(6,182,212,0.3)]',
  label: 'bg-[rgba(148,163,184,0.1)] text-[#94A3B8] border border-[rgba(148,163,184,0.2)]',
  status: '', // Determined by `status` prop
};

export function Badge({ variant = 'skill', status, children, icon, className }: BadgeProps) {
  const styleClass =
    variant === 'status' && status
      ? statusStyles[status]
      : variantStyles[variant];

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold tracking-wide',
        styleClass,
        className
      )}
    >
      {icon && <span className="flex-shrink-0">{icon}</span>}
      {children}
    </span>
  );
}

// Convenience: Status Badge
export function StatusBadge({ status }: { status: ApplicationStatus }) {
  const labels: Record<ApplicationStatus, string> = {
    DISCOVERED: 'Discovered',
    MATCHED: 'Matched',
    PREPARING: 'Preparing',
    PENDING_REVIEW: 'Needs Review',
    SUBMITTED: 'Submitted',
    INTERVIEWING: 'Interviewing',
    OFFERED: 'Offered',
    REJECTED: 'Rejected',
    WITHDRAWN: 'Withdrawn',
  };
  return (
    <Badge variant="status" status={status}>
      {labels[status]}
    </Badge>
  );
}

// Strong Match Badge
export function StrongMatchBadge() {
  return (
    <Badge variant="match-strong">
      ⚡ Strong Match
    </Badge>
  );
}
