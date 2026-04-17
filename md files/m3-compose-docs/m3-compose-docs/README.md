# Material 3 & Jetpack Compose Docs

> **Last updated:** April 2026 · Covers **M3 Expressive** (stable + alpha) · Compose BOM `2025.12.00` · material3 `1.4.0` stable / `1.5.0-alpha11` latest alpha

This folder is a reference knowledge-base for AI agents working with **Material Design 3 (M3)** and **Jetpack Compose** on Android. It covers design principles, component specs, Compose APIs, and code examples — with a dedicated section for the new **M3 Expressive** components introduced in 2025.

---

## 📁 Folder Structure

```
m3-compose-docs/
├── README.md                        ← you are here
│
├── overview/
│   ├── what-is-m3.md                ← M3 fundamentals & design philosophy
│   ├── m3-expressive.md             ← M3 Expressive overview & new components list
│   └── getting-started.md           ← Gradle setup, BOM, dependencies
│
├── theming/
│   ├── color-system.md              ← Color roles, dynamic color, schemes
│   ├── typography.md                ← Type scale, fonts, Compose API
│   ├── shape-system.md              ← Shape tokens, morphing, corner radii
│   └── motion-scheme.md             ← MotionScheme, spring animations
│
├── components/
│   ├── expressive/                  ← New M3 Expressive components
│   │   ├── button-groups.md
│   │   ├── split-button.md
│   │   ├── fab-menu.md
│   │   ├── floating-toolbar.md
│   │   ├── loading-indicator.md
│   │   ├── search-app-bar.md
│   │   └── expressive-list-items.md
│   │
│   └── core/                        ← Stable M3 components
│       ├── buttons.md
│       ├── text-fields.md
│       ├── navigation.md
│       ├── cards.md
│       ├── dialogs-sheets.md
│       ├── chips.md
│       ├── carousel.md
│       └── progress-indicators.md
│
├── adaptive/
│   └── adaptive-layouts.md          ← Adaptive UI, foldables, tablets
│
└── reference/
    ├── versions.md                  ← BOM & library version matrix
    └── sources.md                   ← Official links & resources
```

---

## ⚡ Quick-Start for Agents

**Key facts to always know:**
- The current **stable** material3 version is `1.4.0` (released Sept 24, 2025)
- The current **alpha** version is `1.5.0-alpha11` — this is where M3 Expressive APIs live
- Use the **Compose BOM** to align all Compose library versions: `2025.12.00`
- M3 Expressive experimental APIs require `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`
- The standard theme composable is `MaterialTheme { }` wrapping your app content

**Critical M3 Expressive components (new in 2025):**
`ButtonGroup` · `SplitButton` · `FloatingToolbar` · `FABMenu` · `LoadingIndicator` · `SearchAppBar` · `ExpressiveListItem`

---

## 🔗 Official Sources

| Resource | URL |
|---|---|
| Material Design 3 | https://m3.material.io |
| M3 Expressive Blog | https://m3.material.io/blog/building-with-m3-expressive |
| Compose Material3 Releases | https://developer.android.com/jetpack/androidx/releases/compose-material3 |
| Compose API Reference | https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary |
| Android Developers – M3 in Compose | https://developer.android.com/develop/ui/compose/designsystems/material3 |
