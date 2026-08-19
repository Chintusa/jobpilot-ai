import { cn } from '@/utils/cn';
import type { AgentStatus, ActivityEntry, LogEntry, ScreeningQuestion } from '@/types';
import { getMatchColor, getMatchLabel } from '@/utils/matchScore';
import { CheckCircle2, Circle, Loader2, Zap, Lightbulb } from 'lucide-react';
import { Button } from '@/components/ui/Button';

// ============================================================
// AIOrb — The signature AI visual
// ============================================================
interface AIOrbProps {
  size?: 'sm' | 'md' | 'lg' | 'xl';
  status?: 'active' | 'busy' | 'idle';
  label?: string;
  className?: string;
}

const orbSizes = {
  sm: { container: 80, core: 60 },
  md: { container: 120, core: 90 },
  lg: { container: 160, core: 120 },
  xl: { container: 220, core: 160 },
};

export function AIOrb({ size = 'md', status = 'active', label, className }: AIOrbProps) {
  const { container, core } = orbSizes[size];
  const glowColor = status === 'active' ? 'rgba(37,99,235,0.6)' : status === 'busy' ? 'rgba(245,158,11,0.5)' : 'rgba(100,116,139,0.3)';

  return (
    <div className={cn('flex flex-col items-center gap-3', className)}>
      <div
        className="relative flex items-center justify-center"
        style={{ width: container, height: container }}
      >
        {/* Outer ring */}
        <div
          className="absolute rounded-full border border-[rgba(59,130,246,0.25)] animate-[orbit_10s_linear_infinite]"
          style={{ width: container, height: container * 0.4, top: '30%' }}
        />
        {/* Middle ring */}
        <div
          className="absolute rounded-full border border-[rgba(139,92,246,0.2)] animate-[orbit_6s_linear_infinite_reverse]"
          style={{ width: container * 0.7, height: container * 0.3, top: '35%' }}
        />
        {/* Core sphere */}
        <div
          className="relative rounded-full animate-[ai-pulse_3s_ease-in-out_infinite] flex items-center justify-center"
          style={{
            width: core,
            height: core,
            background: 'radial-gradient(ellipse at 35% 35%, rgba(99,179,237,0.9) 0%, rgba(99,102,241,0.8) 40%, rgba(139,92,246,0.6) 70%, rgba(22,211,238,0.3) 100%)',
            boxShadow: `0 0 40px ${glowColor}, 0 0 80px ${glowColor.replace('0.6', '0.3')}`,
          }}
        >
          <span className="text-white font-bold" style={{ fontSize: core * 0.22 }}>AI</span>
        </div>
        {/* Particle dots */}
        {status === 'active' && (
          <>
            <div className="absolute w-2 h-2 rounded-full bg-[#22D3EE] top-[10%] right-[15%] animate-[ai-pulse_2s_ease-in-out_infinite]" />
            <div className="absolute w-1.5 h-1.5 rounded-full bg-[#818CF8] bottom-[15%] left-[10%] animate-[ai-pulse_2.5s_ease-in-out_infinite_0.5s]" />
            <div className="absolute w-1 h-1 rounded-full bg-[#60A5FA] top-[40%] left-[5%] animate-[ai-pulse_1.8s_ease-in-out_infinite_1s]" />
          </>
        )}
      </div>
      {label && (
        <span className="text-xs font-semibold tracking-[0.1em] text-[#22D3EE] uppercase">
          {label}
        </span>
      )}
    </div>
  );
}

// ============================================================
// AIAgentStatus — Pulsing status indicator
// ============================================================
interface AIAgentStatusProps {
  status: AgentStatus;
  variant?: 'pill' | 'badge' | 'dot';
  showLabel?: boolean;
  className?: string;
}

const statusConfig: Record<AgentStatus, { label: string; color: string; pulse: string }> = {
  ACTIVE:   { label: 'AI agent active',  color: '#10B981', pulse: 'rgba(16,185,129,0.4)' },
  BUSY:     { label: 'AI agent busy',    color: '#F59E0B', pulse: 'rgba(245,158,11,0.4)' },
  PAUSED:   { label: 'AI agent paused',  color: '#64748B', pulse: 'rgba(100,116,139,0.3)' },
  INACTIVE: { label: 'AI agent offline', color: '#64748B', pulse: 'transparent' },
  ERROR:    { label: 'AI agent error',   color: '#EF4444', pulse: 'rgba(239,68,68,0.4)' },
};

