# JobPilot AI — Frontend Architecture

> Stack: React + Vite + TypeScript + Tailwind CSS + React Router + TanStack Query + Axios + React Hook Form + Zod

---

## Directory Structure

```
frontend/
├── public/
│   ├── favicon.ico
│   ├── robots.txt
│   └── assets/
│       ├── images/
│       │   ├── logo.svg
│       │   ├── ai-orb-hero.webp        # Landing page 3D crystal
│       │   ├── ai-orb-dashboard.webp   # Dashboard AI sphere
│       │   ├── job-detail-3d.webp      # Job detail isometric illustration
│       │   └── ai-agent-sphere.webp    # Application agent sphere
│       └── fonts/
│           └── inter/                  # Self-hosted Inter font files
│
├── src/
│   ├── main.tsx                        # Entry point
│   ├── App.tsx                         # Root component + router
│   ├── vite-env.d.ts
│   │
│   ├── app/                            # App-level configuration
│   │   ├── providers.tsx               # All context providers (Query, Auth, Theme)
│   │   ├── router.tsx                  # Route definitions
│   │   └── store.ts                    # Zustand global store (agent status, user)
│   │
│   ├── components/                     # Shared, reusable components
│   │   ├── ui/                         # Atoms
│   │   │   ├── Button/
│   │   │   │   ├── Button.tsx
│   │   │   │   ├── Button.types.ts
│   │   │   │   └── index.ts
│   │   │   ├── Input/
│   │   │   ├── Select/
│   │   │   ├── Toggle/
│   │   │   ├── Slider/
│   │   │   ├── Checkbox/
│   │   │   ├── Badge/
│   │   │   ├── Avatar/
│   │   │   ├── Spinner/
│   │   │   ├── Tooltip/
│   │   │   ├── ProgressBar/
│   │   │   └── Divider/
│   │   │
│   │   ├── feedback/                   # State display components
│   │   │   ├── EmptyState/
│   │   │   ├── LoadingState/
│   │   │   ├── ErrorState/
│   │   │   ├── Toast/
│   │   │   └── AlertBanner/
│   │   │
│   │   ├── layout/                     # Structural components
│   │   │   ├── AppShell/
│   │   │   ├── Sidebar/
│   │   │   ├── TopNav/
│   │   │   ├── PageHeader/
│   │   │   └── ContentArea/
│   │   │
│   │   ├── navigation/                 # Navigation molecules
│   │   │   ├── NavItem/
│   │   │   ├── NavGroup/
│   │   │   ├── Breadcrumb/
│   │   │   └── Tabs/
│   │   │
│   │   ├── data-display/               # Charts, tables, stats
│   │   │   ├── StatCard/
│   │   │   ├── DonutChart/
│   │   │   ├── LineChart/
│   │   │   ├── BarChart/
│   │   │   ├── ProgressRing/
│   │   │   └── Table/
│   │   │
│   │   ├── overlays/                   # Modals, drawers, dialogs
│   │   │   ├── Modal/
│   │   │   ├── Drawer/
│   │   │   ├── ConfirmDialog/
│   │   │   └── DropdownMenu/
│   │   │
│   │   ├── ai/                         # AI visual components
│   │   │   ├── AIOrb/
│   │   │   ├── AIAgentStatus/
│   │   │   ├── AIInsightCard/
│   │   │   ├── AIActivityTimeline/
│   │   │   ├── MatchScoreRing/
│   │   │   ├── ScreeningQuestionCard/
│   │   │   └── AgentActivityLog/
│   │   │
│   │   └── forms/                      # Form molecules
│   │       ├── SearchBar/
│   │       ├── FilterPanel/
│   │       └── FormField/
│   │
│   ├── features/                       # Feature modules (vertical slices)
│   │   ├── landing/
│   │   │   ├── components/
│   │   │   │   ├── HeroSection/
│   │   │   │   ├── HeroOrb3D/
│   │   │   │   ├── WorkflowStepper/
│   │   │   │   └── FeaturePreviewCard/
│   │   │   └── index.ts
│   │   │
│   │   ├── auth/
│   │   │   ├── components/
│   │   │   │   ├── LoginForm/
│   │   │   │   └── RegisterForm/
│   │   │   ├── hooks/
│   │   │   │   └── useAuth.ts
│   │   │   ├── api/
│   │   │   │   └── authApi.ts
│   │   │   └── types.ts
│   │   │
│   │   ├── dashboard/
│   │   │   ├── components/
│   │   │   │   ├── AICareerAgentBanner/
│   │   │   │   ├── StatCardRow/
│   │   │   │   ├── TopOpportunities/
│   │   │   │   ├── AICareerInsights/
│   │   │   │   ├── ApplicationPipeline/
│   │   │   │   └── RecentAgentActivity/
│   │   │   ├── hooks/
│   │   │   │   └── useDashboard.ts
│   │   │   ├── api/
│   │   │   │   └── dashboardApi.ts
│   │   │   └── types.ts
│   │   │
│   │   ├── jobs/
│   │   │   ├── components/
│   │   │   │   ├── JobCard/
│   │   │   │   ├── JobList/
│   │   │   │   ├── JobFilterPanel/
│   │   │   │   ├── JobHeader/
│   │   │   │   ├── AIMatchScoreRing/
│   │   │   │   ├── AIRecruiterAssessment/
│   │   │   │   ├── SkillComparisonPanel/
│   │   │   │   ├── AIReasoningCard/
│   │   │   │   ├── ApplicationReadinessChecklist/
│   │   │   │   └── JobDetail3DAsset/
│   │   │   ├── hooks/
│   │   │   │   ├── useJobs.ts
│   │   │   │   └── useJobDetail.ts
│   │   │   ├── api/
│   │   │   │   └── jobsApi.ts
│   │   │   └── types.ts
│   │   │
│   │   ├── agent/
│   │   │   ├── components/
│   │   │   │   ├── ApplicationProgressStepper/
│   │   │   │   ├── ApplicationFormPreview/
│   │   │   │   ├── AIAgentPanel/
│   │   │   │   ├── AIAgentSphere/
│   │   │   │   ├── AgentActivityList/
│   │   │   │   ├── ScreeningQuestionsPanel/
│   │   │   │   ├── ApplicationReadinessFooter/
│   │   │   │   ├── AgentGlobe3D/
│   │   │   │   ├── JobDiscoveryPanel/
│   │   │   │   ├── ApplicationsConfigPanel/
│   │   │   │   ├── MatchingConfigPanel/
│   │   │   │   └── HumanInterventionPanel/
│   │   │   ├── hooks/
│   │   │   │   ├── useAgentConfig.ts
│   │   │   │   ├── useAgentStatus.ts
│   │   │   │   └── useApplicationAgent.ts
│   │   │   ├── api/
│   │   │   │   └── agentApi.ts
│   │   │   └── types.ts
│   │   │
│   │   ├── applications/
│   │   │   ├── components/
│   │   │   │   ├── ApplicationTable/
│   │   │   │   ├── ApplicationFilterBar/
│   │   │   │   ├── ApplicationDetailDrawer/
│   │   │   │   └── ApplicationTimeline/
│   │   │   ├── hooks/
│   │   │   │   └── useApplications.ts
│   │   │   ├── api/
│   │   │   │   └── applicationsApi.ts
│   │   │   └── types.ts
│   │   │
│   │   ├── interventions/
│   │   │   ├── components/
│   │   │   │   ├── InterventionCard/
│   │   │   │   ├── InterventionQueue/
│   │   │   │   └── InterventionDialog/
│   │   │   ├── hooks/
│   │   │   │   └── useInterventions.ts
│   │   │   ├── api/
│   │   │   │   └── interventionsApi.ts
│   │   │   └── types.ts
│   │   │
│   │   ├── profile/
│   │   │   ├── components/
│   │   │   │   ├── ProfileHeader/
│   │   │   │   ├── SkillsSection/
│   │   │   │   ├── ExperienceSection/
│   │   │   │   ├── EducationSection/
│   │   │   │   ├── ProjectsSection/
│   │   │   │   └── PreferencesSection/
│   │   │   ├── hooks/
│   │   │   │   └── useProfile.ts
│   │   │   ├── api/
│   │   │   │   └── profileApi.ts
│   │   │   └── types.ts
│   │   │
│   │   ├── resume/
│   │   │   ├── components/
│   │   │   │   ├── ResumeUploadZone/
│   │   │   │   ├── ResumeExtractionPanel/
│   │   │   │   └── ResumePreview/
│   │   │   ├── hooks/
│   │   │   │   └── useResume.ts
│   │   │   ├── api/
│   │   │   │   └── resumeApi.ts
│   │   │   └── types.ts
│   │   │
│   │   └── analytics/
│   │       ├── components/
│   │       │   ├── AnalyticsStatRow/
│   │       │   ├── JobActivityChart/
│   │       │   └── ConversionMetrics/
│   │       ├── hooks/
│   │       │   └── useAnalytics.ts
│   │       ├── api/
│   │       │   └── analyticsApi.ts
│   │       └── types.ts
│   │
│   ├── layouts/                        # Page-level layout templates
│   │   ├── AppLayout.tsx               # Standard: Sidebar + TopNav + Content
│   │   ├── PublicLayout.tsx            # Landing page layout (no sidebar)
│   │   ├── AgentLayout.tsx             # Application agent (icon-only nav)
│   │   └── ControlCenterLayout.tsx     # Full-screen agent control center
│   │
│   ├── pages/                          # Route-level page components (thin)
│   │   ├── LandingPage.tsx
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   ├── DashboardPage.tsx
│   │   ├── JobsPage.tsx
│   │   ├── JobDetailPage.tsx
│   │   ├── ApplicationAgentPage.tsx
│   │   ├── AgentControlCenterPage.tsx
│   │   ├── ApplicationsPage.tsx
│   │   ├── ApplicationDetailPage.tsx
│   │   ├── InterventionsPage.tsx
│   │   ├── ProfilePage.tsx
│   │   ├── ResumePage.tsx
│   │   ├── AnalyticsPage.tsx
│   │   └── SettingsPage.tsx
│   │
│   ├── routes/                         # Routing configuration
│   │   ├── index.tsx                   # Root router with lazy loading
│   │   ├── ProtectedRoute.tsx          # Auth guard HOC
│   │   └── routes.ts                   # Route constant definitions
│   │
│   ├── hooks/                          # Global shared hooks
│   │   ├── useBreakpoint.ts            # Responsive breakpoint detection
│   │   ├── useLocalStorage.ts
│   │   ├── useDebounce.ts
│   │   ├── useWebSocket.ts             # Agent real-time connection
│   │   └── useCountUp.ts              # Stat card count-up animation
│   │
│   ├── services/                       # Cross-cutting service singletons
│   │   ├── queryClient.ts              # TanStack Query client config
│   │   ├── websocketService.ts         # WebSocket manager
│   │   └── toastService.ts             # Toast notification singleton
│   │
│   ├── api/                            # HTTP layer
│   │   ├── client.ts                   # Axios instance with interceptors
│   │   ├── endpoints.ts                # API URL constants
│   │   └── types.ts                    # Shared API response types
│   │
│   ├── types/                          # Global TypeScript types
│   │   ├── index.ts                    # Re-exports
│   │   ├── agent.types.ts
│   │   ├── application.types.ts
│   │   ├── job.types.ts
│   │   ├── user.types.ts
│   │   └── api.types.ts
│   │
│   ├── utils/                          # Pure utility functions
│   │   ├── format.ts                   # Date, currency, number formatting
│   │   ├── matchScore.ts               # Score → color/label mapping
│   │   ├── cn.ts                       # clsx + twMerge utility
│   │   └── validators.ts               # Zod schema re-exports
│   │
│   └── assets/                         # Static assets imported in JS
│       ├── icons/                      # SVG icons as React components
│       └── styles/
│           ├── globals.css             # CSS custom properties (design tokens)
│           └── animations.css          # Keyframe definitions
│
├── index.html
├── package.json
├── vite.config.ts
├── tailwind.config.ts
├── tsconfig.json
├── tsconfig.app.json
└── .env.example
```

