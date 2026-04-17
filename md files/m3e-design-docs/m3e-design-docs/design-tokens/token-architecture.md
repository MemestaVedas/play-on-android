# Design Token Architecture

Design tokens are the **single source of truth** for all style values in M3 Expressive. They create a systematic, scalable backbone that makes theming, multi-brand support, and design-to-code handoff reliable and consistent.

---

## What Are Design Tokens?

A design token is a named, platform-agnostic style value:

```
// Instead of:
color: #6750A4

// Use:
color: {color.primary}  →  resolves to #6750A4 in light mode, #D0BCFF in dark mode
```

Tokens abstract style decisions from raw values. When the raw value changes (new brand color), every component that references the token updates automatically.

---

## The Three Token Tiers

### Tier 1 — Reference Tokens (Raw Values)
The complete palette of all possible values. These are never used in components directly — they are the "library" from which roles are selected.

```
ref.palette.primary10  = #21005D
ref.palette.primary20  = #381E72
ref.palette.primary30  = #4F378B
ref.palette.primary40  = #6750A4  ← often mapped to sys.color.primary
ref.palette.primary80  = #D0BCFF  ← often mapped to dark mode sys.color.primary
ref.palette.primary90  = #EADDFF
...
```

In Compose, reference tokens live in the generated `Color.kt` file as named constants.

### Tier 2 — System Tokens (Semantic Roles)
Named roles that reference values from Tier 1. These are what components use.

```
sys.color.primary           → ref.palette.primary40  (light)
                            → ref.palette.primary80  (dark)
sys.color.on-primary        → ref.palette.primary100 (light)
                            → ref.palette.primary20  (dark)
sys.typescale.body-large    → size: 16sp, weight: 400, line-height: 24sp
sys.shape.corner.medium     → 12dp rounded
sys.motion.spatial.default  → spring(stiffness=Medium, damping=NoBouncy)
```

In Compose, system tokens are accessed via `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, etc.

### Tier 3 — Component Tokens (Per-Component Overrides)
Token values specific to one component that may differ from system defaults.

```
comp.button.container.color         → sys.color.primary
comp.button.label.color             → sys.color.on-primary
comp.button.container.shape         → sys.shape.corner.full
comp.button.disabled.container.color → sys.color.on-surface @ 12% alpha
comp.navigation-bar.active.icon.color → sys.color.secondary  (changed in 1.4.0)
```

In Compose, component tokens are set via `ComponentDefaults.colors()`, `ComponentDefaults.shape`, etc.

---

## Token Naming Convention

M3 tokens follow a structured naming pattern:

```
{tier}.{category}.{role/property}.{variant/state}

Examples:
sys.color.primary                    ← system, color, primary role
sys.color.surface-container-low      ← system, color, surface container variant
sys.typescale.display-large.size     ← system, typescale, display large, size property
comp.fab.container.color             ← component, fab, container, color
comp.chip.selected.label.color       ← component, chip, selected state, label color
```

---

## Token Categories

### Color Tokens
```
ref.palette.{key-color}{tone}        — raw palette values
sys.color.{role}                     — semantic color roles
comp.{component}.{part}.color        — component-specific colors
```

### Typography Tokens
```
sys.typescale.{role}.{property}
  → roles: display, headline, title, body, label (× large/medium/small)
  → properties: font-family, weight, size, line-height, letter-spacing
  → M3E adds: emphasized variants for each role
```

### Shape Tokens
```
sys.shape.corner.{scale}
  → scales: none, extra-small, small, medium, large, extra-large, full
sys.shape.polygon.{name}
  → new in M3E: 35 named polygon shapes
```

### Motion Tokens
```
sys.motion.{type}.{speed}
  → types: spatial, effects
  → speeds: default, fast, slow
```

### State Tokens
```
sys.state.{component}.{state}.{property}
  → states: hover, pressed, focused, dragged, selected, disabled
  → property: color-overlay-opacity, container-color, etc.
```

---

## Tokens in Compose

Compose's `MaterialTheme` object is the runtime representation of system tokens:

```kotlin
// sys.color.primary
MaterialTheme.colorScheme.primary

// sys.typescale.body-large
MaterialTheme.typography.bodyLarge

// sys.shape.corner.medium
MaterialTheme.shapes.medium

// sys.motion.spatial.default
MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
```

Component tokens are encapsulated in `Defaults` objects:

```kotlin
// comp.button.container.color = primary (by default)
ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary  // override component token
)

// comp.card.elevation = 1dp (ElevatedCard default)
CardDefaults.cardElevation(defaultElevation = 4.dp)  // override elevation token

// comp.navigation-bar.item.active.indicator.color = secondaryContainer (new in 1.4.0)
NavigationBarItemDefaults.colors(
    indicatorColor = MaterialTheme.colorScheme.secondaryContainer  // default; override if needed
)
```

---

## Custom Component Tokens

When building custom components, define their tokens as function parameters mirroring M3's pattern:

```kotlin
@Immutable
data class MyCardColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color
)

object MyCardDefaults {
    @Composable
    fun cardColors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        borderColor: Color = MaterialTheme.colorScheme.outlineVariant
    ) = MyCardColors(containerColor, contentColor, borderColor)
}

@Composable
fun MyCard(
    colors: MyCardColors = MyCardDefaults.cardColors(),
    content: @Composable () -> Unit
) {
    Surface(
        color = colors.containerColor,
        contentColor = colors.contentColor,
        border = BorderStroke(1.dp, colors.borderColor)
    ) { content() }
}
```

This pattern ensures your custom components are as themeable as M3 built-ins.

---

## Token Synchronization: Figma → Code

The Figma M3 Design Kit uses the same token names as the Compose API. The handoff workflow:

1. Designer applies `sys.color.primary` in Figma → appears in spec as `MaterialTheme.colorScheme.primary`
2. Designer specifies `sys.motion.spatial.default` → developer uses `MaterialTheme.motionScheme.defaultSpatialSpec()`
3. Designer uses shape token `sys.shape.corner.large` → developer uses `MaterialTheme.shapes.large`

No translation required — the vocabulary is shared.

---

## Token Stability in M3 Expressive

The M3 color role token set has been **stable** since material3 1.0. New tokens added in M3 Expressive (surface containers, motion specs, emphasized typography) are `@ExperimentalMaterial3ExpressiveApi` until they graduate to stable.

**Current stable token categories:**
- All color roles
- All 15 baseline typography styles
- Shape scale (extraSmall → full)
- MotionScheme specs

**Currently experimental (as of 1.5.0-alpha11):**
- 15 emphasized typography styles (`*Emphasized`)
- Named polygon shapes (`MaterialShapes.*`)
- `MotionScheme.expressive()` / `standard()` named constructors
