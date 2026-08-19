import { apiClient } from './client';

export interface JobSourceHealth {
  totalSearchRuns: number;
  totalFailures: number;
  failuresBySource: Record<string, number>;
}

export interface AiUsageMetrics {
  totalRequests: number;
  requestsByProvider: Record<string, number>;
  averageLatencyMs: number;
}

export interface WorkerHealth {
  status: string;
  activeWorkers: number;
  totalFailedApplications: number;
}

export interface SchedulerStatus {
  status: string;
  lastRunTime: string;
  nextScheduledRun: string;
}

export interface AdminDashboardStatsDto {
  totalUsers: number;
  totalApplications: number;
  jobSourceHealth: JobSourceHealth;
  aiUsage: AiUsageMetrics;
  workerHealth: WorkerHealth;
  schedulerStatus: SchedulerStatus;
}

export const getAdminStats = async (): Promise<AdminDashboardStatsDto> => {
  const response = await apiClient.get('/admin/dashboard-stats');
  return response.data;
};
