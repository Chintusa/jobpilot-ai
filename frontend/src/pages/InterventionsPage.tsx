import { AppShell } from '@/components/layout/AppShell';
import { PageHeader } from '@/components/layout/PageHeader';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/overlays';
import { mockInterventions } from '@/data/mockData';
import {
  AlertTriangle,
  Bot,
  Shield,
  CheckCircle2,
  XCircle,
  HelpCircle,
  KeyRound,
  FileCheck,
  ExternalLink,
  Layers,
  Clock
} from 'lucide-react';
import { formatRelativeDate } from '@/utils/format';
import { useState } from 'react';
import {
  useInterventionsQuery,
  useResolveInterventionMutation,
  useCancelInterventionMutation,
  type BackendHumanIntervention
} from '@/api/jobsApi';

const TYPE_CONFIG: Record<
  string,
  { label: string; color: string; icon: any }
> = {
  CAPTCHA: { label: 'CAPTCHA Verification', color: '#F59E0B', icon: <Bot size={18} /> },
  MFA: { label: 'MFA / 2FA Verification', color: '#EF4444', icon: <KeyRound size={18} /> },
  UNKNOWN_QUESTION: { label: 'Screening Question', color: '#38BDF8', icon: <HelpCircle size={18} /> },
  LEGAL_DECLARATION: { label: 'Legal Declaration', color: '#8B5CF6', icon: <FileCheck size={18} /> },
  LEGAL: { label: 'Legal Declaration', color: '#8B5CF6', icon: <FileCheck size={18} /> },
  MISSING_INFO: { label: 'Missing Profile Info', color: '#06B6D4', icon: <AlertTriangle size={18} /> },
  UNSUPPORTED_FLOW: { label: 'Unsupported Flow', color: '#64748B', icon: <AlertTriangle size={18} /> },
  ACCESS_CONTROL: { label: 'Security Check', color: '#EF4444', icon: <Shield size={18} /> },
};

