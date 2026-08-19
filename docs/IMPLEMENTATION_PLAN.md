# JobPilot AI — Implementation Plan

> This document defines the exact frontend build order.
> Each phase produces a demonstrable, testable output.
> Do NOT skip phases or implement pages out of order.

---

## Pre-Conditions

Before implementation begins, confirm:
- [ ] Vite + React + TypeScript project scaffolded
- [ ] Tailwind CSS configured
- [ ] React Router configured
- [ ] TanStack Query configured
- [ ] Axios client created
- [ ] ESLint + Prettier configured

---

## Phase 1 — Project Scaffolding & Design Tokens

**Goal**: Functioning Vite project with the full design system wired in.

### Tasks

#### 1.1 Project Setup
- [ ] Create Vite + React + TypeScript project in `frontend/`
- [ ] Install all dependencies:
  ```
  react react-dom react-router-dom
  @tanstack/react-query axios
  react-hook-form @hookform/resolvers zod
  zustand
  recharts
  lucide-react
  clsx tailwind-merge
  ```
- [ ] Configure `tailwind.config.ts` with all design tokens (colors, fonts, radii, shadows, animations)
- [ ] Configure `tsconfig.json` with path aliases (`@/` → `src/`)
- [ ] Configure `vite.config.ts` with path aliases

#### 1.2 Design Token CSS
- [ ] Create `src/assets/styles/globals.css`
  - All CSS custom properties from `DESIGN_SYSTEM.md`
  - Background scale, text scale, semantic colors, AI colors, borders
- [ ] Create `src/assets/styles/animations.css`
  - All `@keyframes`: orbit, ai-pulse, status-pulse, fade-in, slide-in
- [ ] Load both files in `src/main.tsx`

#### 1.3 Inter Font
- [ ] Add Google Fonts import for Inter (weights: 400, 500, 600, 700)
- [ ] Set `font-family: Inter` on `body` in globals.css

#### 1.4 Base Reset
- [ ] Apply dark background to `html, body`: `background: var(--color-bg-root)`
- [ ] Set default text color: `color: var(--color-text-primary)`
- [ ] Remove default margins/padding

#### 1.5 Utility Setup
- [ ] Create `src/utils/cn.ts` — clsx + tailwind-merge helper
- [ ] Create `src/utils/matchScore.ts` — score to color/label mapping
- [ ] Create `src/utils/format.ts` — date, currency, number formatters
- [ ] Create `src/routes/routes.ts` — route constants

**Deliverable**: `npm run dev` shows dark page with correct background color and Inter font.

---

## Phase 2 — API Layer & State Foundation

**Goal**: HTTP client + global state ready for data binding.

### Tasks

#### 2.1 Axios Client
- [ ] Create `src/api/client.ts`
  - Base URL from `VITE_API_BASE_URL` env var
  - JWT request interceptor
  - 401 response interceptor (redirect to `/login`)
  - Default timeout: 15s
- [ ] Create `src/api/endpoints.ts` — all API URL constants

#### 2.2 TanStack Query
- [ ] Create `src/services/queryClient.ts`
  - `staleTime: 5 * 60 * 1000` (5 min default)
  - `retry: 1`
  - Global `onError` handler
- [ ] Wrap app in `QueryClientProvider` in `src/app/providers.tsx`

#### 2.3 Zustand Store
- [ ] Create `src/app/store.ts`
  - `sidebarCollapsed: boolean`
  - `agentStatus: AgentStatus`
  - `user: UserProfile | null`
  - `accessToken: string | null`

#### 2.4 WebSocket Service
- [ ] Create `src/services/websocketService.ts`
  - Connect to `VITE_WS_URL/agent/status`
  - Parse agent status events
  - Update Zustand store on message
- [ ] Create `src/hooks/useWebSocket.ts`

#### 2.5 Global Types
- [ ] Create all files in `src/types/`:
  - `job.types.ts` — Job, JobAssessment, Skill, etc.
  - `application.types.ts` — Application, ApplicationStatus, PipelineStage, etc.
  - `agent.types.ts` — AgentConfig, AgentStatus, LogEntry, etc.
  - `user.types.ts` — UserProfile, etc.
  - `api.types.ts` — ApiResponse, PaginatedResponse, etc.

