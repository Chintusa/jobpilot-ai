import { AppShell } from '@/components/layout/AppShell';
import { PageHeader } from '@/components/layout/PageHeader';
import { AIOrb, ApplicationProgressStepper } from '@/components/ai';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/overlays';
import { mockJobs } from '@/data/mockData';
import { useParams, useNavigate } from 'react-router-dom';
import { ROUTES } from '@/routes/routes';
import { useState, useEffect } from 'react';
import {
  CheckCircle2,
  ArrowLeft,
  Send,
  Sparkles,
  FileText,
  AlertTriangle,
  Edit3,
  ShieldAlert,
  AlertCircle,
  Clock,
  Loader2
} from 'lucide-react';
import {
  useJobDetailQuery,
  usePrepareApplicationMutation,
  useUpdateApplicationContentMutation,
  useSubmitApplicationMutation,
  type BackendApplication,
  type BackendScreeningQuestion
} from '@/api/jobsApi';

const APPLICATION_STEPS = [
  { label: 'Job analyzed', status: 'completed' as const },
  { label: 'Candidate matched', status: 'completed' as const },
  { label: 'Resume selected', status: 'completed' as const },
  { label: 'Cover letter generated', status: 'completed' as const },
  { label: 'Screening questions', status: 'active' as const },
  { label: 'Review', status: 'pending' as const },
  { label: 'Submit', status: 'pending' as const },
];

