# JobPilot AI — Component Architecture

> All components below are directly justified by the design references.
> No speculative components are included.

---

## Architecture Principles

1. **Atomic Design**: Atoms → Molecules → Organisms → Templates → Pages
2. **Feature Co-location**: Feature-specific components live inside their feature folder; shared components live in `components/`
3. **Composition over Configuration**: Prefer composing small focused components
4. **Design Token Compliance**: All components consume CSS custom properties from the design system

---

## Component Hierarchy

```
components/
├── ui/                    # Atoms — pure presentational, no business logic
│   ├── Button/
│   ├── Input/
│   ├── Select/
│   ├── Toggle/
│   ├── Slider/
│   ├── Checkbox/
│   ├── Badge/
│   ├── Avatar/
│   ├── Spinner/
│   ├── Tooltip/
│   ├── ProgressBar/
│   └── Divider/
│
├── feedback/              # Atoms — state displays
│   ├── EmptyState/
│   ├── LoadingState/
│   ├── ErrorState/
│   ├── Toast/
│   └── AlertBanner/
│
├── layout/                # Structural layout components
│   ├── AppShell/
│   ├── Sidebar/
│   ├── TopNav/
│   ├── PageHeader/
│   └── ContentArea/
│
├── navigation/            # Navigation molecules
│   ├── NavItem/
│   ├── NavGroup/
│   ├── Breadcrumb/
│   └── Tabs/
│
├── data-display/          # Data visualization molecules
│   ├── StatCard/
│   ├── DonutChart/
│   ├── LineChart/
│   ├── BarChart/
│   ├── ProgressRing/
│   └── Table/
│
├── overlays/              # Overlay organisms
│   ├── Modal/
│   ├── Drawer/
│   ├── ConfirmDialog/
│   └── DropdownMenu/
│
├── ai/                    # AI-specific visual components
│   ├── AIOrb/
│   ├── AIAgentStatus/
│   ├── AIInsightCard/
│   ├── AIActivityTimeline/
│   ├── MatchScoreRing/
│   ├── ScreeningQuestionCard/
│   └── AgentActivityLog/
│
└── forms/                 # Form molecules
    ├── SearchBar/
    ├── FilterPanel/
    └── FormField/
```

---

## Detailed Component Specs

### UI Atoms

---

#### `Button`
**Location**: `components/ui/Button/`

**Props**
```typescript
interface ButtonProps {
  variant:  'primary' | 'secondary' | 'tertiary' | 'danger' | 'success';
  size:     'sm' | 'md' | 'lg' | 'xl';
  leftIcon?:  React.ReactNode;
  rightIcon?: React.ReactNode;
  isLoading?: boolean;
  isDisabled?: boolean;
  fullWidth?: boolean;
  onClick?: () => void;
  type?: 'button' | 'submit' | 'reset';
  children: React.ReactNode;
}
```

**States**: Default, Hover, Active, Focus, Disabled, Loading  
**Design ref**: `06-design-system.png` Buttons section — Primary/Secondary/Tertiary × Default/Hover/Active/Disabled

---

#### `Input`
**Location**: `components/ui/Input/`

**Props**
```typescript
interface InputProps {
  variant:     'filled' | 'outlined';
  label?:      string;
  placeholder?: string;
  leftIcon?:   React.ReactNode;
  rightElement?: React.ReactNode;
  error?:      string;
  isDisabled?: boolean;
  register?:   UseFormRegisterReturn; // React Hook Form
}
```

**States**: Default, Hover, Focus, Error, Disabled  
**Design ref**: `06-design-system.png` Inputs section

---

#### `Toggle`
**Location**: `components/ui/Toggle/`

**Props**
```typescript
interface ToggleProps {
  checked:  boolean;
  onChange: (value: boolean) => void;
  label?:   string;
  size?:    'sm' | 'md';
}
```

**Design ref**: `06-design-system.png` (Agent Control Center toggles — ON/OFF)

---

#### `Badge`
**Location**: `components/ui/Badge/`

**Props**
```typescript
interface BadgeProps {
  variant: 'status' | 'skill' | 'match' | 'label';
  status?: ApplicationStatus | MatchLevel;
  children: React.ReactNode;
  icon?: React.ReactNode;
}
```

**Design ref**: `06-design-system.png` Skill Badges, Status Badges sections

---

#### `Slider`
**Location**: `components/ui/Slider/`

**Props**
```typescript
interface SliderProps {
  min:   number;
  max:   number;
  step:  number;
  value: number;
  onChange: (value: number) => void;
  label?: string;
  displayValue?: string;
}
```

**Design ref**: `06-design-system.png` matching score slider (85%)

---

#### `ProgressBar`
**Location**: `components/ui/ProgressBar/`

