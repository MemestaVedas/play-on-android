# What Is Material Design 3?

Material Design 3 (M3) is Google's latest design system, built on top of Material You. It provides a comprehensive set of design tokens, components, and guidelines for building Android (and cross-platform) apps. M3 is the default design system for Jetpack Compose and is the basis for Material 3 Expressive.

---

## Core Design Pillars

### 1. Color
M3 uses a **dynamic color system** driven by a single seed color that generates a full tonal palette. The system defines **color roles** (Primary, Secondary, Tertiary, Error, Surface, Outline, etc.) rather than hardcoded hex values.

- Dynamic Color adapts to the user's wallpaper (Android 12+ / API 31+)
- Always provide a static fallback for older APIs
- Color roles ensure consistent contrast ratios for accessibility

### 2. Typography
A **type scale** of 15 named text styles (Display Large → Label Small) each with predefined size, weight, and line-height tokens. In Compose, these are available via `MaterialTheme.typography.*`.

### 3. Shape
M3 uses a **shape scale** from `None` (0dp) to `Full` (fully rounded), applied consistently across components. In M3 Expressive, the shape system expanded with 35 new shape tokens and **shape morphing** animation support.

### 4. Motion
The **MotionScheme** (introduced in material3 `1.4.0` stable) provides a unified animation vocabulary: `defaultSpatialSpec`, `fastSpatialSpec`, `slowSpatialSpec`, `defaultEffectsSpec`, etc. Motion is now a first-class subsystem alongside color, typography, and shape.

---

## M3 vs Material You vs M2

| | Material 2 | Material You / M3 | M3 Expressive |
|---|---|---|---|
| Color system | Fixed palette | Tonal palette + dynamic color | Same, extended tertiary colors |
| Shape | Basic corners | Extrapolated shape scale | +35 new shapes, morphing |
| Motion | Manual animations | Emerging MotionScheme | MotionScheme stable, spring physics |
| New components | — | Cards, NavigationBar, etc. | ButtonGroup, SplitButton, FloatingToolbar, FABMenu, LoadingIndicator |
| Jetpack Compose lib | `material` | `material3` | `material3` 1.5.0-alpha+ |

---

## The Material Theme Composable

Every M3 Compose app wraps content in `MaterialTheme`:

```kotlin
@Composable
fun MyApp() {
    MaterialTheme(
        colorScheme = myColorScheme,  // light or dark
        typography = myTypography,
        shapes = myShapes,
        // motionScheme available in 1.4.0+
    ) {
        // Your content here
    }
}
```

You can access theme values anywhere in the tree:

```kotlin
MaterialTheme.colorScheme.primary
MaterialTheme.typography.bodyLarge
MaterialTheme.shapes.medium
MaterialTheme.motionScheme.defaultSpatialSpec()
```

---

## Accessibility First

Every M3 subsystem — color roles, type scale, shape, and motion — is designed with accessibility as a baseline requirement, not an afterthought. Contrast ratios between roles are pre-computed to meet WCAG 2.1 AA standards. Components use minimum touch target sizes of 48x48dp by default (enforced via `Modifier.minimumInteractiveComponentSize`).
