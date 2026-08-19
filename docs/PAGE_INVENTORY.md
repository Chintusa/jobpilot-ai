# JobPilot AI — Page Inventory

> Derived from `design-reference/` images 01–07.
> Each entry maps one design reference to a specific application route.

---

## Image → Page Mapping

| File                        | Page                    | Route                  |
|-----------------------------|-------------------------|------------------------|
| `01-dashboard.png`          | Dashboard (multi-screen overview) | `/dashboard`  |
| `02-landing-page.png`       | Landing / Marketing Page          | `/`           |
| `03-job-details.png`        | Dashboard / AI Career Agent       | `/dashboard`  |
| `04-application-agent.png`  | Job Detail & AI Assessment        | `/jobs/:id`   |
| `05-agent-control-center.png`| Application Agent (Form + AI)    | `/agent/apply/:jobId` |
| `06-design-system.png`      | AI Agent Control Center           | `/agent`      |
| `07-application-tracker.png`| Design System Reference           | (internal)    |

> **Note**: Image 01 is a mosaic of many screens; each sub-screen is documented below.
> Image 06 is the design system reference (component library, not a runtime page).

---

## Page 1 — Landing / Marketing Page

**Source**: `02-landing-page.png`  
**Route**: `/`

### Purpose
Public-facing marketing page. Communicates the product value proposition and funnel to sign-up.

### Main Sections
1. **Top Navigation Bar** — Logo + nav links (Futerniam, Features, Application, Pricing) + Login + CTA
2. **Hero Section** — Headline, subheading, 3D crystal visual, floating data pipeline UI cards, 2 CTA buttons
3. **Workflow Section** — "Realistic 3D Workflow" — 7-step horizontal 3D icon stepper
4. **Feature Previews** — 6 floating UI screenshots in corners (AI Career Agent, Intelligent Job Discovery, Application Automation × 2, Human-in-the-loop, Analytics)

### Components
- `TopNav` (public variant, no sidebar)
- `HeroSection`
- `HeroOrb3D` (3D crystal asset)
- `WorkflowStepper`
- `FeaturePreviewCard`
- `Button` (primary: "Start AI Job Search", secondary: "See How It Works")

### Data Required
- None (static marketing content)

### User Actions
- Click "Start AI Job Search" → `/register`
- Click "Log in" → `/login`
- Click "See How It Works" → anchor scroll to workflow section

### Responsive Behavior
- Mobile: hero stacks vertically, workflow stepper scrolls horizontally, feature previews hidden
- Tablet: 2-column feature previews, condensed hero

---

## Page 2 — Dashboard (AI Career Agent Overview)

**Source**: `03-job-details.png` (large view), `01-dashboard.png` (mosaic)  
**Route**: `/dashboard`

### Purpose
Primary landing page after login. Summarises agent status, top opportunities, application pipeline, and recent activity.

### Main Sections

#### Section A: AI Career Agent Hero Banner
- Title: "AI Career Agent" + subtitle "Actively searching and analyzing opportunities"
- 3D AI orb image (top-right)
- Agent status row: Agent Active (green dot) + Searching / Analyzing / Ranking / Preparing dots
- Source: `03-job-details.png`

#### Section B: Stat Cards Row
- Jobs Found: **127**
- Strong Matches: **18**
- Applications: **7**
- Interviews: **2**

#### Section C: Top Opportunities
- Featured job card (91% match, Java Backend Developer, TechNova)
- Skills matched / missing
- View Job + Prepare Application CTAs

#### Section D: AI Career Insights
- Panel with 3 AI insight cards (scrollable horizontal)
- Career recommendation text

#### Section E: Application Pipeline
- Horizontal stage flow: Discovered → Matched → Preparing → Review → Submitted → Interview → Offer
- Current stage highlighted

#### Section F: Recent Agent Activity
- Horizontal timeline with timestamps and actions

### Components
- `AppShell` (Sidebar + TopNav)
- `AICareerAgentBanner`
- `StatCardRow`
- `JobCard` (featured variant)
- `AIInsightPanel`
- `ApplicationPipeline`
- `AgentActivityTimeline`