**Deliverable**: TypeScript compiles cleanly. API client instantiated.

---

## Phase 3 — UI Atom Components

**Goal**: Full library of primitive UI components ready for composition.

### Tasks (build in this order)

#### 3.1 Core Atoms
- [ ] `Button` — all 5 variants, 4 sizes, all states (loading spinner, disabled)
- [ ] `Input` — filled + outlined variants, all states (focus ring, error state)
- [ ] `Select` — dropdown component, custom styled (no native)
- [ ] `Toggle` — iOS-style ON/OFF switch (from Agent Control Center)
- [ ] `Slider` — range slider with custom thumb (for match score config)
- [ ] `Checkbox` — custom styled with brand color
- [ ] `Avatar` — circular image with fallback initials
- [ ] `Spinner` — animated loading ring
- [ ] `Tooltip` — hover tooltip using Floating UI or manual positioning
- [ ] `ProgressBar` — horizontal bar, animated fill, multiple color modes
- [ ] `Divider` — horizontal rule using border-subtle

#### 3.2 Badge Variants
- [ ] `Badge` — skill, status, match, label variants
- [ ] All status colors wired (MATCHED, PREPARING, SUBMITTED, etc.)
- [ ] Strong Match badge with lightning bolt icon

#### 3.3 Feedback Components
- [ ] `EmptyState` — icon + title + description + action button
- [ ] `LoadingState` — spinner variant + skeleton variant
- [ ] `ErrorState` — red triangle icon + message + retry button
- [ ] `Toast` — slide-in notification (success/warning/error/info)
- [ ] `AlertBanner` — inline banner variant

**Deliverable**: All atoms render correctly in isolation. Can be demonstrated with a simple test page.

---

## Phase 4 — Layout Shell

**Goal**: Complete application shell with sidebar, top nav, and content area.

### Tasks

#### 4.1 Sidebar
- [ ] Logo area (240px wide, 64px tall)
- [ ] Nav items with icons + labels (all 10 routes)
- [ ] Collapsible to 64px icon-only rail
- [ ] Active state: blue bg + left 3px accent
- [ ] Sub-item support (Applications sub-items)
- [ ] Agent status chip at bottom
- [ ] User avatar + name area at bottom
- [ ] Smooth collapse/expand animation

#### 4.2 TopNav
- [ ] Logo area aligned to sidebar width
- [ ] Center: `SearchBar` (48px, with filter button)
- [ ] Right: Notification bell with count badge
- [ ] Right: AI Agent Status pill (green dot + label, pulsing)
- [ ] Right: Avatar with dropdown

#### 4.3 AppShell
- [ ] Compose Sidebar + TopNav + ContentArea
- [ ] Sidebar collapse state via Zustand
- [ ] Mobile: sidebar becomes `Drawer` via hamburger
- [ ] Correct z-index layering

#### 4.4 Layout Variants
- [ ] `AppLayout` — standard authenticated layout
- [ ] `PublicLayout` — no sidebar, full viewport
- [ ] `AgentLayout` — icon-only narrow left nav
- [ ] `ControlCenterLayout` — full screen, custom grid

#### 4.5 Router
- [ ] Configure all routes in `src/routes/index.tsx`
- [ ] `ProtectedRoute` wrapping all authenticated routes
- [ ] Lazy loading with `React.lazy` + `Suspense`
- [ ] 404 page

**Deliverable**: App shell renders. Sidebar collapses. Navigation links work. Shows empty content area per route.

---

## Phase 5 — AI Visual Components

**Goal**: AI-specific components that define the product's visual identity.

### Tasks

#### 5.1 MatchScoreRing
- [ ] SVG circular donut chart
- [ ] Animated `stroke-dashoffset` on mount (1200ms)
- [ ] Three color modes based on score threshold
- [ ] Center text: percentage + label
- [ ] Glow shadow via CSS

#### 5.2 AIOrb
- [ ] Layered CSS `radial-gradient` sphere (blue core → violet → cyan rim)
- [ ] Orbital ring SVG (1–2 rings, CSS rotation animation 8s loop)
- [ ] Particle dots around orbit (CSS animation)
- [ ] Three status states: active (bright), busy (amber), idle (dim)
- [ ] Optional "AI AGENT ACTIVE" label below

