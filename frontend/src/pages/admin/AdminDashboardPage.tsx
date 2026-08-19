import { useQuery } from '@tanstack/react-query';
import { getAdminStats } from '@/api/adminApi';
import { Card } from '@/components/ui/Card';
import { 
  Users, 
  Briefcase, 
  Activity, 
  Cpu, 
  ServerCrash, 
  Clock, 
  ShieldAlert,
  Server
} from 'lucide-react';
import { LoadingState, ErrorState } from '@/components/feedback';

export default function AdminDashboardPage() {
  const { data: stats, isLoading, error } = useQuery({
    queryKey: ['admin-stats'],
    queryFn: getAdminStats,
    refetchInterval: 30000, // Refresh every 30s
  });

  if (isLoading) return <LoadingState message="Initializing Command Center..." className="min-h-screen bg-neutral-900 text-white" />;
  if (error) return <ErrorState title="Telemetry Error" description="Failed to load admin metrics." onRetry={() => window.location.reload()} />;
  if (!stats) return null;

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 selection:bg-indigo-500/30">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        
        <header className="flex flex-col gap-2">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/10 text-indigo-400 w-fit border border-indigo-500/20 text-sm font-medium">
            <ShieldAlert className="w-4 h-4" />
            Admin Command Center
          </div>
          <h1 className="text-4xl font-extrabold tracking-tight text-white">System Telemetry</h1>
          <p className="text-neutral-400">Live operational metrics and system health indicators.</p>
        </header>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          
          {/* Global Metrics */}
          <MetricCard 
            title="Total Users" 
            value={stats.totalUsers} 
            icon={<Users className="w-6 h-6 text-blue-400" />} 
            trend="+12% this week" 
          />
          <MetricCard 
            title="Total Applications" 
            value={stats.totalApplications} 
            icon={<Briefcase className="w-6 h-6 text-emerald-400" />} 
            trend="+8% this week" 
          />
          
          {/* AI Metrics */}
          <MetricCard 
            title="AI Requests" 
            value={stats.aiUsage.totalRequests} 
            icon={<Cpu className="w-6 h-6 text-purple-400" />} 
            trend={`${stats.aiUsage.averageLatencyMs.toFixed(0)}ms avg latency`} 
          />
          
          {/* Worker Metrics */}
          <MetricCard 
            title="Active Workers" 
            value={stats.workerHealth.activeWorkers} 
            icon={<Server className="w-6 h-6 text-cyan-400" />} 
            status={stats.workerHealth.status} 
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          
          {/* Health Diagnostics */}
          <Card className="bg-neutral-900 border-neutral-800 p-6 overflow-hidden relative group">
            <div className="absolute inset-0 bg-gradient-to-br from-rose-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
            <h3 className="text-lg font-semibold text-white flex items-center gap-2 mb-6">
              <Activity className="w-5 h-5 text-rose-400" />
              Diagnostics & Failures
            </h3>
            
            <div className="space-y-4">
              <div className="flex justify-between items-center p-4 bg-neutral-950/50 rounded-lg border border-neutral-800">
                <div>
                  <p className="text-sm text-neutral-400">Job Source Failures</p>
                  <p className="text-2xl font-bold text-rose-400">{stats.jobSourceHealth.totalFailures}</p>
                </div>
                <ServerCrash className="w-8 h-8 text-rose-500/50" />
              </div>
              
              <div className="flex justify-between items-center p-4 bg-neutral-950/50 rounded-lg border border-neutral-800">
                <div>
                  <p className="text-sm text-neutral-400">Failed Applications</p>
                  <p className="text-2xl font-bold text-amber-400">{stats.workerHealth.totalFailedApplications}</p>
                </div>
                <ShieldAlert className="w-8 h-8 text-amber-500/50" />
              </div>
            </div>
          </Card>

          {/* Operations & Schedulers */}
          <Card className="bg-neutral-900 border-neutral-800 p-6 overflow-hidden relative group">
            <div className="absolute inset-0 bg-gradient-to-br from-indigo-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
            <h3 className="text-lg font-semibold text-white flex items-center gap-2 mb-6">
              <Clock className="w-5 h-5 text-indigo-400" />
              Scheduler Operations
            </h3>
            
            <div className="space-y-4">
              <div className="p-4 bg-neutral-950/50 rounded-lg border border-neutral-800 flex justify-between items-center">
                <span className="text-neutral-400 text-sm">Engine Status</span>
                <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${
                  stats.schedulerStatus.status === 'ACTIVE' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                }`}>
                  {stats.schedulerStatus.status}
                </span>
              </div>
              
              <div className="p-4 bg-neutral-950/50 rounded-lg border border-neutral-800">
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-neutral-400">Total Search Runs</span>
                  <span className="text-white font-medium">{stats.jobSourceHealth.totalSearchRuns}</span>
                </div>
                <div className="w-full bg-neutral-800 rounded-full h-1.5 mt-2">
                  <div className="bg-indigo-500 h-1.5 rounded-full w-full opacity-50"></div>
                </div>
              </div>
            </div>
          </Card>

        </div>
      </div>
    </div>
  );
}

function MetricCard({ title, value, icon, trend, status }: { title: string, value: string | number, icon: React.ReactNode, trend?: string, status?: string }) {
  return (
    <Card className="bg-neutral-900 border-neutral-800 p-6 flex flex-col justify-between relative overflow-hidden group hover:border-neutral-700 transition-colors">
      <div className="absolute top-0 right-0 p-4 opacity-50 group-hover:opacity-100 group-hover:scale-110 transition-all duration-300">
        {icon}
      </div>
      <div>
        <p className="text-sm font-medium text-neutral-400 mb-1">{title}</p>
        <p className="text-3xl font-bold text-white tracking-tight">{value}</p>
      </div>
      <div className="mt-4 flex items-center gap-2">
        {trend && <span className="text-sm text-neutral-400">{trend}</span>}
        {status && (
          <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
            {status}
          </span>
        )}
      </div>
    </Card>
  );
}
