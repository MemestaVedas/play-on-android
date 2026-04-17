# Spring Physics System

M3 Expressive replaces duration-based easing curves with a **spring physics model** for all spatial animations. Springs create motion that feels physically natural — elements have mass, momentum, and elasticity, just like objects in the real world.

---

## Why Springs Instead of Durations

**Duration-based animation (old approach):**
- Fixed duration + easing curve
- If interrupted mid-animation (e.g., user reverses direction), motion looks unnatural — it either snaps or restarts
- Feels mechanical and predetermined

**Spring-based animation (M3 Expressive):**
- Physics simulation with stiffness and damping
- Self-interrupting: if the target changes mid-flight, the spring naturally redirects with momentum preserved
- Feels alive and responsive because it mimics real elastic behavior

The difference is most visible when a user reverses an animation before it completes (e.g., drag-to-dismiss that the user lets go early). Spring physics handles this gracefully; duration physics does not.

---

## The Two Key Spring Parameters

### Stiffness
Controls how "tight" or "loose" the spring is — directly affects speed and bounce character.

| Stiffness constant | Value | Feel |
|---|---|---|
| `Spring.StiffnessHigh` | 10,000 | Very snappy, almost instant |
| `Spring.StiffnessMediumDampingRatio` | 4,000 | Fast, slight bounce |
| `Spring.StiffnessMedium` | 1,500 | Default, balanced |
| `Spring.StiffnessMediumLow` | 400 | Relaxed, more travel |
| `Spring.StiffnessLow` | 200 | Slow, deliberate |
| `Spring.StiffnessVeryLow` | 50 | Very slow, very bouncy |

### Damping Ratio
Controls how much the spring oscillates / bounces after reaching its target.

| Damping constant | Value | Feel |
|---|---|---|
| `Spring.DampingRatioNoBouncy` | 1.0 | No bounce, smooth stop (critically damped) |
| `Spring.DampingRatioLowBouncy` | 0.75 | Subtle bounce |
| `Spring.DampingRatioMediumBouncy` | 0.5 | Clear bounce, playful |
| `Spring.DampingRatioHighBouncy` | 0.2 | Very bouncy, significant overshoot |

---

## MotionScheme Spec Mapping

The `MotionScheme` abstracts spring params into semantically named specs:

| Spec | Stiffness | Damping | Use case |
|---|---|---|---|
| `defaultSpatialSpec()` | Medium | NoBouncy | Most transitions, layout changes |
| `fastSpatialSpec()` | High | NoBouncy | Quick responses (dismiss, tap feedback) |
| `slowSpatialSpec()` | Low | NoBouncy | Deliberate, emphasized moves (hero transitions) |
| `defaultEffectsSpec()` | Medium | NoBouncy | Color/opacity fades |
| `fastEffectsSpec()` | High | NoBouncy | Quick fade-in/out |
| `slowEffectsSpec()` | Low | NoBouncy | Gradual appearance/disappearance |

For **expressive bouncy behavior**, use raw `spring()` with `DampingRatioMediumBouncy`:

```kotlin
// Expressive bounce (not in MotionScheme — intentionally raw)
val bouncySpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)
```

---

## MotionScheme.expressive() vs MotionScheme.standard()

M3 Expressive introduces two named `MotionScheme` presets:

```kotlin
// In Theme.kt
MaterialExpressiveTheme(
    motionScheme = MotionScheme.expressive(),  // bouncy, playful, alive
    // OR
    motionScheme = MotionScheme.standard(),    // smooth, professional, no bounce
    ...
)
```

| | `MotionScheme.expressive()` | `MotionScheme.standard()` |
|---|---|---|
| Spatial animations | Spring with slight overshoot | Critically damped spring |
| Effects animations | Spring | Spring |
| Feel | Playful, alive, personality | Calm, precise, professional |
| Recommended for | Consumer apps, media, games | Productivity, enterprise, medical |

You can also override the motion scheme for a subtree:

```kotlin
// Override to standard for a dense data table within an otherwise expressive app
CompositionLocalProvider(
    LocalMotionScheme provides MotionScheme.standard()
) {
    DataTable()
}
```

---

## Physics in Practice

### Translate animation
```kotlin
val offsetX by animateFloatAsState(
    targetValue = if (swiped) -screenWidth else 0f,
    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
    label = "swipe offset"
)
Box(modifier = Modifier.offset { IntOffset(offsetX.roundToInt(), 0) }) { }
```

### Scale animation with bounce
```kotlin
val scale by animateFloatAsState(
    targetValue = if (selected) 1.1f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    label = "selection scale"
)
Box(modifier = Modifier.scale(scale)) { }
```

### Size animation
```kotlin
val size by animateDpAsState(
    targetValue = if (expanded) 200.dp else 56.dp,
    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
    label = "container size"
)
Box(modifier = Modifier.size(size)) { }
```

### Alpha (effects spec, not spatial)
```kotlin
val alpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
    label = "visibility alpha"
)
Box(modifier = Modifier.alpha(alpha)) { }
```

---

## AnimateBounds Modifier

`animateBounds` auto-animates a composable's position and size when its layout changes. Uses `defaultSpatialSpec` internally.

```kotlin
@Composable
fun ExpandingCard(expanded: Boolean) {
    LookaheadScope {
        Card(
            modifier = Modifier
                .animateBounds(this@LookaheadScope)
                .fillMaxWidth()
                .height(if (expanded) 200.dp else 80.dp)
        ) { }
    }
}
```

---

## Custom Indication (Scale Ripple)

The Androidify sample replaces the default ripple with a physics-based scale indication:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class ScaleIndicationNodeFactory(
    private val animationSpec: AnimationSpec<Float>
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return ScaleIndicationNode(interactionSource, animationSpec)
    }
}

// Usage
val animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
Box(
    modifier = Modifier
        .indication(interactionSource, ScaleIndicationNodeFactory(animationSpec))
        .clickable(interactionSource = interactionSource, indication = null) { }
)
```

---

## Reduce Motion Accessibility

Always respect the system's reduce motion setting:

```kotlin
@Composable
fun AccessibleAnimation(expanded: Boolean): Modifier {
    val reduceMotion = LocalReduceMotion.current  // from Compose Ui 1.7+

    return if (reduceMotion) {
        // No animation — instant state change
        Modifier
    } else {
        val size by animateDpAsState(
            targetValue = if (expanded) 200.dp else 80.dp,
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
        )
        Modifier.height(size)
    }
}
```

On Android, this reads the system "Remove Animations" accessibility setting.