---

## Directory Responsibilities

### `src/app/`
- **`providers.tsx`**: Wraps the app with `QueryClientProvider`, `BrowserRouter`, auth context, and toast provider.
- **`router.tsx`**: Defines all routes using React Router v6 with lazy-loaded page components.
- **`store.ts`**: Zustand store for global UI state: sidebar collapse, active agent status, user session.

### `src/components/`
Shared, domain-agnostic components. These have **no business logic** and no direct API calls. They accept props and render UI. Divided by atomic design level.

### `src/features/`
Feature modules following vertical slice architecture. Each feature owns its:
- **UI components** (feature-specific organisms)
- **Custom hooks** (data fetching, local state)
- **API functions** (TanStack Query query/mutation functions)
- **TypeScript types** (feature-specific)

This avoids cross-feature coupling and enables independent development.

### `src/layouts/`
Page-level layout templates. Compose shared `AppShell`, `Sidebar`, `TopNav` etc. into a complete page frame. Pages import from layouts, not directly from component primitives.

**Layout variants:**
- `AppLayout` — Standard authenticated app (sidebar + topnav + content)
- `PublicLayout` — Landing/auth pages (full viewport, no sidebar)
- `AgentLayout` — Application Agent page (icon-only narrow nav, content fills viewport)
- `ControlCenterLayout` — Agent Control Center (full viewport, different grid)