**Props**
```typescript
interface ProgressBarProps {
  value:     number; // 0-100
  max?:      number;
  label?:    string;
  showValue?: boolean;
  color?:    'brand' | 'success' | 'warning' | 'error';
  size?:     'sm' | 'md';
  animated?: boolean;
}
```

**Design ref**: `04-application-agent.png` AI Recruiter Assessment progress bars

---

### Feedback Components

---

#### `EmptyState`
**Location**: `components/feedback/EmptyState/`

**Props**
```typescript
interface EmptyStateProps {
  icon?:        React.ReactNode;
  title:        string;
  description?: string;
  action?:      { label: string; onClick: () => void };
  size?:        'sm' | 'md' | 'lg';
}
```

**Design ref**: `06-design-system.png` (tablet view, "Empty States" section)

---

#### `LoadingState`
**Location**: `components/feedback/LoadingState/`

**Variants**
- `spinner` — centered spinner with optional message
- `skeleton` — content-matching shimmer skeleton

**Design ref**: `06-design-system.png` (tablet view, "Loading States" section)

---

#### `ErrorState`
**Location**: `components/feedback/ErrorState/`

**Props**
```typescript
interface ErrorStateProps {
  title?:       string;
  description?: string;
  onRetry?:     () => void;
}
```

**Design ref**: `06-design-system.png` (mobile view, "Error States" section) — red warning triangle icon

---

### Layout Components

---

#### `AppShell`
**Location**: `components/layout/AppShell/`

**Description**: Root layout wrapper. Composes `Sidebar` + `TopNav` + `ContentArea`. Handles sidebar collapse state, mobile drawer.

```typescript
interface AppShellProps {
  children: React.ReactNode;
}
```

**Behavior**
- Desktop (1024px+): Sidebar 240px fixed + scrollable content
- Tablet (768–1023px): Sidebar 64px icon-only
- Mobile (<768px): Sidebar hidden, hamburger opens `Drawer`

---

#### `Sidebar`
**Location**: `components/layout/Sidebar/`

**Description**: Left navigation panel with logo, nav items, agent status, user profile.

**Props**
```typescript
interface SidebarProps {
  isCollapsed: boolean;
  onToggle:    () => void;
  activeRoute: string;
  agentStatus: AgentStatus;
  user:        UserProfile;
}
```

**Sub-components**: `NavItem`, `NavGroup`, `AgentStatusChip`

---

#### `TopNav`
**Location**: `components/layout/TopNav/`

**Description**: Top navigation bar with search, notifications, AI agent status, avatar.

**Props**
```typescript
interface TopNavProps {
  agentStatus:       AgentStatus;
  notificationCount: number;
  user:              UserProfile;
  onMenuToggle?:     () => void;
}
```

---

#### `PageHeader`
**Location**: `components/layout/PageHeader/`

**Props**
```typescript
interface PageHeaderProps {
  title:        string;
  subtitle?:    string;
  actions?:     React.ReactNode; // CTA buttons
  breadcrumb?:  BreadcrumbItem[];
}
```

---

### Data Display

---

#### `StatCard`
**Location**: `components/data-display/StatCard/`

**Props**
```typescript
interface StatCardProps {
  label:     string;
  value:     number | string;
  trend?:    { value: number; direction: 'up' | 'down' };
  icon?:     React.ReactNode;
  isLoading?: boolean;
}
```

**Behavior**: Animate value count-up on mount  
**Design ref**: `03-job-details.png` — Jobs Found: 127, Strong Matches: 18, etc.

---

#### `ProgressRing` / `DonutChart`
**Location**: `components/data-display/ProgressRing/`

**Props**
```typescript
interface ProgressRingProps {
  value:      number; // 0-100
  size?:      number; // px diameter, default 80
  strokeWidth?: number;
  label?:     string; // "STRONG MATCH"
  colorMode?: 'match' | 'brand' | 'custom';
  animated?:  boolean;
}
```

**Technique**: SVG `stroke-dashoffset` animation  
**Design ref**: `04-application-agent.png` — 91% match score ring, `03-job-details.png` stat cards

---

#### `Table`
**Location**: `components/data-display/Table/`

**Props**
```typescript
interface TableProps<T> {
  columns:    ColumnDef<T>[];
  data:       T[];
  isLoading?: boolean;
  onRowClick?: (row: T) => void;
  selectable?: boolean;
  pagination?: PaginationConfig;
}
```

**Design ref**: `06-design-system.png` Tables section

---

#### `LineChart`
**Location**: `components/data-display/LineChart/`

**Description**: Recharts-based line chart with area fill. Used in Analytics page.

---

#### `BarChart`
**Location**: `components/data-display/BarChart/`

**Description**: Recharts-based bar chart with rounded tops. Used in Analytics.

---

### Overlay Components

---

#### `Modal`
**Location**: `components/overlays/Modal/`

