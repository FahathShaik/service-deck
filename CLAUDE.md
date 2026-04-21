# DevPilot — Microservices Manager

## Project Overview
DevPilot is a desktop application for managing local microservices (Spring Boot, Quarkus, Micronaut). Built with Kotlin Multiplatform (Compose Multiplatform) targeting Desktop (primary) and Android/iOS (future).

## Design Reference
- `design/design-reference.html` — Full interactive HTML prototype. Open in a browser to see every component, state, and interaction.
- `design/DESIGN_SPEC.md` — Complete design specification: tokens, colors, typography, spacing, component catalog, states, and screen-by-screen layout. **Read this file before building any UI component.**

## Tech Stack
- **Kotlin Multiplatform** with **Compose Multiplatform**
- Desktop target via Compose Desktop (JVM)
- State management: MVI pattern or ViewModel + StateFlow
- Process management: `ProcessBuilder` for launching JVM services
- Log streaming: Reading process stdout/stderr via coroutines

## Architecture
```
src/
├── commonMain/          # Shared code
│   ├── domain/          # Models, use cases
│   ├── data/            # Repositories, process management
│   └── ui/
│       ├── theme/       # Colors, typography, spacing tokens (from DESIGN_SPEC.md)
│       ├── components/  # Reusable UI components
│       └── screens/     # Screen composables
└── desktopMain/         # Desktop-specific (process execution, file system)
```

## Key Rules for Claude Code
1. **Always read `design/DESIGN_SPEC.md` before creating or modifying UI** — it has exact color hex values, spacing values, font specs, and component behaviors.
2. **Map CSS variables → Compose Theme** — Every CSS variable in the spec maps to a Compose `Color` or dimension value. Use the MaterialTheme or a custom DevPilotTheme object.
3. **Component naming** — Match the component names in DESIGN_SPEC.md (e.g., `ServiceCard`, `LogPanel`, `DetailDrawer`).
4. **States are explicit** — Every service has exactly one of: `STOPPED`, `STARTING`, `RUNNING`, `ERROR`. Colors and icons change per state. Refer to the state table in the spec.
5. **Dark theme only (for now)** — The design is dark-mode. Define all colors as constants, not system-theme-dependent.
6. **Monospace for data** — Service names, ports, metrics, logs, timestamps always use the mono font. UI labels use the sans font.