### `src/pages/`
Thin page components. Each page:
1. Imports its feature components
2. Uses feature hooks for data
3. Composes the page layout
4. Contains minimal logic

Pages are **lazy-loaded** via `React.lazy()` for code splitting.

### `src/routes/`
- **`index.tsx`**: Root router with `Suspense` boundaries and `ProtectedRoute` wrappers.
- **`ProtectedRoute.tsx`**: Redirects unauthenticated users to `/login`.
- **`routes.ts`**: Typed route constants (`ROUTES.DASHBOARD`, `ROUTES.JOBS`, etc.) to avoid string literals.

### `src/hooks/`
Global hooks used across multiple features:
- **`useBreakpoint`**: Returns current breakpoint (`mobile | tablet | desktop`). Used by layout components.
- **`useWebSocket`**: Manages WebSocket connection to the agent. Provides real-time agent status updates.
- **`useCountUp`**: Animates numeric values counting up from 0 to target. Used by `StatCard`.
- **`useDebounce`**: Debounces search input values.

### `src/services/`
- **`queryClient.ts`**: Configures TanStack Query defaults (staleTime, retry, error handling).
- **`websocketService.ts`**: Singleton managing the WebSocket connection and event subscription pattern.
- **`toastService.ts`**: Programmatic toast notification API wrapping the toast library.

