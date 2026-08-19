# JobPilot AI — Design System

> **Source of Truth**: Extracted directly from `design-reference/` images 01–07.
> Do NOT deviate from these tokens without explicit designer approval.

---

## 1. Brand

| Property       | Value                                                              |
|----------------|--------------------------------------------------------------------|
| Product Name   | **JobPilot AI**                                                    |
| Tagline        | "Your AI Agent for the Entire Job Search"                          |
| Logo           | Geometric angular icon (stylised "J" / rocket motif) + wordmark   |
| Logo Treatment | White icon + white "JobPilot" + electric-blue "AI" suffix          |
| Tone           | Premium AI SaaS — confident, precise, futuristic but approachable  |

---

## 2. Color Tokens

### 2.1 Primitive Palette (extracted from `06-design-system.png`)

```
--color-brand-primary:   #2563EB   /* Electric blue — primary CTA, active states */
--color-brand-secondary: #7C3AED   /* Deep violet — secondary accents, gradients  */
--color-brand-active:    #06B6D4   /* Cyan/teal — active indicators, highlights   */
--color-brand-silver:    #94A3B8   /* Silver — tertiary text, dividers            */
--color-brand-tertiary:  #64748B   /* Tertiary/inactive items                     */
```

### 2.2 Background Scale

```
--color-bg-root:         #0A0F1E   /* Deepest dark — page root / behind sidebar  */
--color-bg-sidebar:      #0D1526   /* Sidebar background                          */
--color-bg-surface:      #111827   /* Main content surface                        */
--color-bg-card:         #1A2235   /* Cards, panels                               */
--color-bg-card-hover:   #1E2A42   /* Card hover state                            */
--color-bg-elevated:     #243047   /* Elevated inputs, modals                     */
--color-bg-input:        #1E293B   /* Form fields                                 */
--color-bg-overlay:      rgba(10,15,30,0.85) /* Modal backdrop                   */
```

### 2.3 Text Scale

```
--color-text-primary:    #F1F5F9   /* Primary text (near-white)                   */
--color-text-secondary:  #94A3B8   /* Secondary / meta text                       */
--color-text-muted:      #64748B   /* Muted, placeholder, disabled                */
--color-text-inverse:    #0A0F1E   /* Text on light/colored backgrounds            */
```

### 2.4 Semantic Colors

```
--color-success:         #10B981   /* Green — matched, ready, confirmed           */
--color-success-bg:      rgba(16,185,129,0.12)
--color-success-border:  rgba(16,185,129,0.25)

--color-warning:         #F59E0B   /* Amber — needs review, unknown, action req.  */
--color-warning-bg:      rgba(245,158,11,0.12)
--color-warning-border:  rgba(245,158,11,0.25)

--color-error:           #EF4444   /* Red — missing skills, errors, action req.   */
--color-error-bg:        rgba(239,68,68,0.12)
--color-error-border:    rgba(239,68,68,0.25)

--color-info:            #06B6D4   /* Cyan — informational, processing            */
--color-info-bg:         rgba(6,182,212,0.12)
```

### 2.5 AI-Specific Colors

```
--color-ai-glow-blue:    #3B82F6   /* AI orb primary glow                         */
--color-ai-glow-purple:  #8B5CF6   /* AI orb secondary glow                       */
--color-ai-glow-cyan:    #22D3EE   /* AI orb tertiary glow                        */
--color-ai-active:       #10B981   /* "Agent Active" green pulse                  */
--color-match-strong:    #10B981   /* 85%+ match — green                          */
--color-match-good:      #3B82F6   /* 70-84% match — blue                         */
--color-match-weak:      #F59E0B   /* Less than 70% match — amber                 */
```

### 2.6 Border Colors

```
--color-border-subtle:   rgba(148,163,184,0.08)  /* Hairline dividers            */
--color-border-default:  rgba(148,163,184,0.15)  /* Standard card borders        */
--color-border-strong:   rgba(148,163,184,0.30)  /* Input focus rings            */
--color-border-brand:    rgba(37,99,235,0.50)    /* Brand-colored borders        */
--color-border-glass:    rgba(255,255,255,0.06)  /* Glassmorphic card borders    */
```

---

## 3. Typography

