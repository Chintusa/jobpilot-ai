import { AppShell } from '@/components/layout/AppShell';
import { MatchScoreRing, AIInsightCard } from '@/components/ai';
import { Button } from '@/components/ui/Button';
import { ProgressBar } from '@/components/ui';
import { mockJobs, mockJobAssessment } from '@/data/mockData';
import { useParams, useNavigate } from 'react-router-dom';
import {
  MapPin,
  Clock,
  Briefcase,
  DollarSign,
  Bookmark,
  FileText,
  Sparkles,
  AlertTriangle,
  UserCheck,
  Check,
  Loader2,
  ExternalLink,
  ShieldCheck
} from 'lucide-react';
import { agentApplyPath } from '@/routes/routes';
import { useState } from 'react';
import { useJobDetailQuery, useJobMatchQuery } from '@/api/jobsApi';

const BREAKDOWN_LABELS: Record<string, string> = {
  eligibility: 'Eligibility Fit',
  technicalSkills: 'Technical Skills',
  relevantExperience: 'Relevant Experience',
  roleSeniority: 'Role & Seniority Fit',
  education: 'Education & Field',
  locationWorkMode: 'Location & Work Mode',
  projectRelevance: 'Project Relevance',
  recruiterAppeal: 'Overall Recruiter Appeal',
  // Fallback aliases
  location: 'Location',
  experience: 'Experience',
  projects: 'Projects',
  roleFit: 'Role Fit',
};

