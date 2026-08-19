import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from './client';

export interface BackendJob {
  id: string;
  title: string;
  company: string;
  location: string;
  workMode: string;
  salaryMin?: number;
  salaryMax?: number;
  salaryCurrency?: string;
  salaryDisplay?: string;
  experienceMin?: number;
  experienceMax?: number;
  description: string;
  requiredSkills: string;
  preferredSkills?: string;
  jobUrl?: string;
  canonicalUrl?: string;
  sourceName?: string;
  status: string;
  postedAt: string;
  matchScore?: number;
}

export interface JobMatchAssessment {
  id: string;
  jobId: string;
  overallScore: number;
  classification: string; // EXCELLENT, STRONG, GOOD, POSSIBLE, LOW
  recommendation: string; // APPLY, REVIEW, SKIP
  scoreBreakdown: string;
  reasoning: string;
  status: string;
}

export interface SearchRun {
  id: string;
  status: string;
  searchStrategies: string[];
  roleVariations: string[];
  sourcesQueried: string[];
  query: string;
  numberFound: number;
  duplicatesRemoved: number;
  filteredJobs: number;
  matchedJobs: number;
  recommendedJobs: number;
  errors?: string;
  startedAt: string;
  completedAt?: string;
  durationMs: number;
  auditLog?: string;
}

export interface BackendScreeningQuestion {
  id: string;
  question: string;
  aiAnswer?: string;
  candidateAnswer?: string;
  confidence: string;
  source: string;
  status: string;
}

export interface BackendApplication {
  id: string;
  jobId: string;
  jobTitle: string;
  company: string;
  location: string;
  status: string;
  preparationState: string; // PREPARING, READY_FOR_REVIEW, REQUIRES_USER_INPUT, USER_APPROVED, SUBMITTED
  applicationSummary?: string;
  missingInformation?: string[];
  tailoredResumeContent?: string;
  tailoredResumeUrl?: string;
  coverLetter?: string;
  submissionMethod?: string;
  appliedAt?: string;
  createdAt: string;
  screeningQuestions: BackendScreeningQuestion[];
}

export interface BackendHumanIntervention {
  id: string;
  applicationId?: string;
  jobTitle?: string;
  company?: string;
  reason: string; // CAPTCHA, MFA, UNKNOWN_QUESTION, LEGAL_DECLARATION, MISSING_INFO, UNSUPPORTED_FLOW
  type?: string;
  description: string;
  status: string; // PENDING, IN_PROGRESS, RESOLVED, CANCELLED
  requiredInput?: string;
  requiredInputType?: string;
  context?: string;
  resolutionPayload?: string;
  createdAt: string;
  resolvedAt?: string;
}

export interface JobSearchParams {
  keyword?: string;
  location?: string;
  workMode?: string;
  page?: number;
  size?: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export function useJobsQuery(params: JobSearchParams) {
  return useQuery({
    queryKey: ['jobs', params],
    queryFn: async () => {
      const res = await apiClient.get('/api/v1/jobs', { params });
      return res.data?.data as PaginatedResponse<BackendJob>;
    },
    staleTime: 60 * 1000,
  });
}

export function useJobDetailQuery(jobId?: string) {
  return useQuery({
    queryKey: ['job', jobId],
    queryFn: async () => {
      if (!jobId) throw new Error('Job ID is required');
      const res = await apiClient.get(`/api/v1/jobs/${jobId}`);
      return res.data?.data as BackendJob;
    },
    enabled: !!jobId,
  });
}

export function useJobMatchQuery(jobId?: string) {
  return useQuery({
    queryKey: ['job-match', jobId],
    queryFn: async () => {
      if (!jobId) throw new Error('Job ID is required');
      const res = await apiClient.get(`/api/v1/jobs/${jobId}/match`);
      return res.data?.data as JobMatchAssessment;
    },
    enabled: !!jobId,
  });
}

export function useSyncJobsMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      const res = await apiClient.post('/api/v1/jobs/sync', { limit: 25 });
      return res.data?.data as BackendJob[];
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
      queryClient.invalidateQueries({ queryKey: ['search-runs'] });
    },
  });
}

export function useSearchRunsQuery() {
  return useQuery({
    queryKey: ['search-runs'],
    queryFn: async () => {
      const res = await apiClient.get('/api/v1/agent/search-runs');
      return res.data?.data as SearchRun[];
    },
    staleTime: 30 * 1000,
  });
}

export function useStartSearchRunMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      const res = await apiClient.post('/api/v1/agent/search-runs/start');
      return res.data?.data as SearchRun;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['search-runs'] });
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
    },
  });
}

export function useApplicationsQuery(status?: string) {
  return useQuery({
    queryKey: ['applications', status],
    queryFn: async () => {
      const res = await apiClient.get('/api/v1/applications', {
        params: status ? { status } : undefined,
      });
      return res.data?.data as BackendApplication[];
    },
  });
}

export function usePrepareApplicationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (jobId: string) => {
      const res = await apiClient.post(`/api/v1/applications/prepare/${jobId}`);
      return res.data?.data as BackendApplication;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
}

export function useUpdateApplicationContentMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({
      applicationId,
      data,
    }: {
      applicationId: string;
      data: {
        coverLetter?: string;
        tailoredResumeContent?: string;
        userApproved?: boolean;
        screeningAnswers?: {
          questionId: string;
          candidateAnswer: string;
          status?: string;
        }[];
      };
    }) => {
      const res = await apiClient.put(
        `/api/v1/applications/${applicationId}/content`,
        data
      );
      return res.data?.data as BackendApplication;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
}

export function useSubmitApplicationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (applicationId: string) => {
      const res = await apiClient.post(
        `/api/v1/applications/${applicationId}/submit`
      );
      return res.data?.data as BackendApplication;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
}

export function useInterventionsQuery(status?: string) {
  return useQuery({
    queryKey: ['interventions', status],
    queryFn: async () => {
      const res = await apiClient.get('/api/v1/interventions', {
        params: status && status !== 'ALL' ? { status } : undefined,
      });
      return res.data?.data as BackendHumanIntervention[];
    },
    staleTime: 10 * 1000,
  });
}

export function useResolveInterventionMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({
      interventionId,
      resolutionPayload,
    }: {
      interventionId: string;
      resolutionPayload?: string;
    }) => {
      const res = await apiClient.post(
        `/api/v1/interventions/${interventionId}/resolve`,
        { resolutionPayload }
      );
      return res.data?.data as BackendHumanIntervention;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['interventions'] });
      queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
}

export function useCancelInterventionMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (interventionId: string) => {
      const res = await apiClient.post(
        `/api/v1/interventions/${interventionId}/cancel`
      );
      return res.data?.data as BackendHumanIntervention;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['interventions'] });
      queryClient.invalidateQueries({ queryKey: ['applications'] });
    },
  });
}
