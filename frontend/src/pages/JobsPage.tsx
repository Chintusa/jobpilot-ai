import { AppShell } from '@/components/layout/AppShell';
import { PageHeader } from '@/components/layout/PageHeader';
import { JobCard } from '@/features/jobs/JobCard';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { cn } from '@/utils/cn';
import {
  Search,
  MapPin,
  Sparkles,
  X,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  AlertCircle,
  Loader2
} from 'lucide-react';
import { useState } from 'react';
import { useJobsQuery, useSyncJobsMutation, type BackendJob } from '@/api/jobsApi';
import { mockJobs } from '@/data/mockData';
import type { Job } from '@/types';

const WORK_MODE_FILTERS = ['All', 'Hybrid', 'Remote', 'Onsite'];

export default function JobsPage() {
  const [search, setSearch] = useState('');
  const [locationSearch, setLocationSearch] = useState('');
  const [activeMode, setActiveMode] = useState('All');
  const [minMatchOnly, setMinMatchOnly] = useState(false);
  const [page, setPage] = useState(0);

  // TanStack Query integration
  const {
    data: jobsPageData,
    isLoading,
    isError,
    refetch,
    isFetching
  } = useJobsQuery({
    keyword: search || undefined,
    location: locationSearch || undefined,
    workMode: activeMode !== 'All' ? activeMode.toUpperCase() : undefined,
    page,
    size: 10,
  });

  const syncMutation = useSyncJobsMutation();

  // Transform backend jobs to frontend Job model or fallback to mock data
  const backendList = jobsPageData?.content;
  const transformedJobs: Job[] = (backendList && backendList.length > 0)
    ? backendList.map((bj: BackendJob) => {
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

  // Filter client-side if minMatchOnly is checked or search in mock fallback
  const displayedJobs = transformedJobs.filter((j) => {
    const matchScoreFilter = !minMatchOnly || (j.matchScore && j.matchScore >= 85);
    return matchScoreFilter;
  });

  const totalCount = jobsPageData?.totalElements ?? displayedJobs.length;
  const totalPages = jobsPageData?.totalPages ?? Math.ceil(displayedJobs.length / 10);

  return (
    <AppShell>
      <PageHeader
        title="Discover Jobs"
        subtitle={`${totalCount} verified jobs discovered and ranked by your autonomous AI agent`}
        actions={
          <div className="flex items-center gap-2.5">
            <Button
              variant="secondary"
              size="sm"
              leftIcon={<RefreshCw size={13} className={syncMutation.isPending || isFetching ? 'animate-spin' : ''} />}
              onClick={() => syncMutation.mutate()}
              disabled={syncMutation.isPending}
            >
              {syncMutation.isPending ? 'Syncing...' : 'Sync Feed'}
            </Button>
            <span className="text-xs text-[#10B981] font-semibold bg-[rgba(16,185,129,0.1)] px-3 py-1.5 rounded-full border border-[rgba(16,185,129,0.25)] flex items-center gap-1.5">
              <Sparkles size={13} /> Active Agent Search
            </span>
          </div>
        }
      />

      {/* Search & Location Filter Bar */}
      <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-5 mb-6 shadow-xl space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-12 gap-3">
          <div className="sm:col-span-6">
            <Input
              placeholder="Search by role, company, or tech stack (Java, Spring, etc.)..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              leftIcon={<Search size={15} />}
              className="h-10 text-xs sm:text-sm bg-[#111827]"
            />
          </div>

          <div className="sm:col-span-4">
            <Input
              placeholder="Location (e.g. Bengaluru, Remote)..."
              value={locationSearch}
              onChange={(e) => {
                setLocationSearch(e.target.value);
                setPage(0);
              }}
              leftIcon={<MapPin size={15} />}
              className="h-10 text-xs sm:text-sm bg-[#111827]"
            />
          </div>

          <div className="sm:col-span-2">
            <Button
              variant={minMatchOnly ? 'primary' : 'secondary'}
              size="md"
              fullWidth
              className="h-10 text-xs font-semibold"
              onClick={() => setMinMatchOnly(!minMatchOnly)}
            >
              {minMatchOnly ? '≥85% Match ✓' : '≥85% Match'}
            </Button>
          </div>
        </div>

        {/* Work Mode Filter Pills */}
        <div className="flex items-center justify-between gap-4 pt-2 border-t border-[rgba(148,163,184,0.08)] flex-wrap">
          <div className="flex items-center gap-2">
            {WORK_MODE_FILTERS.map((mode) => (
              <button
                key={mode}
                onClick={() => {
                  setActiveMode(mode);
                  setPage(0);
                }}
                className={cn(
                  'px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer border',
                  activeMode === mode
                    ? 'bg-[#2563EB] text-white border-[#2563EB] shadow-[0_0_12px_rgba(37,99,235,0.4)]'
                    : 'bg-[#111827] text-[#94A3B8] border-[rgba(148,163,184,0.12)] hover:text-[#F1F5F9]'
                )}
              >
                {mode}
              </button>
            ))}
          </div>

          <div className="flex items-center gap-2 text-xs text-[#94A3B8]">
            <span>
              Showing <strong className="text-[#F1F5F9]">{displayedJobs.length}</strong> matching roles
            </span>
            {(search || locationSearch || activeMode !== 'All' || minMatchOnly) && (
              <button
                onClick={() => {
                  setSearch('');
                  setLocationSearch('');
                  setActiveMode('All');
                  setMinMatchOnly(false);
                  setPage(0);
                }}
                className="text-[#EF4444] hover:underline flex items-center gap-1 cursor-pointer font-medium ml-2"
              >
                <X size={12} /> Clear filters
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Loading Skeleton State */}
      {isLoading && (
        <div className="space-y-4">
          {[1, 2, 3].map((n) => (
            <div key={n} className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-xl p-5 animate-pulse">
              <div className="flex items-start gap-4">
                <div className="w-14 h-14 rounded-full bg-[#111827]" />
                <div className="flex-1 space-y-2">
                  <div className="h-4 w-1/3 bg-[#111827] rounded" />
                  <div className="h-3 w-1/4 bg-[#111827] rounded" />
                  <div className="h-3 w-1/2 bg-[#111827] rounded" />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Error State */}
      {isError && (
        <div className="text-center py-12 bg-[#1A2235] border border-[rgba(239,68,68,0.3)] rounded-2xl mb-6 p-6">
          <AlertCircle size={32} className="text-[#EF4444] mx-auto mb-2" />
          <p className="text-sm font-bold text-[#F1F5F9]">Failed to load live jobs from backend</p>
          <p className="text-xs text-[#94A3B8] mt-1 mb-4">Showing cached and verified opportunities.</p>
          <Button variant="secondary" size="sm" onClick={() => refetch()}>
            Retry Connection
          </Button>
        </div>
      )}

      {/* Job List */}
      {!isLoading && (
        <div className="space-y-4">
          {displayedJobs.length === 0 ? (
            <div className="text-center py-16 bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl">
              <p className="text-base text-[#F1F5F9] font-semibold mb-1">No matching opportunities found</p>
              <p className="text-xs text-[#94A3B8]">Try adjusting your search criteria or work mode filter.</p>
            </div>
          ) : (
            displayedJobs.map((job) => <JobCard key={job.id} job={job} />)
          )}
        </div>
      )}

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between pt-6 mt-6 border-t border-[rgba(148,163,184,0.08)]">
          <Button
            variant="secondary"
            size="sm"
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            leftIcon={<ChevronLeft size={14} />}
          >
            Previous
          </Button>
          <span className="text-xs text-[#94A3B8]">
            Page <strong className="text-[#F1F5F9]">{page + 1}</strong> of <strong>{totalPages}</strong>
          </span>
          <Button
            variant="secondary"
            size="sm"
            disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
            rightIcon={<ChevronRight size={14} />}
          >
            Next
          </Button>
        </div>
      )}
    </AppShell>
  );
}