export function AIAgentStatus({ status, variant = 'pill', showLabel = true, className }: AIAgentStatusProps) {
  const cfg = statusConfig[status];

  if (variant === 'dot') {
    return (
      <div className={cn('relative flex items-center', className)}>
        <div
          className="absolute w-4 h-4 rounded-full animate-[status-pulse_2s_ease-in-out_infinite]"
          style={{ backgroundColor: cfg.pulse }}
        />
        <div className="relative w-2.5 h-2.5 rounded-full" style={{ backgroundColor: cfg.color }} />
      </div>
    );
  }

  return (
    <div
      className={cn(
        'inline-flex items-center gap-2 rounded-full px-3 py-1.5',
        'border',
        className
      )}
      style={{
        backgroundColor: cfg.pulse.replace('0.4)', '0.12)'),
        borderColor: cfg.pulse.replace('0.4)', '0.3)'),
      }}
    >
      <div className="relative flex items-center">
        <div
          className="absolute w-3.5 h-3.5 rounded-full animate-[status-pulse_2s_ease-in-out_infinite]"
          style={{ backgroundColor: cfg.pulse }}
        />
        <div className="relative w-2 h-2 rounded-full" style={{ backgroundColor: cfg.color }} />
      </div>
      {showLabel && (
        <span className="text-xs font-medium" style={{ color: cfg.color }}>
          {cfg.label}
        </span>
      )}
    </div>
  );
}

// ============================================================
// MatchScoreRing — Animated circular progress
// ============================================================
interface MatchScoreRingProps {
  score: number;
  size?: number;
  strokeWidth?: number;
  showLabel?: boolean;
  className?: string;
}

export function MatchScoreRing({
  score,
  size = 80,
  strokeWidth = 8,
  showLabel = true,
  className,
}: MatchScoreRingProps) {
  const color = getMatchColor(score);
  const label = getMatchLabel(score);
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const dash = (score / 100) * circumference;

  return (
    <div className={cn('flex flex-col items-center gap-1', className)}>
      <div className="relative" style={{ width: size, height: size }}>
        <svg width={size} height={size} className="-rotate-90">
          {/* Track */}
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            fill="none"
            stroke="#243047"
            strokeWidth={strokeWidth}
          />
          {/* Fill */}
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            fill="none"
            stroke={color}
            strokeWidth={strokeWidth}
            strokeLinecap="round"
            strokeDasharray={`${dash} ${circumference - dash}`}
            style={{ filter: `drop-shadow(0 0 8px ${color}80)` }}
          />
        </svg>
        {/* Center text */}
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="font-bold leading-none" style={{ fontSize: size * 0.22, color }}>
            {score}%
          </span>
        </div>
      </div>
      {showLabel && (
        <span className="text-[10px] font-semibold tracking-wide" style={{ color }}>
          {label}
        </span>
      )}
    </div>
  );
}

// ============================================================
// AIInsightCard
// ============================================================
interface AIInsightCardProps {
  type?: 'insight' | 'recommendation' | 'notification';
  title?: string;
  content: string;
  className?: string;
}

export function AIInsightCard({ type = 'insight', title, content, className }: AIInsightCardProps) {
  const accent = type === 'recommendation' ? '#7C3AED' : '#2563EB';
  const icon = type === 'recommendation' ? <Zap size={14} /> : <Lightbulb size={14} />;
  const typeLabel = type === 'recommendation' ? 'Recommendation' : 'AI Insight';

  return (
    <div
      className={cn(
        'rounded-xl p-4 border border-[rgba(255,255,255,0.06)]',
        'bg-gradient-to-br from-[rgba(37,99,235,0.08)] to-[rgba(124,58,237,0.04)]',
        className
      )}
      style={{ borderLeft: `3px solid ${accent}` }}
    >
      <div className="flex items-center gap-1.5 mb-2">
        <span style={{ color: '#22D3EE' }}>{icon}</span>
        <span className="text-[11px] font-semibold tracking-wider uppercase text-[#22D3EE]">
          {title || typeLabel}
        </span>
      </div>
      <p className="text-sm text-[#94A3B8] leading-relaxed">{content}</p>
    </div>
  );
}

// ============================================================
// AIActivityTimeline
// ============================================================
interface AIActivityTimelineProps {
  entries: ActivityEntry[];
  orientation?: 'horizontal' | 'vertical';
  className?: string;
}

