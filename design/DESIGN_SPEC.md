# DevPilot Design Specification

> This document is the single source of truth for the DevPilot UI. Reference it when building any Compose Multiplatform component.
> The interactive HTML prototype is at `design/design-reference.html`.

---

## 1. Design Tokens

### 1.1 Colors — Backgrounds
| Token | Hex | Usage |
|-------|-----|-------|
| `bg-0` | `#06090F` | App base / main area background |
| `bg-1` | `#0B1019` | Sidebar, log panel background |
| `bg-2` | `#101723` | Card backgrounds, form inputs |
| `bg-3` | `#16203A` | Active items, pressed states, modal backgrounds |
| `bg-4` | `#1C2844` | Hover backgrounds inside cards |
| `bg-hover` | `#1A2540` | General hover state |
| `bg-overlay` | `#040E0E` at 72% alpha | Modal backdrop overlay |

### 1.2 Colors — Borders
| Token | Value | Usage |
|-------|-------|-------|
| `border-1` | `white 6% alpha` | Default subtle borders |
| `border-2` | `white 10% alpha` | Medium emphasis borders, input borders |
| `border-3` | `white 16% alpha` | High emphasis borders, focus rings |

### 1.3 Colors — Text
| Token | Hex | Usage |
|-------|-----|-------|
| `text-0` | `#EAF0FF` | Primary text, headings |
| `text-1` | `#B0BCDA` | Secondary text, descriptions |
| `text-2` | `#6B7A9E` | Muted text, labels, placeholders |
| `text-3` | `#3D4E6F` | Disabled text, dividers, hints |

### 1.4 Colors — Semantic / Accent
| Token | Hex | Dim (12% alpha) | Mid (25% alpha) | Usage |
|-------|-----|-----------------|-----------------|-------|
| `accent` (teal) | `#00D4AA` | `rgba(0,212,170,0.12)` | `rgba(0,212,170,0.25)` | Primary accent, CTA buttons, active project |
| `blue` | `#4B9CFF` | `rgba(75,156,255,0.12)` | `rgba(75,156,255,0.25)` | Links, branch chip, dependencies, port values |
| `red` | `#FF5574` | `rgba(255,85,116,0.10)` | `rgba(255,85,116,0.22)` | Stopped status, errors, destructive actions |
| `amber` | `#FFAD33` | `rgba(255,173,51,0.10)` | `rgba(255,173,51,0.22)` | Starting status, warnings |
| `green` | `#2DD881` | `rgba(45,216,129,0.10)` | `rgba(45,216,129,0.22)` | Running status, success, health OK |
| `purple` | `#A78BFA` | `rgba(167,139,250,0.10)` | — | Debug log level, project dot color option |

### 1.5 Typography
| Token | Font Family | Weights | Usage |
|-------|-------------|---------|-------|
| `font-ui` | `DM Sans` | 300, 400, 500, 600, 700 | All UI text, labels, buttons, headings |
| `font-mono` | `IBM Plex Mono` | 400, 500, 600 | Service names, ports, metrics, log output, code, timestamps |

**KMP mapping:** Use `FontFamily` with bundled `.ttf` files in `commonMain/resources/font/`.

#### Font Sizes
| Element | Size | Weight | Font |
|---------|------|--------|------|
| Logo title | 15px | 600 | ui |
| Logo version | 10px | 400 | mono |
| Sidebar label | 10px | 600, uppercase, letter-spacing 0.08em | ui |
| Project item | 12.5px | 400 (500 if active) | ui |
| Breadcrumb | 12.5px | 400 (500 for current) | ui |
| Toolbar button | 12.5px | 500 | ui |
| Card service name | 12.5px | 600 | mono |
| Card meta tags (port, framework) | 10px | 400 | mono |
| Status badge | 9.5px | 600, uppercase, letter-spacing 0.05em | ui |
| Metric value | 12px | 600 | mono |
| Metric label | 9px | 400, uppercase, letter-spacing 0.05em | ui |
| Action button | 11px | 500 | ui |
| Log timestamp | 11px | 400 | mono |
| Log level | 10px | 600 | mono |
| Log message | 11px | 400 | mono |
| Modal title | 15px | 600 | ui |
| Form label | 11.5px | 500 | ui |
| Form input | 12.5px | 400 | ui |
| Toast message | 12.5px | 400 | ui |

