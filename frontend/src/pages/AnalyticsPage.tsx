import { AppShell } from '@/components/layout/AppShell';
import { PageHeader } from '@/components/layout/PageHeader';
import { StatCard } from '@/components/ui';
import { Card } from '@/components/ui/Card';
import { ProgressBar } from '@/components/ui';
import { mockDashboardSummary } from '@/data/mockData';
import { TrendingUp, Target, Award, Clock } from 'lucide-react';

// Mock chart with CSS bars
function BarChart({ data }: { data: { label: string; value: number }[] }) {
  const max = Math.max(...data.map((d) => d.value));
  return (
    <div className="flex items-end gap-3 h-40 pt-4">
      {data.map((d) => (
        <div key={d.label} className="flex-1 flex flex-col items-center gap-1">
          <div className="w-full flex items-end justify-center" style={{ height: 120 }}>
            <div
              className="w-full rounded-t-sm bg-gradient-to-t from-[#2563EB] to-[#7C3AED] transition-all duration-500"
              style={{ height: `${(d.value / max) * 100}%`, minHeight: '4px' }}
            />
          </div>
          <span className="text-[10px] text-[#64748B]">{d.label}</span>
          <span className="text-xs font-medium text-[#F1F5F9]">{d.value}</span>
        </div>
      ))}
    </div>
  );
}

const WEEKLY_DATA = [
  { label: 'Mon', value: 8 },
  { label: 'Tue', value: 15 },
  { label: 'Wed', value: 22 },
  { label: 'Thu', value: 18 },
  { label: 'Fri', value: 27 },
  { label: 'Sat', value: 12 },
  { label: 'Sun', value: 5 },
];

export default function AnalyticsPage() {
  return (
    <AppShell>
      <PageHeader
        title="Analytics"
        subtitle="Performance insights for your job search"
      />

      {/* Stat row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard label="Jobs Discovered" value={127} icon={<TrendingUp size={16} />} accent />
        <StatCard label="Match Rate" value="14%" icon={<Target size={16} />} />
        <StatCard label="Applications" value={7} icon={<Award size={16} />} />
        <StatCard label="Avg. Response Time" value="3.2d" icon={<Clock size={16} />} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        {/* Weekly activity chart */}
        <Card variant="default" padding="md">
          <h3 className="text-sm font-semibold text-[#F1F5F9] mb-2">Job Activity (This Week)</h3>
          <p className="text-xs text-[#64748B] mb-4">Jobs discovered per day</p>
          <BarChart data={WEEKLY_DATA} />
        </Card>

        {/* Conversion funnel */}
        <Card variant="default" padding="md">
          <h3 className="text-sm font-semibold text-[#F1F5F9] mb-4">Conversion Metrics</h3>
          <div className="space-y-4">
            {[
              { label: 'Jobs Discovered → Strong Matches', value: 14, color: 'brand' as const },
              { label: 'Strong Matches → Applications', value: 39, color: 'success' as const },
              { label: 'Applications → Interviews', value: 28, color: 'cyan' as const },
              { label: 'Interviews → Offers', value: 0, color: 'warning' as const },
            ].map((metric) => (
              <div key={metric.label}>
                <div className="flex justify-between text-xs mb-1.5">
                  <span className="text-[#94A3B8]">{metric.label}</span>
                  <span className="font-semibold text-[#F1F5F9]">{metric.value}%</span>
                </div>
                <ProgressBar value={metric.value} color={metric.color} size="sm" />
              </div>
            ))}
          </div>
        </Card>
      </div>

      {/* Key metrics */}
      <Card variant="default" padding="md">
        <h3 className="text-sm font-semibold text-[#F1F5F9] mb-4">Key Metrics</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          {[
            { label: 'Avg. Match Score', value: '79%', change: '+4%' },
            { label: 'Response Rate', value: '28.5%', change: '+2.1%' },
            { label: 'Interview Rate', value: '28.5%', change: '+5%' },
            { label: 'Applications Today', value: '3', change: '' },
          ].map((metric) => (
            <div key={metric.label} className="text-center">
              <div className="text-2xl font-bold text-[#F1F5F9]">{metric.value}</div>
              <div className="text-xs text-[#64748B] mt-1">{metric.label}</div>
              {metric.change && (
                <div className="text-xs text-[#10B981] mt-0.5">{metric.change} vs last week</div>
              )}
            </div>
          ))}
        </div>
      </Card>
    </AppShell>
  );
}
