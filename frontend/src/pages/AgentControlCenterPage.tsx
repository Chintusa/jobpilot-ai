import { AppShell } from '@/components/layout/AppShell';
import { PageHeader } from '@/components/layout/PageHeader';
import { AIAgentStatus, AgentActivityLog } from '@/components/ai';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { mockAgentConfig } from '@/data/mockData';
import { useStore } from '@/app/store';
import { useState } from 'react';
import {
  Shield,
  Play,
  Pause,
  RefreshCw,
  Sparkles,
  CheckCircle2,
  AlertCircle,
  FileCode,
  Layers,
  Clock,
  ChevronDown,
  ChevronUp
} from 'lucide-react';
import { useSearchRunsQuery, useStartSearchRunMutation, type SearchRun } from '@/api/jobsApi';

function AgentToggle({
  checked,
  onChange,
  label,
}: {
  checked: boolean;
  onChange: (v: boolean) => void;
  label: string;
}) {
  return (
    <div className="flex items-center justify-between py-2.5">
      <div className="flex items-center gap-2 text-sm text-[#F1F5F9]">
        <span className="text-[#10B981]">✓</span>
        {label}
      </div>
      <div
        className={`relative w-12 h-6 rounded-full cursor-pointer transition-all duration-200 border ${
          checked
            ? 'bg-[#2563EB] border-[rgba(37,99,235,0.5)]'
            : 'bg-[#243047] border-[rgba(148,163,184,0.2)]'
        }`}
        onClick={() => onChange(!checked)}
        role="switch"
        aria-checked={checked}
      >
        <div
          className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform duration-200 ${
            checked ? 'translate-x-6' : 'translate-x-0.5'
          }`}
        />
        <span
          className={`absolute text-[9px] font-bold ${
            checked ? 'left-1.5 text-white' : 'right-1.5 text-[#64748B]'
          } top-1`}
        >
          {checked ? 'ON' : 'OFF'}
        </span>
      </div>
    </div>
  );
}

export default function AgentControlCenterPage() {
  const { agentStatus, setAgentStatus } = useStore();
  const [config, setConfig] = useState(mockAgentConfig);
  const [matchScore, setMatchScore] = useState(85);
  const [selectedRunLog, setSelectedRunLog] = useState<string | null>(null);

  // TanStack Query integration
  const { data: searchRuns, isLoading: isRunsLoading } = useSearchRunsQuery();
  const startRunMutation = useStartSearchRunMutation();

  const updateJobDiscovery = (key: keyof typeof config.jobDiscovery, value: boolean) => {
    setConfig((prev) => ({
      ...prev,
      jobDiscovery: { ...prev.jobDiscovery, [key]: value },
    }));
  };

  const updateApplications = (key: keyof typeof config.applications, value: boolean) => {
    setConfig((prev) => ({
      ...prev,
      applications: { ...prev.applications, [key]: value as never },
    }));
  };

  const toggleAgent = () => {
    setAgentStatus(agentStatus === 'ACTIVE' ? 'PAUSED' : 'ACTIVE');
  };

  return (
    <AppShell>
      <PageHeader
        title="AI Agent Control Center"
        subtitle="Manage autonomous search, application filters, thresholds, and guardrails"
        actions={
          <div className="flex items-center gap-3">
            <Button
              variant="primary"
              size="sm"
              leftIcon={
                <Sparkles
                  size={14}
                  className={startRunMutation.isPending ? 'animate-spin' : ''}
                />
              }
              onClick={() => startRunMutation.mutate()}
              disabled={startRunMutation.isPending}
            >
              {startRunMutation.isPending ? 'Executing 9 Steps...' : 'Run Search Agent Now'}
            </Button>
            <Button
              variant={agentStatus === 'ACTIVE' ? 'secondary' : 'primary'}
              size="sm"
              leftIcon={agentStatus === 'ACTIVE' ? <Pause size={14} /> : <Play size={14} />}
              onClick={toggleAgent}
            >
              {agentStatus === 'ACTIVE' ? 'Pause Agent' : 'Activate Agent'}
            </Button>
          </div>
        }
      />

      {/* Main control center grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-12 gap-6 items-start">
        {/* JOB DISCOVERY — Left Top Card */}
        <Card variant="default" padding="md" className="lg:col-span-4 space-y-2">
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-xs font-bold tracking-widest text-[#F1F5F9] uppercase">
              Job Discovery
            </h2>
            <span className="text-[10px] font-semibold text-[#10B981] bg-[rgba(16,185,129,0.1)] px-2 py-0.5 rounded-full border border-[rgba(16,185,129,0.2)]">
              4 ACTIVE
            </span>
          </div>

          <div className="divide-y divide-[rgba(148,163,184,0.08)]">
            <AgentToggle
              label="Search automatically"
              checked={config.jobDiscovery.searchAutomatically}
              onChange={(v) => updateJobDiscovery('searchAutomatically', v)}
            />
            <AgentToggle
              label="Search multiple sources"
              checked={config.jobDiscovery.multipleSourceSearch}
              onChange={(v) => updateJobDiscovery('multipleSourceSearch', v)}
            />
            <AgentToggle
              label="Remove duplicates"
              checked={config.jobDiscovery.removeDuplicates}
              onChange={(v) => updateJobDiscovery('removeDuplicates', v)}
            />
            <AgentToggle
              label="Analyze new jobs"
              checked={config.jobDiscovery.analyzeNewJobs}
              onChange={(v) => updateJobDiscovery('analyzeNewJobs', v)}
            />
          </div>
        </Card>

        {/* Center Globe / AI Hologram Visualization */}
        <div className="lg:col-span-4 flex flex-col items-center justify-center p-6 bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl shadow-xl space-y-6">
          <div className="relative flex items-center justify-center py-4">
            {/* Outer animated glow rings */}
            <div className="absolute w-[260px] h-[260px] rounded-full border border-[rgba(6,182,212,0.15)] animate-[orbit_12s_linear_infinite]" />
            <div className="absolute w-[210px] h-[210px] rounded-full border border-[rgba(37,99,235,0.2)] animate-[orbit_8s_linear_infinite_reverse]" />

            {/* Globe body */}
            <div
              className="w-44 h-44 rounded-full relative flex items-center justify-center"
              style={{
                background:
                  'radial-gradient(ellipse at 35% 35%, rgba(6,182,212,0.5) 0%, rgba(37,99,235,0.4) 40%, rgba(30,41,59,0.9) 70%)',
                boxShadow:
                  '0 0 50px rgba(6,182,212,0.35), 0 0 100px rgba(37,99,235,0.2)',
                border: '1px solid rgba(6,182,212,0.3)',
              }}
            >
              {/* Grid lines effect */}
              <div className="absolute inset-0 rounded-full overflow-hidden opacity-30">
                {[0, 30, 60, 90, 120, 150].map((deg) => (
                  <div
                    key={deg}
                    className="absolute inset-0 border border-[rgba(6,182,212,0.3)] rounded-full"
                    style={{ transform: `rotate(${deg}deg) scaleX(0.3)` }}
                  />
                ))}
              </div>

              {/* Active label in center of globe */}
              <div
                className="px-4 py-1.5 rounded-lg font-bold text-xs tracking-widest uppercase shadow-lg"
                style={{
                  background:
                    agentStatus === 'ACTIVE'
                      ? 'rgba(16,185,129,0.18)'
                      : 'rgba(245,158,11,0.18)',
                  border: `1px solid ${
                    agentStatus === 'ACTIVE'
                      ? 'rgba(16,185,129,0.5)'
                      : 'rgba(245,158,11,0.5)'
                  }`,
                  color: agentStatus === 'ACTIVE' ? '#10B981' : '#F59E0B',
                  boxShadow: `0 0 16px ${
                    agentStatus === 'ACTIVE'
                      ? 'rgba(16,185,129,0.4)'
                      : 'rgba(245,158,11,0.4)'
                  }`,
                }}
              >
                {agentStatus}
              </div>
            </div>

            {/* Orbiting node dots */}
            {[0, 60, 120, 180, 240, 300].map((angle) => {
              const rad = (angle * Math.PI) / 180;
              const x = Math.cos(rad) * 115;
              const y = Math.sin(rad) * 115;
              return (
                <div
                  key={angle}
                  className="absolute w-2 h-2 rounded-full bg-[#22D3EE] animate-[ai-pulse_2s_ease-in-out_infinite]"
                  style={{
                    transform: `translate(${x}px, ${y}px)`,
                    boxShadow: '0 0 8px rgba(34,211,238,0.8)',
                    animationDelay: `${(angle / 60) * 0.3}s`,
                  }}
                />
              );
            })}
          </div>

          <div className="flex flex-col items-center gap-2">
            <AIAgentStatus status={agentStatus} variant="pill" showLabel />
            <p className="text-xs text-[#64748B] text-center">
              Agent runs every 30 mins scanning pluggable modular job sources
            </p>
          </div>
        </div>

        {/* APPLICATIONS — Right Top Card */}
        <Card variant="default" padding="md" className="lg:col-span-4 space-y-4">
          <h2 className="text-xs font-bold tracking-widest text-[#F1F5F9] uppercase mb-2">
            Applications
          </h2>

          <div className="space-y-4">
            <AgentToggle
              label="Prepare applications automatically"
              checked={config.applications.prepareAutomatically}
              onChange={(v) => updateApplications('prepareAutomatically', v)}
            />

            <div>
              <label className="text-xs text-[#64748B] mb-1.5 block">
                Final submission mode
              </label>
              <div className="w-full h-10 px-3 flex items-center justify-between bg-[#1E293B] border border-[rgba(239,68,68,0.3)] rounded-lg text-xs text-[#F1F5F9] cursor-pointer hover:border-[rgba(239,68,68,0.5)] transition-colors">
                <span className="text-[#F87171] font-semibold flex items-center gap-1.5">
                  <Shield size={13} /> USER APPROVAL REQUIRED
                </span>
                <span className="text-[#64748B]">▼</span>
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs text-[#64748B]">Daily application limit</label>
                <span className="text-xs font-bold text-[#F1F5F9] bg-[#243047] px-2 py-0.5 rounded">
                  {config.applications.dailyApplicationLimit} / day
                </span>
              </div>
              <div className="flex items-center gap-3">
                <button
                  className="w-7 h-7 rounded-lg bg-[#243047] text-[#94A3B8] flex items-center justify-center text-sm font-bold hover:bg-[#1E2A42] hover:text-white transition-colors cursor-pointer"
                  onClick={() =>
                    setConfig((prev) => ({
                      ...prev,
                      applications: {
                        ...prev.applications,
                        dailyApplicationLimit: Math.max(
                          1,
                          prev.applications.dailyApplicationLimit - 1
                        ),
                      },
                    }))
                  }
                >
                  −
                </button>
                <div className="flex-1 h-2 bg-[#243047] rounded-full overflow-hidden">
                  <div
                    className="h-full bg-[#2563EB] rounded-full transition-all"
                    style={{
                      width: `${(config.applications.dailyApplicationLimit / 20) * 100}%`,
                    }}
                  />
                </div>
                <button
                  className="w-7 h-7 rounded-lg bg-[#243047] text-[#94A3B8] flex items-center justify-center text-sm font-bold hover:bg-[#1E2A42] hover:text-white transition-colors cursor-pointer"
                  onClick={() =>
                    setConfig((prev) => ({
                      ...prev,
                      applications: {
                        ...prev.applications,
                        dailyApplicationLimit: Math.min(
                          20,
                          prev.applications.dailyApplicationLimit + 1
                        ),
                      },
                    }))
                  }
                >
                  +
                </button>
              </div>
            </div>
          </div>
        </Card>

        {/* EXECUTION HISTORY & AUDIT TRAIL — Full Width Card */}
        <div className="lg:col-span-12 bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-[rgba(148,163,184,0.08)]">
            <h2 className="text-sm font-bold tracking-wider text-[#F1F5F9] uppercase flex items-center gap-2">
              <Layers size={16} className="text-[#38BDF8]" />
              Agent Search Runs & Audit Trail
            </h2>
            <span className="text-xs text-[#94A3B8]">
              {searchRuns?.length || 0} Runs Recorded
            </span>
          </div>

          {isRunsLoading && (
            <p className="text-xs text-[#94A3B8]">Loading execution history...</p>
          )}

          {(!searchRuns || searchRuns.length === 0) && !isRunsLoading && (
            <div className="text-center py-6">
              <p className="text-xs text-[#94A3B8] mb-2">No historical agent runs recorded yet.</p>
              <Button
                variant="secondary"
                size="sm"
                onClick={() => startRunMutation.mutate()}
              >
                Run Search Agent Now
              </Button>
            </div>
          )}

          {searchRuns && searchRuns.length > 0 && (
            <div className="space-y-3">
              {searchRuns.map((run: SearchRun) => (
                <div
                  key={run.id}
                  className="bg-[#111827] border border-[rgba(255,255,255,0.04)] rounded-xl p-4 space-y-2 hover:border-[rgba(59,130,246,0.3)] transition-all"
                >
                  <div className="flex items-center justify-between flex-wrap gap-2 text-xs">
                    <div className="flex items-center gap-2">
                      <span className="px-2 py-0.5 rounded-full bg-[rgba(16,185,129,0.15)] text-[#10B981] font-bold border border-[rgba(16,185,129,0.3)]">
                        {run.status}
                      </span>
                      <span className="font-semibold text-[#F1F5F9]">
                        Query: &quot;{run.query}&quot;
                      </span>
                    </div>

                    <div className="flex items-center gap-4 text-[#94A3B8]">
                      <span>Found: <strong className="text-[#F1F5F9]">{run.numberFound}</strong></span>
                      <span>Dupes Removed: <strong className="text-[#F59E0B]">{run.duplicatesRemoved}</strong></span>
                      <span>Filtered: <strong className="text-[#EF4444]">{run.filteredJobs}</strong></span>
                      <span>Matched: <strong className="text-[#10B981]">{run.matchedJobs}</strong></span>
                      <span className="flex items-center gap-1 text-[#64748B]">
                        <Clock size={12} /> {run.durationMs}ms
                      </span>
                    </div>
                  </div>

                  {/* Toggle Audit Log */}
                  <div className="pt-2 border-t border-[rgba(255,255,255,0.04)] flex justify-between items-center">
                    <span className="text-[11px] text-[#64748B]">
                      Executed at: {new Date(run.startedAt).toLocaleString()}
                    </span>
                    <button
                      onClick={() =>
                        setSelectedRunLog(selectedRunLog === run.id ? null : run.id)
                      }
                      className="text-xs text-[#38BDF8] hover:underline flex items-center gap-1 cursor-pointer font-medium"
                    >
                      {selectedRunLog === run.id ? 'Hide 9-Step Audit' : 'View 9-Step Audit'}
                      {selectedRunLog === run.id ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
                    </button>
                  </div>

                  {selectedRunLog === run.id && run.auditLog && (
                    <pre className="text-[11px] text-[#94A3B8] bg-[#0A0F1E] p-3 rounded-lg border border-[rgba(255,255,255,0.06)] overflow-x-auto whitespace-pre-wrap font-mono mt-2 leading-relaxed">
                      {run.auditLog}
                    </pre>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </AppShell>
  );
}