### `src/api/`
- **`client.ts`**: Axios instance. Sets base URL from env vars, attaches JWT `Authorization` header via interceptor, handles 401 → redirect to login, handles refresh token logic.
- **`endpoints.ts`**: Centralized API endpoint URL map.

### `src/types/`
Global TypeScript interface/type definitions shared across features. Feature-specific types live inside their feature folder.

### `src/utils/`
- **`cn.ts`**: `clsx` + `tailwind-merge` utility for conditional class names.
- **`matchScore.ts`**: Maps a numeric score to color token, label ("STRONG MATCH"), and CSS class.
- **`format.ts`**: Formatters for dates, currency (INR/USD), and large numbers.

### `src/assets/styles/`
- **`globals.css`**: All CSS custom properties from the design system. Loaded globally.
- **`animations.css`**: All `@keyframes` definitions for AI orb, pulse, transitions.

---

## State Management Strategy

| State Type                | Solution               | Location                  |
|---------------------------|------------------------|---------------------------|
| Server state              | TanStack Query         | Feature `hooks/`          |
| Global UI state           | Zustand                | `app/store.ts`            |
| Form state                | React Hook Form + Zod  | Feature components        |
| WebSocket / real-time     | Custom hook + Zustand  | `hooks/useWebSocket.ts`   |
| Navigation / auth state   | Zustand                | `app/store.ts`            |
| Local component state     | `useState`             | Component                 |

