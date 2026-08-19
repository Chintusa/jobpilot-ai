import type {
  Job, Application, DashboardSummary, ActivityEntry, LogEntry,
  AgentConfig, Intervention, ScreeningQuestion, JobAssessment
} from '@/types';

// ============================================================
// Mock Jobs
// ============================================================
export const mockJobs: Job[] = [
  {
    id: '1',
    title: 'Java Backend Developer',
    company: 'TechNova Technologies',
    location: 'Bengaluru',
    workMode: 'HYBRID',
    salaryMin: 600000,
    salaryMax: 900000,
    salaryCurrency: 'INR',
    salaryDisplay: '₹6–9 LPA',
    experience: '0–2 years',
    matchScore: 91,
    skills: [
      { name: 'Java', matched: true },
      { name: 'Spring Boot', matched: true },
      { name: 'REST APIs', matched: true },
      { name: 'SQL', matched: true },
    ],
    missingSkills: [{ name: 'AWS', matched: false }],
    isBookmarked: false,
    postedAt: '2024-01-15',
    source: 'LinkedIn',
  },
  {
    id: '2',
    title: 'Senior Full Stack Engineer',
    company: 'InnovateTech',
    location: 'Hyderabad',
    workMode: 'REMOTE',
    salaryMin: 1200000,
    salaryMax: 1800000,
    salaryCurrency: 'INR',
    salaryDisplay: '₹12–18 LPA',
    experience: '3–5 years',
    matchScore: 78,
    skills: [
      { name: 'React', matched: true },
      { name: 'Node.js', matched: true },
      { name: 'TypeScript', matched: true },
    ],
    missingSkills: [
      { name: 'PostgreSQL', matched: false },
      { name: 'Docker', matched: false },
    ],
    postedAt: '2024-01-14',
    source: 'Naukri',
  },
  {
    id: '3',
    title: 'Backend Engineer — Python',
    company: 'DataFlow Systems',
    location: 'Pune',
    workMode: 'ONSITE',
    salaryDisplay: '₹8–12 LPA',
    experience: '2–4 years',
    matchScore: 65,
    skills: [
      { name: 'Python', matched: true },
      { name: 'FastAPI', matched: true },
    ],
    missingSkills: [
      { name: 'Kafka', matched: false },
      { name: 'Kubernetes', matched: false },
    ],
    postedAt: '2024-01-13',
    source: 'LinkedIn',
  },
  {
    id: '4',
    title: 'Software Engineer II',
    company: 'GlobalSoft',
    location: 'Chennai',
    workMode: 'HYBRID',
    salaryDisplay: '₹10–15 LPA',
    experience: '2–4 years',
    matchScore: 85,
    skills: [
      { name: 'Java', matched: true },
      { name: 'Microservices', matched: true },
    ],
    missingSkills: [{ name: 'Kafka', matched: false }],
    postedAt: '2024-01-12',
    source: 'Indeed',
  },
];

// ============================================================
// Mock Dashboard
// ============================================================
export const mockDashboardSummary: DashboardSummary = {
  jobsFound: 127,
  strongMatches: 18,
  applications: 7,
  interviews: 2,
  agentStatus: 'ACTIVE',
  pipelineCounts: {
    DISCOVERED: 127,
    MATCHED: 18,
    PREPARING: 5,
    REVIEW: 3,
    SUBMITTED: 7,
    INTERVIEW: 2,
    OFFER: 0,
  },
};

// ============================================================
// Mock Activity
// ============================================================
export const mockActivityEntries: ActivityEntry[] = [
  { id: '1', timestamp: '10:32', message: 'Found 18 new Java backend jobs', type: 'info' },
  { id: '2', timestamp: '10:34', message: 'Ranked 6 as strong matches', type: 'success' },
  { id: '3', timestamp: '10:36', message: 'Prepared 2 applications', type: 'success' },
  { id: '4', timestamp: '10:37', message: 'Waiting for your approval', type: 'warning', isActive: true },
];

export const mockAgentLogs: LogEntry[] = [
  { id: '1', timestamp: '10:15:30', message: 'Search started', level: 'info' },
  { id: '2', timestamp: '10:17:12', message: 'Jobs discovered: 18 new listings', level: 'info' },
  { id: '3', timestamp: '10:18:01', message: 'Jobs analyzed: 12 matches found', level: 'info' },
  { id: '4', timestamp: '10:20:45', message: 'Applications prepared: 3 pending review', level: 'info' },
  { id: '5', timestamp: '10:22:10', message: 'Waiting for user approval on Application ID: #JP-A-045', level: 'warning' },
];