#### 5.3 AIAgentStatus
- [ ] Pulsing ring variant (for TopNav pill)
- [ ] Badge variant
- [ ] Icon-only variant
- [ ] Three states: ACTIVE, BUSY, INACTIVE

#### 5.4 AIInsightCard
- [ ] Dark card + 3px left accent bar
- [ ] Sparkle/bulb icon
- [ ] "AI Insight" cyan header label
- [ ] Body text slot

#### 5.5 AIActivityTimeline
- [ ] Horizontal variant (Recent Agent Activity in Dashboard)
- [ ] Vertical variant (left panel in Application Agent)
- [ ] Animated entry on new items (slide-in from left)
- [ ] Active/pending/completed states per entry

#### 5.6 AgentActivityLog
- [ ] Scrollable log panel
- [ ] Monospaced timestamp + message
- [ ] Color coding: info (default), warning (amber), error (red)
- [ ] Auto-scroll to bottom when `isLive`

#### 5.7 ScreeningQuestionCard
- [ ] Question text section
- [ ] AI answer in green-tinted box
- [ ] Confidence level display
- [ ] Accept / Edit buttons
- [ ] "Unknown" state with amber badge + Answer button

**Deliverable**: All AI components visually match the design reference. No functionality needed yet.

---

## Phase 6 — Landing Page

**Source**: `02-landing-page.png`  
**Route**: `/`

### Tasks

#### 6.1 Public TopNav
- [ ] Logo + nav links + Login button + "Start AI Job" CTA button

#### 6.2 Hero Section
- [ ] Full-viewport dark hero with blue/purple gradient overlay
- [ ] Headline: "Your AI Agent for the Entire Job Search" (48px bold)
- [ ] Subheadline (16px, muted)
- [ ] Two CTAs: "Start AI Job Search" (primary) + "See How It Works" (secondary outline)
- [ ] 3D crystal image asset centered (pre-rendered WebP)
- [ ] Floating data pipeline UI cards around crystal (positioned absolutely)

#### 6.3 Workflow Stepper (3D icons)
- [ ] Section heading: "Realistic 3D Workflow"
- [ ] 7-step horizontal stepper with 3D icon assets + labels:
  - Resume → AI Candidate Profile → Job Discovery → Recruiter Matching → Application Preparation → AI Application Agent → Application Tracking
- [ ] Arrow connectors between steps
- [ ] Subtle curved line connecting all

#### 6.4 Feature Preview Cards
- [ ] 6 screenshot preview cards (floating at corners and sides)
  - AI Career Agent (top-left)
  - Intelligent Job Discovery (bottom-left)
  - Application Automation × 2 (right sides)
  - Human-in-the-loop (right)
  - Analytics (bottom-right)
- [ ] Cards have dark glass styling, subtle perspective tilt

**Deliverable**: Landing page renders and matches `02-landing-page.png`.

---

## Phase 7 — Dashboard

**Source**: `03-job-details.png`, `01-dashboard.png`  
**Route**: `/dashboard`

### Tasks

#### 7.1 AI Career Agent Banner
- [ ] Full-width card
- [ ] Title + subtitle on left
- [ ] AI orb image on right (pre-rendered asset)
- [ ] Agent status row: green dot + "Agent Active" + Searching / Analyzing / Ranking / Preparing

#### 7.2 Stat Cards Row
- [ ] 4 stat cards in a row: Jobs Found, Strong Matches, Applications, Interviews
- [ ] Count-up animation on values
- [ ] Responsive: 2×2 on mobile

#### 7.3 Top Opportunities + AI Career Insights
- [ ] Two-column layout
- [ ] Left: Featured job card (91% match) with skills + actions
- [ ] Right: AI Career Insights panel (3 scrollable insight cards)

#### 7.4 Application Pipeline
- [ ] Horizontal stage row: Discovered → Matched → Preparing → Review → Submitted → Interview → Offer
- [ ] Current stage has highlighted styling + gradient bg
- [ ] Arrow connectors between stages