### 1.6 Spacing
| Token | Value | Usage |
|-------|-------|-------|
| `spacing-xs` | 4px | Gap between tiny elements |
| `spacing-sm` | 6px | Card internal gaps |
| `spacing-md` | 8px | Standard gaps |
| `spacing-lg` | 12px | Card grid gap, section margins |
| `spacing-xl` | 16px | Panel padding, major spacing |
| `spacing-2xl` | 20px | Main area horizontal padding |

### 1.7 Corner Radius
| Token | Value | Usage |
|-------|-------|-------|
| `radius-xs` | 4px | Log line hover, tiny elements |
| `radius-sm` | 6px | Buttons, icon buttons, pills, tags |
| `radius-md` | 8px | Inputs, filter groups, cards (secondary) |
| `radius-lg` | 12px | Service cards, modals, toasts, environment badge |
| `radius-xl` | 16px | Modal containers, large panels |

### 1.8 Shadows
| Token | Value | Usage |
|-------|-------|-------|
| `shadow-sm` | `0 1px 3px rgba(0,0,0,0.3)` | Card hover lift |
| `shadow-md` | `0 4px 20px rgba(0,0,0,0.4)` | Toast notifications |
| `shadow-lg` | `0 12px 40px rgba(0,0,0,0.5)` | Modals, context menus, detail drawer |

### 1.9 Transitions
| Token | Value | Usage |
|-------|-------|-------|
| `fast` | 120ms ease | Hover, active states |
| `med` | 200ms ease | Modal fade, selection |
| `slow` | 350ms cubic-bezier(0.4,0,0.2,1) | Drawer slide, panel resize |

---

## 2. Layout

### 2.1 App Shell
```
┌─────────────────────────────────────────┐
│  Sidebar (240px fixed)  │  Main Area    │
│                         │               │
│  ┌──────────────────┐   │  ┌─ Topbar ─┐ │  48px height
│  │ Logo + Settings  │   │  │ Bread/Git │ │
│  ├──────────────────┤   │  ├──────────┤ │
│  │ Projects list    │   │  │ Toolbar   │ │  ~44px height
│  │                  │   │  ├──────────┤ │
│  │                  │   │  │ Services  │ │  flex: 1 (scrollable)
│  │                  │   │  │ Grid/List │ │
│  │                  │   │  │           │ │
│  ├──────────────────┤   │  ├──────────┤ │
│  │ Environment badge│   │  │ Log Panel │ │  230px default (resizable)
│  │ Stats row        │   │  │           │ │
│  └──────────────────┘   │  └──────────┘ │
└─────────────────────────────────────────┘
```

**Sidebar**: fixed 240px wide, bg-1, full height, border-right border-1.
**Main**: flex column, fills remaining width.
**Topbar**: 48px height, bg-0, border-bottom border-1.
**Toolbar**: auto-height (~44px), bg-0, border-bottom border-1, wraps on smaller widths.
**Services scroll area**: flex-1, overflow-y auto, padding 16px 20px.
**Log panel**: fixed at bottom, default height 230px, resizable by dragging top edge (min 80px, max 600px), collapsible.

### 2.2 Service Grid
- `display: grid` with `grid-template-columns: repeat(auto-fill, minmax(320px, 1fr))`
- Gap: 12px
- Cards animate in with staggered delay (50ms per card)

### 2.3 Service List View
- Table-like rows using CSS grid: `30px 1.5fr 80px 80px 80px 80px 1fr auto`
- Columns: Status dot, Name, Status text, Port, CPU, Heap, Dependencies, Actions
- Header row with uppercase labels

---

## 3. Component Catalog

### 3.1 ServiceCard (Grid View)
**Structure (top to bottom):**
1. **Top accent bar** — 2px height, full width, color by status
2. **Header row** — Status icon (34x34 rounded square) + Name/meta + Status badge
3. **Health bar** — 3px tall progress bar
4. **Metrics row** — 3-column grid: CPU, Heap, Port (with border-top and border-bottom)
5. **Dependencies row** — Chain icon + dep chips (or "No dependencies")
6. **Actions row** — Start/Stop button + Restart + icon buttons (folder, logs, details)

