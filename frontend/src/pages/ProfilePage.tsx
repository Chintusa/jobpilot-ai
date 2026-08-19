import { AppShell } from '@/components/layout/AppShell';
import { PageHeader } from '@/components/layout/PageHeader';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Tabs, TabContent } from '@/components/ui/Tabs';
import { Avatar } from '@/components/ui';
import { Modal } from '@/components/overlays';
import { useStore } from '@/app/store';
import { useState } from 'react';
import {
  MapPin, Building2, Calendar, Edit3, Plus, X,
  CheckCircle2, Sparkles, Briefcase, GraduationCap, DollarSign
} from 'lucide-react';

const INITIAL_SKILLS = [
  'Java', 'Spring Boot', 'REST APIs', 'SQL', 'Git', 'Maven', 'JUnit',
  'Python', 'FastAPI', 'React', 'TypeScript', 'AWS',
];

const INITIAL_EXPERIENCE = [
  {
    title: 'Junior Backend Developer',
    company: 'TechFirm Technologies',
    period: 'Jan 2024 – Present',
    desc: 'Developing microservices architecture, REST APIs, and database migrations for high-traffic financial systems.',
  },
  {
    title: 'Backend Developer Intern',
    company: 'StartupXYZ Labs',
    period: 'Jun 2023 – Dec 2023',
    desc: 'Built REST APIs using Spring Boot, improved database query performance by 40%, and wrote unit tests with JUnit.',
  },
];

const PROFILE_TABS = [
  { label: 'Profile Overview', value: 'profile' },
  { label: 'Skills & Tech Stack', value: 'skills' },
  { label: 'Work Experience', value: 'experience' },
  { label: 'Job Preferences', value: 'preferences' },
];