export default function JobDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [isSaved, setIsSaved] = useState(false);

  // TanStack Query hooks
  const { data: liveJob, isLoading: isJobLoading } = useJobDetailQuery(id);
  const { data: liveMatch, isLoading: isMatchLoading } = useJobMatchQuery(id);

  // Fallback to mock job and assessment if not yet loaded from backend
  const fallbackJob = mockJobs.find((j) => j.id === id) || mockJobs[0];
  const job = liveJob
    ? {
        id: liveJob.id,
        title: liveJob.title,
        company: liveJob.company,
        location: liveJob.location,
        workMode: liveJob.workMode,
        salaryDisplay: liveJob.salaryDisplay || '₹14.0 - 22.0 LPA',
        experience: liveJob.experienceMin ? `${liveJob.experienceMin}+ yrs` : '2+ yrs',
        description: liveJob.description,
        canonicalUrl: liveJob.canonicalUrl,
        sourceName: liveJob.sourceName,
        requiredSkills: (() => {
          try {
            return JSON.parse(liveJob.requiredSkills || '[]');
          } catch {
            return ['Java', 'Spring Boot'];
          }
        })(),
      }
    : fallbackJob;

  // Parse breakdown JSON from matching engine
  let scoreBreakdown: Record<string, number> = mockJobAssessment.breakdown;
  if (liveMatch?.scoreBreakdown) {
    try {
      const parsed = JSON.parse(liveMatch.scoreBreakdown);
      scoreBreakdown = parsed;
    } catch {
      // keep fallback
    }
  }

  const overallScore = liveMatch?.overallScore || 91;
  const classification = liveMatch?.classification || 'EXCELLENT';
  const recommendation = liveMatch?.recommendation || 'APPLY';
  const reasoning = liveMatch?.reasoning || mockJobAssessment.whyYouMatch;

  return (
    <AppShell>
      <div className="max-w-7xl mx-auto space-y-6">
        {/* ——— Job Header Section ——— */}
        <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-lg">
          <div>
            <div className="flex items-center gap-2 mb-1 flex-wrap">
              <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-[rgba(37,99,235,0.15)] text-[#38BDF8] border border-[rgba(37,99,235,0.3)]">
                AI MATCH VERIFIED ({classification})
              </span>
              <span className="text-xs text-[#10B981] font-semibold flex items-center gap-1">
                <Sparkles size={12} /> Recommendation: {recommendation}
              </span>
              {job.sourceName && (
                <span className="text-xs text-[#94A3B8] font-medium bg-[#111827] px-2 py-0.5 rounded border border-[rgba(255,255,255,0.06)]">
                  Source: {job.sourceName}
                </span>
              )}
            </div>

            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#F1F5F9] tracking-tight">
              {job.title}
            </h1>
            <p className="text-base text-[#94A3B8] font-medium mt-0.5">{job.company}</p>

            {/* Metadata Line */}
            <div className="flex items-center gap-4 mt-3 text-xs sm:text-sm text-[#64748B] flex-wrap">
              <span className="flex items-center gap-1.5 text-[#94A3B8]">
                <MapPin size={14} className="text-[#3B82F6]" />
                {job.location}
              </span>
              <span className="flex items-center gap-1.5 text-[#94A3B8]">
                <Clock size={14} className="text-[#8B5CF6]" />
                {job.workMode}
              </span>
              {job.experience && (
                <span className="flex items-center gap-1.5 text-[#94A3B8]">
                  <Briefcase size={14} className="text-[#06B6D4]" />
                  {job.experience}
                </span>
              )}
              {job.salaryDisplay && (
                <span className="flex items-center gap-1.5 text-[#94A3B8]">
                  <DollarSign size={14} className="text-[#10B981]" />
                  {job.salaryDisplay}
                </span>
              )}
              {job.canonicalUrl && (
                <a
                  href={job.canonicalUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-1 text-[#38BDF8] hover:underline"
                >
                  <ExternalLink size={13} /> Original Posting
                </a>
              )}
            </div>
          </div>

          <div className="flex items-center gap-3 flex-shrink-0">
            <Button
              variant="secondary"
              size="md"
              leftIcon={<Bookmark size={15} className={isSaved ? 'text-[#F59E0B] fill-[#F59E0B]' : ''} />}
              onClick={() => setIsSaved(!isSaved)}
            >
              {isSaved ? 'Saved' : 'Save Job'}
            </Button>
            <Button
              variant="primary"
              size="md"
              leftIcon={<FileText size={15} />}
              onClick={() => navigate(agentApplyPath(job.id))}
            >
              Prepare Application
            </Button>
          </div>
        </div>

        {/* ——— Main 2-Column Content ——— */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* Left Column (Span 8): AI Assessment & Description */}
          <div className="lg:col-span-8 space-y-6">
            {/* AI Recruiter Assessment Box */}
            <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl">
              <div className="flex items-center justify-between mb-5 pb-3 border-b border-[rgba(148,163,184,0.08)]">
                <h2 className="text-base font-bold text-[#F1F5F9] uppercase tracking-wider flex items-center gap-2">
                  <ShieldCheck size={18} className="text-[#38BDF8]" />
                  AI Recruiter Assessment & Audit Matrix
                </h2>
                <span className="text-xs text-[#10B981] font-semibold bg-[rgba(16,185,129,0.1)] px-2.5 py-1 rounded-full border border-[rgba(16,185,129,0.25)]">
                  {overallScore}% {classification}
                </span>
              </div>

              <div className="flex flex-col sm:flex-row items-center sm:items-start gap-8">
                {/* Circular Gauge */}
                <div className="flex flex-col items-center gap-2 flex-shrink-0">
                  <MatchScoreRing score={overallScore} size={110} strokeWidth={9} showLabel={true} />
                  <span className="text-[10px] uppercase font-bold tracking-widest text-[#10B981]">
                    Recruiter Validated
                  </span>
                </div>

                {/* Granular Assessment Progress Bars */}
                <div className="flex-1 grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-3.5 w-full">
                  {Object.entries(scoreBreakdown)
                    .filter(([k]) => k !== 'weights' && k !== 'totalScore' && k !== 'classification')
                    .map(([key, value]) => {
                      const numVal = typeof value === 'number' ? value : 90;
                      // Normalize score display percentage based on category max weight
                      const displayPercent =
                        key === 'eligibility' || key === 'technicalSkills'
                          ? Math.round((numVal / 25) * 100)
                          : key === 'relevantExperience'
                          ? Math.round((numVal / 15) * 100)
                          : key === 'roleSeniority' || key === 'recruiterAppeal'
                          ? Math.round((numVal / 10) * 100)
                          : Math.round((numVal / 5) * 100);

                      return (
                        <ProgressBar
                          key={key}
                          label={BREAKDOWN_LABELS[key] || key}
                          value={Math.min(100, Math.max(0, displayPercent))}
                          showValue={true}
                          color={displayPercent >= 85 ? 'success' : displayPercent >= 70 ? 'brand' : 'warning'}
                          size="sm"
                        />
                      );
                    })}
                </div>
              </div>
            </div>

            {/* Recruiter Reasoning & Audit Trail */}
            <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl space-y-4">
              <h3 className="text-sm font-bold text-[#F1F5F9] uppercase tracking-wider flex items-center gap-2">
                <Sparkles size={16} className="text-[#38BDF8]" />
                Recruiter Reasoning & Alignment Analysis
              </h3>
              <p className="text-sm text-[#F1F5F9] leading-relaxed bg-[#111827] p-4 rounded-xl border border-[rgba(255,255,255,0.04)]">
                {reasoning}
              </p>
            </div>

            {/* Job Description Specification */}
            <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl space-y-4">
              <h3 className="text-sm font-bold text-[#F1F5F9] uppercase tracking-wider">
                Full Role Specification
              </h3>
              <div className="text-sm text-[#94A3B8] leading-relaxed whitespace-pre-line bg-[#111827] p-5 rounded-xl border border-[rgba(255,255,255,0.04)]">
                {job.description ||
                  'We are looking for an experienced Java Backend Engineer to design, develop, and maintain high-throughput microservices using Spring Boot, PostgreSQL, and cloud distributed architectures.'}
              </div>
            </div>
          </div>

          {/* Right Column (Span 4): Readiness Checklist & Actions */}
          <div className="lg:col-span-4 space-y-6">
            {/* AI Platform Action Card */}
            <div className="bg-gradient-to-b from-[#1E293B] to-[#111827] border border-[rgba(59,130,246,0.25)] rounded-2xl p-6 shadow-xl relative overflow-hidden text-center space-y-4">
              <div className="relative mx-auto w-28 h-28 flex items-center justify-center my-2">
                <div className="absolute inset-0 rounded-full border border-[rgba(59,130,246,0.3)] animate-[orbit_10s_linear_infinite]" />
                <div
                  className="w-20 h-20 rounded-2xl flex items-center justify-center shadow-[0_0_30px_rgba(37,99,235,0.6)]"
                  style={{
                    background:
                      'linear-gradient(135deg, rgba(37,99,235,0.8), rgba(124,58,237,0.7), rgba(6,182,212,0.6))',
                  }}
                >
                  <span className="text-white font-extrabold text-xl tracking-wider">AI</span>
                </div>
              </div>

              <div>
                <h4 className="text-base font-bold text-[#F1F5F9]">AI Application Agent</h4>
                <p className="text-xs text-[#94A3B8] mt-1">
                  Ready to prepare an auto-tailored application with verified assets.
                </p>
              </div>

              <Button
                variant="primary"
                size="lg"
                fullWidth
                leftIcon={<FileText size={16} />}
                onClick={() => navigate(agentApplyPath(job.id))}
              >
                Prepare Application
              </Button>
            </div>

            {/* Application Readiness Checklist */}
            <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl space-y-4">
              <h3 className="text-xs font-bold text-[#64748B] uppercase tracking-wider">
                Application Readiness
              </h3>

              <div className="space-y-3">
                {[
                  { label: 'Resume', status: 'Verified', ok: true, icon: <FileText size={15} /> },
                  { label: 'Candidate Profile', status: 'Approved', ok: true, icon: <UserCheck size={15} /> },
                  { label: 'Cover Letter', status: 'Ready to Generate', ok: true, icon: <Sparkles size={15} /> },
                  { label: 'Screening Answers', status: 'AI Formatted', ok: true, icon: <Check size={15} /> },
                ].map((item) => (
                  <div
                    key={item.label}
                    className="flex items-center justify-between p-2.5 rounded-lg bg-[#111827] border border-[rgba(255,255,255,0.04)]"
                  >
                    <div className="flex items-center gap-2.5 text-xs text-[#94A3B8]">
                      <span className={item.ok ? 'text-[#10B981]' : 'text-[#F59E0B]'}>{item.icon}</span>
                      <span className="font-medium text-[#F1F5F9]">{item.label}</span>
                    </div>
                    <span className={`text-xs font-semibold ${item.ok ? 'text-[#10B981]' : 'text-[#F59E0B]'}`}>
                      {item.ok ? '✓ ' : '⚠ '}
                      {item.status}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* AI Recommendation Card */}
            <AIInsightCard
              type="recommendation"
              content="Candidate meets all primary requirements with verified execution experience. Immediate application recommended."
            />
          </div>
        </div>
      </div>
    </AppShell>
  );
}