### 3.1 Font Family

```
--font-primary: 'Inter', system-ui, -apple-system, sans-serif;
```
> **Note**: Design system image explicitly shows "Inter" (Geometric typeface) at all weights.

### 3.2 Type Scale

| Token               | Size    | Weight | Line Height | Usage                          |
|---------------------|---------|--------|-------------|--------------------------------|
| `--text-display`    | 48px    | 700    | 1.1         | Landing hero heading           |
| `--text-h1`         | 32px    | 700    | 1.2         | Page titles                    |
| `--text-h2`         | 24px    | 600    | 1.3         | Section headers                |
| `--text-h3`         | 18px    | 600    | 1.4         | Card titles, modal headers     |
| `--text-h4`         | 16px    | 600    | 1.4         | Sub-section labels             |
| `--text-body-lg`    | 16px    | 400    | 1.6         | Body content                   |
| `--text-body`       | 14px    | 400    | 1.6         | Standard UI text               |
| `--text-body-sm`    | 13px    | 400    | 1.5         | Metadata, descriptions         |
| `--text-label`      | 12px    | 500    | 1.4         | Form labels, column headers    |
| `--text-caption`    | 11px    | 400    | 1.4         | Timestamps, secondary meta     |
| `--text-badge`      | 10px    | 600    | 1.0         | Badges, status chips           |

### 3.3 Font Weights

```
--font-normal:   400
--font-medium:   500
--font-semibold: 600
--font-bold:     700
```

---

## 4. Spacing

8px base grid system. All values are multiples of 4 or 8.

```
--space-1:   4px
--space-2:   8px
--space-3:   12px
--space-4:   16px
--space-5:   20px
--space-6:   24px
--space-8:   32px
--space-10:  40px
--space-12:  48px
--space-16:  64px
--space-20:  80px
--space-24:  96px
```

### Semantic Spacing
```
--spacing-card-padding:      24px
--spacing-card-padding-sm:   16px
--spacing-section-gap:       32px
--spacing-sidebar-width:     240px
--spacing-sidebar-collapsed: 64px
--spacing-topnav-height:     64px
```

---

## 5. Border Radius

```
--radius-xs:   4px    /* Tags, tiny badges */
--radius-sm:   6px    /* Inputs, small cards */
--radius-md:   8px    /* Standard cards, buttons */
--radius-lg:   12px   /* Large cards, panels */
--radius-xl:   16px   /* Hero cards, major panels */
--radius-2xl:  24px   /* Floating cards, modals */
--radius-full: 9999px /* Pills, toggles, avatar */
```

---

## 6. Shadows

```css
/* Card elevation — standard */
--shadow-card: 0 1px 3px rgba(0,0,0,0.4), 0 4px 16px rgba(0,0,0,0.3);

/* Card elevation — raised (hover) */
--shadow-card-hover: 0 4px 24px rgba(0,0,0,0.5), 0 1px 2px rgba(0,0,0,0.3);

/* AI glow — blue */
--shadow-ai-blue: 0 0 24px rgba(37,99,235,0.35), 0 0 48px rgba(37,99,235,0.15);

/* AI glow — purple */
--shadow-ai-purple: 0 0 24px rgba(139,92,246,0.35), 0 0 48px rgba(139,92,246,0.15);

/* AI orb glow — combined */
--shadow-ai-orb: 0 0 40px rgba(59,130,246,0.5), 0 0 80px rgba(139,92,246,0.3),
                 0 0 120px rgba(34,211,238,0.15);

/* Match score ring glow */
--shadow-match-ring: 0 0 16px rgba(16,185,129,0.5);

/* Input focus glow */
--shadow-input-focus: 0 0 0 2px rgba(37,99,235,0.4);

/* Modal shadow */
--shadow-modal: 0 24px 64px rgba(0,0,0,0.6);
```

---

## 7. Gradients