**States:**

| State | Accent bar | Icon bg | Icon | Badge color | Health bar | Metrics |
|-------|-----------|---------|------|-------------|------------|---------|
| `STOPPED` | red | red-dim | Square (outline) | red-dim bg, red text, red-mid border | empty (0%, gray) | "—" in text-3 |
| `STARTING` | amber, pulsing | amber-dim | Spinner (rotating) | amber-dim bg, amber text, pulsing opacity | partial (random), amber | "—" in text-3 |
| `RUNNING` | green | green-dim | Play (filled) + pulse dot | green-dim bg, green text, green-mid border | filled (70-100%), green | Values in green/blue |
| `ERROR` | red | red-dim | X circle (outline) | red-dim bg, red text, red-mid border | low (10-30%), red | Values if partially running |

**Interactions:**
- Click → select card (highlight border with accent-mid, bg-3)
- Double-click → open detail drawer
- Right-click → context menu
- Hover → border-2, translateY(-1px), shadow-sm

**Card CSS:**
```
background: bg-2
border: 1px solid border-1
border-radius: radius-lg (12px)
padding: 14px 16px
```

### 3.2 ServiceCard (List View)
Single row per service. Columns match the grid definition in 2.3.
- Status dot: 8px circle, colored by status. Running has box-shadow glow.
- Name: mono font, 12px, weight 500
- Click to select, double-click for drawer, right-click for context menu

### 3.3 Sidebar — Project Item
```
Layout: horizontal row
  [8px colored dot] [project name] [count badge, right-aligned]
```
- Padding: 7px 8px
- Border-radius: radius-md
- Active: bg-3, text-0, font-weight 500. Count badge: accent-dim bg, accent text, accent-mid border
- Hover: bg-hover, text-0
- Right-click: context menu with Rename, Duplicate, Delete

### 3.4 Sidebar — Environment Badge
```
Layout: row
  [7px green pulsing dot] [Docker Desktop label + JDK/CPU mono meta]
```
- Background: bg-2, border: border-1, radius: radius-md
- Below: 3-column stats grid (Up count, Heap total, Active ports)

### 3.5 Toolbar
**Elements in order (left to right):**
1. Start All button (btn-accent: teal bg, dark text)
2. Stop All button (btn-danger: transparent bg, red text, red-mid border)
3. Restart All button (btn-ghost: transparent bg, text-1, border-2)
4. Vertical divider (1px × 20px, border-1 color)
5. Search input (bg-1, border-1, radius-md, 280px max-width, with search icon)
6. Filter group (All | Running | Stopped | Error) — pill-style segmented control
7. View toggle (Grid | List) — icon-only segmented control
8. Spacer (flex: 1)
9. Add Service button (btn-blue: blue-dim bg, blue text, blue-mid border)

### 3.6 Log Panel
**Header bar:**
- Tabs: Tail, All, Errors (with red count badge), Traces
- Log search input (mono font, smaller)
- Service selector chip (accent-dim bg, accent text, cycles through services on click)
- Action buttons: Auto-scroll toggle, Clear logs, Close/minimize panel

**Log body:**
- Monospace font, 11px, line-height 1.8
- Each line: `[timestamp text-3] [LEVEL colored] [message text-1]`
- Level colors: INFO=blue, WARN=amber, ERROR=red, DEBUG=purple
- Hover on line: bg-2 highlight
- Search highlights matching text with amber-25% background

**Resize:** Top edge of panel has 4px invisible drag handle. On hover: accent-mid colored.

### 3.7 Detail Drawer
- Slides in from right edge, 460px wide, full height
- Header: status icon + service name (mono, 13px bold) + status badge + close button
- Body sections: Configuration (port, framework, path, JVM args), Runtime (status, cpu, heap, health, uptime), Dependencies
- Bottom: Start/Stop + Restart + Delete action buttons
- Transition: 350ms slide with shadow-lg

### 3.8 Modals
**Add Service modal (520px wide):**
- Fields: Name (text, required), Port (number, required), Framework (select dropdown), Project path (text), Dependencies (tag input with Enter to add), JVM arguments (text)
- Footer: Cancel (ghost) + Add Service (accent)