export default function InterventionsPage() {
  const [filterStatus, setFilterStatus] = useState<string>('PENDING');
  const [activeIntervention, setActiveIntervention] = useState<BackendHumanIntervention | null>(null);
  const [answerInput, setAnswerInput] = useState('');

  // TanStack Query hooks
  const { data: serverInterventions, isLoading } = useInterventionsQuery(filterStatus);
  const resolveMutation = useResolveInterventionMutation();
  const cancelMutation = useCancelInterventionMutation();

  // Fallback to mock data if empty
  const interventions: BackendHumanIntervention[] =
    serverInterventions && serverInterventions.length > 0
      ? serverInterventions
      : mockInterventions.map((mi) => ({
          id: mi.id,
          jobTitle: mi.jobTitle,
          company: mi.company,
          reason: mi.type,
          type: mi.type,
          description: mi.description,
          status: mi.status,
          requiredInput: 'TEXT',
          createdAt: mi.createdAt,
        }));

  const pendingList = interventions.filter((i) => i.status === 'PENDING');

  const handleOpenResolve = (item: BackendHumanIntervention) => {
    setActiveIntervention(item);
    setAnswerInput('');
  };

  const handleConfirmResolve = () => {
    if (!activeIntervention) return;
    resolveMutation.mutate(
      {
        interventionId: activeIntervention.id,
        resolutionPayload: answerInput || 'USER_CONFIRMED',
      },
      {
        onSuccess: () => setActiveIntervention(null),
      }
    );
  };

  const handleCancelFlow = (id: string) => {
    cancelMutation.mutate(id);
  };

  return (
    <AppShell>
      <PageHeader
        title="Human Intervention Center"
        subtitle="Review security, verification, and critical decisions requiring candidate authorization"
        actions={
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-[rgba(239,68,68,0.1)] border border-[rgba(239,68,68,0.25)]">
            <AlertTriangle size={14} className="text-[#EF4444]" />
            <span className="text-xs font-bold text-[#EF4444]">
              {pendingList.length} Pending Actions
            </span>
          </div>
        }
      />

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 mb-6 border-b border-[rgba(148,163,184,0.08)] pb-3">
        {['PENDING', 'RESOLVED', 'CANCELLED', 'ALL'].map((st) => (
          <button
            key={st}
            onClick={() => setFilterStatus(st)}
            className={`px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              filterStatus === st
                ? 'bg-[#2563EB] text-white shadow-md'
                : 'text-[#94A3B8] hover:text-[#F1F5F9] hover:bg-[#1A2235]'
            }`}
          >
            {st}
          </button>
        ))}
      </div>

      {interventions.length === 0 ? (
        <div className="text-center py-16 bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-8 shadow-xl">
          <div className="w-16 h-16 rounded-2xl bg-[rgba(16,185,129,0.15)] flex items-center justify-center text-[#10B981] mx-auto mb-4">
            <Shield size={32} />
          </div>
          <h3 className="text-xl font-bold text-[#F1F5F9] mb-2">No Interventions Found</h3>
          <p className="text-sm text-[#94A3B8] max-w-md mx-auto">
            Your AI Agent is running within all authorized boundaries and safety guardrails.
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {interventions.map((intervention) => {
            const key = intervention.reason || intervention.type || 'UNKNOWN_QUESTION';
            const cfg = TYPE_CONFIG[key] || TYPE_CONFIG.UNKNOWN_QUESTION;

            return (
              <div
                key={intervention.id}
                className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl hover:border-[rgba(59,130,246,0.3)] transition-all"
              >
                <div className="flex flex-col sm:flex-row items-start gap-4">
                  <div
                    className="w-12 h-12 rounded-xl flex items-center justify-center text-2xl flex-shrink-0"
                    style={{
                      background: `${cfg.color}22`,
                      border: `1px solid ${cfg.color}44`,
                      color: cfg.color,
                    }}
                  >
                    {cfg.icon}
                  </div>

                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1 flex-wrap">
                      <span className="text-sm font-bold" style={{ color: cfg.color }}>
                        {cfg.label}
                      </span>
                      <span className="text-xs text-[#64748B]">·</span>
                      <span className="text-sm font-bold text-[#F1F5F9]">
                        {intervention.jobTitle || 'Carrier Application'}
                      </span>
                      <span className="text-xs text-[#94A3B8]">
                        at {intervention.company || 'Employer Portal'}
                      </span>
                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded-full ml-auto ${
                          intervention.status === 'PENDING'
                            ? 'bg-[rgba(245,158,11,0.2)] text-[#F59E0B] border border-[rgba(245,158,11,0.3)]'
                            : intervention.status === 'RESOLVED'
                            ? 'bg-[rgba(16,185,129,0.2)] text-[#10B981] border border-[rgba(16,185,129,0.3)]'
                            : 'bg-[rgba(239,68,68,0.2)] text-[#EF4444] border border-[rgba(239,68,68,0.3)]'
                        }`}
                      >
                        {intervention.status}
                      </span>
                    </div>

                    <p className="text-sm text-[#94A3B8] mb-3 leading-relaxed">
                      {intervention.description}
                    </p>

                    <div className="flex items-center gap-2 text-xs text-[#64748B]">
                      <span className="flex items-center gap-1 text-[#38BDF8]">
                        <Bot size={12} /> Playwright Application Worker
                      </span>
                      <span>·</span>
                      <span className="flex items-center gap-1">
                        <Clock size={12} /> Triggered {formatRelativeDate(intervention.createdAt)}
                      </span>
                    </div>
                  </div>

                  {intervention.status === 'PENDING' && (
                    <div className="flex sm:flex-col gap-2 flex-shrink-0 w-full sm:w-auto pt-3 sm:pt-0 border-t sm:border-t-0 border-[rgba(148,163,184,0.08)]">
                      <Button
                        size="sm"
                        variant="primary"
                        className="flex-1 sm:flex-none text-xs font-semibold"
                        onClick={() => handleOpenResolve(intervention)}
                      >
                        Inspect & Answer
                      </Button>
                      <Button
                        size="sm"
                        variant="secondary"
                        className="flex-1 sm:flex-none text-xs font-semibold"
                        onClick={() => handleCancelFlow(intervention.id)}
                        disabled={cancelMutation.isPending}
                      >
                        Cancel Flow
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Intervention Resolution & Context Inspection Modal */}
      <Modal
        isOpen={Boolean(activeIntervention)}
        onClose={() => setActiveIntervention(null)}
        title={
          activeIntervention
            ? `${TYPE_CONFIG[activeIntervention.reason || activeIntervention.type || 'UNKNOWN_QUESTION']?.label || 'Human Intervention'}`
            : 'Intervention'
        }
        size="md"
      >
        {activeIntervention && (
          <div className="space-y-4 text-xs">
            {/* Target Job Context */}
            <div className="p-3.5 rounded-xl bg-[#111827] border border-[rgba(255,255,255,0.06)] space-y-1">
              <p className="font-bold text-[#F1F5F9]">
                {activeIntervention.jobTitle} @ {activeIntervention.company}
              </p>
              <p className="text-[#94A3B8]">{activeIntervention.description}</p>
            </div>

            {/* Zero-Fabrication Rule Banner */}
            <div className="p-3 rounded-xl bg-[rgba(59,130,246,0.1)] border border-[rgba(59,130,246,0.25)] flex items-start gap-2.5 text-[#93C5FD]">
              <Shield size={16} className="text-[#38BDF8] flex-shrink-0 mt-0.5" />
              <span>
                <strong>Safety Policy Active:</strong> Never bypass CAPTCHA, never bypass MFA, never fabricate candidate information. Please provide verified authorization.
              </span>
            </div>

            {/* MFA OTP Input */}
            {(activeIntervention.reason === 'MFA' || activeIntervention.type === 'MFA') && (
              <div className="space-y-2">
                <label className="text-[#F1F5F9] font-semibold block">
                  Enter 6-Digit OTP / MFA Verification Code
                </label>
                <input
                  type="text"
                  maxLength={6}
                  placeholder="123456"
                  value={answerInput}
                  onChange={(e) => setAnswerInput(e.target.value)}
                  className="w-full text-center text-lg tracking-widest font-mono rounded-lg bg-[#111827] border border-[rgba(148,163,184,0.2)] p-2.5 text-[#F1F5F9] focus:border-[#2563EB]"
                />
              </div>
            )}

            {/* CAPTCHA Solve Confirmation */}
            {(activeIntervention.reason === 'CAPTCHA' || activeIntervention.type === 'CAPTCHA') && (
              <div className="space-y-3">
                <p className="text-[#94A3B8]">
                  Carrier portal requires manual security challenge solve. Please solve the challenge in the isolated browser or confirm solve below:
                </p>
                <div className="p-4 rounded-xl bg-[#111827] text-center border border-[rgba(16,185,129,0.3)]">
                  <span className="text-[#10B981] font-bold">
                    ✓ Manual Challenge Solved & Ready to Resume
                  </span>
                </div>
              </div>
            )}

            {/* Screening Question Input */}
            {(activeIntervention.reason === 'UNKNOWN_QUESTION' ||
              activeIntervention.type === 'UNKNOWN_QUESTION' ||
              activeIntervention.reason === 'MISSING_INFO') && (
              <div className="space-y-2">
                <label className="text-[#F1F5F9] font-semibold block">
                  Your Verified Response
                </label>
                <textarea
                  value={answerInput}
                  onChange={(e) => setAnswerInput(e.target.value)}
                  placeholder="Provide your verified answer for this employer question..."
                  rows={3}
                  className="w-full rounded-lg bg-[#111827] border border-[rgba(148,163,184,0.2)] p-3 text-sm text-[#F1F5F9] focus:outline-none focus:border-[#2563EB]"
                />
              </div>
            )}

            {/* Modal Actions */}
            <div className="flex justify-end gap-2 pt-2">
              <Button
                variant="secondary"
                size="sm"
                onClick={() => setActiveIntervention(null)}
              >
                Cancel
              </Button>
              <Button
                variant="primary"
                size="sm"
                onClick={handleConfirmResolve}
                disabled={resolveMutation.isPending}
              >
                {resolveMutation.isPending ? 'Resuming...' : 'Provide Answer & Resume Worker'}
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </AppShell>
  );
}