### Data Required
- `GET /api/dashboard/summary` → stats, top job, pipeline counts
- `GET /api/jobs/top-matches?limit=3` → top job cards
- `GET /api/agent/activity?limit=10` → recent activity
- `GET /api/insights/career` → AI insight texts

### API Contracts
```typescript
// GET /api/dashboard/summary
interface DashboardSummary {
  jobsFound: number;
  strongMatches: number;
  applications: number;
  interviews: number;
  agentStatus: 'ACTIVE' | 'BUSY' | 'PAUSED' | 'INACTIVE';
  pipelineCounts: Record<PipelineStage, number>;
}
```

### Loading State
- Skeleton cards for stat row
- Shimmer placeholder for job card
- Skeleton bars for pipeline

### Error State
- Inline error message with retry button per section
- Agent status shows "Unknown" with reconnect prompt

### Empty State
- "No jobs discovered yet — your agent is warming up" with animation

---

## Page 3 — Job Detail & AI Assessment

**Source**: `04-application-agent.png`  
**Route**: `/jobs/:id`

### Purpose
Full detail view of a specific job with AI match assessment, skill comparison, AI reasoning, and application readiness.

### Main Sections

#### Section A: Job Header
- Job title + Company name
- Meta: Location · Work Mode · Experience · Salary
- Company logo (left sidebar icon)

#### Section B: AI Recruiter Assessment Panel
- Large circular match score (91% — "STRONG MATCH")
- 6 progress bars: Technical Skills, Experience, Education, Location, Projects, Role Fit
- Each bar: label + percent + progress visualization

#### Section C: Skill Comparison (Profile vs. Requirements)
- Two columns: "YOUR PROFILE" vs "JOB REQUIREMENTS"
- Matched skills: green checkmark chips
- Missing skills: amber dash chips + warning icon

#### Section D: Why You Match
- AI-generated explanation paragraph
- Heading: "Why you match"

#### Section E: Potential Concerns
- Bulleted AI-generated concerns
- Heading: "Potential concerns"

#### Section F: Application Readiness Checklist
- Resume: Ready (green)
- Candidate Profile: Complete (green)
- Cover Letter: Generated (green)
- Screening Questions: "2 need review" (amber)

#### Section G: 3D Illustration (decorative)
- Isometric tech workspace 3D image — top right of page

#### Section H: Action Buttons
- "Prepare Application" (primary, blue, with edit icon)
- "Save Job" (secondary, with bookmark icon)

### Components
- `AppShell`
- `JobHeader`
- `AIMatchScoreRing`
- `AIRecruiterAssessment` (progress bars grid)
- `SkillComparisonPanel`
- `AIReasoningCard` (Why you match + Concerns)
- `ApplicationReadinessChecklist`
- `JobDetail3DAsset`
- `Button`

### Data Required
- `GET /api/jobs/:id` → job details
- `GET /api/jobs/:id/assessment` → AI match score + breakdown
- `GET /api/jobs/:id/readiness` → application readiness

### API Contracts
```typescript
// GET /api/jobs/:id/assessment
interface JobAssessment {
  matchScore: number; // 0-100
  label: 'STRONG_MATCH' | 'GOOD_MATCH' | 'WEAK_MATCH';
  breakdown: {
    technicalSkills: number;
    experience: number;
    education: number;
    location: number;
    projects: number;
    roleFit: number;
  };
  profileSkills: Skill[];
  requiredSkills: Skill[];
  matchedSkills: Skill[];
  missingSkills: Skill[];
  whyYouMatch: string;
  potentialConcerns: string[];
}

interface ApplicationReadiness {
  resume: 'READY' | 'MISSING' | 'NEEDS_UPDATE';
  candidateProfile: 'COMPLETE' | 'INCOMPLETE';
  coverLetter: 'GENERATED' | 'PENDING' | 'MISSING';
  screeningQuestions: { total: number; needReview: number; };
}
```

---

## Page 4 — Application Agent (Form + AI Co-pilot)

**Source**: `05-agent-control-center.png`  
**Route**: `/agent/apply/:jobId`