**Add Project modal (380px wide):**
- Fields: Project name (text, required), Color (6 color dot options)
- Footer: Cancel (ghost) + Create (accent)

**Keyboard Shortcuts modal (400px wide):**
- List of shortcut key + description rows

**All modals:**
- Overlay: bg-overlay with backdrop-filter blur(8px)
- Container: bg-2, border border-2, radius-xl, shadow-lg
- Open animation: fade overlay + scale(0.95→1) translateY(10→0) on container

### 3.9 Toast Notifications
- Fixed bottom-right, stack upward
- Background: bg-3, border border-2, radius-lg, shadow-md
- Layout: [colored icon circle 20px] [message text] [close button]
- Types: success (green), error (red), info (blue), warning (amber)
- Animation: slide up + fade in, auto-dismiss after 4 seconds
- Min width 260px, max 400px

### 3.10 Context Menu
- Fixed position at cursor coordinates
- Background: bg-3, border border-2, radius-lg, padding 4px, shadow-lg
- Items: 7px 10px padding, radius-sm, full width
- Hover: bg-4
- Danger items: red text, hover bg red-dim
- Separator: 1px line, border-1, margin 3px 6px
- Keyboard shortcut hint: mono, 10px, text-3, right-aligned

### 3.11 Buttons

| Variant | Background | Text color | Border |
|---------|-----------|-----------|--------|
| `btn-accent` | `#00D4AA` | `#050E10` (dark) | none |
| `btn-ghost` | transparent | text-1 | border-2 |
| `btn-danger` | transparent | red | red-mid |
| `btn-blue` | blue-dim | blue | blue-mid |
| `act-start` | green-dim | green | green-mid |
| `act-stop` | red-dim | red | red-mid |
| `act-restart` | bg-hover | text-1 | border-2 |
| Card icon btn | transparent | text-2 | border-1 |

All buttons: radius-md, 500 weight, fast transition. Hover: slightly brighter bg.

### 3.12 Form Elements
- **Text input**: bg-1, border border-2, radius-md, 12.5px, padding 7px 11px. Focus: border accent-mid.
- **Select**: Same as input + custom dropdown arrow SVG.
- **Tag input**: Container with bg-1 border-2, chips inside (blue-dim bg, blue text, mono 10px, 999px radius).

### 3.13 Status Badge (pill)
- 9.5px, weight 600, uppercase, letter-spacing 0.05em
- Padding: 2px 7px, border-radius: 999px
- Per-status colors: `{status}-dim` bg, `{status}` text, `{status}-mid` border
- Starting state: pulsing opacity animation

### 3.14 Dependency Chip
- Mono font, 9.5px, blue-dim bg, blue text, 999px radius
- Has a 5px status dot: green if dependency is running, red if down
- Hover: slightly brighter blue bg

---

## 4. Interaction Patterns

### 4.1 Service Lifecycle
```
STOPPED → (click Start) → STARTING → (after 1.5-2.5s) → RUNNING
RUNNING → (click Stop) → STOPPED
RUNNING → (click Restart) → STOPPED → STARTING → RUNNING
ANY → Error during start → ERROR
```

### 4.2 Start All in Order
Services start sequentially with 800ms delay between each. Respects dependency order (dependencies start first).

### 4.3 Log Streaming
When a service is running, it generates log entries that stream to the log panel. Logs are per-service (keyed by service name). Max 500 entries per service (trim to 300 when exceeded).

### 4.4 Keyboard Shortcuts
| Shortcut | Action |
|----------|--------|
| `Ctrl+K` | Focus search |
| `Ctrl+N` | Open Add Service modal |
| `Ctrl+Shift+S` | Start all services |
| `Ctrl+Shift+X` | Stop all services |
| `Ctrl+L` | Toggle log panel |
| `Escape` | Close modals and drawer |

### 4.5 Card Selection
- Single selection only
- Click selects, highlights card with accent-mid border + bg-3
- Selected card's logs show in log panel

---

## 5. Animations