#### 7.5 Recent Agent Activity
- [ ] Horizontal timeline with 4 visible entries
- [ ] Timestamps above, descriptions below
- [ ] Active line shows progress

#### 7.6 Data Binding
- [ ] `useDashboard` hook with TanStack Query
- [ ] Loading skeletons for each section
- [ ] Error states per section

**Deliverable**: Dashboard page renders with mock/live data. Matches design reference.

---

## Phase 8 — Job Discovery

**Route**: `/jobs`

### Tasks

#### 8.1 Search + Filter Bar
- [ ] Full-width search bar at top
- [ ] Filter panel (collapsible left panel or top row)
  - Work mode toggles (Remote / Hybrid / Onsite)
  - Salary range slider
  - Min match score slider
  - Location input

#### 8.2 Job List
- [ ] Paginated list of `JobCard` components
- [ ] Standard variant (not featured) by default
- [ ] Skeleton loading for initial load

#### 8.3 JobCard (standard variant)
- [ ] Company logo + name + title
- [ ] Location · Mode · Salary meta
- [ ] Skills (matched blue, missing amber)
- [ ] Match score ring (compact, left)
- [ ] View Job + Prepare Application buttons
- [ ] Bookmark icon

#### 8.4 Data Binding
- [ ] `useJobs` hook with filters → TanStack Query
- [ ] Debounced search input
- [ ] URL param sync for filters

**Deliverable**: Job list renders with data. Filtering works.

---

## Phase 9 — Job Detail & AI Assessment

**Source**: `04-application-agent.png`  
**Route**: `/jobs/:id`

### Tasks

#### 9.1 Job Header
- [ ] Company logo (left sidebar icon nav, icon-only layout)
- [ ] Title + company + meta (location, mode, experience, salary)

#### 9.2 AI Recruiter Assessment Panel
- [ ] Large match score ring (91%, center, animated)
- [ ] 6 progress bars grid (2 columns)
- [ ] Each bar: label + percentage + animated fill

#### 9.3 Skill Comparison Panel
- [ ] Two-column: YOUR PROFILE vs JOB REQUIREMENTS
- [ ] Matched skills: green check chips
- [ ] Missing skills: amber dash chips + warning icon

#### 9.4 AI Reasoning Section
- [ ] "Why you match" paragraph
- [ ] "Potential concerns" bullet list

#### 9.5 Application Readiness Checklist
- [ ] Checklist rows: Resume, Candidate Profile, Cover Letter, Screening Questions
- [ ] Status icons: green check, amber warning, pending circle

#### 9.6 3D Illustration
- [ ] Pre-rendered isometric image in top-right quadrant
- [ ] Responsive: hidden on mobile

#### 9.7 Sticky Footer / Actions
- [ ] "Prepare Application" (primary) + "Save Job" (secondary)
- [ ] Sticky at bottom on scroll

**Deliverable**: Job detail page fully matches `04-application-agent.png`.

---

## Phase 10 — Application Agent

**Source**: `05-agent-control-center.png` (Application Agent page)  
**Route**: `/agent/apply/:jobId`

### Tasks

#### 10.1 AgentLayout
- [ ] Icon-only narrow left nav (no labels)
- [ ] Full content area

#### 10.2 Page Header
- [ ] Title: "Application Agent"
- [ ] Subtitle: "Preparing your application for [Job] at [Company]"

#### 10.3 Progress Stepper
- [ ] 7-step horizontal stepper with status icons
- [ ] Solid line (done) → dashed line (pending)
- [ ] Animated progress indicator

#### 10.4 Application Form Preview (left column)
- [ ] Readonly field display for each form field
- [ ] Name, Email, Phone, Location, Experience, Education, Skills, Resume
- [ ] Click-to-edit any field
- [ ] Linked resume file display

#### 10.5 AI Agent Panel (right column)
- [ ] AI sphere animation
- [ ] "Preparing application" status chip
- [ ] Agent Activity list with checkmarks

#### 10.6 Screening Question Cards
- [ ] Stack of `ScreeningQuestionCard` components
- [ ] Accept / Edit / Answer interactions

#### 10.7 Footer
- [ ] Application readiness percentage bar
- [ ] "Review Application" CTA

