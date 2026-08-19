import { cn } from '@/utils/cn';
import type { ReactNode } from 'react';

interface CardProps {
  children: ReactNode;
  className?: string;
  variant?: 'default' | 'glass' | 'elevated';
  padding?: 'none' | 'sm' | 'md' | 'lg';
  hover?: boolean;
}

export function Card({
  children,
  className,
  variant = 'default',
  padding = 'md',
  hover = false,
}: CardProps) {
  const variants = {
    default:
      'bg-[#1A2235] border border-[rgba(255,255,255,0.06)] shadow-[0_1px_3px_rgba(0,0,0,0.4),0_4px_16px_rgba(0,0,0,0.3)]',
    glass:
      'glass-card shadow-[0_4px_24px_rgba(0,0,0,0.5)]',
    elevated:
      'bg-[#243047] border border-[rgba(255,255,255,0.08)] shadow-[0_4px_24px_rgba(0,0,0,0.5)]',
  };

  const paddings = {
    none: '',
    sm: 'p-4',
    md: 'p-6',
    lg: 'p-8',
  };

  return (
    <div
      className={cn(
        'rounded-xl transition-all duration-200',
        variants[variant],
        paddings[padding],
        hover && 'hover:-translate-y-0.5 hover:shadow-[0_8px_32px_rgba(0,0,0,0.6)] cursor-pointer',
        className
      )}
    >
      {children}
    </div>
  );
}