```css
/* Primary brand gradient — buttons, highlights */
--gradient-brand: linear-gradient(135deg, #2563EB 0%, #7C3AED 100%);

/* AI orb gradient */
--gradient-ai-orb: radial-gradient(ellipse at center,
  rgba(59,130,246,0.8) 0%,
  rgba(139,92,246,0.6) 40%,
  rgba(22,211,238,0.3) 70%,
  transparent 100%);

/* Page background gradient */
--gradient-page-bg:
  radial-gradient(ellipse at 20% 20%, rgba(37,99,235,0.08) 0%, transparent 60%),
  radial-gradient(ellipse at 80% 80%, rgba(124,58,237,0.06) 0%, transparent 60%);

/* Card glass gradient */
--gradient-glass: linear-gradient(135deg,
  rgba(255,255,255,0.06) 0%,
  rgba(255,255,255,0.02) 100%);

/* Sidebar gradient */
--gradient-sidebar: linear-gradient(180deg, #0D1526 0%, #0A0F1E 100%);

/* Pipeline stage gradient */
--gradient-pipeline: linear-gradient(90deg, #2563EB, #7C3AED);

/* Landing hero background */
--gradient-hero:
  radial-gradient(ellipse at 50% 0%, rgba(37,99,235,0.15) 0%,
  rgba(124,58,237,0.10) 40%, transparent 70%);
```

---

## 8. Buttons

### 8.1 Variants

| Variant     | Background                | Text       | Border               | Usage                      |
|-------------|---------------------------|------------|----------------------|----------------------------|
| `primary`   | `--gradient-brand`        | White      | None                 | Primary CTA                |
| `secondary` | `--color-bg-elevated`     | White      | `--color-border-default` | Secondary action       |
| `tertiary`  | Transparent               | `--color-text-secondary` | `--color-border-subtle` | Ghost/link button |
| `danger`    | `--color-error-bg`        | `#EF4444`  | `--color-error-border` | Destructive actions      |
| `success`   | `--color-success-bg`      | `#10B981`  | `--color-success-border` | Confirmation           |

### 8.2 Sizes

| Size  | Height | Padding  | Font size | Radius     |
|-------|--------|----------|-----------|------------|
| `sm`  | 32px   | 0 12px   | 12px      | `--radius-md` |
| `md`  | 40px   | 0 16px   | 14px      | `--radius-md` |
| `lg`  | 48px   | 0 24px   | 16px      | `--radius-lg` |
| `xl`  | 56px   | 0 32px   | 16px      | `--radius-lg` |

### 8.3 States
- **Default**: Base styles above
- **Hover**: `brightness(1.1)`, subtle box-shadow added
- **Active**: `scale(0.98)`, `brightness(0.95)`
- **Focus**: 2px ring via `--shadow-input-focus`
- **Disabled**: `opacity: 0.4`, `cursor: not-allowed`
- **Loading**: Spinner icon replaces left icon, text unchanged

---

## 9. Inputs

### 9.1 Variants

| Variant    | Background            | Border                  | Text           |
|------------|-----------------------|-------------------------|----------------|
| `filled`   | `--color-bg-input`    | `--color-border-default` | `--color-text-primary` |
| `outlined`  | transparent           | `--color-border-default` | `--color-text-primary` |

### 9.2 States

| State   | Border Color              | Shadow               |
|---------|---------------------------|----------------------|
| Default | `--color-border-default`  | None                 |
| Hover   | `--color-border-strong`   | None                 |
| Focus   | `--color-brand-primary`   | `--shadow-input-focus` |
| Error   | `--color-error`           | `0 0 0 2px rgba(239,68,68,0.3)` |
| Disabled| `--color-border-subtle`   | None, opacity 0.5    |

### 9.3 Metrics
- Height: **40px** (standard), **48px** (large/search bar)
- Padding: `12px 16px`
- Font size: **14px**
- Border radius: **`--radius-sm`** (6px)
- Label: **12px**, 500 weight, `--color-text-secondary`, 8px gap above input

### 9.4 Search Bar
- Height: **48px**
- Background: `--color-bg-elevated`
- Left icon: search icon (`--color-text-muted`)
- Right: filter toggle button with "Filtering" label
- Placeholder: `"Search for jobs..."`
- Full-width in top nav

---

## 10. Cards

### 10.1 Standard Card
```css
background:      var(--color-bg-card);
border:          1px solid var(--color-border-glass);
border-radius:   var(--radius-lg); /* 12px */
padding:         var(--spacing-card-padding); /* 24px */
box-shadow:      var(--shadow-card);
backdrop-filter: blur(12px);
```

