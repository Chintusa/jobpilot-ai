import { cn } from '@/utils/cn';
import type { Job } from '@/types';
import { MatchScoreRing } from '@/components/ai';
import { Badge, StrongMatchBadge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Bookmark, MapPin, Clock, DollarSign } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { jobDetailPath, agentApplyPath } from '@/routes/routes';

interface JobCardProps {
  job: Job;
  variant?: 'standard' | 'featured' | 'compact';
}

export function JobCard({ job, variant = 'standard' }: JobCardProps) {
  const navigate = useNavigate();
  const isStrong = (job.matchScore || 0) >= 85;

  return (
    <div
      className={cn(
        'bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-xl p-5',
        'hover:-translate-y-0.5 hover:shadow-[0_8px_32px_rgba(0,0,0,0.5)] transition-all duration-200',
        'relative overflow-hidden'
      )}
    >
      <div className="flex items-start gap-4">
        {/* Match score ring */}
        {job.matchScore && (
          <div className="flex-shrink-0">
            <MatchScoreRing score={job.matchScore} size={60} showLabel={false} />
          </div>
        )}

        {/* Job info */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <div>
              <h3 className="font-semibold text-[#F1F5F9] text-base leading-tight">{job.title}</h3>
              <p className="text-sm text-[#94A3B8] mt-0.5">{job.company}</p>
            </div>
            <div className="flex items-center gap-2 flex-shrink-0">
              {isStrong && <StrongMatchBadge />}
              <button className="text-[#64748B] hover:text-[#F59E0B] transition-colors">
                <Bookmark size={16} />
              </button>
            </div>
          </div>

          {/* Meta */}
          <div className="flex items-center gap-4 mt-2 text-xs text-[#64748B] flex-wrap">
            <span className="flex items-center gap-1">
              <MapPin size={11} />{job.location}
            </span>
            <span className="flex items-center gap-1">
              <Clock size={11} />{job.workMode}
            </span>
            {job.salaryDisplay && (
              <span className="flex items-center gap-1">
                <DollarSign size={11} />{job.salaryDisplay}
              </span>
            )}
            {job.experience && (
              <span>{job.experience} exp</span>
            )}
          </div>

          {/* Skills */}
          {job.skills && job.skills.length > 0 && (
            <div className="mt-3 flex flex-wrap items-center gap-1.5">
              {job.skills.map((s) => (
                <Badge key={s.name} variant="skill">{s.name}</Badge>
              ))}
              {job.missingSkills && job.missingSkills.length > 0 && (
                <span className="text-xs text-[#EF4444] ml-1">
                  Missing: {job.missingSkills.map((s) => s.name).join(', ')}
                </span>
              )}
            </div>
          )}

          {/* Actions */}
          <div className="mt-4 flex gap-2">
            <Button
              size="sm"
              variant="secondary"
              onClick={() => navigate(jobDetailPath(job.id))}
            >
              View Job
            </Button>
            <Button
              size="sm"
              variant="primary"
              onClick={() => navigate(agentApplyPath(job.id))}
            >
              Prepare Application
            </Button>
          </div>
        </div>
      </div>

      {/* Left accent based on match */}
      {job.matchScore && (
        <div
          className="absolute left-0 top-0 bottom-0 w-1 rounded-l-xl"
          style={{
            backgroundColor:
              job.matchScore >= 85 ? '#10B981'
              : job.matchScore >= 70 ? '#3B82F6'
              : '#F59E0B',
          }}
        />
      )}
    </div>
  );
}
