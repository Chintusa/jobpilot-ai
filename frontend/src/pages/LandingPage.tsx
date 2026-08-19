import { cn } from '@/utils/cn';
import { Button } from '@/components/ui/Button';
import { ROUTES } from '@/routes/routes';
import { useNavigate } from 'react-router-dom';
import { Zap, ChevronRight, ArrowRight } from 'lucide-react';

const WORKFLOW_STEPS = [
  { label: 'Resume', icon: '📄', color: '#2563EB' },
  { label: 'AI Candidate Profile', icon: '🤖', color: '#3B82F6' },
  { label: 'Job Discovery', icon: '🔍', color: '#06B6D4' },
  { label: 'Recruiter Matching', icon: '🤝', color: '#7C3AED' },
  { label: 'Application Preparation', icon: '📝', color: '#8B5CF6' },
  { label: 'AI Application Agent', icon: '⚡', color: '#EC4899' },
  { label: 'Application Tracking', icon: '📊', color: '#10B981' },
];

const FEATURES = [
  {
    title: 'Intelligent Job Discovery',
    desc: 'AI scans thousands of job boards automatically to find your perfect roles.',
    icon: '🔍',
  },
  {
    title: 'AI Recruiter Assessment',
    desc: 'Get a detailed 91% match analysis before applying to any role.',
    icon: '🎯',
  },
  {
    title: 'Application Automation',
    desc: 'AI fills and submits application forms while you focus on interviews.',
    icon: '⚡',
  },
  {
    title: 'Human-in-the-Loop',
    desc: 'Review and approve before submission. Always in control.',
    icon: '🛡️',
  },
  {
    title: 'Real-time Analytics',
    desc: 'Track match rates, response rates and interviews in one dashboard.',
    icon: '📊',
  },
  {
    title: 'AI Career Agent',
    desc: 'Your 24/7 career agent actively works while you sleep.',
    icon: '🤖',
  },
];