### Purpose
Human-in-the-loop application preparation screen. AI fills the application form, user reviews and approves each field, especially screening questions.

### Main Sections

#### Section A: Page Header
- Title: "Application Agent"
- Subtitle: "Preparing your application for [Job Title] at [Company]"

#### Section B: Progress Stepper
- Horizontal: Job analyzed ✓ → Candidate matched ✓ → Resume selected ✓ → Cover letter generated ✓ → Screening questions (active) → Final review → Submit
- Completed steps: checkmark + solid line
- Active step: filled dot
- Pending steps: empty circle + dashed line

#### Section C: Application Form Preview (left column)
- Read-only form fields showing what AI has filled:
  - Name, Email, Phone × 2, Location, Experience, Education, Skills, Resume (linked PDF)
- User can click to edit any field

#### Section D: AI Agent Panel (right column)
- AI sphere animation
- "Preparing application" status chip
- **Agent Activity log**: Analyzed job description ✓, Matched 14 skills ✓, Selected relevant project ✓, Generated cover letter ✓, Detected 3 screening questions (in-progress)

#### Section E: Screening Question Cards
- Question text
- AI-suggested answer in green box ("Based on your verified profile: 2.5 years")
- Confidence: "Confidence: High" in green
- Accept / Edit buttons
- Unknown question: amber badge + "Answer" button

#### Section F: Footer
- "Application readiness 86%" progress indicator
- "Review Application" CTA (primary)

### Components
- `ApplicationAgentShell` (special layout — no standard sidebar, icon-only nav)
- `ApplicationProgressStepper`
- `ApplicationFormPreview`
- `AIAgentPanel`
- `AIAgentSphere`
- `AgentActivityLog`
- `ScreeningQuestionCard`
- `ApplicationReadinessFooter`

### Data Required
- `GET /api/applications/:id/form-data` → pre-filled form fields
- `GET /api/applications/:id/screening-questions` → questions + AI answers
- `POST /api/applications/:id/accept-answer` → accept AI answer
- `PUT /api/applications/:id/edit-answer` → override AI answer
- `POST /api/applications/:id/submit` → trigger Playwright worker

### API Contracts
```typescript
interface ApplicationFormData {
  name: string; email: string; phone: string[];
  location: string; experience: string;
  education: string; skills: string;
  resumeUrl: string; coverLetter: string;
}

interface ScreeningQuestion {
  id: string;
  question: string;
  aiAnswer: string | null;
  confidence: 'HIGH' | 'MEDIUM' | 'LOW' | 'UNKNOWN';
  status: 'ACCEPTED' | 'EDITED' | 'PENDING' | 'NEEDS_ANSWER';
  source: string; // "From your verified profile"
}

interface ApplicationReadiness {
  percentage: number;
  blockers: string[];
}
```

---

## Page 5 — AI Agent Control Center

**Source**: `06-design-system.png` (top section showing "AI Agent Control Center")  
**Route**: `/agent`

### Purpose
Global configuration and control panel for the AI agent. Users configure job discovery settings, matching thresholds, application automation rules, and human intervention triggers.

### Main Sections

#### Section A: Page Header
- Logo (top-left)
- Title: "AI Agent Control Center" (centered, large)
- Settings icon (top-right)

#### Section B: Center Visual
- 3D wireframe globe with "ACTIVE" label in glowing green
- This is the real-time agent status visualization

#### Section C: JOB DISCOVERY Panel (top-left)
- Toggle switches:
  - Search automatically: ON
  - Search multiple supported sources: ON
  - Remove duplicates: ON
  - Analyze new jobs: ON

#### Section D: APPLICATIONS Panel (top-right)
- Toggle: Prepare applications automatically: ON
- Dropdown: Final submission = "USER APPROVAL REQUIRED"
- Number input: Daily application limit = 5

#### Section E: MATCHING Panel (bottom-left)
- Slider: Minimum match score = 85%
- Checkbox: "No mandatory requirements missing"

#### Section F: HUMAN INTERVENTION Panel (bottom-center)
- Checklist of pause conditions:
  - CAPTCHA
  - MFA
  - Unknown questions
  - Legal declarations
  - Missing candidate information
  - Unsupported application flows

