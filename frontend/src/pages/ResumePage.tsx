import { AppShell } from '@/components/layout/AppShell';
import { PageHeader } from '@/components/layout/PageHeader';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { ProgressBar } from '@/components/ui';
import { Modal } from '@/components/overlays';
import { useState } from 'react';
import {
  Upload, FileText, CheckCircle2, Bot, Trash2, Eye,
  Sparkles, Plus, AlertCircle, Clock
} from 'lucide-react';

interface ResumeFile {
  id: string;
  name: string;
  size: string;
  uploadedAt: string;
  isActive: boolean;
  score: number;
}

const INITIAL_RESUMES: ResumeFile[] = [
  {
    id: '1',
    name: 'Jhasaketan_Java_Backend_Resume.pdf',
    size: '1.2 MB',
    uploadedAt: '2 days ago',
    isActive: true,
    score: 94,
  },
  {
    id: '2',
    name: 'Jhasaketan_FullStack_Resume.pdf',
    size: '1.4 MB',
    uploadedAt: '1 week ago',
    isActive: false,
    score: 87,
  },
];

export default function ResumePage() {
  const [resumes, setResumes] = useState(INITIAL_RESUMES);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [previewResume, setPreviewResume] = useState<ResumeFile | null>(null);

  const handleSimulatedUpload = () => {
    setIsUploading(true);
    setUploadProgress(15);
    const interval = setInterval(() => {
      setUploadProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval);
          setIsUploading(false);
          const newDoc: ResumeFile = {
            id: String(Date.now()),
            name: 'Updated_Candidate_Resume_2026.pdf',
            size: '1.1 MB',
            uploadedAt: 'Just now',
            isActive: false,
            score: 96,
          };
          setResumes((r) => [newDoc, ...r]);
          return 100;
        }
        return prev + 25;
      });
    }, 300);
  };

  const handleSetActive = (id: string) => {
    setResumes((prev) =>
      prev.map((r) => ({
        ...r,
        isActive: r.id === id,
      }))
    );
  };

  const handleDelete = (id: string) => {
    setResumes((prev) => prev.filter((r) => r.id !== id));
  };

  return (
    <AppShell>
      <PageHeader
        title="Resume Manager"
        subtitle="Manage and optimize tailored resumes for autonomous AI application submission"
        actions={
          <Button
            variant="primary"
            size="sm"
            leftIcon={<Upload size={14} />}
            onClick={handleSimulatedUpload}
          >
            Upload New Resume
          </Button>
        }
      />

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column: Upload Dropzone & Stored Resumes */}
        <div className="lg:col-span-7 space-y-6">
          {/* Upload Dropzone Card */}
          <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl space-y-4">
            <h3 className="text-sm font-bold text-[#F1F5F9] uppercase tracking-wider">
              Upload Resume File
            </h3>

            <div
              onClick={handleSimulatedUpload}
              className="flex flex-col items-center justify-center py-10 px-6 border-2 border-dashed border-[rgba(37,99,235,0.3)] rounded-xl hover:border-[#2563EB] transition-all cursor-pointer group bg-[rgba(37,99,235,0.03)] hover:bg-[rgba(37,99,235,0.08)] text-center"
            >
              <div className="w-14 h-14 rounded-2xl bg-[rgba(37,99,235,0.15)] flex items-center justify-center text-[#2563EB] mb-3 group-hover:scale-110 transition-transform">
                <Upload size={28} />
              </div>
              <p className="text-sm font-bold text-[#F1F5F9] mb-1">
                Drag and drop your PDF resume here, or <span className="text-[#38BDF8]">browse</span>
              </p>
              <p className="text-xs text-[#64748B]">PDF, DOCX up to 10MB · Parsed with Apache Tika AI engine</p>
            </div>

            {isUploading && (
              <div className="p-4 rounded-xl bg-[#111827] border border-[rgba(37,99,235,0.3)] space-y-2">
                <div className="flex justify-between text-xs font-semibold">
                  <span className="text-[#38BDF8] flex items-center gap-1.5">
                    <Bot size={14} className="animate-spin" /> AI Parsing & Vectorizing Resume...
                  </span>
                  <span className="text-[#F1F5F9]">{uploadProgress}%</span>
                </div>
                <ProgressBar value={uploadProgress} color="brand" size="sm" />
              </div>
            )}
          </div>

          {/* Stored Resumes List */}
          <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl space-y-4">
            <h3 className="text-sm font-bold text-[#F1F5F9] uppercase tracking-wider">
              Available Resumes ({resumes.length})
            </h3>

            <div className="space-y-3">
              {resumes.map((resume) => (
                <div
                  key={resume.id}
                  className={`p-4 rounded-xl border transition-all flex items-center justify-between gap-4 ${
                    resume.isActive
                      ? 'bg-[#1E293B] border-[rgba(37,99,235,0.4)] shadow-md'
                      : 'bg-[#111827] border-[rgba(255,255,255,0.04)] hover:bg-[#1E293B]'
                  }`}
                >
                  <div className="flex items-center gap-3.5 min-w-0">
                    <div className="w-10 h-10 rounded-xl bg-[rgba(37,99,235,0.15)] flex items-center justify-center text-[#60A5FA] flex-shrink-0">
                      <FileText size={20} />
                    </div>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <p className="text-sm font-bold text-[#F1F5F9] truncate">{resume.name}</p>
                        {resume.isActive && (
                          <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-[rgba(16,185,129,0.15)] text-[#10B981] border border-[rgba(16,185,129,0.3)]">
                            ACTIVE
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-[#64748B] mt-0.5">
                        {resume.size} · Uploaded {resume.uploadedAt} · <span className="text-[#10B981] font-semibold">{resume.score}% Match Fit</span>
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 flex-shrink-0">
                    <button
                      onClick={() => setPreviewResume(resume)}
                      className="p-1.5 text-[#94A3B8] hover:text-[#F1F5F9] hover:bg-[#243047] rounded-lg transition-colors cursor-pointer"
                      title="Preview Resume"
                    >
                      <Eye size={16} />
                    </button>
                    {!resume.isActive && (
                      <Button
                        size="sm"
                        variant="secondary"
                        className="text-xs h-7 px-2.5"
                        onClick={() => handleSetActive(resume.id)}
                      >
                        Set Active
                      </Button>
                    )}
                    {resumes.length > 1 && (
                      <button
                        onClick={() => handleDelete(resume.id)}
                        className="p-1.5 text-[#64748B] hover:text-[#EF4444] hover:bg-[#243047] rounded-lg transition-colors cursor-pointer"
                        title="Delete Resume"
                      >
                        <Trash2 size={16} />
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Column: AI Extraction Details */}
        <div className="lg:col-span-5 bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl space-y-5">
          <div className="flex items-center gap-2 pb-3 border-b border-[rgba(148,163,184,0.08)]">
            <Bot size={18} className="text-[#22D3EE]" />
            <h3 className="text-sm font-bold text-[#F1F5F9] uppercase tracking-wider">
              AI Extraction & Alignment
            </h3>
          </div>

          <div className="space-y-3">
            {[
              { label: 'Candidate Name', value: 'Jhasaketan M.', status: 'Verified' },
              { label: 'Contact Email', value: 'jhasaketan@example.com', status: 'Verified' },
              { label: 'Primary Tech Stack', value: 'Java, Spring Boot, REST APIs, SQL', status: '12 Skills' },
              { label: 'Total Experience', value: '2.5 Years Verified', status: 'Valid' },
              { label: 'Education Degree', value: 'B.Tech in Computer Science', status: 'Verified' },
              { label: 'ATS Compatibility Score', value: '98% (Clean Formatting)', status: 'High' },
            ].map((item) => (
              <div
                key={item.label}
                className="flex items-center justify-between p-2.5 rounded-lg bg-[#111827] border border-[rgba(255,255,255,0.04)] text-xs"
              >
                <div>
                  <span className="text-[#64748B] block text-[11px]">{item.label}</span>
                  <span className="text-[#F1F5F9] font-medium">{item.value}</span>
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-[rgba(16,185,129,0.15)] text-[#10B981]">
                  {item.status}
                </span>
              </div>
            ))}
          </div>

          <div className="pt-2">
            <div className="flex justify-between text-xs mb-1.5">
              <span className="text-[#94A3B8]">Overall Extraction Completeness</span>
              <span className="font-bold text-[#10B981]">96%</span>
            </div>
            <ProgressBar value={96} color="success" size="sm" />
          </div>
        </div>
      </div>

      {/* Resume Preview Modal */}
      <Modal
        isOpen={Boolean(previewResume)}
        onClose={() => setPreviewResume(null)}
        title={previewResume?.name || 'Resume Preview'}
        size="lg"
      >
        <div className="space-y-4">
          <div className="p-4 rounded-xl bg-[#111827] border border-[rgba(255,255,255,0.06)] font-mono text-xs text-[#94A3B8] space-y-3 max-h-[60vh] overflow-y-auto">
            <div className="text-center pb-3 border-b border-[rgba(148,163,184,0.1)]">
              <h2 className="text-base font-bold text-[#F1F5F9]">Jhasaketan M.</h2>
              <p className="text-xs text-[#60A5FA]">jhasaketan@example.com · +91 98765 43210 · Bengaluru, India</p>
            </div>
            <div>
              <h4 className="font-bold text-[#F1F5F9] uppercase">SUMMARY</h4>
              <p>Backend Developer with 2.5+ years of experience specializing in Java, Spring Boot, microservices architecture, and SQL databases.</p>
            </div>
            <div>
              <h4 className="font-bold text-[#F1F5F9] uppercase">CORE SKILLS</h4>
              <p>Java, Spring Boot, REST APIs, Microservices, SQL, MySQL, PostgreSQL, Git, Maven, JUnit, Docker, AWS.</p>
            </div>
            <div>
              <h4 className="font-bold text-[#F1F5F9] uppercase">EXPERIENCE</h4>
              <p>Junior Developer @ TechFirm (2024–Present): Built scalable REST APIs and improved database performance.</p>
              <p>Backend Intern @ StartupXYZ (2023): Designed Spring Boot services and unit tests.</p>
            </div>
          </div>

          <div className="flex justify-end gap-2">
            <Button variant="secondary" size="sm" onClick={() => setPreviewResume(null)}>
              Close Preview
            </Button>
          </div>
        </div>
      </Modal>
    </AppShell>
  );
}
