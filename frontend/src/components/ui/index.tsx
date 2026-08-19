import { cn } from '@/utils/cn';
import type { ReactNode } from 'react';

// ============================================================
// ProgressBar
// ============================================================
interface ProgressBarProps {
  value: number;
  max?: number;
  label?: string;
  showValue?: boolean;
  color?: 'brand' | 'success' | 'warning' | 'error' | 'cyan';
  size?: 'sm' | 'md';
  animated?: boolean;
  className?: string;
}

const progressColors = {
  brand: 'bg-[#2563EB]',
  success: 'bg-[#10B981]',
  warning: 'bg-[#F59E0B]',
  error: 'bg-[#EF4444]',
  cyan: 'bg-[#06B6D4]',
};

export function ProgressBar({
  value,
  max = 100,
  label,
  showValue = false,
  color = 'brand',
  size = 'md',
  animated = true,
  className,
}: ProgressBarProps) {
  const pct = Math.min(100, Math.max(0, (value / max) * 100));

  return (
    <div className={cn('w-full', className)}>
      {(label || showValue) && (
        <div className="flex justify-between items-center mb-1.5">
          {label && <span className="text-xs text-[#94A3B8]">{label}</span>}
          {showValue && <span className="text-xs font-semibold text-[#F1F5F9]">{value}%</span>}
        </div>
      )}
      <div
        className={cn(
          'w-full bg-[#243047] rounded-full overflow-hidden',
          size === 'sm' ? 'h-1.5' : 'h-2'
        )}
      >
        <div
          className={cn(
            'h-full rounded-full',
            progressColors[color],
            animated && 'transition-all duration-700 ease-out'
          )}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  );
}

// ============================================================
// Avatar
// ============================================================
interface AvatarProps {
  src?: string;
  name?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg';
  className?: string;
}

const avatarSizes = {
  xs: 'w-6 h-6 text-[10px]',
  sm: 'w-8 h-8 text-xs',
  md: 'w-10 h-10 text-sm',
  lg: 'w-12 h-12 text-base',
};

export function Avatar({ src, name, size = 'md', className }: AvatarProps) {
  const initials = name
    ? name.split(' ').map((n) => n[0]).join('').slice(0, 2).toUpperCase()
    : '?';

  return (
    <div
      className={cn(
        'rounded-full flex items-center justify-center overflow-hidden flex-shrink-0',
        'bg-gradient-to-br from-[#2563EB] to-[#7C3AED] text-white font-semibold',
        avatarSizes[size],
        className
      )}
    >
      {src ? (
        <img src={src} alt={name} className="w-full h-full object-cover" />
      ) : (
        initials
      )}
    </div>
  );
}

// ============================================================
// Spinner
// ============================================================
interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export function Spinner({ size = 'md', className }: SpinnerProps) {
  const sizes = { sm: 'w-4 h-4', md: 'w-6 h-6', lg: 'w-8 h-8' };
  return (
    <div
      className={cn(
        'border-2 border-[rgba(148,163,184,0.2)] border-t-[#2563EB] rounded-full animate-spin',
        sizes[size],
        className
      )}
    />
  );
}

// ============================================================
// Toggle
// ============================================================
interface ToggleProps {
  checked: boolean;
  onChange: (value: boolean) => void;
  label?: string;
  size?: 'sm' | 'md';
  className?: string;
}

export function Toggle({ checked, onChange, label, size = 'md', className }: ToggleProps) {
  return (
    <label className={cn('flex items-center gap-2 cursor-pointer', className)}>
      {label && <span className="text-sm text-[#F1F5F9]">{label}</span>}
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        onClick={() => onChange(!checked)}
        className={cn(
          'relative inline-flex flex-shrink-0 rounded-full transition-all duration-200',
          size === 'sm' ? 'w-8 h-4' : 'w-10 h-6',
          checked ? 'bg-[#2563EB]' : 'bg-[#243047]',
          'border',
          checked ? 'border-[rgba(37,99,235,0.5)]' : 'border-[rgba(148,163,184,0.2)]'
        )}
      >
        <span
          className={cn(
            'absolute top-0.5 inline-block rounded-full bg-white shadow transition-transform duration-200',
            size === 'sm' ? 'w-3 h-3' : 'w-5 h-5',
            checked
              ? size === 'sm' ? 'translate-x-4' : 'translate-x-4'
              : 'translate-x-0.5'
          )}
        />
      </button>
    </label>
  );
}

// ============================================================
// Divider
// ============================================================
export function Divider({ className }: { className?: string }) {
  return (
    <hr className={cn('border-0 border-t border-[rgba(148,163,184,0.08)]', className)} />
  );
}

// ============================================================
// StatCard
// ============================================================
interface StatCardProps {
  label: string;
  value: number | string;
  icon?: ReactNode;
  accent?: boolean;
  className?: string;
}

export function StatCard({ label, value, icon, accent = false, className }: StatCardProps) {
  return (
    <div
      className={cn(
        'bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-xl p-5',
        'flex flex-col gap-2',
        className
      )}
    >
      <div className="flex items-center justify-between">
        <span className="text-sm text-[#94A3B8]">{label}</span>
        {icon && <span className="text-[#64748B]">{icon}</span>}
      </div>
      <div
        className={cn(
          'text-3xl font-bold',
          accent ? 'text-[#2563EB]' : 'text-[#F1F5F9]'
        )}
      >
        {value}
      </div>
    </div>
  );
}

// ============================================================
// Slider
// ============================================================
interface SliderProps {
  min?: number;
  max?: number;
  step?: number;
  value: number;
  onChange: (value: number) => void;
  label?: string;
  className?: string;
}

export function Slider({ min = 0, max = 100, step = 1, value, onChange, label, className }: SliderProps) {
  return (
    <div className={cn('w-full space-y-2', className)}>
      {label && (
        <div className="flex justify-between items-center text-xs">
          <span className="text-[#94A3B8]">{label}</span>
          <span className="font-semibold text-[#F1F5F9]">{value}%</span>
        </div>
      )}
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full h-1.5 rounded-full appearance-none cursor-pointer"
        style={{
          background: `linear-gradient(to right, #2563EB ${((value - min) / (max - min)) * 100}%, #243047 ${((value - min) / (max - min)) * 100}%)`,
        }}
      />
    </div>
  );
}