// ============================================================
// Mock Applications
// ============================================================
export const mockApplications: Application[] = [
  {
    id: 'a1',
    jobTitle: 'Java Backend Developer',
    company: 'TechNova Technologies',
    location: 'Bengaluru',
    salary: '₹6–9 LPA',
    matchScore: 91,
    status: 'PENDING_REVIEW',
    appliedAt: '2024-01-15',
    lastUpdatedAt: '2024-01-16',
    submittedVia: 'AI_AGENT',
  },
  {
    id: 'a2',
    jobTitle: 'Senior Full Stack Engineer',
    company: 'InnovateTech',
    location: 'Hyderabad',
    salary: '₹12–18 LPA',
    matchScore: 78,
    status: 'SUBMITTED',
    appliedAt: '2024-01-14',
    lastUpdatedAt: '2024-01-15',
    submittedVia: 'AI_AGENT',
  },
  {
    id: 'a3',
    jobTitle: 'Software Engineer II',
    company: 'GlobalSoft',
    location: 'Chennai',
    salary: '₹10–15 LPA',
    matchScore: 85,
    status: 'INTERVIEWING',
    appliedAt: '2024-01-10',
    lastUpdatedAt: '2024-01-16',
    submittedVia: 'AI_AGENT',
  },
  {
    id: 'a4',
    jobTitle: 'Backend Engineer',
    company: 'DataFlow Systems',
    location: 'Pune',
    salary: '₹8–12 LPA',
    matchScore: 72,
    status: 'MATCHED',
    appliedAt: '2024-01-16',
    lastUpdatedAt: '2024-01-16',
    submittedVia: 'AI_AGENT',
  },
];

// ============================================================
// Mock Job Assessment
// ============================================================
export const mockJobAssessment: JobAssessment = {
  matchScore: 91,
  label: 'STRONG_MATCH',
  breakdown: {
    technicalSkills: 94,
    experience: 90,
    education: 100,
    location: 100,
    projects: 88,
    roleFit: 92,
  },
  profileSkills: [
    { name: 'Java', matched: true },
    { name: 'Spring Boot', matched: true },
    { name: 'REST APIs', matched: true },
    { name: 'AI', matched: true },
    { name: 'MySQL', matched: false },
  ],
  requiredSkills: [
    { name: 'Java', matched: true },
    { name: 'Spring Boot', matched: true },
    { name: 'ANI', matched: true },
    { name: 'Defost', matched: true },
    { name: 'PostgreSQL', matched: false },
    { name: 'Docker', matched: false },
  ],
  matchedSkills: [
    { name: 'Java', matched: true },
    { name: 'Spring Boot', matched: true },
    { name: 'REST APIs', matched: true },
  ],
  missingSkills: [
    { name: 'PostgreSQL', matched: false },
    { name: 'Docker', matched: false },
  ],
  whyYouMatch:
    'AI-generated explanation for this Java Backend role. Your Java and Spring Boot skills align strongly with the technical requirements. Your project experience demonstrates enterprise-level application development, and your background matches the expected profile for this hybrid role in Bengaluru.',
  potentialConcerns: [
    'Missing PostgreSQL experience — however MySQL is transferable.',
    'Docker containerisation is listed as preferred, not mandatory.',
  ],
};

// ============================================================
// Mock Agent Config
// ============================================================
export const mockAgentConfig: AgentConfig = {
  jobDiscovery: {
    searchAutomatically: true,
    multipleSourceSearch: true,
    removeDuplicates: true,
    analyzeNewJobs: true,
  },
  applications: {
    prepareAutomatically: true,
    finalSubmission: 'USER_APPROVAL_REQUIRED',
    dailyApplicationLimit: 5,
  },
  matching: {
    minimumMatchScore: 85,
    requireAllMandatory: true,
  },
  humanIntervention: {
    pauseOnCaptcha: true,
    pauseOnMFA: true,
    pauseOnUnknownQuestions: true,
    pauseOnLegalDeclarations: true,
    pauseOnMissingInfo: true,
    pauseOnUnsupportedFlows: true,
  },
};

// ============================================================
// Mock Screening Questions
// ============================================================
export const mockScreeningQuestions: ScreeningQuestion[] = [
  {
    id: 'sq1',
    question: 'How many years of experience do you have with Spring Boot?',
    aiAnswer: '2.5 years',
    confidence: 'HIGH',
    status: 'PENDING',
    source: 'Based on your verified profile',
  },
  {
    id: 'sq2',
    question: 'Are you legally authorized to work in the country?',
    aiAnswer: null,
    confidence: 'UNKNOWN',
    status: 'NEEDS_ANSWER',
  },
  {
    id: 'sq3',
    question: 'Are you willing to relocate to Bengaluru?',
    aiAnswer: 'Yes',
    confidence: 'HIGH',
    status: 'ACCEPTED',
    source: 'From your preferred locations',
  },
];

// ============================================================
// Mock Interventions
// ============================================================
export const mockInterventions: Intervention[] = [
  {
    id: 'i1',
    applicationId: 'a1',
    jobTitle: 'Java Backend Developer',
    company: 'TechNova Technologies',
    type: 'UNKNOWN_QUESTION',
    description: 'Application contains a question the AI could not answer: "Describe your experience with microservices architecture."',
    createdAt: '2024-01-16T10:22:10Z',
    status: 'PENDING',
  },
  {
    id: 'i2',
    applicationId: 'a2',
    jobTitle: 'Senior Full Stack Engineer',
    company: 'InnovateTech',
    type: 'CAPTCHA',
    description: 'CAPTCHA detected on application form. Human verification required.',
    createdAt: '2024-01-15T14:30:00Z',
    status: 'PENDING',
  },
];
