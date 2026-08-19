import { cn } from '@/utils/cn';
import type { InputHTMLAttributes, ReactNode } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  leftIcon?: ReactNode;
  rightElement?: ReactNode;
  variant?: 'filled' | 'outlined';
}

export function Input({
  label,
  error,
  leftIcon,
  rightElement,
  variant = 'filled',
  className,
  id,
  ...props
}: InputProps) {
  const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className="flex flex-col gap-1.5 w-full">
      {label && (
        <label
          htmlFor={inputId}
          className="text-xs font-medium text-[#94A3B8]"
        >
          {label}
        </label>
      )}
      <div className="relative flex items-center">
        {leftIcon && (
          <span className="absolute left-3 text-[#64748B] pointer-events-none flex-shrink-0">
            {leftIcon}
          </span>
        )}
        <input
          id={inputId}
          {...props}
          className={cn(
            'w-full h-10 text-sm rounded-md transition-all duration-150 outline-none',
            'text-[#F1F5F9] placeholder:text-[#64748B]',
            variant === 'filled'
              ? 'bg-[#1E293B] border border-[rgba(148,163,184,0.15)]'
              : 'bg-transparent border border-[rgba(148,163,184,0.15)]',
            'hover:border-[rgba(148,163,184,0.3)]',
            'focus:border-[#2563EB] focus:shadow-[0_0_0_2px_rgba(37,99,235,0.25)]',
            error
              ? 'border-[#EF4444] focus:border-[#EF4444] focus:shadow-[0_0_0_2px_rgba(239,68,68,0.2)]'
              : '',
            leftIcon ? 'pl-10' : 'px-3',
            rightElement ? 'pr-10' : 'pr-3',
            className
          )}
        />
        {rightElement && (
          <span className="absolute right-3 text-[#64748B]">{rightElement}</span>
        )}
      </div>
      {error && (
        <p className="text-xs text-[#EF4444]">{error}</p>
      )}
    </div>
  );
}
