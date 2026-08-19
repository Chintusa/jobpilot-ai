import { AppShell } from '@/components/layout/AppShell';
import { StatCard } from '@/components/ui';
import {
  AIOrb, AIAgentStatus, AIInsightCard,
  AIActivityTimeline, ApplicationPipeline, MatchScoreRing
} from '@/components/ai';
import { Badge, StrongMatchBadge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/overlays';
import {
  mockDashboardSummary, mockJobs, mockActivityEntries
} from '@/data/mockData';
import { useStore } from '@/app/store';
import {
  BriefcaseBusiness, MapPin, Clock, DollarSign,
  Search, Users, Zap, Download, Settings,
  AlertTriangle, CheckCircle2, ChevronRight,
  TrendingUp, Sparkles, Lightbulb, FileText, Send, Bookmark
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { jobDetailPath, agentApplyPath, ROUTES } from '@/routes/routes';
import { useState } from 'react';
import { useJobsQuery, type BackendJob } from '@/api/jobsApi';
import type { Job } from '@/types';

const AI_INSIGHT_CARDS = [
  {
    icon: <Lightbulb size={16} className="text-[#38BDF8]" />,
    title: 'Why this job matches you',
    text: 'Your 2.5 years of Java & Spring Boot experience directly fulfill the core backend requirements. Your verified SQL knowledge matches 100% of their database stack.',
    tag: '91% MATCH',
  },
  {
    icon: <Sparkles size={16} className="text-[#34D399]" />,
    title: 'Why this job saves you time',
    text: 'AI Agent has pre-formatted 100% of standard application fields and generated a tailored cover letter referencing your distributed systems experience.',
    tag: 'AUTO-READY',
  },
  {
    icon: <TrendingUp size={16} className="text-[#A78BFA]" />,
    title: 'Market Competitiveness',
    text: 'You rank in the top 8% of applicants for this salary bracket (₹6–9 LPA) based on skill depth and recent project complexity.',
    tag: 'TOP CANDIDATE',
  },
];

export default function DashboardPage() {
  const { agentStatus, user } = useStore();
  const navigate = useNavigate();
  const summary = mockDashboardSummary;

  const { data: liveJobsData } = useJobsQuery({ page: 0, size: 5 });

  const liveList = liveJobsData?.content;
  const transformedJobs: Job[] = (liveList && liveList.length > 0)
    ? liveList.map((bj: BackendJob) => {
        let parsedSkills: { name: string; required: boolean; category: string }[] = [];
        try {
          const list = JSON.parse(bj.requiredSkills || '[]');
          parsedSkills = (Array.isArray(list) ? list : []).map((s: string) => ({
            name: s,
            required: true,
            category: 'Technical',
          }));
        } catch {
          parsedSkills = [{ name: 'Java', required: true, category: 'Technical' }];
        }

        return {
          id: bj.id,
          title: bj.title,
          company: bj.company,
          location: bj.location,
          workMode: (bj.workMode || 'HYBRID') as any,
          salaryDisplay: bj.salaryDisplay || '₹14.0 - 22.0 LPA',
          experience: bj.experienceMin ? `${bj.experienceMin}+ yrs` : '2+ yrs',
          matchScore: bj.matchScore || 91,
          description: bj.description,
          skills: parsedSkills,
          postedAt: bj.postedAt,
        };
      })
    : mockJobs;

  const topJob = transformedJobs[0] || mockJobs[0];
  const secondJob = transformedJobs[1] || mockJobs[1] || mockJobs[0];
  const [showActionModal, setShowActionModal] = useState(false);
  const [selectedInsight, setSelectedInsight] = useState(0);

  return (
    <AppShell>
      {/* ——— Top Greeting & Header Actions (01-dashboard.png) ——— */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl lg:text-3xl font-bold text-[#F1F5F9] tracking-tight">
            Good morning, {user?.name?.split(' ')[0] || 'Jhasaketan'}
          </h1>
          <p className="text-sm text-[#94A3B8] mt-0.5">
            Your AI career agent is working.
          </p>
        </div>

        <div className="flex items-center gap-2.5 flex-wrap">
          <Button
            variant="secondary"
            size="sm"
            leftIcon={<Download size={14} />}
            onClick={() => {}}
          >
            Export data
          </Button>

          <Button
            variant="secondary"
            size="sm"
            leftIcon={<Settings size={14} />}
            onClick={() => navigate(ROUTES.AGENT)}
          >
            Current agent
          </Button>

          <Button
            variant="primary"
            size="sm"
            leftIcon={<Zap size={14} />}
            onClick={() => navigate(ROUTES.JOBS)}
          >
            Start automatic search
          </Button>
        </div>
      </div>

      {/* ——— 6 Statistics Cards Row (01-dashboard.png) ——— */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3.5 mb-6">
        <StatCard
          label="Jobs Discovered Today"
          value="30"
          icon={<Search size={16} className="text-[#3B82F6]" />}
          accent
        />
        <StatCard
          label="Strong Matches"
          value="33"
          icon={<Zap size={16} className="text-[#10B981]" />}
        />
        <StatCard
          label="Applications Prepared"
          value="20"
          icon={<BriefcaseBusiness size={16} className="text-[#8B5CF6]" />}
        />
        <StatCard
          label="Applications Submitted"
          value="0"
          icon={<Send size={16} className="text-[#38BDF8]" />}
        />
        <StatCard
          label="Interviews"
          value="1"
          icon={<Users size={16} className="text-[#F59E0B]" />}
        />
        <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-xl p-4 flex flex-col justify-between shadow-sm">
          <span className="text-xs text-[#94A3B8] font-medium">Current AI Agent Status</span>
          <div className="mt-2">
            <AIAgentStatus status={agentStatus} variant="pill" showLabel={true} className="text-xs scale-90 origin-left" />
          </div>
        </div>
      </div>

      {/* ——— Main 3D AI Career Agent Hero Card (01-dashboard.png) ——— */}
      <div className="relative rounded-2xl p-6 lg:p-8 mb-6 overflow-hidden border border-[rgba(255,255,255,0.08)] bg-gradient-to-r from-[#111827] via-[#151D30] to-[#1A2235] shadow-[0_8px_32px_rgba(0,0,0,0.5)]">
        {/* Ambient atmospheric backdrop */}
        <div
          className="absolute inset-0 opacity-15 pointer-events-none"
          style={{
            backgroundImage: `radial-gradient(rgba(59,130,246,0.35) 1px, transparent 1px)`,
            backgroundSize: '24px 24px',
          }}
        />

        <div className="relative z-10 flex flex-col lg:flex-row lg:items-center justify-between gap-8">
          {/* Left Column: Title + Stage status dots */}
          <div className="flex-1 max-w-lg">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[rgba(37,99,235,0.15)] border border-[rgba(37,99,235,0.3)] mb-3">
              <Sparkles size={13} className="text-[#38BDF8]" />
              <span className="text-xs font-semibold uppercase tracking-wider text-[#38BDF8]">
                Continuous Autonomous Search
              </span>
            </div>

            <h2 className="text-2xl lg:text-3xl font-extrabold text-[#F1F5F9] mb-2 tracking-tight">
              AI Career Agent
            </h2>
            <p className="text-sm text-[#94A3B8] mb-6 leading-relaxed">
              Actively searching job boards, matching skills against candidate profile, and preparing compliant applications.
            </p>

            <div className="flex flex-wrap items-center gap-3">
              <AIAgentStatus status={agentStatus} variant="pill" showLabel={true} />

              <div className="flex items-center gap-3 px-3 py-1.5 rounded-full bg-[rgba(17,24,39,0.8)] border border-[rgba(255,255,255,0.06)] text-xs text-[#94A3B8]">
                <span className="flex items-center gap-1.5 font-medium">
                  <span className="w-2 h-2 rounded-full bg-[#3B82F6] animate-pulse" />
                  Searching
                </span>
                <span className="text-[#475569]">·</span>
                <span className="flex items-center gap-1.5 font-medium">
                  <span className="w-2 h-2 rounded-full bg-[#8B5CF6]" />
                  Analyzing
                </span>
                <span className="text-[#475569]">·</span>
                <span className="flex items-center gap-1.5 font-medium">
                  <span className="w-2 h-2 rounded-full bg-[#06B6D4]" />
                  Ranking
                </span>
                <span className="text-[#475569]">·</span>
                <span className="flex items-center gap-1.5 font-medium">
                  <span className="w-2 h-2 rounded-full bg-[#10B981]" />
                  Preparing
                </span>
              </div>
            </div>
          </div>

          {/* Right Column: 3D Orb with Orbital Node Tags from 01-dashboard.png */}
          <div className="relative flex items-center justify-center min-w-[300px] lg:min-w-[400px] py-4">
            {/* Left Satellite Nodes */}
            <div className="hidden sm:flex flex-col gap-3 absolute -left-4 lg:-left-12 z-20 text-right">
              <div className="flex items-center justify-end gap-2 text-xs font-medium text-[#F1F5F9] bg-[rgba(15,23,42,0.9)] backdrop-blur-md px-3 py-1.5 rounded-lg border border-[rgba(59,130,246,0.3)] shadow-lg">
                <span>Searching jobs</span>
                <span className="w-2 h-2 rounded-full bg-[#3B82F6]" />
              </div>
              <div className="flex items-center justify-end gap-2 text-xs font-medium text-[#F1F5F9] bg-[rgba(15,23,42,0.9)] backdrop-blur-md px-3 py-1.5 rounded-lg border border-[rgba(245,158,11,0.3)] shadow-lg">
                <span>Analyzing opportunities</span>
                <span className="w-2 h-2 rounded-full bg-[#F59E0B]" />
              </div>
              <div className="flex items-center justify-end gap-2 text-xs font-medium text-[#F1F5F9] bg-[rgba(15,23,42,0.9)] backdrop-blur-md px-3 py-1.5 rounded-lg border border-[rgba(139,92,246,0.3)] shadow-lg">
                <span>Ranking candidates</span>
                <span className="w-2 h-2 rounded-full bg-[#8B5CF6]" />
              </div>
            </div>

            {/* Glowing 3D AI Orb */}
            <div className="relative z-10 scale-110 lg:scale-125">
              <AIOrb size="xl" status={agentStatus === 'ACTIVE' ? 'active' : 'idle'} label="AI AGENT ACTIVE" />
            </div>

            {/* Right Satellite Nodes */}
            <div className="hidden sm:flex flex-col gap-3 absolute -right-4 lg:-right-10 z-20 text-left">
              <div className="flex items-center gap-2 text-xs font-medium text-[#F1F5F9] bg-[rgba(15,23,42,0.9)] backdrop-blur-md px-3 py-1.5 rounded-lg border border-[rgba(34,211,238,0.3)] shadow-lg">
                <span className="w-2 h-2 rounded-full bg-[#22D3EE]" />
                <span>Ranking candidates</span>
              </div>
              <div className="flex items-center gap-2 text-xs font-medium text-[#F1F5F9] bg-[rgba(15,23,42,0.9)] backdrop-blur-md px-3 py-1.5 rounded-lg border border-[rgba(16,185,129,0.3)] shadow-lg">
                <span className="w-2 h-2 rounded-full bg-[#10B981]" />
                <span>Preparing applications</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ——— Middle 2-Column Section: Top Opportunities + AI Insights (01-dashboard.png) ——— */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 mb-6">
        {/* Left Column (Span 7): Top Opportunities */}
        <div className="lg:col-span-7 bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-[#F1F5F9]">Top Opportunities</h3>
                <span className="px-2 py-0.5 rounded-full text-[11px] font-semibold bg-[#243047] text-[#94A3B8]">
                  2 of 33 Matches
                </span>
              </div>
              <button
                onClick={() => navigate(ROUTES.JOBS)}
                className="text-xs text-[#3B82F6] hover:text-[#60A5FA] flex items-center gap-1 font-medium transition-colors cursor-pointer"
              >
                View all jobs <ChevronRight size={14} />
              </button>
            </div>

            <div className="space-y-4">
              {/* Primary Opportunity Card (91% Match) */}
              <div className="bg-[#111827] border border-[rgba(255,255,255,0.06)] rounded-xl p-5 hover:border-[rgba(59,130,246,0.3)] transition-all duration-200 shadow-md">
                <div className="flex items-start gap-5">
                  <div className="flex-shrink-0 pt-1">
                    <MatchScoreRing score={topJob.matchScore || 91} size={84} strokeWidth={8} showLabel={true} />
                  </div>

                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <h4
                          className="text-lg font-bold text-[#F1F5F9] hover:text-[#60A5FA] cursor-pointer transition-colors"
                          onClick={() => navigate(jobDetailPath(topJob.id))}
                        >
                          {topJob.title}
                        </h4>
                        <p className="text-sm text-[#94A3B8] font-medium">{topJob.company}</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <StrongMatchBadge />
                        <button className="text-[#64748B] hover:text-[#F59E0B] transition-colors p-1" title="Bookmark">
                          <Bookmark size={15} />
                        </button>
                      </div>
                    </div>

                    <div className="flex items-center gap-4 mt-2 text-xs text-[#64748B] flex-wrap">
                      <span className="flex items-center gap-1 text-[#94A3B8]">
                        <MapPin size={12} className="text-[#3B82F6]" />
                        {topJob.location}
                      </span>
                      <span className="flex items-center gap-1 text-[#94A3B8]">
                        <Clock size={12} className="text-[#8B5CF6]" />
                        {topJob.workMode}
                      </span>
                      <span className="flex items-center gap-1 text-[#94A3B8]">
                        <DollarSign size={12} className="text-[#10B981]" />
                        {topJob.salaryDisplay}
                      </span>
                    </div>

                    <div className="mt-3 pt-3 border-t border-[rgba(148,163,184,0.08)]">
                      <div className="flex flex-wrap items-center gap-1.5">
                        <span className="text-xs text-[#64748B] mr-1">Skills:</span>
                        {topJob.skills?.map((s) => (
                          <Badge key={s.name} variant="skill">
                            {s.name}
                          </Badge>
                        ))}
                        {topJob.missingSkills && topJob.missingSkills.length > 0 && (
                          <span className="text-xs text-[#EF4444] ml-2 font-medium">
                            Missing: {topJob.missingSkills.map((s) => s.name).join(', ')}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="flex items-center justify-end gap-2.5 mt-4 pt-3.5 border-t border-[rgba(148,163,184,0.08)]">
                  <Button
                    size="sm"
                    variant="secondary"
                    onClick={() => navigate(jobDetailPath(topJob.id))}
                  >
                    View Job
                  </Button>
                  <Button
                    size="sm"
                    variant="primary"
                    className="bg-white text-[#0A0F1E] font-semibold hover:bg-[#F1F5F9] shadow-md"
                    onClick={() => navigate(agentApplyPath(topJob.id))}
                  >
                    Prepare Application
                  </Button>
                </div>
              </div>

              {/* Secondary Opportunity Card (88% Match) from 01-dashboard.png */}
              <div className="bg-[#111827] border border-[rgba(255,255,255,0.06)] rounded-xl p-4 hover:border-[rgba(59,130,246,0.2)] transition-all duration-200">
                <div className="flex items-center justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <MatchScoreRing score={secondJob.matchScore || 88} size={48} strokeWidth={5} showLabel={false} />
                    <div>
                      <h4
                        className="text-sm font-bold text-[#F1F5F9] hover:text-[#60A5FA] cursor-pointer"
                        onClick={() => navigate(jobDetailPath(secondJob.id))}
                      >
                        {secondJob.title}
                      </h4>
                      <p className="text-xs text-[#94A3B8]">{secondJob.company} · {secondJob.location}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <Button
                      size="sm"
                      variant="secondary"
                      className="text-xs h-7 px-2.5"
                      onClick={() => navigate(agentApplyPath(secondJob.id))}
                    >
                      Prepare
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column (Span 5): AI Career Insights (01-dashboard.png) */}
        <div className="lg:col-span-5 bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-base font-bold text-[#F1F5F9]">AI Insights</h3>
              <span className="text-xs text-[#38BDF8] font-medium flex items-center gap-1">
                <Sparkles size={12} /> Recruiter Assessment
              </span>
            </div>

            {/* Featured Insight Box */}
            <div className="p-4 rounded-xl mb-4 border border-[rgba(59,130,246,0.25)] bg-gradient-to-br from-[rgba(37,99,235,0.12)] to-[rgba(124,58,237,0.06)]">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <span className="p-1 rounded-md bg-[rgba(59,130,246,0.2)] text-[#38BDF8]">
                    {AI_INSIGHT_CARDS[selectedInsight].icon}
                  </span>
                  <span className="text-xs font-bold uppercase tracking-wider text-[#38BDF8]">
                    {AI_INSIGHT_CARDS[selectedInsight].title}
                  </span>
                </div>
                <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-[rgba(59,130,246,0.2)] text-[#93C5FD]">
                  {AI_INSIGHT_CARDS[selectedInsight].tag}
                </span>
              </div>
              <p className="text-sm text-[#F1F5F9] leading-relaxed">
                {AI_INSIGHT_CARDS[selectedInsight].text}
              </p>
            </div>

            {/* Insight tabs / carousel items */}
            <div className="space-y-2.5">
              {AI_INSIGHT_CARDS.map((card, idx) => (
                <div
                  key={idx}
                  onClick={() => setSelectedInsight(idx)}
                  className={`p-3 rounded-xl border transition-all duration-150 cursor-pointer flex items-center justify-between gap-3 ${
                    selectedInsight === idx
                      ? 'bg-[#243047] border-[rgba(59,130,246,0.4)] shadow-md'
                      : 'bg-[#111827] border-[rgba(255,255,255,0.04)] hover:bg-[#1E293B]'
                  }`}
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <span className="flex-shrink-0">{card.icon}</span>
                    <p className="text-xs text-[#94A3B8] truncate">{card.title}: {card.text}</p>
                  </div>
                  <ChevronRight size={14} className="text-[#64748B] flex-shrink-0" />
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* ——— Bottom Section: Application Pipeline & Activity Timeline (01-dashboard.png) ——— */}
      <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl space-y-6">
        {/* Application Pipeline */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <div>
              <h3 className="text-base font-bold text-[#F1F5F9]">Application pipeline</h3>
              <p className="text-xs text-[#64748B] mt-0.5">Elegant sequence to match each application pipeline.</p>
            </div>
          </div>

          <ApplicationPipeline currentStage="MATCHED" />
        </div>

        {/* Horizontal Divider */}
        <hr className="border-0 border-t border-[rgba(148,163,184,0.08)]" />

        {/* AI Agent Activity Timeline */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-base font-bold text-[#F1F5F9]">AI Agent activity timeline</h3>
            <button
              onClick={() => setShowActionModal(true)}
              className="text-xs text-[#F59E0B] hover:underline flex items-center gap-1.5 font-semibold cursor-pointer bg-[rgba(245,158,11,0.1)] px-2.5 py-1 rounded-full border border-[rgba(245,158,11,0.25)]"
            >
              <AlertTriangle size={13} /> Action required (1)
            </button>
          </div>

          <AIActivityTimeline
            entries={mockActivityEntries}
            orientation="horizontal"
          />
        </div>
      </div>

      {/* ——— Action Required Modal (from 01-dashboard.png) ——— */}
      <Modal
        isOpen={showActionModal}
        onClose={() => setShowActionModal(false)}
        size="md"
      >
        <div className="text-center py-2 space-y-4">
          <div className="w-14 h-14 mx-auto rounded-2xl bg-[rgba(245,158,11,0.15)] border border-[rgba(245,158,11,0.3)] flex items-center justify-center text-[#F59E0B] shadow-[0_0_24px_rgba(245,158,11,0.3)]">
            <AlertTriangle size={28} />
          </div>

          <div>
            <h3 className="text-lg font-bold text-[#F1F5F9]">Action required</h3>
            <p className="text-sm text-[#94A3B8] mt-1 max-w-sm mx-auto">
              Application confirms to send application confirmation on <strong>TechNova Technologies</strong>.
            </p>
          </div>

          <div className="bg-[#111827] rounded-xl p-4 text-left border border-[rgba(255,255,255,0.06)] space-y-2 text-xs">
            <div className="flex justify-between">
              <span className="text-[#64748B]">Target Role:</span>
              <span className="text-[#F1F5F9] font-medium">Java Backend Developer</span>
            </div>
            <div className="flex justify-between">
              <span className="text-[#64748B]">Match Score:</span>
              <span className="text-[#10B981] font-semibold">91% Match</span>
            </div>
            <div className="flex justify-between">
              <span className="text-[#64748B]">Screening Answers:</span>
              <span className="text-[#38BDF8] font-medium">3 Generated (Review Needed)</span>
            </div>
          </div>

          <div className="flex flex-col gap-2 pt-2">
            <Button
              variant="primary"
              size="lg"
              fullWidth
              onClick={() => {
                setShowActionModal(false);
                navigate(agentApplyPath(topJob.id));
              }}
            >
              Continue & Review
            </Button>
            <div className="flex gap-2">
              <Button
                variant="secondary"
                size="md"
                className="flex-1"
                onClick={() => setShowActionModal(false)}
              >
                Review
              </Button>
              <Button
                variant="ghost"
                size="md"
                className="flex-1 text-[#64748B]"
                onClick={() => setShowActionModal(false)}
              >
                Skip
              </Button>
            </div>
          </div>
        </div>
      </Modal>
    </AppShell>
  );
}
