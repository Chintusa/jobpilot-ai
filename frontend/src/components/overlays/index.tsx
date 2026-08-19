import { cn } from '@/utils/cn';
import { useEffect, type ReactNode } from 'react';
import { X } from 'lucide-react';

// ============================================================
// Modal
// ============================================================
interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  children: ReactNode;
  footer?: ReactNode;
}

const modalSizes = {
  sm: 'max-w-sm',
  md: 'max-w-lg',
  lg: 'max-w-2xl',
  xl: 'max-w-4xl',
};

export function Modal({ isOpen, onClose, title, size = 'md', children, footer }: ModalProps) {
  // Close on Escape
  useEffect(() => {
    if (!isOpen) return;
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [isOpen, onClose]);

  // Lock body scroll
  useEffect(() => {
    document.body.style.overflow = isOpen ? 'hidden' : '';
    return () => { document.body.style.overflow = ''; };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-[rgba(10,15,30,0.85)] backdrop-blur-sm animate-[fade-in_0.2s_ease-in-out]"
        onClick={onClose}
      />
      {/* Dialog */}
      <div
        className={cn(
          'relative w-full rounded-2xl',
          'bg-[#1A2235] border border-[rgba(255,255,255,0.08)]',
          'shadow-[0_24px_64px_rgba(0,0,0,0.6)]',
          'animate-[slide-in-up_0.2s_ease-out]',
          modalSizes[size]
        )}
      >
        {/* Header */}
        {title && (
          <div className="flex items-center justify-between px-6 py-4 border-b border-[rgba(148,163,184,0.08)]">
            <h2 className="text-base font-semibold text-[#F1F5F9]">{title}</h2>
            <button
              onClick={onClose}
              className="text-[#64748B] hover:text-[#F1F5F9] transition-colors p-1 rounded-md hover:bg-[rgba(255,255,255,0.05)]"
            >
              <X size={18} />
            </button>
          </div>
        )}
        {!title && (
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-[#64748B] hover:text-[#F1F5F9] transition-colors p-1 rounded-md hover:bg-[rgba(255,255,255,0.05)] z-10"
          >
            <X size={18} />
          </button>
        )}
        {/* Content */}
        <div className="px-6 py-5">{children}</div>
        {/* Footer */}
        {footer && (
          <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-[rgba(148,163,184,0.08)]">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}

// ============================================================
// Drawer
// ============================================================
interface DrawerProps {
  isOpen: boolean;
  onClose: () => void;
  side?: 'left' | 'right';
  title?: string;
  children: ReactNode;
  size?: 'sm' | 'md' | 'lg';
}

const drawerWidths = {
  sm: 'w-72',
  md: 'w-96',
  lg: 'w-[480px]',
};

export function Drawer({ isOpen, onClose, side = 'right', title, children, size = 'md' }: DrawerProps) {
  useEffect(() => {
    if (!isOpen) return;
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [isOpen, onClose]);

  useEffect(() => {
    document.body.style.overflow = isOpen ? 'hidden' : '';
    return () => { document.body.style.overflow = ''; };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex">
      <div
        className="absolute inset-0 bg-[rgba(10,15,30,0.7)] backdrop-blur-sm"
        onClick={onClose}
      />
      <div
        className={cn(
          'relative flex flex-col h-full',
          'bg-[#111827] border-[rgba(255,255,255,0.06)]',
          'shadow-[0_0_48px_rgba(0,0,0,0.6)]',
          drawerWidths[size],
          side === 'right'
            ? 'ml-auto border-l animate-[slide-in-right_0.28s_ease-out]'
            : 'mr-auto border-r animate-[slide-in-left_0.28s_ease-out]'
        )}
      >
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-[rgba(148,163,184,0.08)]">
          {title && <h2 className="text-base font-semibold text-[#F1F5F9]">{title}</h2>}
          <button
            onClick={onClose}
            className="ml-auto text-[#64748B] hover:text-[#F1F5F9] transition-colors"
          >
            <X size={18} />
          </button>
        </div>
        {/* Content */}
        <div className="flex-1 overflow-y-auto p-5">{children}</div>
      </div>
    </div>
  );
}