export default function ProfilePage() {
  const { user } = useStore();
  const [activeTab, setActiveTab] = useState('profile');
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [newSkillInput, setNewSkillInput] = useState('');
  const [skills, setSkills] = useState(INITIAL_SKILLS);

  const [profileData, setProfileData] = useState({
    name: user?.name || 'Jhasaketan M.',
    title: 'Java Backend Developer',
    location: 'Bengaluru, India',
    experience: '2.5 years',
    education: 'B.Tech in Computer Science',
    preferredMode: 'Hybrid / Remote',
    expectedSalary: '₹8–12 LPA',
    about: 'Enthusiastic Java developer with hands-on experience in Spring Boot, RESTful APIs, and microservices. Passionate about building resilient distributed systems and utilizing AI-powered workflows for agile execution.',
  });

  const [tempData, setTempData] = useState(profileData);

  const handleOpenEdit = () => {
    setTempData(profileData);
    setIsEditModalOpen(true);
  };

  const handleSaveProfile = () => {
    setProfileData(tempData);
    setIsEditModalOpen(false);
  };

  const handleAddSkill = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newSkillInput.trim()) return;
    if (!skills.includes(newSkillInput.trim())) {
      setSkills([...skills, newSkillInput.trim()]);
    }
    setNewSkillInput('');
  };

  const handleRemoveSkill = (skillToRemove: string) => {
    setSkills(skills.filter((s) => s !== skillToRemove));
  };

  return (
    <AppShell>
      <PageHeader
        title="Candidate Profile"
        subtitle="Manage your professional background, skills, and target job criteria"
        actions={
          <Button
            variant="primary"
            size="sm"
            leftIcon={<Edit3 size={14} />}
            onClick={handleOpenEdit}
          >
            Edit Profile
          </Button>
        }
      />

      {/* Profile Banner */}
      <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 mb-6 flex flex-col sm:flex-row items-center sm:items-start gap-6 shadow-xl">
        <Avatar name={profileData.name} size="lg" className="w-20 h-20 text-2xl ring-2 ring-[#2563EB]" />
        <div className="flex-1 text-center sm:text-left">
          <div className="flex flex-col sm:flex-row sm:items-center gap-2 mb-1">
            <h2 className="text-2xl font-bold text-[#F1F5F9]">{profileData.name}</h2>
            <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-0.5 rounded-full bg-[rgba(16,185,129,0.15)] text-[#10B981] border border-[rgba(16,185,129,0.3)] mx-auto sm:mx-0">
              <CheckCircle2 size={12} /> Verified Profile
            </span>
          </div>

          <p className="text-[#94A3B8] text-sm font-medium">{profileData.title}</p>

          <div className="flex items-center justify-center sm:justify-start gap-4 mt-3 text-xs text-[#64748B] flex-wrap">
            <span className="flex items-center gap-1 text-[#94A3B8]">
              <MapPin size={13} className="text-[#3B82F6]" /> {profileData.location}
            </span>
            <span className="flex items-center gap-1 text-[#94A3B8]">
              <Building2 size={13} className="text-[#10B981]" /> Open to work
            </span>
            <span className="flex items-center gap-1 text-[#94A3B8]">
              <Calendar size={13} className="text-[#8B5CF6]" /> {profileData.experience}
            </span>
          </div>
        </div>

        <div className="bg-[#111827] border border-[rgba(255,255,255,0.06)] rounded-xl p-4 text-center sm:text-right min-w-[140px]">
          <div className="text-2xl font-black text-[#2563EB]">94%</div>
          <div className="text-[11px] text-[#64748B] font-medium">Profile Completeness</div>
          <div className="w-full h-1.5 bg-[#243047] rounded-full mt-2 overflow-hidden">
            <div className="h-full bg-[#2563EB] rounded-full w-[94%]" />
          </div>
        </div>
      </div>

      <Tabs
        tabs={PROFILE_TABS}
        activeTab={activeTab}
        onChange={setActiveTab}
        variant="line"
        className="mb-6"
      />

      <TabContent>
        {activeTab === 'profile' && (
          <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 space-y-6 shadow-lg">
            <div>
              <h3 className="text-xs font-bold text-[#64748B] uppercase tracking-wider mb-2 flex items-center gap-2">
                <Sparkles size={14} className="text-[#38BDF8]" /> About Candidate
              </h3>
              <p className="text-sm text-[#F1F5F9] leading-relaxed">
                {profileData.about}
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 pt-5 border-t border-[rgba(148,163,184,0.08)]">
              <div className="p-4 rounded-xl bg-[#111827] border border-[rgba(255,255,255,0.04)]">
                <span className="text-xs text-[#64748B] flex items-center gap-1.5 mb-1">
                  <Briefcase size={13} className="text-[#3B82F6]" /> Target Role
                </span>
                <p className="text-sm font-bold text-[#F1F5F9]">{profileData.title}</p>
              </div>

              <div className="p-4 rounded-xl bg-[#111827] border border-[rgba(255,255,255,0.04)]">
                <span className="text-xs text-[#64748B] flex items-center gap-1.5 mb-1">
                  <Calendar size={13} className="text-[#8B5CF6]" /> Experience
                </span>
                <p className="text-sm font-bold text-[#F1F5F9]">{profileData.experience}</p>
              </div>

              <div className="p-4 rounded-xl bg-[#111827] border border-[rgba(255,255,255,0.04)]">
                <span className="text-xs text-[#64748B] flex items-center gap-1.5 mb-1">
                  <GraduationCap size={13} className="text-[#06B6D4]" /> Education
                </span>
                <p className="text-sm font-bold text-[#F1F5F9]">{profileData.education}</p>
              </div>

              <div className="p-4 rounded-xl bg-[#111827] border border-[rgba(255,255,255,0.04)]">
                <span className="text-xs text-[#64748B] flex items-center gap-1.5 mb-1">
                  <MapPin size={13} className="text-[#10B981]" /> Preferred Locations
                </span>
                <p className="text-sm font-bold text-[#F1F5F9]">{profileData.location}, Remote</p>
              </div>

              <div className="p-4 rounded-xl bg-[#111827] border border-[rgba(255,255,255,0.04)]">
                <span className="text-xs text-[#64748B] flex items-center gap-1.5 mb-1">
                  <Building2 size={13} className="text-[#F59E0B]" /> Work Mode
                </span>
                <p className="text-sm font-bold text-[#F1F5F9]">{profileData.preferredMode}</p>
              </div>

              <div className="p-4 rounded-xl bg-[#111827] border border-[rgba(255,255,255,0.04)]">
                <span className="text-xs text-[#64748B] flex items-center gap-1.5 mb-1">
                  <DollarSign size={13} className="text-[#10B981]" /> Expected Compensation
                </span>
                <p className="text-sm font-bold text-[#F1F5F9]">{profileData.expectedSalary}</p>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'skills' && (
          <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 space-y-6 shadow-lg">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <h3 className="text-base font-bold text-[#F1F5F9]">Verified Technical Skills</h3>
                <p className="text-xs text-[#94A3B8]">Skills used by the AI Agent to match against job descriptions</p>
              </div>

              <form onSubmit={handleAddSkill} className="flex gap-2">
                <input
                  type="text"
                  placeholder="Add skill (e.g. Docker)..."
                  value={newSkillInput}
                  onChange={(e) => setNewSkillInput(e.target.value)}
                  className="px-3 py-1.5 rounded-lg bg-[#111827] border border-[rgba(148,163,184,0.2)] text-xs text-[#F1F5F9] focus:outline-none focus:border-[#2563EB]"
                />
                <Button type="submit" size="sm" variant="primary" leftIcon={<Plus size={14} />}>
                  Add
                </Button>
              </form>
            </div>

            <div className="flex flex-wrap gap-2.5 pt-2">
              {skills.map((skill) => (
                <div
                  key={skill}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#111827] border border-[rgba(37,99,235,0.3)] text-xs font-semibold text-[#60A5FA] hover:border-[#2563EB] transition-colors"
                >
                  <span>{skill}</span>
                  <button
                    onClick={() => handleRemoveSkill(skill)}
                    className="p-0.5 hover:text-[#EF4444] transition-colors cursor-pointer"
                    title="Remove skill"
                  >
                    <X size={12} />
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'experience' && (
          <div className="space-y-4">
            {INITIAL_EXPERIENCE.map((exp) => (
              <div
                key={exp.title}
                className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-lg space-y-2"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h4 className="text-base font-bold text-[#F1F5F9]">{exp.title}</h4>
                    <p className="text-xs text-[#38BDF8] font-medium mt-0.5">{exp.company}</p>
                  </div>
                  <span className="text-xs text-[#94A3B8] bg-[#111827] px-3 py-1 rounded-full border border-[rgba(255,255,255,0.06)]">
                    {exp.period}
                  </span>
                </div>
                <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed pt-1">
                  {exp.desc}
                </p>
              </div>
            ))}
          </div>
        )}

        {activeTab === 'preferences' && (
          <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 space-y-5 shadow-lg">
            <h3 className="text-base font-bold text-[#F1F5F9]">Search Criteria & Guardrails</h3>
            <div className="space-y-3 text-xs text-[#94A3B8]">
              <div className="flex justify-between p-3 rounded-lg bg-[#111827]">
                <span>Automated Application Submission:</span>
                <span className="text-[#10B981] font-bold">Require Human Review Before Sending</span>
              </div>
              <div className="flex justify-between p-3 rounded-lg bg-[#111827]">
                <span>Target Geography:</span>
                <span className="text-[#F1F5F9] font-medium">India (Bengaluru, Hyderabad, Pune, Remote)</span>
              </div>
              <div className="flex justify-between p-3 rounded-lg bg-[#111827]">
                <span>Minimum Salary Floor:</span>
                <span className="text-[#F1F5F9] font-medium">₹6 LPA</span>
              </div>
            </div>
          </div>
        )}
      </TabContent>

      {/* Edit Profile Modal */}
      <Modal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        title="Edit Candidate Profile"
        size="md"
      >
        <div className="space-y-4 text-xs">
          <div>
            <label className="text-[#94A3B8] block mb-1 font-medium">Full Name</label>
            <input
              type="text"
              value={tempData.name}
              onChange={(e) => setTempData({ ...tempData, name: e.target.value })}
              className="w-full rounded-lg bg-[#111827] border border-[rgba(148,163,184,0.2)] p-2.5 text-[#F1F5F9] focus:outline-none focus:border-[#2563EB]"
            />
          </div>

          <div>
            <label className="text-[#94A3B8] block mb-1 font-medium">Target Role / Title</label>
            <input
              type="text"
              value={tempData.title}
              onChange={(e) => setTempData({ ...tempData, title: e.target.value })}
              className="w-full rounded-lg bg-[#111827] border border-[rgba(148,163,184,0.2)] p-2.5 text-[#F1F5F9] focus:outline-none focus:border-[#2563EB]"
            />
          </div>

          <div>
            <label className="text-[#94A3B8] block mb-1 font-medium">Location</label>
            <input
              type="text"
              value={tempData.location}
              onChange={(e) => setTempData({ ...tempData, location: e.target.value })}
              className="w-full rounded-lg bg-[#111827] border border-[rgba(148,163,184,0.2)] p-2.5 text-[#F1F5F9] focus:outline-none focus:border-[#2563EB]"
            />
          </div>

          <div>
            <label className="text-[#94A3B8] block mb-1 font-medium">About Candidate</label>
            <textarea
              rows={3}
              value={tempData.about}
              onChange={(e) => setTempData({ ...tempData, about: e.target.value })}
              className="w-full rounded-lg bg-[#111827] border border-[rgba(148,163,184,0.2)] p-2.5 text-[#F1F5F9] focus:outline-none focus:border-[#2563EB]"
            />
          </div>

          <div className="flex justify-end gap-2 pt-3">
            <Button variant="secondary" size="sm" onClick={() => setIsEditModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" size="sm" onClick={handleSaveProfile}>
              Save Changes
            </Button>
          </div>
        </div>
      </Modal>
    </AppShell>
  );
}
