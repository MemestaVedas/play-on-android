# Motion Scheme

The `MotionScheme` is a first-class M3 theme subsystem, stable as of **material3 1.4.0**. It provides spring-based animation specs that give UI a physical, alive feel consistent with M3 Expressive.

---

## Accessing the Motion Scheme

```kotlin
val motionScheme = MaterialTheme.motionScheme
```

---

## Spec Types

### Spatial Specs (for position, size, bounds changes)

Use for animations where an element moves or changes size in space.

| Spec | Description |
|---|---|
| `defaultSpatialSpec()` | Standard spatial animation — most common |
| `fastSpatialSpec()` | Quick spatial movement (drawer dismiss, etc.) |
| `slowSpatialSpec()` | Deliberate, emphasized movement |

```kotlin
val sizeAnimation = MaterialTheme.motionScheme.defaultSpatialSpec<Dp>()

val size by animateDpAsState(
    targetValue = if (expanded) 200.dp else 100.dp,
    animationSpec = sizeAnimation
)
```

### Effects Specs (for color, opacity, non-spatial changes)

Use for fade-ins, color transitions, opacity changes.

| Spec | Description |
|---|---|
| `defaultEffectsSpec()` | Standard effects animation |
| `fastEffectsSpec()` | Quick fade / color change |
| `slowEffectsSpec()` | Gradual fade |

```kotlin
val alpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
)
```

---

## Spring Physics in M3 Expressive

M3 Expressive uses **spring-based physics** rather than fixed duration+easing curves for spatial animations. Springs:

- Feel physically natural — elements have mass and momentum
- Self-interrupt cleanly (reversing mid-animation looks right)
- Automatically tune to feel appropriate for the element's visual weight

All the new expressive components (ButtonGroup interactions, SplitButton morph, LoadingIndicator) use spring specs internally.

```kotlin
// Custom spring spec (advanced)
val mySpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

// Or use the themed spec (preferred — stays consistent with theme)
val mySpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
```

---

## Animate Bounds (M3 Expressive / Compose 1.7+)

`AnimateBounds` modifier animates a composable's position and size automatically when its layout changes, using the MotionScheme's spatial spec:

```kotlin
LookaheadScope {
    Row {
        Box(
            modifier = Modifier
                .animateBounds(this@LookaheadScope)
                .weight(if (selected) 2f else 1f)
        )
    }
}
```

---

## Shared Element Transitions

Compose supports shared element transitions for screen-to-screen animations, using motion scheme specs:

```kotlin
// In the source screen
Box(
    modifier = Modifier.sharedElement(
        state = rememberSharedContentState(key = "hero-image"),
        animatedVisibilityScope = this
    )
)

// In the destination screen — same key
AsyncImage(
    modifier = Modifier.sharedElement(
        state = rememberSharedContentState(key = "hero-image"),
        animatedVisibilityScope = this
    )
)
```

---

## MotionScheme Graduation

As of material3 `1.4.0`, `motionScheme` graduated from experimental to **stable**. You no longer need `@OptIn(ExperimentalMaterial3Api::class)` to use `MaterialTheme.motionScheme` or the standard specs.