**Deliverable**: Application Agent page matches `05-agent-control-center.png` (left page).

---

## Phase 11 — AI Agent Control Center

**Source**: `06-design-system.png` (top-level "AI Agent Control Center" page)  
**Route**: `/agent`

### Tasks

#### 11.1 ControlCenterLayout
- [ ] Full-viewport dark background
- [ ] Logo top-left, settings icon top-right
- [ ] No standard sidebar

#### 11.2 Page Title
- [ ] "AI Agent Control Center" — large, centered, bold

#### 11.3 Agent Globe (center)
- [ ] Three.js wireframe globe OR pre-rendered animated asset
- [ ] "ACTIVE" label with green glow
- [ ] Platform base below globe

#### 11.4 Job Discovery Panel (top-left card)
- [ ] 4 toggle switches (Search automatically, Multiple sources, Remove duplicates, Analyze new jobs)
- [ ] Each switch: label + ON/OFF toggle

#### 11.5 Applications Panel (top-right card)
- [ ] Toggle: Prepare automatically
- [ ] Select dropdown: Final submission mode
- [ ] Number input: Daily application limit

#### 11.6 Matching Panel (bottom-left card)
- [ ] Slider: Minimum match score
- [ ] Checkbox: No mandatory requirements missing

#### 11.7 Human Intervention Panel (bottom-center)
- [ ] Checklist of pause conditions
- [ ] All always-on checkmarks

#### 11.8 Agent Activity Log (bottom-right)
- [ ] Scrollable real-time log
- [ ] Live WebSocket data

#### 11.9 Config Save
- [ ] `PUT /api/agent/config` on any change (debounced)
- [ ] Toast on save success/error

**Deliverable**: Control Center page matches `06-design-system.png` top section.

---

## Phase 12 — Application Tracker

**Source**: `01-dashboard.png` (mosaic "Application page")  
**Route**: `/applications`

### Tasks

#### 12.1 Page Header + CTAs
- [ ] "Applications" title + "Automate Ops" button

#### 12.2 Filter Bar
- [ ] Status filter tabs (All, Preparing, Submitted, Interviewing, Offered, Rejected)
- [ ] OR dropdown filter

#### 12.3 Application Table
- [ ] Columns: Company, Role, Match%, Status, Applied Date, Actions
- [ ] Status badges with correct colors
- [ ] Row hover state
- [ ] Checkbox selection

#### 12.4 Application Detail Drawer
- [ ] Slide-in from right on row click
- [ ] Job detail summary at top
- [ ] Application form preview
- [ ] Status history timeline

**Deliverable**: Applications page shows list and detail drawer.

---

## Phase 13 — Supporting Pages

**Routes**: `/profile`, `/resume`, `/interventions`, `/analytics`

### Tasks (in parallel)

#### 13.1 Candidate Profile (`/profile`)
- [ ] Profile tabs (Resume, Forecasts, Shortlists)
- [ ] Skills, Experience, Education sections
- [ ] Projects, Certifications, Preferred Roles

#### 13.2 Resume Manager (`/resume`)
- [ ] Upload dropzone (drag & drop)
- [ ] AI extraction panel
- [ ] Resume document preview

#### 13.3 Interventions (`/interventions`)
- [ ] Intervention queue with pending cards
- [ ] Action Required card design (from `01-dashboard.png`)
- [ ] Review / Continue / Skip actions
- [ ] `ConfirmDialog` for Continue

#### 13.4 Analytics (`/analytics`)
- [ ] Stat row: Jobs Discovered, Match Rate, Applications
- [ ] Line chart: job application activity over time
- [ ] Response Rate + Interviews + Conversion metrics
- [ ] Bar chart: application volume

---

## Phase 14 — Authentication Pages

**Routes**: `/login`, `/register`

### Tasks
- [ ] `LoginPage` — email + password + submit
- [ ] `RegisterPage` — name + email + password + confirm password
- [ ] Form validation with React Hook Form + Zod
- [ ] Link to register from login (and vice versa)
- [ ] Glassmorphic card centered on gradient dark background

---

## Phase 15 — Responsive Optimization

**Goal**: Every page works correctly at 390px, 1024px, 1440px.

### Tasks