export default function ApplicationAgentPage() {
  const { jobId } = useParams();
  const navigate = useNavigate();

  // Queries & Mutations
  const { data: jobData } = useJobDetailQuery(jobId);
  const prepareMutation = usePrepareApplicationMutation();
  const updateContentMutation = useUpdateApplicationContentMutation();
  const submitMutation = useSubmitApplicationMutation();

  const [application, setApplication] = useState<BackendApplication | null>(null);
  const [editingQuestion, setEditingQuestion] = useState<BackendScreeningQuestion | null>(null);
  const [editAnswerText, setEditAnswerText] = useState('');
  const [editingCoverLetter, setEditingCoverLetter] = useState(false);
  const [coverLetterText, setCoverLetterText] = useState('');
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [showSubmitSuccess, setShowSubmitSuccess] = useState(false);

  const fallbackJob = mockJobs.find((j) => j.id === jobId) || mockJobs[0];
  const job = jobData || fallbackJob;

  useEffect(() => {
    if (jobId) {
      prepareMutation.mutate(jobId, {
        onSuccess: (data) => {
          setApplication(data);
          setCoverLetterText(data.coverLetter || '');
        },
      });
    }
  }, [jobId]);

  const questions: BackendScreeningQuestion[] = application?.screeningQuestions || [
    {
      id: 'q1',
      question: 'How many years of relevant software engineering experience do you have?',
      aiAnswer: '2.5 years of verified software development experience',
      confidence: 'HIGH',
      source: 'Extracted from verified candidate profile',
      status: 'ACCEPTED',
    },
    {
      id: 'q2',
      question: 'Are you comfortable working in a Hybrid setup in Bengaluru?',
      aiAnswer: 'Yes, fully comfortable with Hybrid work in Bengaluru',
      confidence: 'HIGH',
      source: 'Derived from user preferences and location settings',
      status: 'ACCEPTED',
    },
    {
      id: 'q3',
      question: 'What is your expected annual compensation (CTC)?',
      aiAnswer: '₹8.0 LPA (open to discussion based on role)',
      confidence: 'MEDIUM',
      source: 'Extracted from job preferences target salary',
      status: 'PENDING',
    },
    {
      id: 'q4',
      question: 'Do you currently hold an active government security clearance or sponsorship requirement?',
      aiAnswer: undefined,
      candidateAnswer: undefined,
      confidence: 'UNKNOWN',
      source: 'Unrecorded legal/clearance status',
      status: 'REQUIRES_USER_INPUT',
    },
  ];

  const handleAcceptQuestion = (qId: string) => {
    if (!application) return;
    const target = questions.find((q) => q.id === qId);
    if (!target) return;

    updateContentMutation.mutate({
      applicationId: application.id,
      data: {
        screeningAnswers: [
          {
            questionId: qId,
            candidateAnswer: target.aiAnswer || target.candidateAnswer || '',
            status: 'ACCEPTED',
          },
        ],
      },
    }, {
      onSuccess: (updated) => setApplication(updated),
    });
  };

  const handleSaveQuestionEdit = () => {
    if (!editingQuestion || !application) return;
    updateContentMutation.mutate({
      applicationId: application.id,
      data: {
        screeningAnswers: [
          {
            questionId: editingQuestion.id,
            candidateAnswer: editAnswerText,
            status: 'EDITED',
          },
        ],
      },
    }, {
      onSuccess: (updated) => {
        setApplication(updated);
        setEditingQuestion(null);
      },
    });
  };

  const handleSaveCoverLetter = () => {
    if (!application) return;
    updateContentMutation.mutate({
      applicationId: application.id,
      data: {
        coverLetter: coverLetterText,
      },
    }, {
      onSuccess: (updated) => {
        setApplication(updated);
        setEditingCoverLetter(false);
      },
    });
  };

  const handleSubmitApplication = () => {
    if (!application) {
      setShowReviewModal(false);
      setShowSubmitSuccess(true);
      return;
    }
    submitMutation.mutate(application.id, {
      onSuccess: () => {
        setShowReviewModal(false);
        setShowSubmitSuccess(true);
      },
    });
  };

  const prepState = application?.preparationState || 'PREPARING';
  const missingInfo = application?.missingInformation || [];

  return (
    <AppShell>
      <PageHeader
        title="Application Agent"
        subtitle={`Preparing and automating application for ${job.title} at ${job.company}`}
        actions={
          <div className="flex items-center gap-3">
            <Button
              variant="secondary"
              size="sm"
              leftIcon={<ArrowLeft size={14} />}
              onClick={() => navigate(ROUTES.JOBS)}
            >
              Back to Jobs
            </Button>
            <Button
              variant="primary"
              size="sm"
              leftIcon={<Send size={14} />}
              onClick={() => setShowReviewModal(true)}
            >
              Review & Submit
            </Button>
          </div>
        }
      />

      {/* Progress Stepper Bar */}
      <div className="p-4 rounded-xl bg-[#1A2235] border border-[rgba(255,255,255,0.06)] mb-6 shadow-md overflow-x-auto">
        <ApplicationProgressStepper
          steps={APPLICATION_STEPS.map((s, idx) => ({
            ...s,
            status:
              idx < 4
                ? 'completed'
                : idx === 4
                ? prepState === 'READY_FOR_REVIEW' || prepState === 'USER_APPROVED'
                  ? 'completed'
                  : 'active'
                : 'pending',
          }))}
        />
      </div>

      {/* Missing Information / Preparation Alert Banner */}
      {missingInfo.length > 0 && (
        <div className="mb-6 p-4 rounded-2xl bg-[rgba(245,158,11,0.1)] border border-[rgba(245,158,11,0.3)] flex items-start gap-3">
          <AlertCircle size={20} className="text-[#F59E0B] flex-shrink-0 mt-0.5" />
          <div className="space-y-1">
            <h4 className="text-xs font-bold text-[#F59E0B] uppercase tracking-wider">
              Missing Information & Zero-Fabrication Guardrail (State: {prepState})
            </h4>
            <p className="text-xs text-[#F1F5F9]">
              To prevent hallucinated candidate claims, the following items require your direct verification before final submission:
            </p>
            <ul className="list-disc list-inside text-xs text-[#94A3B8] space-y-0.5 pt-1">
              {missingInfo.map((item, idx) => (
                <li key={idx}>{item}</li>
              ))}
            </ul>
          </div>
        </div>
      )}

      {/* Main 2-Column Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column (Span 6): Application Form Preview & Tailored Cover Letter */}
        <div className="lg:col-span-6 space-y-6">
          {/* Form Fields Card */}
          <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-[rgba(148,163,184,0.08)]">
              <div className="flex items-center gap-2">
                <h3 className="text-sm font-bold text-[#F1F5F9] uppercase tracking-wider">
                  Application Form Preview
                </h3>
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-[rgba(37,99,235,0.2)] text-[#60A5FA]">
                  FACT GROUNDED
                </span>
              </div>
              <span className="text-xs text-[#10B981] font-medium flex items-center gap-1">
                <CheckCircle2 size={13} /> Verified Profile
              </span>
            </div>

            <div className="space-y-3">
              {[
                { label: 'Target Position', value: job.title },
                { label: 'Employer', value: job.company },
                { label: 'Location', value: job.location },
                { label: 'Work Mode', value: job.workMode },
              ].map((field) => (
                <div key={field.label}>
                  <label className="text-xs font-medium text-[#64748B] mb-1 block">
                    {field.label}
                  </label>
                  <div className="w-full h-10 px-3.5 flex items-center bg-[#111827] border border-[rgba(148,163,184,0.12)] rounded-lg text-xs sm:text-sm text-[#F1F5F9]">
                    {field.value}
                  </div>
                </div>
              ))}

              {/* Tailored Resume File */}
              <div>
                <label className="text-xs font-medium text-[#64748B] mb-1 block">
                  Application-Ready Resume
                </label>
                <div className="w-full h-11 px-3.5 flex items-center justify-between bg-[#111827] border border-[rgba(37,99,235,0.3)] rounded-lg text-xs sm:text-sm text-[#60A5FA]">
                  <div className="flex items-center gap-2">
                    <FileText size={16} />
                    <span className="font-medium">
                      {application?.tailoredResumeUrl ? 'Tailored_Resume_' + job.company.replace(/\s+/g, '') + '.pdf' : 'Tailored_Java_Resume.pdf'}
                    </span>
                  </div>
                  <span className="text-[11px] text-[#10B981] font-semibold bg-[rgba(16,185,129,0.15)] px-2 py-0.5 rounded">
                    Fact-Verified ✓
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Fact-Grounded Cover Letter Editor Card */}
          <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-[rgba(148,163,184,0.08)]">
              <h3 className="text-sm font-bold text-[#F1F5F9] uppercase tracking-wider flex items-center gap-2">
                <FileText size={16} className="text-[#38BDF8]" />
                Tailored Cover Letter
              </h3>
              <Button
                variant="secondary"
                size="sm"
                leftIcon={<Edit3 size={13} />}
                onClick={() => setEditingCoverLetter(!editingCoverLetter)}
              >
                {editingCoverLetter ? 'Cancel' : 'Edit Letter'}
              </Button>
            </div>

            {editingCoverLetter ? (
              <div className="space-y-3">
                <textarea
                  value={coverLetterText}
                  onChange={(e) => setCoverLetterText(e.target.value)}
                  rows={8}
                  className="w-full rounded-xl bg-[#111827] border border-[rgba(37,99,235,0.4)] p-4 text-xs sm:text-sm text-[#F1F5F9] focus:outline-none leading-relaxed"
                />
                <div className="flex justify-end">
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={handleSaveCoverLetter}
                    disabled={updateContentMutation.isPending}
                  >
                    Save Changes
                  </Button>
                </div>
              </div>
            ) : (
              <div className="bg-[#111827] p-4 rounded-xl border border-[rgba(255,255,255,0.04)] text-xs sm:text-sm text-[#94A3B8] leading-relaxed whitespace-pre-line">
                {coverLetterText || application?.coverLetter || 'Generating fact-grounded cover letter...'}
              </div>
            )}
          </div>
        </div>

        {/* Right Column (Span 6): AI Agent Status & Screening Questions */}
        <div className="lg:col-span-6 space-y-6">
          {/* AI Agent Status Card */}
          <div className="bg-[#1A2235] border border-[rgba(255,255,255,0.06)] rounded-2xl p-6 shadow-xl space-y-4">
            <div className="flex items-center gap-4">
              <AIOrb size="sm" status="active" />
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="text-base font-bold text-[#F1F5F9]">AI Application Agent</h3>
                  <Badge variant={prepState === 'READY_FOR_REVIEW' ? 'match-strong' : 'info'}>
                    {prepState}
                  </Badge>
                </div>
                <p className="text-xs text-[#94A3B8] mt-0.5">
                  Grounded strictly in candidate profile facts.
                </p>
              </div>
            </div>

            {application?.applicationSummary && (
              <div className="p-3.5 rounded-xl bg-[#111827] border border-[rgba(255,255,255,0.04)] text-xs text-[#94A3B8] leading-relaxed whitespace-pre-line">
                {application.applicationSummary}
              </div>
            )}
          </div>

          {/* Screening Question Cards */}
          <div className="space-y-3.5">
            <div className="flex items-center justify-between">
              <h3 className="text-xs font-bold text-[#64748B] uppercase tracking-wider">
                Screening Questions & Zero-Fabrication Guardrail
              </h3>
              <span className="text-xs text-[#F59E0B] font-semibold bg-[rgba(245,158,11,0.1)] px-2.5 py-0.5 rounded-full border border-[rgba(245,158,11,0.2)]">
                {questions.filter((q) => q.status !== 'ACCEPTED').length} Action Items
              </span>
            </div>

            {questions.map((q) => {
              const isUnknown = q.status === 'REQUIRES_USER_INPUT' || q.confidence === 'UNKNOWN';
              const answerDisplay = q.candidateAnswer || q.aiAnswer;

              return (
                <div
                  key={q.id}
                  className={`bg-[#1A2235] border rounded-xl p-5 space-y-3 shadow-md transition-all ${
                    isUnknown
                      ? 'border-[rgba(245,158,11,0.4)] bg-gradient-to-r from-[#1A2235] to-[rgba(245,158,11,0.04)]'
                      : 'border-[rgba(255,255,255,0.08)]'
                  }`}
                >
                  <div className="flex items-start justify-between gap-3">
                    <p className="text-sm font-bold text-[#F1F5F9] leading-snug">{q.question}</p>
                    {isUnknown ? (
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-[rgba(245,158,11,0.2)] text-[#F59E0B] border border-[rgba(245,158,11,0.3)] flex items-center gap-1 flex-shrink-0">
                        <AlertTriangle size={11} /> REQUIRES_USER_INPUT
                      </span>
                    ) : (
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-[rgba(16,185,129,0.2)] text-[#10B981] border border-[rgba(16,185,129,0.3)] flex-shrink-0">
                        {q.confidence} Confidence
                      </span>
                    )}
                  </div>

                  {answerDisplay ? (
                    <div className="rounded-lg p-3 bg-[rgba(16,185,129,0.08)] border border-[rgba(16,185,129,0.25)]">
                      <p className="text-xs text-[#94A3B8] mb-0.5">
                        {q.source || 'Based on your verified profile'}:
                      </p>
                      <p className="text-xs font-bold text-[#F1F5F9]">{answerDisplay}</p>
                    </div>
                  ) : (
                    <div className="rounded-lg p-3 bg-[rgba(245,158,11,0.08)] border border-[rgba(245,158,11,0.25)]">
                      <p className="text-xs text-[#F59E0B] font-medium">
                        ⚠ AI cannot determine this from your verified profile without inventing data. Please provide your answer.
                      </p>
                    </div>
                  )}

                  {/* Question Actions */}
                  <div className="flex items-center gap-2.5 pt-1">
                    {q.status === 'ACCEPTED' ? (
                      <div className="flex items-center gap-1.5 text-xs text-[#10B981] font-bold">
                        <CheckCircle2 size={15} /> Answer Confirmed
                      </div>
                    ) : answerDisplay ? (
                      <>
                        <Button
                          size="sm"
                          variant="primary"
                          className="px-4 text-xs font-semibold"
                          onClick={() => handleAcceptQuestion(q.id)}
                        >
                          Accept
                        </Button>
                        <Button
                          size="sm"
                          variant="secondary"
                          className="px-4 text-xs font-semibold"
                          onClick={() => {
                            setEditingQuestion(q);
                            setEditAnswerText(answerDisplay || '');
                          }}
                        >
                          Edit
                        </Button>
                      </>
                    ) : (
                      <Button
                        size="sm"
                        variant="primary"
                        className="px-4 text-xs font-semibold bg-[#F59E0B] hover:bg-[#D97706] text-[#0A0F1E]"
                        onClick={() => {
                          setEditingQuestion(q);
                          setEditAnswerText('');
                        }}
                      >
                        Provide Answer
                      </Button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* ——— Edit Screening Answer Modal ——— */}
      <Modal
        isOpen={!!editingQuestion}
        onClose={() => setEditingQuestion(null)}
        title="Edit Screening Answer"
        size="md"
      >
        {editingQuestion && (
          <div className="space-y-4 py-2">
            <div className="p-3.5 rounded-xl bg-[#111827] border border-[rgba(255,255,255,0.06)]">
              <span className="text-[10px] text-[#64748B] uppercase font-bold tracking-wider block mb-1">
                Question
              </span>
              <p className="text-sm font-semibold text-[#F1F5F9]">{editingQuestion.question}</p>
            </div>

            <div>
              <label className="text-xs text-[#64748B] mb-1.5 block">Your Verified Answer</label>
              <textarea
                value={editAnswerText}
                onChange={(e) => setEditAnswerText(e.target.value)}
                placeholder="Enter verified response..."
                rows={3}
                className="w-full rounded-lg bg-[#111827] border border-[rgba(148,163,184,0.2)] p-3 text-sm text-[#F1F5F9] focus:outline-none focus:border-[#2563EB]"
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button variant="secondary" size="sm" onClick={() => setEditingQuestion(null)}>
                Cancel
              </Button>
              <Button variant="primary" size="sm" onClick={handleSaveQuestionEdit}>
                Confirm Answer
              </Button>
            </div>
          </div>
        )}
      </Modal>

      {/* ——— Review Application Modal (Human-in-the-Loop) ——— */}
      <Modal
        isOpen={showReviewModal}
        onClose={() => setShowReviewModal(false)}
        title="Review Application Submission"
        size="lg"
      >
        <div className="space-y-5">
          <div className="p-4 rounded-xl bg-[#111827] border border-[rgba(255,255,255,0.06)] flex items-center justify-between">
            <div>
              <h3 className="font-bold text-base text-[#F1F5F9]">{job.title}</h3>
              <p className="text-xs text-[#94A3B8]">{job.company} · {job.location}</p>
            </div>
            <span className="text-xs font-bold text-[#10B981] bg-[rgba(16,185,129,0.15)] px-3 py-1 rounded-full">
              91% Match
            </span>
          </div>

          <div className="space-y-2">
            <h4 className="text-xs font-bold text-[#64748B] uppercase tracking-wider">Screening Answers</h4>
            <div className="space-y-2 max-h-44 overflow-y-auto pr-1">
              {questions.map((q) => (
                <div key={q.id} className="p-2.5 rounded-lg bg-[#111827] text-xs space-y-1">
                  <p className="font-semibold text-[#94A3B8]">{q.question}</p>
                  <p className="text-[#10B981] font-bold">Answer: {q.candidateAnswer || q.aiAnswer || 'Not answered'}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="p-3 rounded-xl bg-[rgba(37,99,235,0.1)] border border-[rgba(37,99,235,0.25)] flex items-start gap-2.5 text-xs text-[#93C5FD]">
            <ShieldAlert size={16} className="text-[#38BDF8] flex-shrink-0 mt-0.5" />
            <span>
              By confirming, you authorize JobPilot AI to submit this application to {job.company} using verified candidate profile data.
            </span>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button variant="secondary" size="md" onClick={() => setShowReviewModal(false)}>
              Back to Edit
            </Button>
            <Button
              variant="primary"
              size="md"
              leftIcon={<Send size={15} />}
              onClick={handleSubmitApplication}
              disabled={submitMutation.isPending}
            >
              {submitMutation.isPending ? 'Submitting...' : 'Confirm & Submit Application'}
            </Button>
          </div>
        </div>
      </Modal>

      {/* ——— Submission Success Modal ——— */}
      <Modal
        isOpen={showSubmitSuccess}
        onClose={() => {
          setShowSubmitSuccess(false);
          navigate(ROUTES.APPLICATIONS);
        }}
        size="sm"
      >
        <div className="text-center py-4 space-y-4">
          <div className="w-14 h-14 mx-auto rounded-2xl bg-[rgba(16,185,129,0.15)] border border-[rgba(16,185,129,0.3)] flex items-center justify-center text-[#10B981] shadow-[0_0_24px_rgba(16,185,129,0.3)]">
            <CheckCircle2 size={32} />
          </div>

          <div>
            <h3 className="text-lg font-bold text-[#F1F5F9]">Application Submitted!</h3>
            <p className="text-xs text-[#94A3B8] mt-1">
              Your application for <strong>{job.title}</strong> at <strong>{job.company}</strong> has been logged in your Application Tracker.
            </p>
          </div>

          <Button
            variant="primary"
            size="md"
            fullWidth
            onClick={() => {
              setShowSubmitSuccess(false);
              navigate(ROUTES.APPLICATIONS);
            }}
          >
            View Applications Tracker
          </Button>
        </div>
      </Modal>
    </AppShell>
  );
}