export function AIActivityTimeline({ entries, orientation = 'vertical', className }: AIActivityTimelineProps) {
  if (orientation === 'horizontal') {
    return (
      <div className={cn('flex items-start gap-6 overflow-x-auto pb-2', className)}>
        {entries.map((entry, i) => (
          <div key={entry.id} className="flex flex-col items-center gap-2 min-w-[140px]">
            <div className="flex items-center w-full">
              <div
                className="w-3 h-3 rounded-full flex-shrink-0"
                style={{ backgroundColor: entry.type === 'warning' ? '#F59E0B' : '#2563EB' }}
              />
              {i < entries.length - 1 && (
                <div className="h-px flex-1 bg-[rgba(37,99,235,0.3)]" />
              )}
            </div>
            <div className="text-center">
              <div className="text-[11px] text-[#64748B] mb-0.5">{entry.timestamp}</div>
              <div className="text-xs text-[#F1F5F9]">{entry.message}</div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className={cn('flex flex-col gap-3', className)}>
      {entries.map((entry) => (
        <div key={entry.id} className="flex items-start gap-3">
          <div className="flex flex-col items-center gap-1 pt-0.5">
            <div
              className={cn(
                'w-2.5 h-2.5 rounded-full flex-shrink-0',
                entry.isActive && 'animate-[status-pulse_2s_ease-in-out_infinite]'
              )}
              style={{
                backgroundColor:
                  entry.type === 'success' ? '#10B981'
                  : entry.type === 'warning' ? '#F59E0B'
                  : entry.type === 'error' ? '#EF4444'
                  : '#2563EB',
              }}
            />
          </div>
          <div>
            <span className="text-[11px] text-[#64748B] mr-2">{entry.timestamp}</span>
            <span className={cn(
              'text-sm',
              entry.type === 'warning' ? 'text-[#F59E0B]'
              : entry.type === 'error' ? 'text-[#EF4444]'
              : 'text-[#F1F5F9]'
            )}>
              {entry.message}
            </span>
          </div>
        </div>
      ))}
    </div>
  );
}

// ============================================================
// AgentActivityLog
// ============================================================
interface AgentActivityLogProps {
  entries: LogEntry[];
  maxHeight?: number;
  className?: string;
}

export function AgentActivityLog({ entries, maxHeight = 200, className }: AgentActivityLogProps) {
  return (
    <div
      className={cn(
        'overflow-y-auto space-y-1.5 font-mono text-xs',
        className
      )}
      style={{ maxHeight }}
    >
      {entries.map((entry) => (
        <div key={entry.id} className="flex gap-2">
          <span className="text-[#64748B] flex-shrink-0">[{entry.timestamp}]</span>
          <span
            className={
              entry.level === 'warning' ? 'text-[#F59E0B]'
              : entry.level === 'error' ? 'text-[#EF4444]'
              : 'text-[#94A3B8]'
            }
          >
            {entry.message}
          </span>
        </div>
      ))}
    </div>
  );
}

// ============================================================
// ScreeningQuestionCard
// ============================================================
interface ScreeningQuestionCardProps {
  question: ScreeningQuestion;
  onAccept?: (id: string) => void;
  onEdit?: (id: string) => void;
  onAnswer?: (id: string) => void;
}

export function ScreeningQuestionCard({ question, onAccept, onEdit, onAnswer }: ScreeningQuestionCardProps) {
  const hasAnswer = question.aiAnswer !== null;
  const needsAnswer = question.status === 'NEEDS_ANSWER';

  return (
    <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.08)] rounded-xl p-4 space-y-3">
      <p className="text-sm font-semibold text-[#F1F5F9]">{question.question}</p>

      {hasAnswer && (
        <div className="rounded-lg p-3 bg-[rgba(16,185,129,0.08)] border border-[rgba(16,185,129,0.2)]">
          <p className="text-xs text-[#94A3B8] mb-1">
            {question.source || 'Based on your verified profile'}:{' '}
            <span className="font-semibold text-[#F1F5F9]">{question.aiAnswer}</span>
          </p>
          <p className="text-[11px] text-[#10B981]">Confidence: {question.confidence}</p>
        </div>
      )}

      {needsAnswer && (
        <div className="rounded-lg p-3 bg-[rgba(245,158,11,0.08)] border border-[rgba(245,158,11,0.2)]">
          <p className="text-xs text-[#94A3B8]">AI cannot determine this from your verified profile.</p>
        </div>
      )}

      <div className="flex gap-2">
        {hasAnswer && question.status === 'PENDING' && (
          <>
            <Button size="sm" variant="primary" onClick={() => onAccept?.(question.id)}>
              Accept
            </Button>
            <Button size="sm" variant="secondary" onClick={() => onEdit?.(question.id)}>
              Edit
            </Button>
          </>
        )}
        {needsAnswer && (
          <Button size="sm" variant="secondary" onClick={() => onAnswer?.(question.id)}>
            Answer
          </Button>
        )}
        {question.status === 'ACCEPTED' && (
          <div className="flex items-center gap-1 text-[#10B981] text-xs">
            <CheckCircle2 size={14} />
            Accepted
          </div>
        )}
      </div>
    </div>
  );
}

// ============================================================
// ApplicationProgressStepper
// ============================================================
interface StepperStep {
  label: string;
  status: 'completed' | 'active' | 'pending';
}

interface ApplicationProgressStepperProps {
  steps: StepperStep[];
  className?: string;
}

export function ApplicationProgressStepper({ steps, className }: ApplicationProgressStepperProps) {
  return (
    <div className={cn('flex items-center w-full', className)}>
      {steps.map((step, i) => (
        <div key={step.label} className="flex items-center flex-1 last:flex-none">
          <div className="flex flex-col items-center gap-1">
            <div className="flex items-center justify-center w-6 h-6">
              {step.status === 'completed' ? (
                <CheckCircle2 size={20} className="text-[#10B981]" />
              ) : step.status === 'active' ? (
                <div className="w-4 h-4 rounded-full bg-[#2563EB] flex items-center justify-center">
                  <div className="w-2 h-2 rounded-full bg-white" />
                </div>
              ) : (
                <Circle size={20} className="text-[#64748B]" />
              )}
            </div>
            <span
              className={cn(
                'text-[11px] text-center whitespace-nowrap',
                step.status === 'completed' ? 'text-[#10B981]'
                : step.status === 'active' ? 'text-[#F1F5F9]'
                : 'text-[#64748B]'
              )}
            >
              {step.label}
            </span>
          </div>
          {i < steps.length - 1 && (
            <div
              className={cn(
                'flex-1 h-px mx-2 mt-[-14px]',
                step.status === 'completed' ? 'bg-[#10B981]' : 'border-t border-dashed border-[#243047]'
              )}
            />
          )}
        </div>
      ))}
    </div>
  );
}

// ============================================================
// ApplicationPipeline
// ============================================================
const PIPELINE_STAGES: { key: string; label: string }[] = [
  { key: 'DISCOVERED', label: 'Discovered' },
  { key: 'MATCHED', label: 'Matched' },
  { key: 'PREPARING', label: 'Preparing' },
  { key: 'REVIEW', label: 'Review' },
  { key: 'SUBMITTED', label: 'Submitted' },
  { key: 'INTERVIEW', label: 'Interview' },
  { key: 'OFFER', label: 'Offer' },
];

interface ApplicationPipelineProps {
  currentStage?: string;
  className?: string;
}

export function ApplicationPipeline({ currentStage = 'MATCHED', className }: ApplicationPipelineProps) {
  const currentIndex = PIPELINE_STAGES.findIndex((s) => s.key === currentStage);

  return (
    <div className={cn('flex items-center gap-2 overflow-x-auto pb-1', className)}>
      {PIPELINE_STAGES.map((stage, i) => {
        const isActive = stage.key === currentStage;
        const isDone = i < currentIndex;

        return (
          <div key={stage.key} className="flex items-center gap-2 flex-shrink-0">
            <div
              className={cn(
                'flex items-center gap-1.5 px-4 py-2 rounded-full text-sm font-medium border',
                'transition-all duration-200',
                isActive
                  ? 'bg-gradient-to-r from-[#2563EB] to-[#7C3AED] text-white border-transparent shadow-[0_0_16px_rgba(37,99,235,0.4)]'
                  : isDone
                  ? 'bg-[rgba(16,185,129,0.1)] text-[#10B981] border-[rgba(16,185,129,0.3)]'
                  : 'bg-[#1A2235] text-[#64748B] border-[rgba(148,163,184,0.1)]'
              )}
            >
              {isDone && <CheckCircle2 size={14} />}
              {stage.label}
              {isActive && <Loader2 size={12} className="animate-spin" />}
            </div>
            {i < PIPELINE_STAGES.length - 1 && (
              <span className="text-[#64748B]">→</span>
            )}
          </div>
        );
      })}
    </div>
  );
}