### 10.2 Glassmorphic Card
```css
background:      linear-gradient(135deg, rgba(255,255,255,0.06), rgba(255,255,255,0.02));
border:          1px solid rgba(255,255,255,0.08);
border-radius:   var(--radius-xl); /* 16px */
backdrop-filter: blur(16px);
box-shadow:      var(--shadow-card-hover);
```

### 10.3 Job Card Anatomy
- Company logo: 48px × 48px, `--radius-lg`
- Job title: `--text-h3` (18px, semibold)
- Company: `--text-body` (14px, `--color-text-secondary`)
- Meta row: Location · Work Mode · Salary — icon + text, muted color
- Skills row: pill badges (matched = blue, missing = amber/red)
- Match score: circular donut (top-right or left)
- Bookmark icon: top-right corner
- Actions: "View Job" (secondary) + "Prepare Application" (primary)
- Hover: `translateY(-2px)` + shadow expand

### 10.4 Stat Card
- Value: `--text-h1` (32px, bold), colored by context
- Label: `--text-body` (14px, `--color-text-secondary`)
- Background: `--color-bg-card`

### 10.5 AI Insight Card
- Left accent bar: 3px, `--color-brand-primary`
- Header: "AI Insight" small-caps label, cyan color
- Icon: bulb/sparkle, electric blue
- Content: body text, slightly muted

---

## 11. Badges

### 11.1 Skill Badge
```css
background:    rgba(37,99,235,0.15);
color:         #93C5FD;
border:        1px solid rgba(37,99,235,0.25);
border-radius: var(--radius-xs); /* 4px */
padding:       2px 8px;
font-size:     12px;
font-weight:   500;
```

### 11.2 Status Badges

| Status        | Background                  | Text       | Border                      |
|---------------|-----------------------------|------------|-----------------------------|
| `MATCHED`     | `rgba(16,185,129,0.15)`     | `#10B981`  | `rgba(16,185,129,0.3)`      |
| `PREPARING`   | `rgba(245,158,11,0.15)`     | `#F59E0B`  | `rgba(245,158,11,0.3)`      |
| `SUBMITTED`   | `rgba(37,99,235,0.15)`      | `#60A5FA`  | `rgba(37,99,235,0.3)`       |
| `INTERVIEWING`| `rgba(139,92,246,0.15)`     | `#A78BFA`  | `rgba(139,92,246,0.3)`      |
| `REJECTED`    | `rgba(239,68,68,0.15)`      | `#F87171`  | `rgba(239,68,68,0.3)`       |
| `OFFERED`     | `rgba(16,185,129,0.20)`     | `#34D399`  | `rgba(16,185,129,0.4)`      |
| `UNKNOWN`     | `rgba(100,116,139,0.15)`    | `#94A3B8`  | `rgba(100,116,139,0.3)`     |

### 11.3 Strong Match Badge
```css
background:    rgba(16,185,129,0.15);
color:         #10B981;
border:        1px solid rgba(16,185,129,0.3);
padding:       4px 10px;
border-radius: var(--radius-full);
font-size:     12px;
font-weight:   600;
```
Contains lightning bolt (⚡) prefix.

---

## 12. Navigation

### 12.1 Top Navigation
- Height: **64px**
- Background: `--color-bg-surface` + `border-bottom: 1px solid var(--color-border-subtle)`
- Left: Logo area (aligned to sidebar width)
- Center: Search bar (full width, 48px)
- Right: Notification bell + badge, AI agent status pill, Avatar

#### AI Agent Status Pill
```css
background:    rgba(16,185,129,0.15);
border:        1px solid rgba(16,185,129,0.3);
border-radius: var(--radius-full);
padding:       6px 12px;
color:         #10B981;
```
- Green dot with pulse animation + "AI agent status" text

### 12.2 Sidebar

**Dimensions**
- Desktop: **240px** fixed
- Collapsed: **64px** icon-only rail
- Mobile: hidden, accessed via drawer

**Structure**
1. Logo area — 64px tall, `padding: 0 20px`
2. Nav items — icon + label, 44px height, 8px vertical gap
3. Sub-items — collapsible, indented 16px
4. Settings — pinned to bottom
5. User/Agent area — avatar + name + agent status dropdown