export default function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-[#0A0F1E] text-[#F1F5F9]"
      style={{
        background: 'radial-gradient(ellipse at 50% 0%, rgba(37,99,235,0.12) 0%, rgba(124,58,237,0.08) 40%, transparent 70%), #0A0F1E',
      }}
    >
      {/* ——— Top Nav ——— */}
      <nav className="fixed top-0 left-0 right-0 z-50 flex items-center justify-between px-8 py-4 backdrop-blur-md bg-[rgba(10,15,30,0.8)] border-b border-[rgba(255,255,255,0.06)]">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg gradient-brand flex items-center justify-center">
            <Zap size={16} className="text-white" />
          </div>
          <span className="font-bold text-lg text-[#F1F5F9]">
            JobPilot <span className="text-[#3B82F6]">AI</span>
          </span>
        </div>
        <div className="hidden md:flex items-center gap-8 text-sm text-[#94A3B8]">
          <a href="#features" className="hover:text-[#F1F5F9] transition-colors">Features</a>
          <a href="#workflow" className="hover:text-[#F1F5F9] transition-colors">How it works</a>
          <a href="#pricing" className="hover:text-[#F1F5F9] transition-colors">Pricing</a>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="sm" onClick={() => navigate(ROUTES.LOGIN)}>
            Log in
          </Button>
          <Button variant="primary" size="sm" onClick={() => navigate(ROUTES.REGISTER)}>
            Start AI Job
          </Button>
        </div>
      </nav>

      {/* ——— Hero Section ——— */}
      <section className="relative min-h-screen flex flex-col items-center justify-center text-center px-6 pt-24 pb-16">
        {/* Decorative orb */}
        <div className="absolute inset-0 flex items-center justify-center pointer-events-none overflow-hidden">
          <div
            className="w-[600px] h-[600px] rounded-full opacity-20"
            style={{
              background: 'radial-gradient(ellipse at center, rgba(37,99,235,0.8) 0%, rgba(139,92,246,0.6) 40%, transparent 70%)',
              filter: 'blur(80px)',
            }}
          />
        </div>

        {/* 3D Crystal placeholder */}
        <div className="relative mb-8">
          <div
            className="w-40 h-40 mx-auto rounded-2xl animate-[ai-pulse_4s_ease-in-out_infinite] flex items-center justify-center"
            style={{
              background: 'linear-gradient(135deg, rgba(59,130,246,0.6), rgba(139,92,246,0.5), rgba(22,211,238,0.4))',
              boxShadow: '0 0 80px rgba(37,99,235,0.5), 0 0 160px rgba(139,92,246,0.3)',
              transform: 'rotate(15deg)',
            }}
          >
            <div className="w-24 h-24 rounded-xl"
              style={{
                background: 'linear-gradient(135deg, rgba(255,255,255,0.15), rgba(255,255,255,0.05))',
                backdropFilter: 'blur(8px)',
                border: '1px solid rgba(255,255,255,0.2)',
              }}
            />
          </div>
          {/* Floating cards */}
          <div className="absolute -left-24 top-4 bg-[#1A2235] border border-[rgba(255,255,255,0.1)] rounded-xl p-3 text-xs text-left hidden lg:block shadow-xl">
            <div className="text-[#64748B] mb-1">Resume Data</div>
            <div className="w-24 h-1.5 bg-[#2563EB] rounded mb-1" />
            <div className="w-16 h-1.5 bg-[#243047] rounded" />
          </div>
          <div className="absolute -right-24 top-4 bg-[#1A2235] border border-[rgba(255,255,255,0.1)] rounded-xl p-3 text-xs text-left hidden lg:block shadow-xl">
            <div className="text-[#64748B] mb-1">Match Score</div>
            <div className="text-2xl font-bold text-[#10B981]">91%</div>
          </div>
        </div>

        <h1 className="text-4xl md:text-6xl font-bold leading-tight mb-6 max-w-3xl">
          Your AI Agent for the{' '}
          <span className="text-gradient-brand">Entire Job Search</span>
        </h1>
        <p className="text-lg text-[#94A3B8] max-w-xl mb-8">
          Find the right jobs. Understand your match. Prepare applications. Apply with confidence.
        </p>

        <div className="flex items-center gap-4 flex-wrap justify-center">
          <Button
            variant="primary"
            size="lg"
            rightIcon={<ChevronRight size={18} />}
            onClick={() => navigate(ROUTES.REGISTER)}
          >
            Start AI Job Search
          </Button>
          <Button
            variant="secondary"
            size="lg"
            onClick={() => navigate(ROUTES.DASHBOARD)}
          >
            See How It Works
          </Button>
        </div>
      </section>

      {/* ——— Workflow Section ——— */}
      <section id="workflow" className="py-20 px-6">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl font-bold text-center mb-4">Realistic 3D Workflow</h2>
          <p className="text-[#94A3B8] text-center mb-12">From resume to offer, fully automated</p>

          <div className="flex items-center justify-center gap-2 flex-wrap">
            {WORKFLOW_STEPS.map((step, i) => (
              <div key={step.label} className="flex items-center gap-2">
                <div className="flex flex-col items-center gap-2">
                  <div
                    className="w-16 h-16 rounded-2xl flex items-center justify-center text-2xl shadow-xl"
                    style={{
                      background: `linear-gradient(135deg, ${step.color}33, ${step.color}22)`,
                      border: `1px solid ${step.color}44`,
                      boxShadow: `0 8px 24px ${step.color}33`,
                    }}
                  >
                    {step.icon}
                  </div>
                  <span className="text-xs text-[#94A3B8] text-center max-w-[80px]">{step.label}</span>
                </div>
                {i < WORKFLOW_STEPS.length - 1 && (
                  <ArrowRight size={16} className="text-[#64748B] mt-[-20px] flex-shrink-0" />
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ——— Features Section ——— */}
      <section id="features" className="py-20 px-6 bg-[rgba(255,255,255,0.02)]">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl font-bold text-center mb-12">
            Everything you need to land your dream job
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {FEATURES.map((feature) => (
              <div
                key={feature.title}
                className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-xl p-6 hover:-translate-y-1 transition-transform duration-200"
              >
                <div className="text-3xl mb-4">{feature.icon}</div>
                <h3 className="font-semibold text-[#F1F5F9] mb-2">{feature.title}</h3>
                <p className="text-sm text-[#94A3B8]">{feature.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ——— CTA Section ——— */}
      <section className="py-20 px-6 text-center">
        <div className="max-w-2xl mx-auto">
          <h2 className="text-3xl font-bold mb-4">Ready to let AI find your job?</h2>
          <p className="text-[#94A3B8] mb-8">Join thousands of professionals using JobPilot AI</p>
          <Button
            variant="primary"
            size="xl"
            fullWidth={false}
            rightIcon={<ChevronRight size={20} />}
            onClick={() => navigate(ROUTES.REGISTER)}
          >
            Get started for free
          </Button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-[rgba(255,255,255,0.06)] py-8 px-6 text-center text-sm text-[#64748B]">
        © 2024 JobPilot AI. All rights reserved.
      </footer>
    </div>
  );
}