#### Section G: Agent Activity Log (bottom-right)
- Scrollable log: `[HH:MM:SS] Event description`
- Urgent entries highlighted in red

### Components
- `AgentControlShell` (special layout)
- `AgentGlobe3D`
- `AgentStatusBadge` ("ACTIVE")
- `SettingsPanel` (Job Discovery, Applications, Matching)
- `ToggleSwitch`
- `RangeSlider`
- `HumanInterventionPanel`
- `AgentActivityLog`

### Data Required
- `GET /api/agent/config` → all agent configuration
- `PUT /api/agent/config` → save configuration
- `GET /api/agent/logs?limit=20` → activity log entries
- WebSocket: `ws://api/agent/status` → real-time status updates

### API Contracts
```typescript
interface AgentConfig {
  jobDiscovery: {
    searchAutomatically: boolean;
    multipleSourceSearch: boolean;
    removeDuplicates: boolean;
    analyzeNewJobs: boolean;
  };
  applications: {
    prepareAutomatically: boolean;
    finalSubmission: 'AUTO' | 'USER_APPROVAL_REQUIRED';
    dailyApplicationLimit: number;
  };
  matching: {
    minimumMatchScore: number;
    requireAllMandatory: boolean;
  };
  humanIntervention: {
    pauseOnCaptcha: boolean;
    pauseOnMFA: boolean;
    pauseOnUnknownQuestions: boolean;
    pauseOnLegalDeclarations: boolean;
    pauseOnMissingInfo: boolean;
    pauseOnUnsupportedFlows: boolean;
  };
}
```

---

## Page 6 — Application Tracker

**Source**: `01-dashboard.png` (mosaic sub-screen labeled "Application page")  
**Route**: `/applications`

### Purpose
Tabular and card-based view of all job applications with status tracking, filtering, and quick actions.

### Main Sections
1. **Page Header** — "Applications" title + CTA buttons (e.g., "Automate Ops")
2. **Filter Bar** — Status filter tabs or dropdown
3. **Application Table / Cards** — List of applications with status badges
4. **Application Detail Drawer** — Side panel with job detail + form preview + status history

### Components
- `AppShell`
- `PageHeader`
- `ApplicationTable`
- `StatusBadge`
- `ApplicationFilterBar`
- `ApplicationDetailDrawer`
- `ApplicationTimeline`

### Data Required
- `GET /api/applications?status=&page=&limit=` → paginated application list
- `GET /api/applications/:id` → single application detail

### API Contracts
```typescript
interface Application {
  id: string;
  jobTitle: string;
  company: string;
  companyLogoUrl: string;
  location: string;
  salary: string;
  matchScore: number;
  status: ApplicationStatus;
  appliedAt: string;
  lastUpdatedAt: string;
  submittedVia: 'AI_AGENT' | 'MANUAL';
}

type ApplicationStatus =
  'DISCOVERED' | 'MATCHED' | 'PREPARING' |
  'PENDING_REVIEW' | 'SUBMITTED' | 'INTERVIEWING' |
  'OFFERED' | 'REJECTED' | 'WITHDRAWN';
```

---

## Page 7 — Candidate Profile

**Source**: `01-dashboard.png` (mosaic sub-screen "Candidate Profile")  
**Route**: `/profile`

### Purpose
Structured profile derived from resume. AI-extracted and user-verified candidate data used for job matching and application autofill.

### Main Sections
1. **Header** — "Candidate Profile" + tabs (Resume, Forecasts, Shortlists)
2. **Sub-tabs** — Skills, Experience, Education
3. **Skills Section** — Skills cloud with tags (Sorts, Experience, Education groupings)
4. **AI Extraction Indicators** — Shows what AI extracted vs user-confirmed
5. **Projects Section**
6. **Certifications Section**
7. **Preferred Roles Section**
8. **Preferred Locations Section**

### Components
- `AppShell`
- `ProfileHeader`
- `ProfileTabs`
- `SkillsCloud`
- `ExperienceList`
- `EducationList`
- `ProjectsList`
- `CertificationsList`
- `PreferencePanel`