---

## Code Splitting Strategy

All pages are lazy-loaded:
```typescript
const DashboardPage = lazy(() => import('../pages/DashboardPage'));
const JobDetailPage = lazy(() => import('../pages/JobDetailPage'));
// etc.
```

Heavy dependencies:
- **Three.js** (Globe): Dynamic import only on `AgentControlCenterPage`
- **Recharts**: Lazy-loaded in analytics feature
- **PDF viewer**: Dynamic import only on `ResumePage`

---

## API Layer Architecture

```
Component
  → Feature Hook (useJobs, useDashboard)
    → TanStack Query (useQuery / useMutation)
      → Feature API function (jobsApi.getJobs)
        → Axios client (client.ts)
          → Spring Boot REST API
```

### Axios Client Configuration
```typescript
// src/api/client.ts
const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor: attach JWT
client.interceptors.request.use((config) => {
  const token = useStore.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Response interceptor: handle 401
client.interceptors.response.use(
  (res) => res,
  async (err) => {
    if (err.response?.status === 401) {
      // Attempt token refresh or redirect to login
    }
    return Promise.reject(err);
  }
);
```

---

## Tailwind Configuration

```typescript
// tailwind.config.ts
export default {
  content: ['./src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          primary:   '#2563EB',
          secondary: '#7C3AED',
          active:    '#06B6D4',
        },
        bg: {
          root:     '#0A0F1E',
          sidebar:  '#0D1526',
          surface:  '#111827',
          card:     '#1A2235',
          elevated: '#243047',
          input:    '#1E293B',
        },
        // ... all design tokens mapped
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        'xs': '4px',
        'sm': '6px',
        // ...
      },
      boxShadow: {
        'card':     '0 1px 3px rgba(0,0,0,0.4), 0 4px 16px rgba(0,0,0,0.3)',
        'ai-blue':  '0 0 24px rgba(37,99,235,0.35), 0 0 48px rgba(37,99,235,0.15)',
        // ...
      },
      animation: {
        'ai-pulse':     'ai-pulse 2s ease-in-out infinite',
        'orbit':        'orbit 8s linear infinite',
        'status-pulse': 'status-pulse 2s ease-in-out infinite',
        'count-up':     'none', // handled in JS
      },
      keyframes: {
        'ai-pulse': {
          '0%, 100%': { opacity: '1' },
          '50%':       { opacity: '0.75' },
        },
        'orbit': {
          'from': { transform: 'rotateY(0deg) rotateX(15deg)' },
          'to':   { transform: 'rotateY(360deg) rotateX(15deg)' },
        },
        'status-pulse': {
          '0%, 100%': { transform: 'scale(1)', opacity: '1' },
          '50%':       { transform: 'scale(1.4)', opacity: '0.5' },
        },
      },
    },
  },
  plugins: [],
};
```

---

## Environment Variables

```
# .env.example
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws
VITE_APP_NAME=JobPilot AI
```
