# MotionScheme Design Guide

`MotionScheme` is a first-class theme subsystem in M3 Expressive (stable since material3 1.4.0). It gives designers and developers a shared vocabulary for animation — the same way `ColorScheme` and `Typography` do for color and type.

---

## The Problem It Solves

Before `MotionScheme`:
- Each developer wrote their own animation specs (`tween(300, easing = FastOutSlowIn)`)
- Different screens used different durations and easings, creating an inconsistent feel
- No design-to-code handoff mechanism for motion

With `MotionScheme`:
- All spatial animations use the same themed spring spec
- All effects animations (color, opacity) use the same themed effects spec
- Changing the motion feel of the entire app is a single `MaterialTheme` property change

---

## Anatomy of MotionScheme

```kotlin
// MotionScheme has 6 specs
MaterialTheme.motionScheme.defaultSpatialSpec<T>()   // most common
MaterialTheme.motionScheme.fastSpatialSpec<T>()      // fast interactions
MaterialTheme.motionScheme.slowSpatialSpec<T>()      // deliberate moves
MaterialTheme.motionScheme.defaultEffectsSpec<T>()   // color/alpha
MaterialTheme.motionScheme.fastEffectsSpec<T>()      // quick fades
MaterialTheme.motionScheme.slowEffectsSpec<T>()      // gradual fades
```

All return `AnimationSpec<T>` — drop-in replacements for `tween()`, `spring()`, etc.

---

## The Two Preset MotionSchemes

### MotionScheme.expressive()

The M3 Expressive default for consumer apps. Springs are slightly springy (subtle overshoot), making interactions feel alive:

```kotlin
MaterialExpressiveTheme(motionScheme = MotionScheme.expressive()) { }
```

Internals (approximate):
- `defaultSpatialSpec` → spring(stiffness=Medium, damping=0.9) — very slight overshoot
- `fastSpatialSpec` → spring(stiffness=High, damping=1.0) — no overshoot, fast
- `slowSpatialSpec` → spring(stiffness=Low, damping=0.9) — slow, slight overshoot

### MotionScheme.standard()

For professional/enterprise apps that need precise, no-nonsense motion:

```kotlin
MaterialExpressiveTheme(motionScheme = MotionScheme.standard()) { }
```

Internals (approximate):
- `defaultSpatialSpec` → spring(stiffness=Medium, damping=1.0) — no overshoot
- All specs critically damped — never bounces

---

## Setting Up in Theme

```kotlin
@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useExpressiveMotion: Boolean = true,
    content: @Composable () -> Unit
) {
    val motionScheme = if (useExpressiveMotion)
        MotionScheme.expressive()
    else
        MotionScheme.standard()

    MaterialExpressiveTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        motionScheme = motionScheme,
        content = content
    )
}
```

> **Note:** `MaterialExpressiveTheme` is the M3 Expressive variant of `MaterialTheme`. It accepts `motionScheme` as a parameter. `MaterialTheme` also supports `motionScheme` as of 1.4.0 stable.

---

## Scoped Motion Override

You can override `MotionScheme` for a sub-tree using `CompositionLocalProvider`:

```kotlin
// Use standard motion for dense UI sections
CompositionLocalProvider(
    LocalMotionScheme provides MotionScheme.standard()
) {
    DataGrid()
}
```

```kotlin
// Boost to expressive for an onboarding flow
CompositionLocalProvider(
    LocalMotionScheme provides MotionScheme.expressive()
) {
    OnboardingPager()
}
```

---

## Choosing the Right Spec

Decision guide for each animation:

```
Is this a movement (translate, scale, size change)?
  → Use spatial spec
  → How fast should it feel?
    → Standard interaction → defaultSpatialSpec()
    → Quick feedback (tap, dismiss) → fastSpatialSpec()
    → Deliberate transition (page, hero) → slowSpatialSpec()

Is this a visual change (color, opacity, blur)?
  → Use effects spec
  → How fast?
    → Standard → defaultEffectsSpec()
    → Quick (flash, flicker) → fastEffectsSpec()
    → Gradual (fade in after load) → slowEffectsSpec()
```

---

## Creating a Custom MotionScheme

Advanced: define a fully custom MotionScheme with your own spring parameters:

```kotlin
val MyMotionScheme = MotionScheme.create(
    defaultSpatialSpec = spring<Any>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessMedium
    ),
    fastSpatialSpec = spring<Any>(
        dampingRatio = 1.0f,
        stiffness = Spring.StiffnessHigh
    ),
    slowSpatialSpec = spring<Any>(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessLow
    ),
    defaultEffectsSpec = tween<Any>(durationMillis = 300),
    fastEffectsSpec = tween<Any>(durationMillis = 150),
    slowEffectsSpec = tween<Any>(durationMillis = 500)
)

MaterialExpressiveTheme(motionScheme = MyMotionScheme) { }
```

---

## MotionScheme vs Raw spring()

| Scenario | Use |
|---|---|
| Standard app animation | `MaterialTheme.motionScheme.defaultSpatialSpec()` |
| Expressive "bounce" feedback | Raw `spring(DampingRatioMediumBouncy, StiffnessMedium)` |
| Fine-tuned custom spring | Raw `spring(dampingRatio, stiffness)` |
| Compatibility with design tokens | `MaterialTheme.motionScheme.*` always |
| Quick prototyping | `tween(300)` is fine but won't match theme |

**Rule of thumb:** Use `MotionScheme` specs by default. Only reach for raw `spring()` when you specifically want a bounce effect that goes beyond what `MotionScheme.expressive()` provides, or when fine-tuning a custom animation.

---

## Motion Tokens in Figma

In the M3 Figma Design Kit, motion is expressed as **motion tokens** that map to MotionScheme specs:

| Figma token | Compose equivalent |
|---|---|
| `M3/Motion/Spatial/Default` | `defaultSpatialSpec()` |
| `M3/Motion/Spatial/Fast` | `fastSpatialSpec()` |
| `M3/Motion/Spatial/Slow` | `slowSpatialSpec()` |
| `M3/Motion/Effects/Default` | `defaultEffectsSpec()` |
| `M3/Motion/Effects/Fast` | `fastEffectsSpec()` |
| `M3/Motion/Effects/Slow` | `slowEffectsSpec()` |

When a designer specifies a motion token, the developer maps it to the corresponding `MotionScheme` call — no negotiation required.
