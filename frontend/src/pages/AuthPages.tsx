import React, { useState } from 'react';
import { Zap, AlertCircle, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { ROUTES } from '@/routes/routes';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/app/authStore';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isLoading, error, clearError } = useAuthStore();

  const [email, setEmail] = useState('test.engineer@example.com');
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
          <div className="w-9 h-9 rounded-xl gradient-brand flex items-center justify-center">
            <Zap size={18} className="text-white" />
          </div>
          <span className="text-2xl font-bold text-[#F1F5F9]">
            JobPilot <span className="text-[#3B82F6]">AI</span>
          </span>
        </div>

        {/* Card */}
        <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.08)] rounded-2xl p-8 shadow-[0_24px_64px_rgba(0,0,0,0.5)]">
          <h1 className="text-xl font-bold text-[#F1F5F9] mb-1">Welcome back</h1>
          <p className="text-sm text-[#64748B] mb-6">Sign in to your account</p>

          {(formError || error) && (
            <div className="mb-4 p-3 bg-[rgba(239,68,68,0.12)] border border-[rgba(239,68,68,0.25)] rounded-lg flex items-center gap-2 text-xs text-[#EF4444]">
              <AlertCircle size={14} className="shrink-0" />
              <span>{formError || error}</span>
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-4">
            <Input
              label="Email"
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
            <div className="flex justify-end">
              <button type="button" className="text-xs text-[#3B82F6] hover:underline">
                Forgot password?
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
              Sign up
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

  const [name, setName] = useState('Jhasaketan M.');
  const [email, setEmail] = useState('jhasaketan@example.com');
  const [password, setPassword] = useState('Password123');
  const [phone, setPhone] = useState('+91 9876543210');
  const [formError, setFormError] = useState<string | null>(null);

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    clearError();

    if (!name || !email || !password) {
      setFormError('Please fill in all required fields');
      return;
    }

    const success = await register(name, email, password, phone);
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
          <div className="w-9 h-9 rounded-xl gradient-brand flex items-center justify-center">
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
            <div className="mb-4 p-3 bg-[rgba(239,68,68,0.12)] border border-[rgba(239,68,68,0.25)] rounded-lg flex items-center gap-2 text-xs text-[#EF4444]">
              <AlertCircle size={14} className="shrink-0" />
              <span>{formError || error}</span>
            </div>
          )}

          <form onSubmit={handleRegister} className="space-y-4">
            <Input
              label="Full Name"
              placeholder="Jhasaketan M."
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
            <Input
              label="Email"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <Input
              label="Phone Number"
              type="tel"
              placeholder="+91 9876543210"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
            />
            <Input
              label="Password"
              type="password"
              placeholder="Min. 8 characters"
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