| Animation | Duration | Easing | Description |
|-----------|----------|--------|-------------|
| Card enter | 350ms | ease | Fade + translateY(8→0), staggered 50ms per card |
| Card hover | 120ms | ease | translateY(-1px) + shadow-sm |
| Toast enter | 350ms | ease | Fade + translateY(12→0) + scale(0.95→1) |
| Toast exit | 300ms | ease | Fade + translateY(0→-8) + scale(1→0.95) |
| Modal open | 200ms | ease | Overlay fade + container scale(0.95→1) translateY(10→0) |
| Drawer slide | 350ms | cubic-bezier(0.4,0,0.2,1) | right: -480→0 |
| Context menu | 120ms | ease | Fade + scale(0.95→1) |
| Running pulse | 2200ms | infinite | box-shadow green expanding ring |
| Starting bar pulse | 1500ms | infinite | opacity 1→0.4→1 |
| Starting badge pulse | 1500ms | infinite | opacity 1→0.5→1 |
| Spinner rotation | 1000ms | linear infinite | Full 360° rotation |

---

## 6. Data Models (for KMP)

```kotlin
enum class ServiceStatus { STOPPED, STARTING, RUNNING, ERROR }

data class Project(
    val id: String,
    val name: String,
    val color: Long,     // ARGB hex, e.g., 0xFF4B9CFF
    val isActive: Boolean
)

data class Service(
    val id: String,
    val name: String,
    val port: Int,
    val framework: String,
    val status: ServiceStatus,
    val cpu: Float,          // percentage, 0.0 when stopped
    val heapMb: Int,         // megabytes, 0 when stopped
    val healthPercent: Int,  // 0-100
    val dependencies: List<String>, // service names
    val projectId: String,
    val jvmArgs: String,
    val path: String
)

data class LogEntry(
    val timestamp: String,   // "HH:mm:ss.SSS"
    val level: LogLevel,
    val message: String
)

enum class LogLevel { INFO, WARN, ERROR, DEBUG }

data class AppState(
    val projects: List<Project>,
    val services: List<Service>,
    val logs: Map<String, List<LogEntry>>,  // keyed by service name
    val selectedServiceId: String?,
    val logServiceName: String?,
    val viewMode: ViewMode,                 // GRID or LIST
    val statusFilter: ServiceStatus?,       // null = all
    val searchQuery: String,
    val isDrawerOpen: Boolean,
    val drawerServiceId: String?,
    val logPanelHeight: Int,                // dp
    val isLogPanelCollapsed: Boolean,
    val autoScrollLogs: Boolean
)

enum class ViewMode { GRID, LIST }
```

---

## 7. Compose Theme Mapping

```kotlin
object DevPilotColors {
    val bg0 = Color(0xFF06090F)
    val bg1 = Color(0xFF0B1019)
    val bg2 = Color(0xFF101723)
    val bg3 = Color(0xFF16203A)
    val bg4 = Color(0xFF1C2844)
    val bgHover = Color(0xFF1A2540)

    val border1 = Color(0x0FFFFFFF) // 6% white
    val border2 = Color(0x1AFFFFFF) // 10% white
    val border3 = Color(0x29FFFFFF) // 16% white

    val text0 = Color(0xFFEAF0FF)
    val text1 = Color(0xFFB0BCDA)
    val text2 = Color(0xFF6B7A9E)
    val text3 = Color(0xFF3D4E6F)

    val accent = Color(0xFF00D4AA)
    val accentDim = Color(0x1F00D4AA) // 12%
    val accentMid = Color(0x4000D4AA) // 25%

    val blue = Color(0xFF4B9CFF)
    val blueDim = Color(0x1F4B9CFF)
    val blueMid = Color(0x404B9CFF)

    val red = Color(0xFFFF5574)
    val redDim = Color(0x1AFF5574)
    val redMid = Color(0x38FF5574)

    val amber = Color(0xFFFFAD33)
    val amberDim = Color(0x1AFFAD33)
    val amberMid = Color(0x38FFAD33)

    val green = Color(0xFF2DD881)
    val greenDim = Color(0x1A2DD881)
    val greenMid = Color(0x382DD881)

    val purple = Color(0xFFA78BFA)
    val purpleDim = Color(0x1AA78BFA)
}

object DevPilotSpacing {
    val xs = 4.dp
    val sm = 6.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    val xxl = 20.dp
}

object DevPilotRadius {
    val xs = 4.dp
    val sm = 6.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
}
```