#### 15.1 Mobile (390px)
- [ ] Sidebar → full-screen drawer via hamburger
- [ ] Job cards → full-width stacked
- [ ] Stat cards → 2×2 grid
- [ ] Pipeline → horizontal scroll
- [ ] Tables → horizontal scroll
- [ ] Modals → bottom sheet
- [ ] Agent panels → stacked below content

#### 15.2 Tablet (1024px)
- [ ] Sidebar → 64px icon-only rail
- [ ] Job cards → 2-column grid
- [ ] Stat cards → row of 4
- [ ] Charts → full display

#### 15.3 Typography Scaling
- [ ] Apply responsive font-size scaling per design system breakpoint table

#### 15.4 Performance
- [ ] Check all images have `loading="lazy"` and `srcset` where applicable
- [ ] Verify code splitting is working (check bundle sizes)
- [ ] Lighthouse audit target: Performance ≥ 85

---

## Phase 16 — Polish & Quality

**Goal**: The app is production-ready and visually excellent.

### Tasks

#### 16.1 Animations
- [ ] Verify all animations from `DESIGN_SYSTEM.md §17` are implemented
- [ ] Route transition fade
- [ ] Stat count-up on dashboard load
- [ ] Match score ring animate on job detail load
- [ ] Progress bars animate on assessment load
- [ ] Agent status pulse loop

#### 16.2 Empty States
- [ ] Every data-fetching component has an EmptyState
- [ ] Job list empty: "No jobs found matching your criteria"
- [ ] Application tracker empty: "No applications yet — let your AI agent get to work"
- [ ] Dashboard empty: "Your AI agent is warming up..."
- [ ] Interventions empty: "No pending actions — agent is running smoothly"

#### 16.3 Error Handling
- [ ] Every data-fetching component has an ErrorState with retry
- [ ] Global error toast for API failures
- [ ] 404 page with navigation back to dashboard

#### 16.4 Loading States
- [ ] Skeleton loaders for all data sections
- [ ] Match score ring shows placeholder ring while loading
- [ ] Job cards show skeleton variant
- [ ] Stat cards show shimmer placeholder

#### 16.5 Accessibility
- [ ] All interactive elements have aria labels
- [ ] Keyboard navigation works for sidebar and modals
- [ ] Focus rings visible
- [ ] Color contrast meets WCAG AA

---

## Summary Timeline

| Phase | Description                          | Estimated Effort |
|-------|--------------------------------------|-----------------|
| 1     | Scaffolding + Design Tokens          | 0.5 day         |
| 2     | API Layer + State Foundation         | 0.5 day         |
| 3     | UI Atom Components                   | 1 day           |
| 4     | Layout Shell + Router                | 1 day           |
| 5     | AI Visual Components                 | 1.5 days        |
| 6     | Landing Page                         | 1 day           |
| 7     | Dashboard                            | 1.5 days        |
| 8     | Job Discovery                        | 1 day           |
| 9     | Job Detail + AI Assessment           | 1.5 days        |
| 10    | Application Agent                    | 1.5 days        |
| 11    | AI Agent Control Center              | 1.5 days        |
| 12    | Application Tracker                  | 1 day           |
| 13    | Supporting Pages                     | 1.5 days        |
| 14    | Authentication                       | 0.5 day         |
| 15    | Responsive Optimization              | 1 day           |
| 16    | Polish + Quality                     | 1 day           |
| **Total** |                                 | **~17 days**    |

---

## Critical Path

```
Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5
                                           ↓
                         Phase 6 (Landing) ← can be done in parallel after Phase 4
                                           ↓
                                       Phase 7 (Dashboard)
                                           ↓
                                   Phase 8 + Phase 9 (Jobs)
                                           ↓
                                  Phase 10 + Phase 11 (Agent)
                                           ↓
                              Phase 12 + Phase 13 (Tracker + Supporting)
                                           ↓
                                Phase 14 (Auth) → Phase 15 → Phase 16
```

---

## Definition of Done (per phase)

A phase is complete when:
1. All tasks are checked off
2. The page/component matches the corresponding design reference image
3. TypeScript compiles with zero errors
4. No console errors in browser
5. Component renders correctly at all 3 breakpoints
