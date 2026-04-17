# Material 3 Expressive

Material 3 Expressive (M3E) is a major evolution of M3 announced at **Google I/O 2025**. It adds new components, expanded shape options, spring-based motion, and a stronger design philosophy around emotion and personality. It is **not** Material 4 — it is still rooted in M3, but with significantly expanded expressiveness.

M3 Expressive APIs live in the **alpha** releases of the `material3` library (currently `1.5.0-alpha11`). All M3E APIs require opting in:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
```

---

## Design Philosophy

M3 Expressive is built on the idea that design should **communicate function and inspire emotion**. Key principles:

- **Shape as identity** — shapes are now a core branding tool, not just corner radii
- **Motion as feedback** — spring-based, physics-driven animations communicate state changes and give UI elements personality
- **Size contrast** — deliberate variation in component sizes (XS → XL) creates visual hierarchy
- **Containers** — grouping related content in explicit containers for clarity

---

## New & Updated Components in M3 Expressive

### Completely New Components

| Component | Description | Status |
|---|---|---|
| `ButtonGroup` | Container for grouped buttons with shared shape/motion behavior | Alpha |
| `SplitButton` | Two-zone button: action + dropdown trigger, with shape morph on open | Alpha |
| `FloatingToolbar` | Contextual action toolbar that floats over content | Alpha |
| `FABMenu` / `ExpandableFab` | FAB with expandable sub-actions, replaces speed dial | Alpha |
| `LoadingIndicator` | Morphing shape animation for loads < 5s; replaces indeterminate circular progress | Alpha |
| `SearchAppBar` | App bar with built-in search field, hamburger outside the pill | Alpha |
| `ExpressiveListItem` | List items with expressive interactions and segmented styling | Alpha (alpha11) |

### Updated / Evolved Components

| Component | What Changed |
|---|---|
| `Button` | New XS–XL size variants, more shape options |
| `IconButton` | Size variants, shape morphing on interaction |
| `NavigationBar` | Shorter bar (replaces tall bottom bar from Material You) |
| `TopAppBar` | Expressive variants with expanded search |
| `BottomAppBar` | **Deprecated** — migrate to `DockedToolbar` / `FloatingToolbar` |
| `FilterChip` | `horizontalArrangement` parameter, expressive sizing |
| `Carousel` | Multi-browse and uncontained variants now stable; multi-aspect via lazy grids (alpha11) |
| `Menu` | Toggleable/selectable menu items, menu groups (alpha09) |

---

## Shape System Expansion

M3 Expressive added **35 new shape tokens** to the shape library, including:

- New corner radii tokens with fully-rounded set to `full` (previously 50%)
- Shape morphing: shapes can animate smoothly between states (e.g., circle → squircle → rectangle)
- Shapes and text are now designed to work in visual harmony

### Using Shape Morphing in Compose

```kotlin
// Shape morph animation (M3 Expressive)
val morphShape = rememberMorphShape(
    startShape = CircleShape,
    endShape = RoundedCornerShape(16.dp),
    progress = animatedProgress
)

Box(modifier = Modifier.clip(morphShape)) { /* content */ }
```

---

## Motion Scheme

The `MotionScheme` is now **stable** as of material3 `1.4.0` and is a core part of M3 Expressive:

```kotlin
// Access motion specs from theme
val motionScheme = MaterialTheme.motionScheme

// Use in animations
val boundsAnimation = motionScheme.defaultSpatialSpec<Float>()
val fadeAnimation = motionScheme.defaultEffectsSpec<Float>()
```

Spring-based specs replace duration-based ones for spatial animations, giving a more physical, alive feel.

---

## Real-World Adoption

Google's own apps show the breadth of M3 Expressive:

- **Google Meet** — first app with full M3E redesign; large pill-shaped action buttons, container cards
- **Google Drive** — container-grouped file listings, large FAB with dynamic color
- **Gmail** — SearchAppBar with hamburger outside the pill, expressive bottom navigation
- **Google Photos** — pull-to-refresh with morphing shapes, LoadingIndicator
- **Pixel Camera** — floating toolbar for editing actions
- **Fitbit** — contained LoadingIndicator, M3E shapes throughout

---

## Migrating from M3 to M3 Expressive

1. Upgrade to alpha BOM: `2026.xx.00` or add `material3:1.5.0-alpha11` directly
2. Add `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` where needed
3. Replace `BottomAppBar` usages with `DockedToolbar` or `FloatingToolbar`
4. Replace speed-dial FAB patterns with `FABMenu`/`ExpandableFab`
5. Replace indeterminate `CircularProgressIndicator` (short loads) with `LoadingIndicator`
6. Consider adopting `ButtonGroup` and `SplitButton` for action-heavy screens