**Props**
```typescript
interface ModalProps {
  isOpen:    boolean;
  onClose:   () => void;
  title?:    string;
  size?:     'sm' | 'md' | 'lg' | 'xl' | 'fullscreen';
  children:  React.ReactNode;
  footer?:   React.ReactNode;
}
```

**Behavior**: Backdrop blur on open, ESC to close, mobile = bottom sheet  
**Design ref**: `06-design-system.png` Modals & Confirmation Dialogs

---

#### `Drawer`
**Location**: `components/overlays/Drawer/`

**Props**
```typescript
interface DrawerProps {
  isOpen:    boolean;
  onClose:   () => void;
  side?:     'left' | 'right';
  size?:     'sm' | 'md' | 'lg';
  title?:    string;
  children:  React.ReactNode;
}
```

**Design ref**: `06-design-system.png` (mobile sidebar = drawer)

---

#### `ConfirmDialog`
**Location**: `components/overlays/ConfirmDialog/`

**Design ref**: `06-design-system.png` — "Action Required" / "Approval Forum" dialogs, `01-dashboard.png` action required cards

---

### AI Components

---

#### `AIOrb`
**Location**: `components/ai/AIOrb/`

**Description**: Animated AI sphere with glowing orbital rings and particles. The signature visual of the product.

**Props**
```typescript
interface AIOrbProps {
  size?:   'sm' | 'md' | 'lg' | 'xl';
  status?: 'active' | 'busy' | 'idle';
  label?:  string; // "AI AGENT ACTIVE"
}
```

**Technique**: CSS animation (rings) + radial-gradient sphere + particle SVG overlay  
**Design ref**: `03-job-details.png` dashboard banner, `05-agent-control-center.png` agent panel

---

#### `AIAgentStatus`
**Location**: `components/ai/AIAgentStatus/`

**Description**: Status indicator with pulsing ring. Used in TopNav pill and sidebar footer.

**Props**
```typescript
interface AIAgentStatusProps {
  status:  'ACTIVE' | 'BUSY' | 'PAUSED' | 'INACTIVE' | 'ERROR';
  variant: 'pill' | 'badge' | 'icon';
  label?:  string;
}
```

---

#### `AIInsightCard`
**Location**: `components/ai/AIInsightCard/`

**Props**
```typescript
interface AIInsightCardProps {
  type:     'insight' | 'recommendation' | 'notification';
  icon?:    React.ReactNode;
  title?:   string;
  content:  string;
  actions?: { label: string; onClick: () => void }[];
}
```

**Design ref**: `03-job-details.png` AI Career Insights panel, `06-design-system.png` AI Insight Cards

---

#### `AIActivityTimeline`
**Location**: `components/ai/AIActivityTimeline/`

**Props**
```typescript
interface AIActivityTimelineProps {
  entries: ActivityEntry[];
  maxVisible?: number;
  orientation?: 'horizontal' | 'vertical';
}

interface ActivityEntry {
  timestamp: string;
  message:   string;
  type:      'info' | 'success' | 'warning' | 'error';
  isActive?: boolean;
}
```

**Design ref**: `03-job-details.png` Recent Agent Activity (horizontal), `01-dashboard.png` activity timeline (vertical)

---

#### `MatchScoreRing`
**Location**: `components/ai/MatchScoreRing/`

**Description**: Circular SVG donut showing match percentage. Animates on mount.

**Props**
```typescript
interface MatchScoreRingProps {
  score:     number; // 0-100
  size?:     number; // diameter in px
  showLabel?: boolean;
  showBadge?: boolean; // "STRONG MATCH" badge below
}
```

**Color logic**:
- 85–100%: `--color-match-strong` (green)
- 70–84%: `--color-match-good` (blue)
- <70%: `--color-match-weak` (amber)

**Design ref**: `04-application-agent.png` (91% ring), `03-job-details.png` job card match rings

---

#### `ScreeningQuestionCard`
**Location**: `components/ai/ScreeningQuestionCard/`

**Props**
```typescript
interface ScreeningQuestionCardProps {
  question:   string;
  aiAnswer?:  string;
  confidence: 'HIGH' | 'MEDIUM' | 'LOW' | 'UNKNOWN';
  source?:    string;
  status:     'ACCEPTED' | 'EDITED' | 'PENDING' | 'NEEDS_ANSWER';
  onAccept:   () => void;
  onEdit:     () => void;
  onAnswer?:  () => void;
}
```

**Design ref**: `05-agent-control-center.png` screening question cards

---

#### `AgentActivityLog`
**Location**: `components/ai/AgentActivityLog/`

**Props**
```typescript
interface AgentActivityLogProps {
  entries:    LogEntry[];
  maxHeight?: number;
  isLive?:   boolean; // auto-scroll to bottom
}

interface LogEntry {
  timestamp: string;
  message:   string;
  level:     'info' | 'warning' | 'error';
}
```

