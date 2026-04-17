# Shape Morphing Design Principles

Shape morphing is one of the defining visual signatures of M3 Expressive. When a shape transitions from one polygon to another in response to user interaction or state change, it communicates meaning — not just aesthetics.

---

## What Shape Morphing Communicates

| Morph direction | Semantic meaning |
|---|---|
| Circle → RoundedSquare | Expanding / becoming more structured (FAB → dialog) |
| RoundedSquare → Circle | Collapsing / becoming more focused |
| Neutral → Star/Burst | Activation, excitement, special event |
| Pill → Cookie/Sunny | Celebrating, completing, delightful feedback |
| Sharp → Rounded | Softening, calming, confirming |
| Rounded → Sharp | Warning, alerting, drawing attention |

The goal: users subconsciously read shape changes as state changes. They don't need to read a label or check a color — the shape itself tells them something changed.

---

## The Morph API in Compose

Shape morphing uses the `Morph` class from the `graphics-shapes` library, plus animation APIs.

### Dependencies
```kotlin
implementation("androidx.graphics:graphics-shapes:1.0.1")
// Already included transitively via material3 1.4.0+
```

### Basic Morph Setup
```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphingButton(isActive: Boolean, onClick: () -> Unit) {
    val progress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "shape morph"
    )

    // Morph between two RoundedPolygon shapes
    val shape = remember { 
        Morph(
            start = MaterialShapes.Circle.normalized(),
            end = MaterialShapes.Cookie9Sided.normalized()
        )
    }
    val morphShape = remember(progress) {
        shape.toComposePath(progress = progress)
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(morphShape.toShape())  // custom shape from morph
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
    }
}
```

### Using MorphShape Composable
A higher-level API for simpler morph scenarios:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SimpleMorphShape(toggled: Boolean) {
    val progress by animateFloatAsState(
        targetValue = if (toggled) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    val morphShape = rememberMorphShape(
        startShape = MaterialShapes.RoundedSquare,
        endShape = MaterialShapes.Sunny,
        progress = progress
    )

    Surface(
        modifier = Modifier.size(80.dp),
        shape = morphShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {}
}
```

---

## Using Morph in the Androidify Pattern

The Androidify sample app demonstrates advanced morph + shared element transitions:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.sharedBoundsRevealWithShapeMorph(
    sharedContentState: SharedTransitionScope.SharedContentState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    startShape: RoundedPolygon = MaterialShapes.Circle,
    endShape: RoundedPolygon = MaterialShapes.RoundedSquare
): Modifier {
    val morph = remember { Morph(startShape.normalized(), endShape.normalized()) }
    return this.then(
        with(sharedTransitionScope) {
            sharedBounds(
                sharedContentState = sharedContentState,
                animatedVisibilityScope = animatedVisibilityScope,
                clipInOverlayDuringTransition = OverlayClip(
                    // morph-based clip during shared element
                    MorphBasedShape(morph)
                )
            )
        }
    )
}
```

---

## The Button Group Morph Behavior

`ButtonGroup` uses built-in shape morphing. When a button in the group is pressed:
1. The pressed button expands (scale + shape)
2. Adjacent buttons compress (scale + shape change)
3. The group's outer container subtly reshapes to accommodate

This is handled automatically — no manual morph code needed when using `ButtonGroup`.

---

## SplitButton Trailing Button Morph

When the trailing (dropdown) button is tapped, it:
1. Rotates its chevron icon 180°
2. Morphs its shape from a half-pill (right side) into a more circular form
3. This signals "open/expanded" without any text

All automatic via `SplitButtonDefaults.TrailingButton(checked = expanded)`.

---

## Shape Morph for Loading States

The `LoadingIndicator` cycles through a preset morph sequence automatically. For custom loading sequences:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomMorphLoadingIndicator() {
    // Custom polygon sequence
    LoadingIndicator(
        polygons = listOf(
            MaterialShapes.Circle,
            MaterialShapes.Sunny,
            MaterialShapes.Burst,
            MaterialShapes.SoftBurst,
            MaterialShapes.Clover4Leaf,
            MaterialShapes.Circle
        ),
        color = MaterialTheme.colorScheme.tertiary
    )
}
```

---

## Design Rules for Shape Morphing

**When to morph:**
- State change (inactive → active, unselected → selected, collapsed → expanded)
- Loading / processing (continuous morphing indicates pending state)
- Completion / celebration (morph to a "happy" shape like Sunny or Flower)
- Navigation transition (morph as part of a shared element)

**When NOT to morph:**
- Purely decorative animations with no state change
- On every frame / every scroll tick (too much noise)
- For destructive/error states — use color change instead; morphing implies transformation, not danger

**Speed guidelines:**
- Interaction response morph: use `defaultSpatialSpec()` (spring, fast)
- Celebration morph: use `slowSpatialSpec()` (spring, lingering)
- Loading morph: use `tween(600)` or similar sustained duration

**Morph range:**
- Avoid extreme shape changes (circle → pixelTriangle) for state feedback — they are too jarring
- Prefer subtle morphs (circle → softBurst, pill → roundedSquare) for interaction
- Reserve dramatic morphs (circle → star4pointed) for special moments (purchase complete, achievement unlocked)
