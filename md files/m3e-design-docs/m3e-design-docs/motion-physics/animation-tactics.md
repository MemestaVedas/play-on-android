# Animation Tactics

These are the design tactics for applying motion effectively in M3 Expressive — when to animate, what to animate, and how much.

---

## The Purpose-Driven Motion Rule

Every animation in M3 Expressive should serve at least one of these functions:

1. **Orientation** — helps users understand where they are in space (enter/exit, page transitions)
2. **Feedback** — confirms that an action was received (button press, form submit)
3. **State communication** — signals that something changed (selected, loading, completed)
4. **Attention direction** — draws the eye to what changed or what to do next

If an animation doesn't serve any of these, remove it. Purposeless animation increases cognitive load without benefit.

---

## The Five Animation Tactics in M3 Expressive

### 1. Element-Level Spring Response
Small, immediate spring responses to touch. The element reacts the instant the finger makes contact, before the action completes.

**Examples:** Button scale-down on press, list item lift on long-press, toggle thumb spring.

```kotlin
val scale by animateFloatAsState(
    targetValue = if (pressed) 0.96f else 1f,
    animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
)
```

**Design rule:** Keep scale change subtle — 0.94–0.97 range. Going below 0.9 looks broken.

---

### 2. Container Expansion
Content areas expand and contract to reveal or hide related content. The animation communicates the spatial relationship — the content was always "there", just hidden.

**Examples:** Expandable card, accordion, bottom sheet expanding.

```kotlin
AnimatedVisibility(
    visible = expanded,
    enter = expandVertically(
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
    ) + fadeIn(
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
    ),
    exit = shrinkVertically(
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
    ) + fadeOut(
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
    )
) {
    ExpandedContent()
}
```

**Design rule:** Expansion direction should match spatial metaphor. Content from below → expand vertically. Sidebar → expand horizontally.

---

### 3. Positional Shift (Shared Element)
Elements that exist on both the origin and destination screen are animated between those positions, preserving identity and communicating navigation direction.

**Examples:** Hero image from list to detail, FAB to full-screen dialog, card to expanded view.

```kotlin
SharedTransitionLayout {
    AnimatedContent(targetState = isExpanded) { expanded ->
        if (!expanded) {
            Box(
                modifier = Modifier.sharedElement(
                    state = rememberSharedContentState(key = "hero"),
                    animatedVisibilityScope = this@AnimatedContent
                )
            )
        } else {
            Column(
                modifier = Modifier.sharedElement(
                    state = rememberSharedContentState(key = "hero"),
                    animatedVisibilityScope = this@AnimatedContent
                )
            ) { /* expanded view */ }
        }
    }
}
```

**Design rule:** Only use shared element transitions for content that is truly the same entity (same image, same card). Never fake it with visually similar but semantically different elements.

---

### 4. Shape Morph on State Change
See `shape-morphing-design.md`. In summary: morph the shape of an interactive element in response to selection, activation, or completion.

**Design rule:** Morph direction should feel "positive" for success (→ rounder, softer, more petals) and "alert" for warnings (→ spikier, but never destructive).

---

### 5. Stagger on List Entry
When a list of items enters the screen, each item enters with a slight delay from the previous one. This creates a cascade effect that makes the collection feel structured and deliberate.

```kotlin
@Composable
fun StaggeredList(items: List<String>) {
    LazyColumn {
        itemsIndexed(items) { index, item ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(index * 40L)  // 40ms stagger per item
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                        slideInVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) { it / 4 }
            ) {
                ListItem(headlineContent = { Text(item) })
            }
        }
    }
}
```

**Design rule:** Max stagger delay between items: 60ms. Total stagger duration (first to last item): ≤ 400ms. More than this becomes tedious.

---

## Common Animation Patterns

### Page Transition (Compose Navigation)
```kotlin
// With Compose Navigation + Animation
NavHost(
    navController = navController,
    enterTransition = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
        )
    },
    exitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
        )
    }
) { /* destinations */ }
```

### FAB → Action (Collapse to Expand)
```kotlin
val fabSize by animateDpAsState(
    targetValue = if (showMenu) 120.dp else 56.dp,
    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
)
val fabAlpha by animateFloatAsState(
    targetValue = if (showMenu) 0f else 1f,
    animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
)
```

### Cross-Fade Content
```kotlin
AnimatedContent(
    targetState = selectedTab,
    transitionSpec = {
        fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) togetherWith
        fadeOut(MaterialTheme.motionScheme.fastEffectsSpec())
    }
) { tab ->
    TabContent(tab)
}
```

---

## Animation Anti-Patterns

| Anti-pattern | Why it's wrong | Fix |
|---|---|---|
| Animating everything on every frame | Creates noise, burns CPU/battery | Animate only on state changes |
| Using `tween(300)` everywhere | Inconsistent feel, ignores MotionScheme | Use `MotionScheme.*Spec()` |
| Entry + exit animations in opposite directions | Confusing spatial metaphor | Always match animation direction to spatial meaning |
| Zero-duration "animation" | Not an animation — just a state change | Remove the `animateAsState` wrapper entirely |
| Stagger delay > 80ms per item | Feels slow, tedious | Keep stagger ≤ 60ms |
| Animating color without effects spec | Jarring color jump | Use `defaultEffectsSpec()` for all color/alpha |
| Non-interruptible animations | Feels locked/broken during fast interaction | Spring animations are always interruptible by default |

---

## Motion Accessibility Checklist

- [ ] All animations respect `LocalReduceMotion.current`
- [ ] No animation-only feedback (always pair with color or text change)
- [ ] No animations below 100ms duration equivalent (too fast for perception)
- [ ] Looping animations (like `LoadingIndicator`) stop when off-screen
- [ ] Shared element transitions complete in ≤ 400ms
- [ ] No parallax or large-displacement scrolling effects without reduce-motion fallback