**Nav Item States**
- Default: transparent bg, `--color-text-secondary`
- Hover: `rgba(255,255,255,0.05)` bg, `--color-text-primary`
- Active: `rgba(37,99,235,0.15)` bg, `--color-brand-primary` text, 3px left accent

**Nav Items (from designs)**
1. Dashboard
2. AI Job Agent
3. Discover Jobs
4. Recommended
5. Applications (with sub-items)
6. Interventions
7. Resume
8. Candidate Profile
9. Analytics
10. Settings

---

## 13. Tables

### 13.1 Structure
```
[Checkbox] [Company] [Role] [Salary] [Match%] [Status] [Date] [Actions]
```

### 13.2 Styles
- Row height: **52px**
- Header: 12px, 500 weight, uppercase, `--color-text-muted`
- Body: 14px
- Row divider: `1px solid var(--color-border-subtle)`
- Hover row: `rgba(255,255,255,0.03)` bg
- Selected row: `rgba(37,99,235,0.08)` bg
- Checkboxes: custom styled, brand blue when checked

---

## 14. Charts

### 14.1 Line Chart
- Background: transparent
- Line: `--color-brand-primary`, 2px stroke
- Area fill: gradient from `rgba(37,99,235,0.2)` to transparent
- Axis: `--text-caption`, `--color-text-muted`
- Grid lines: `--color-border-subtle`

### 14.2 Bar Chart
- Bars: `--color-brand-primary` or `--gradient-brand` fill
- Background track: `--color-bg-elevated`
- Rounded tops: 4px radius

### 14.3 Donut / Circular Score
- Track: `--color-bg-elevated`
- Fill: green (strong match), blue (general progress)
- Center: bold percentage + small label "STRONG MATCH"
- Stroke width: 8–12px
- Glow: `--shadow-match-ring`

### 14.4 Progress Bar (AI Assessment)
- Height: **6px**
- Background: `--color-bg-elevated`
- Fill: `--color-brand-primary` or `--color-brand-active`
- Radius: `--radius-full`
- Label + value flanking bar

---

## 15. AI Components

### 15.1 AI Orb / Sphere
- Colors: deep blue core → violet mid → cyan edge glow
- Orbital rings: thin 1px lines, rotating
- Particle effects: glowing dots around orbit
- Usage: Dashboard hero, Landing hero, Application Agent panel
- Label: "AI AGENT ACTIVE" small-caps below

### 15.2 AI Agent Status Indicator
- Pulsing ring circle, 3 states:
  - **ACTIVE**: blue core + green pulse ring
  - **BUSY**: amber pulse ring
  - **INACTIVE**: gray, no pulse

### 15.3 AI Activity Timeline
- Vertical timeline, blue progress line on left
- Time-stamped entries: gray timestamp + white action text
- Urgent entries: red/amber highlight
- "Waiting for approval" state: pulsing indicator

### 15.4 AI Insight Cards
- Dark card + left accent bar (blue/purple)
- "AI Insight" or "Recommendation" header in cyan
- Sparkle/bulb icon
- Gradient background subtle

### 15.5 Match Score Ring
- SVG circular path, 64px × 64px on cards
- Animate fill on load
- Color by score threshold

### 15.6 Agent Activity Log
- Monospaced-style timestamps `[HH:MM:SS]`
- Scrollable panel
- Urgent items in `--color-error` or `--color-warning`

### 15.7 Application Progress Steps
- Horizontal stepper: checkmark (done) → active dot → empty circle (pending)
- Connected by lines: solid (done) → dashed (pending)
- Step labels: 12px, muted when pending

### 15.8 Screening Question Card
- Question: bold 14px text
- AI answer: green-tinted box, "Based on your verified profile: X"
- Confidence: "Confidence: High" in green
- Actions: Accept (primary button) / Edit (secondary)
- Unknown questions: amber "Unknown" badge + "Answer" button

---

## 16. 3D Visual Language

### 16.1 Usage Inventory

