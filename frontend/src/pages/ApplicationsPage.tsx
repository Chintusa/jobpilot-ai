import { AppShell } from '@/components/layout/AppShell';
import { PageHeader } from '@/components/layout/PageHeader';
import { StatusBadge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { MatchScoreRing } from '@/components/ai';
import { Tabs } from '@/components/ui/Tabs';
import { Drawer } from '@/components/overlays';
import { mockApplications, mockJobs } from '@/data/mockData';
import { useState } from 'react';
import { MapPin, Calendar, Bot, LayoutGrid, Table as TableIcon, Trash2, ExternalLink, Filter, Search } from 'lucide-react';
import { formatRelativeDate } from '@/utils/format';
import { Input } from '@/components/ui/Input';
import type { Application, ApplicationStatus } from '@/types';
import { useNavigate } from 'react-router-dom';
import { agentApplyPath } from '@/routes/routes';

const STATUS_TABS = [
  { label: 'All', value: 'ALL' },
  { label: 'Matched', value: 'MATCHED' },
  { label: 'Preparing', value: 'PREPARING' },
  { label: 'Submitted', value: 'SUBMITTED' },
  { label: 'Interviewing', value: 'INTERVIEWING' },
  { label: 'Offered', value: 'OFFERED' },
];

export default function ApplicationsPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('ALL');
  const [viewMode, setViewMode] = useState<'table' | 'cards'>('table');
  const [selectedApp, setSelectedApp] = useState<Application | null>(null);
  const [selectedRows, setSelectedRows] = useState<string[]>([]);
  const [search, setSearch] = useState('');

  const filtered = mockApplications.filter((a) => {
    const matchesTab = activeTab === 'ALL' || a.status === activeTab;
    const matchesSearch = !search ||
      a.jobTitle.toLowerCase().includes(search.toLowerCase()) ||
      a.company.toLowerCase().includes(search.toLowerCase());
    return matchesTab && matchesSearch;
  });

  const toggleSelectAll = () => {
    if (selectedRows.length === filtered.length) {
      setSelectedRows([]);
    } else {
      setSelectedRows(filtered.map((a) => a.id));
    }
  };

  const toggleSelectRow = (id: string) => {
    setSelectedRows((prev) =>
      prev.includes(id) ? prev.filter((r) => r !== id) : [...prev, id]
    );
  };

  return (
    <AppShell>
      <PageHeader
        title="Applications Tracker"
        subtitle={`${mockApplications.length} total applications managed by JobPilot AI`}
        actions={
          <div className="flex items-center gap-3">
            <div className="flex items-center p-1 rounded-lg bg-[#1E293B] border border-[rgba(255,255,255,0.06)]">
              <button
                onClick={() => setViewMode('table')}
                className={`p-1.5 rounded-md transition-all ${
                  viewMode === 'table' ? 'bg-[#243047] text-[#F1F5F9]' : 'text-[#64748B] hover:text-[#F1F5F9]'
                }`}
                title="Table View"
              >
                <TableIcon size={16} />
              </button>
              <button
                onClick={() => setViewMode('cards')}
                className={`p-1.5 rounded-md transition-all ${
                  viewMode === 'cards' ? 'bg-[#243047] text-[#F1F5F9]' : 'text-[#64748B] hover:text-[#F1F5F9]'
                }`}
                title="Cards View"
              >
                <LayoutGrid size={16} />
              </button>
            </div>

            <Button variant="primary" size="sm" onClick={() => navigate(agentApplyPath('1'))}>
              Automate Applications
            </Button>
          </div>
        }
      />

      {/* Filter and Search Bar */}
      <div className="flex items-center justify-between gap-4 mb-5 flex-wrap">
        <Tabs
          tabs={STATUS_TABS.map((t) => ({
            ...t,
            badge: t.value === 'ALL' ? undefined : mockApplications.filter((a) => a.status === t.value).length,
          }))}
          activeTab={activeTab}
          onChange={setActiveTab}
          variant="line"
        />

        <div className="flex items-center gap-2">
          <Input
            placeholder="Search applications..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            leftIcon={<Search size={14} />}
            className="w-56 h-9 text-xs"
          />
          <Button variant="secondary" size="sm" leftIcon={<Filter size={13} />}>
            Filter
          </Button>
        </div>
      </div>

      {filtered.length === 0 ? (
        <div className="text-center py-16 bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-xl">
          <p className="text-[#94A3B8]">No applications found matching your filter criteria.</p>
        </div>
      ) : viewMode === 'table' ? (
        /* ——— Table View matching 07-application-tracker.png ——— */
        <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-xl overflow-hidden shadow-lg">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-[#111827] text-xs font-semibold text-[#94A3B8] uppercase tracking-wider border-b border-[rgba(148,163,184,0.08)]">
                <tr>
                  <th className="py-3.5 pl-4 pr-2 w-10">
                    <input
                      type="checkbox"
                      checked={selectedRows.length === filtered.length && filtered.length > 0}
                      onChange={toggleSelectAll}
                      className="rounded border-[rgba(148,163,184,0.3)] bg-[#1E293B] cursor-pointer"
                    />
                  </th>
                  <th className="py-3.5 px-4">Role & Company</th>
                  <th className="py-3.5 px-4">Salary Range</th>
                  <th className="py-3.5 px-4">Matching %</th>
                  <th className="py-3.5 px-4">Status</th>
                  <th className="py-3.5 px-4">Applied Date</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[rgba(148,163,184,0.06)] text-xs">
                {filtered.map((app) => (
                  <tr
                    key={app.id}
                    onClick={() => setSelectedApp(app)}
                    className="hover:bg-[#1E2A42] transition-colors cursor-pointer"
                  >
                    <td className="py-3.5 pl-4 pr-2" onClick={(e) => e.stopPropagation()}>
                      <input
                        type="checkbox"
                        checked={selectedRows.includes(app.id)}
                        onChange={() => toggleSelectRow(app.id)}
                        className="rounded border-[rgba(148,163,184,0.3)] bg-[#1E293B] cursor-pointer"
                      />
                    </td>
                    <td className="py-3.5 px-4">
                      <div>
                        <span className="font-bold text-sm text-[#F1F5F9] hover:text-[#60A5FA] transition-colors">
                          {app.jobTitle}
                        </span>
                        <div className="flex items-center gap-2 text-[#94A3B8] mt-0.5">
                          <span>{app.company}</span>
                          <span>·</span>
                          <span className="flex items-center gap-0.5 text-[11px] text-[#64748B]">
                            <MapPin size={10} /> {app.location}
                          </span>
                        </div>
                      </div>
                    </td>
                    <td className="py-3.5 px-4 text-[#F1F5F9] font-medium">
                      {app.salary || 'Competitive'}
                    </td>
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-2">
                        <div className="w-12 h-1.5 rounded-full bg-[#243047] overflow-hidden">
                          <div
                            className="h-full rounded-full"
                            style={{
                              width: `${app.matchScore}%`,
                              backgroundColor: app.matchScore >= 85 ? '#10B981' : app.matchScore >= 70 ? '#3B82F6' : '#F59E0B',
                            }}
                          />
                        </div>
                        <span className={`font-bold ${app.matchScore >= 85 ? 'text-[#10B981]' : 'text-[#60A5FA]'}`}>
                          {app.matchScore}%
                        </span>
                      </div>
                    </td>
                    <td className="py-3.5 px-4">
                      <StatusBadge status={app.status as ApplicationStatus} />
                    </td>
                    <td className="py-3.5 px-4 text-[#94A3B8]">
                      {formatRelativeDate(app.appliedAt)}
                    </td>
                    <td className="py-3.5 px-4 text-right" onClick={(e) => e.stopPropagation()}>
                      <div className="flex items-center justify-end gap-2">
                        <Button
                          size="sm"
                          variant="secondary"
                          className="h-7 text-xs px-2.5"
                          onClick={() => setSelectedApp(app)}
                        >
                          Details
                        </Button>
                        <button
                          className="p-1 text-[#64748B] hover:text-[#EF4444] transition-colors rounded"
                          title="Archive/Delete"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        /* ——— Cards View ——— */
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filtered.map((app) => (
            <div
              key={app.id}
              onClick={() => setSelectedApp(app)}
              className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-xl p-5 hover:bg-[#1E2A42] hover:border-[rgba(59,130,246,0.3)] transition-all duration-200 cursor-pointer shadow-md"
            >
              <div className="flex items-start gap-4">
                <MatchScoreRing score={app.matchScore} size={60} strokeWidth={6} showLabel={false} />

                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <h3 className="font-bold text-[#F1F5F9] text-base">{app.jobTitle}</h3>
                      <p className="text-xs text-[#94A3B8] font-medium">{app.company}</p>
                    </div>
                    <StatusBadge status={app.status as ApplicationStatus} />
                  </div>

                  <div className="flex items-center gap-3 mt-3 text-xs text-[#64748B] flex-wrap">
                    <span className="flex items-center gap-1"><MapPin size={11} />{app.location}</span>
                    {app.salary && <span>{app.salary}</span>}
                    <span className="flex items-center gap-1"><Calendar size={11} />{formatRelativeDate(app.appliedAt)}</span>
                    {app.submittedVia === 'AI_AGENT' && (
                      <span className="flex items-center gap-1 text-[#3B82F6] font-medium">
                        <Bot size={11} />AI Agent
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ——— Application Detail Drawer ——— */}
      <Drawer
        isOpen={Boolean(selectedApp)}
        onClose={() => setSelectedApp(null)}
        title="Application Details"
        size="md"
      >
        {selectedApp && (
          <div className="space-y-6">
            <div className="flex items-center gap-4 p-4 rounded-xl bg-[#1A2235] border border-[rgba(255,255,255,0.06)]">
              <MatchScoreRing score={selectedApp.matchScore} size={64} strokeWidth={6} showLabel={false} />
              <div>
                <h3 className="text-base font-bold text-[#F1F5F9]">{selectedApp.jobTitle}</h3>
                <p className="text-xs text-[#94A3B8]">{selectedApp.company}</p>
                <div className="mt-1">
                  <StatusBadge status={selectedApp.status as ApplicationStatus} />
                </div>
              </div>
            </div>

            <div className="space-y-3">
              <h4 className="text-xs font-bold text-[#64748B] uppercase tracking-wider">Application Status</h4>
              <div className="p-3 bg-[#1A2235] rounded-lg border border-[rgba(255,255,255,0.06)] text-xs space-y-2">
                <div className="flex justify-between">
                  <span className="text-[#64748B]">Method:</span>
                  <span className="text-[#F1F5F9] font-medium">{selectedApp.submittedVia === 'AI_AGENT' ? 'Autonomous AI Agent' : 'Manual'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-[#64748B]">Applied date:</span>
                  <span className="text-[#F1F5F9]">{selectedApp.appliedAt}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-[#64748B]">Last updated:</span>
                  <span className="text-[#F1F5F9]">{selectedApp.lastUpdatedAt}</span>
                </div>
              </div>
            </div>

            <div className="space-y-3">
              <h4 className="text-xs font-bold text-[#64748B] uppercase tracking-wider">Submitted Assets</h4>
              <div className="space-y-2">
                <div className="p-3 bg-[#1A2235] rounded-lg border border-[rgba(37,99,235,0.2)] flex items-center justify-between text-xs">
                  <span className="text-[#60A5FA]">📄 Tailored_Resume_TechNova.pdf</span>
                  <ExternalLink size={14} className="text-[#64748B] hover:text-[#F1F5F9] cursor-pointer" />
                </div>
                <div className="p-3 bg-[#1A2235] rounded-lg border border-[rgba(148,163,184,0.1)] flex items-center justify-between text-xs">
                  <span className="text-[#94A3B8]">✍️ Cover_Letter_TechNova.pdf</span>
                  <ExternalLink size={14} className="text-[#64748B] hover:text-[#F1F5F9] cursor-pointer" />
                </div>
              </div>
            </div>

            <div className="flex gap-2 pt-4">
              <Button
                variant="primary"
                fullWidth
                size="md"
                onClick={() => {
                  setSelectedApp(null);
                  navigate(agentApplyPath(selectedApp.id));
                }}
              >
                Open Application Agent
              </Button>
            </div>
          </div>
        )}
      </Drawer>
    </AppShell>
  );
}
