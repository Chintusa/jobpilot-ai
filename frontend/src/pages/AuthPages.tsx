import React, { useState } from 'react';
import { Zap, AlertCircle, Loader2, Sparkles, UserCheck } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { ROUTES } from '@/routes/routes';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/app/authStore';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isLoading, error, clearError } = useAuthStore();

  const [email, setEmail] = useState('jhasaketan@example.com');
  const [password, setPassword] = useState('Password123');
  const [formError, setFormError] = useState<string | null>(null);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    clearError();

    if (!email || !password) {
      setFormError('Please enter both email and password');
      return;
    }

    const success = await login(email, password);
    if (success) {
      const from = (location.state as any)?.from?.pathname || ROUTES.DASHBOARD;
      navigate(from, { replace: true });
    }
  };

  return (
    <div
      className="min-h-screen flex items-center justify-center px-4"
      style={{
        background: 'radial-gradient(ellipse at 50% 0%, rgba(37,99,235,0.12) 0%, rgba(124,58,237,0.06) 40%, #0A0F1E 70%)',
      }}
    >
      <div className="w-full max-w-sm">
        {/* Logo */}
        <div className="flex items-center justify-center gap-2.5 mb-8">
          <div className="w-9 h-9 rounded-xl gradient-brand flex items-center justify-center shadow-lg shadow-blue-500/20">
            <Zap size={18} className="text-white" />
          </div>
          <span className="text-2xl font-bold text-[#F1F5F9]">
            JobPilot <span className="text-[#3B82F6]">AI</span>
          </span>
        </div>

        {/* Card */}
        <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.08)] rounded-2xl p-8 shadow-[0_24px_64px_rgba(0,0,0,0.5)]">
          <h1 className="text-xl font-bold text-[#F1F5F9] mb-1">Welcome back</h1>
          <p className="text-sm text-[#64748B] mb-6">Sign in to your AI Job Search cockpit</p>

          {(formError || error) && (
            <div className="mb-4 p-3 bg-[rgba(239,68,68,0.12)] border border-[rgba(239,68,68,0.25)] rounded-lg flex items-start gap-2 text-xs text-[#EF4444]">
              <AlertCircle size={15} className="shrink-0 mt-0.5" />
              <span>{formError || error}</span>
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-4">
            <Input
              label="Email Address"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <Input
              label="Password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <div className="flex items-center justify-between">
              <button
                type="button"
                onClick={() => {
                  setEmail('jhasaketan@example.com');
                  setPassword('Password123');
                }}
                className="text-xs text-[#3B82F6] hover:underline flex items-center gap-1"
              >
                <Sparkles size={12} /> Fill Demo Credentials
              </button>
            </div>
            <Button type="submit" variant="primary" size="lg" fullWidth disabled={isLoading}>
              {isLoading ? (
                <span className="flex items-center gap-2">
                  <Loader2 size={16} className="animate-spin" /> Signing in...
                </span>
              ) : (
                'Sign In'
              )}
            </Button>
          </form>

          <p className="text-center text-sm text-[#64748B] mt-6">
            Don't have an account?{' '}
            <button
              onClick={() => navigate(ROUTES.REGISTER)}
              className="text-[#3B82F6] hover:underline font-medium"
            >
              Create one now
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}

export function RegisterPage() {
  const navigate = useNavigate();
  const { register, isLoading, error, clearError } = useAuthStore();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [formError, setFormError] = useState<string | null>(null);

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    clearError();

    if (!name || !email || !password) {
      setFormError('Please fill in name, email, and password');
      return;
    }

    if (password.length < 6) {
      setFormError('Password must be at least 6 characters');
      return;
    }

    const success = await register(name, email, password, phone || undefined);
    if (success) {
      navigate(ROUTES.DASHBOARD, { replace: true });
    }
  };

  return (
    <div
      className="min-h-screen flex items-center justify-center px-4"
      style={{
        background: 'radial-gradient(ellipse at 50% 0%, rgba(37,99,235,0.12) 0%, rgba(124,58,237,0.06) 40%, #0A0F1E 70%)',
      }}
    >
      <div className="w-full max-w-sm">
        <div className="flex items-center justify-center gap-2.5 mb-8">
          <div className="w-9 h-9 rounded-xl gradient-brand flex items-center justify-center shadow-lg shadow-blue-500/20">
            <Zap size={18} className="text-white" />
          </div>
          <span className="text-2xl font-bold text-[#F1F5F9]">
            JobPilot <span className="text-[#3B82F6]">AI</span>
          </span>
        </div>

        <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.08)] rounded-2xl p-8 shadow-[0_24px_64px_rgba(0,0,0,0.5)]">
          <h1 className="text-xl font-bold text-[#F1F5F9] mb-1">Create your account</h1>
          <p className="text-sm text-[#64748B] mb-6">Start your AI job search journey</p>

          {(formError || error) && (
            <div className="mb-4 p-3 bg-[rgba(239,68,68,0.12)] border border-[rgba(239,68,68,0.25)] rounded-lg flex items-start gap-2 text-xs text-[#EF4444]">
              <AlertCircle size={15} className="shrink-0 mt-0.5" />
              <span>{formError || error}</span>
            </div>
          )}

          <form onSubmit={handleRegister} className="space-y-4">
            <Input
              label="Full Name"
              placeholder="e.g. Jhasaketan M."
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
            <Input
              label="Email Address"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <Input
              label="Phone Number (optional)"
              type="tel"
              placeholder="+91 9876543210"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
            />
            <Input
              label="Password (min 6 chars)"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <Button type="submit" variant="primary" size="lg" fullWidth disabled={isLoading}>
              {isLoading ? (
                <span className="flex items-center gap-2">
                  <Loader2 size={16} className="animate-spin" /> Creating account...
                </span>
              ) : (
                'Create Account'
              )}
            </Button>
          </form>

          <p className="text-center text-sm text-[#64748B] mt-6">
            Already have an account?{' '}
            <button
              onClick={() => navigate(ROUTES.LOGIN)}
              className="text-[#3B82F6] hover:underline font-medium"
            >
              Sign in
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