**Design ref**: `06-design-system.png` Agent activity log panel (bottom-right)

---

### Feature-Specific Organisms

---

#### `JobCard`
**Location**: `features/jobs/components/JobCard/`

**Props**
```typescript
interface JobCardProps {
  job:         Job;
  matchScore?: number;
  variant?:    'compact' | 'standard' | 'featured';
  onView?:     () => void;
  onPrepare?:  () => void;
  onBookmark?: () => void;
}
```

**Design ref**: `03-job-details.png` Top Opportunities section, `01-dashboard.png` job card views

---

#### `ApplicationPipeline`
**Location**: `features/dashboard/components/ApplicationPipeline/`

**Props**
```typescript
interface ApplicationPipelineProps {
  stages:       PipelineStage[];
  currentStage: PipelineStage;
  counts?:      Record<PipelineStage, number>;
}

type PipelineStage =
  'DISCOVERED' | 'MATCHED' | 'PREPARING' |
  'REVIEW' | 'SUBMITTED' | 'INTERVIEW' | 'OFFER';
```

**Design ref**: `03-job-details.png` Application Pipeline section, `01-dashboard.png` pipeline component

---

#### `ApplicationProgressStepper`
**Location**: `features/agent/components/ApplicationProgressStepper/`

**Props**
```typescript
interface ApplicationProgressStepperProps {
  steps:        ApplicationStep[];
  currentStep:  number;
}

interface ApplicationStep {
  label:    string;
  status:   'completed' | 'active' | 'pending';
}
```

**Design ref**: `05-agent-control-center.png` top stepper (Job analyzed → Submit)

---

#### `SkillComparisonPanel`
**Location**: `features/jobs/components/SkillComparisonPanel/`

**Props**
```typescript
interface SkillComparisonPanelProps {
  profileSkills:  Skill[];
  requiredSkills: Skill[];
  matchedSkills:  Skill[];
  missingSkills:  Skill[];
}
```

**Design ref**: `04-application-agent.png` YOUR PROFILE vs JOB REQUIREMENTS skill comparison

---

#### `AICareerAgentBanner`
**Location**: `features/dashboard/components/AICareerAgentBanner/`

**Description**: Full-width hero banner with AI orb, title, status dots, and agent status indicators.

**Design ref**: `03-job-details.png` top banner

---

#### `ApplicationFormPreview`
**Location**: `features/agent/components/ApplicationFormPreview/`

**Description**: Read-only form preview showing AI-filled fields. Each field is editable on click.

**Design ref**: `05-agent-control-center.png` left column (Application Form Preview)

---

#### `JobFilterPanel`
**Location**: `features/jobs/components/JobFilterPanel/`

**Props**
```typescript
interface JobFilterPanelProps {
  filters:    JobFilters;
  onChange:   (filters: JobFilters) => void;
  onReset:    () => void;
}

interface JobFilters {
  workMode?:    ('REMOTE' | 'HYBRID' | 'ONSITE')[];
  salaryRange?: [number, number];
  experience?:  string[];
  location?:    string[];
  sources?:     string[];
  minMatch?:    number;
}
```

---

#### `InterventionCard`
**Location**: `features/interventions/components/InterventionCard/`

**Props**
```typescript
interface InterventionCardProps {
  intervention: Intervention;
  onReview:     () => void;
  onContinue:   () => void;
  onSkip:       () => void;
}
```

**Design ref**: `01-dashboard.png` "Action required" card

---

## SearchBar

**Location**: `components/forms/SearchBar/`

**Props**
```typescript
interface SearchBarProps {
  placeholder?: string;
  value:        string;
  onChange:     (value: string) => void;
  onSearch:     (value: string) => void;
  onFilter?:    () => void;
  filterLabel?: string;
  isLoading?:   boolean;
}
```

**Design ref**: `03-job-details.png` top search bar, `06-design-system.png` Search Bar element

---

## Tabs

**Location**: `components/navigation/Tabs/`

**Props**
```typescript
interface TabsProps {
  tabs:       { label: string; value: string; badge?: number }[];
  activeTab:  string;
  onChange:   (value: string) => void;
  variant?:   'line' | 'pill';
}
```

**Design ref**: `01-dashboard.png` (Candidate Profile tabs: Resume, Forecasts, Shortlists)

---

## Component Count Summary

| Category       | Count |
|----------------|-------|
| UI Atoms       | 12    |
| Feedback       | 5     |
| Layout         | 5     |
| Navigation     | 4     |
| Data Display   | 6     |
| Overlays       | 4     |
| AI Components  | 7     |
| Feature Organisms | 9  |
| Forms          | 3     |
| **Total**      | **55**|