---

## Page 8 — Resume

**Source**: `01-dashboard.png` (mosaic sub-screen "Resume")  
**Route**: `/resume`

### Purpose
Resume upload, AI extraction view, and resume management.

### Main Sections
1. **Header** — "Resume" + tabs (AI Extraction, Redstone)
2. **Skills Section** — AI-extracted name, contact, skills
3. **Experience Section** — Extracted work history
4. **Education Section**
5. **Projects Section**
6. **Resume Preview** — Rendered document preview (right panel)
7. **Upload / Actions** — Upload new resume, select AI provider

### Components
- `AppShell`
- `ResumeHeader`
- `ResumeExtractionPanel`
- `ResumePreview`
- `UploadDropzone`

---

## Page 9 — Analytics

**Source**: `01-dashboard.png` (mosaic sub-screen "Analytics")  
**Route**: `/analytics`

### Purpose
Performance analytics for the AI job search campaign.

### Main Sections
1. **Stat Row** — Jobs Discovered: 39, Match Rate: 68%, Applications: 753
2. **Job Application Activity Chart** — Line chart over time
3. **Response Rate / Interviews / Conversion Rate** stats
4. **Bar Charts** — Application volume breakdown

### Components
- `AppShell`
- `PageHeader`
- `AnalyticsStatRow`
- `LineChart`
- `BarChart`
- `DonutChart`

---

## Page 10 — Job Discovery / Search

**Source**: `02-landing-page.png` (feature preview screenshot "Intelligent Job Discovery")  
**Route**: `/jobs`

### Purpose
Job search and discovery interface with map view, filters, and job listings.

### Main Sections
1. **Search Bar** — Job title + location inputs
2. **Filter Panel** — Work mode, salary, experience, source
3. **Job Listings** — Card list with AI match scores
4. **Map View** (optional) — Geographic job clustering
5. **AI Recommendations Banner**

### Components
- `AppShell`
- `JobSearchBar`
- `JobFilterPanel`
- `JobList`
- `JobCard`
- `MapView` (optional phase)
- `JobMatchScore`

---

## Page 11 — Interventions

**Source**: `01-dashboard.png` (mosaic "Action required" dialog and activity cards)  
**Route**: `/interventions`

### Purpose
Human review queue. Lists all items where the AI agent has paused and requires user input (CAPTCHA responses, unknown screening questions, legal declarations, etc.).

### Main Sections
1. **Intervention Queue** — Cards of pending items requiring human action
2. **Intervention Dialog** — Modal with context, AI suggestion, and action buttons (Review / Continue / Skip)
3. **History** — Recently resolved interventions

### Components
- `AppShell`
- `InterventionQueue`
- `InterventionCard`
- `InterventionDialog`
- `ActionRequiredBanner`

### Data Required
- `GET /api/interventions?status=PENDING` → queue
- `POST /api/interventions/:id/resolve` → resolve with action

---

## Page 12 — Login / Register

**Route**: `/login`, `/register`

### Purpose
Authentication screens.

### Main Sections
- Logo + headline
- Email + Password inputs
- Social login (optional)
- CTA button
- Redirect links

### Notes
- Same dark theme as rest of app
- Could include glassmorphic card centered on dark gradient background
- No sidebar

---

## Page 13 — Settings

**Route**: `/settings`

### Purpose
User account settings, AI provider configuration, notification preferences.

### Sections
- Account
- AI Provider (OpenAI / Gemini / Anthropic)
- Notifications
- Security
- Billing

---

## Summary Route Map

```
/                          → Landing Page (public)
/login                     → Login
/register                  → Register
/dashboard                 → Dashboard (AI Career Agent Overview)
/jobs                      → Job Discovery
/jobs/:id                  → Job Detail + AI Assessment
/agent                     → AI Agent Control Center
/agent/apply/:jobId        → Application Agent (Form + AI Co-pilot)
/applications              → Application Tracker
/applications/:id          → Application Detail
/interventions             → Human Intervention Queue
/profile                   → Candidate Profile
/resume                    → Resume Manager
/analytics                 → Analytics
/settings                  → Settings
```