| Location                 | Element                    | Technique              | Animated? |
|--------------------------|----------------------------|------------------------|-----------|
| Landing page hero        | Faceted crystal/dodecahedron | Pre-rendered PNG/WebP | Yes (CSS rotation)|
| Dashboard AI hero        | Neon sphere + orbit rings  | SVG + CSS animation    | Yes (loop) |
| Job details              | Isometric tech workspace   | Pre-rendered PNG       | No (static)|
| Application Agent        | Neural particle sphere     | Lottie or pre-rendered | Yes (loop) |
| Agent Control Center     | Wireframe globe            | Three.js or pre-rendered | Yes (rotation)|

### 16.2 Principles
- 3D only in **hero/feature areas** — never in tables or forms
- Three.js only for the Control Center globe (interactive, justified)
- All other 3D: static assets or lightweight CSS/SVG animations
- Rule: 3D enhances brand identity; never sacrifices clarity or performance

---

## 17. Animations

### 17.1 Micro-interactions

| Element                | Animation                              | Duration | Easing                    |
|------------------------|----------------------------------------|----------|---------------------------|
| Button hover           | `scale(1.02)`, shadow intensify        | 150ms    | ease-out                  |
| Card hover             | `translateY(-2px)`, shadow expand      | 200ms    | ease-out                  |
| Sidebar item hover     | Background fade in                     | 120ms    | ease-in-out               |
| Match score ring       | SVG stroke-dashoffset animate          | 1200ms   | cubic-bezier(.4,0,.2,1)   |
| Progress bars          | Width 0 to value                       | 800ms    | ease-out                  |
| Stat numbers           | Count-up from 0                        | 1000ms   | ease-out                  |
| Status badge           | Fade + scale in                        | 200ms    | spring                    |
| AI pulse ring          | scale + opacity loop                   | 2000ms   | ease-in-out, infinite     |
| Activity entry         | Slide in from left                     | 300ms    | ease-out                  |
| Page transition        | Fade in                                | 250ms    | ease-in-out               |
| Modal open             | Scale(0.95 to 1) + fade                | 200ms    | ease-out                  |
| Toast notification     | Slide in from top-right                | 250ms    | ease-out                  |

### 17.2 AI Orb Keyframes
```css
@keyframes orbit {
  from { transform: rotateY(0deg) rotateX(15deg); }
  to   { transform: rotateY(360deg) rotateX(15deg); }
}
@keyframes ai-pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.75; }
}
@keyframes status-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50%       { transform: scale(1.4); opacity: 0.5; }
}
```

---

## 18. Responsive Rules

### 18.1 Breakpoints
```
--bp-mobile:  390px   /* From design-reference 06 */
--bp-tablet:  1024px  /* From design-reference 06 */
--bp-desktop: 1280px
--bp-wide:    1440px  /* Primary design target */
```

### 18.2 Layout Behavior

| Breakpoint  | Sidebar          | Top Nav         | Grid Cols |
|-------------|------------------|-----------------|-----------|
| 1440px+     | 240px fixed      | Full + search   | 3–4 cols  |
| 1024–1439px | 240px fixed      | Full + search   | 2–3 cols  |
| 768–1023px  | 64px icon-only   | Compact         | 2 cols    |
| Under 768px | Hidden (drawer)  | Hamburger menu  | 1 col     |

### 18.3 Component Adaptation

| Component       | Mobile                       | Tablet                     |
|-----------------|------------------------------|----------------------------|
| Sidebar         | Full-screen drawer           | Icon-only rail             |
| Job Cards       | Full-width stack             | 2-column grid              |
| Stat Cards      | 2x2 grid                     | Row of 4                   |
| Tables          | Horizontal scroll            | Horizontal scroll          |
| Charts          | Simplified labels            | Full display               |
| Modals          | Full-screen bottom sheet     | Centered dialog            |
| Agent Panel     | Below main content           | Side panel                 |
| Pipeline        | Horizontal scroll            | Full display               |

### 18.4 Typography Scaling

| Token            | Desktop | Tablet | Mobile |
|------------------|---------|--------|--------|
| `--text-display` | 48px    | 40px   | 32px   |
| `--text-h1`      | 32px    | 28px   | 24px   |
| `--text-h2`      | 24px    | 22px   | 20px   |
| `--text-h3`      | 18px    | 18px   | 16px   |
| `--text-body`    | 14px    | 14px   | 14px   |
