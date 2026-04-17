# M3 Expressive on XR & Spatial Computing

M3 Expressive extends to **XR (Extended Reality)** — AR headsets, mixed reality, and spatial computing environments. Google announced XR adaptive layouts at Google I/O 2025 as part of the M3 Expressive rollout.

---

## M3 Expressive + XR: The Design Opportunity

XR environments present new opportunities and constraints vs. flat screens:
- **No physical bounds** — UI can exist anywhere in 3D space, not just a rectangular screen
- **Depth** — elements can be at different Z-depths, creating genuine spatial hierarchy
- **Immersive context** — the UI competes with the real or virtual world for attention
- **Interaction model** — gaze, pinch, voice, hand tracking replace touch

M3 Expressive's design principles scale naturally:
- **Shape** becomes even more important — distinctive shapes stand out in mixed reality
- **Spring motion** maps to physical world expectations in XR
- **Size contrast** is critical — elements far away need to be larger
- **Containment** groups related content in spatial panels

---

## Adaptive Layouts for XR

The Compose adaptive layout system (`ListDetailPaneScaffold`, `NavigationSuiteScaffold`) is being extended to XR:

```kotlin
// XR-aware layout (via Jetpack XR Compose, preview/alpha)
implementation("androidx.xr:xr-compose:1.0.0-alpha01")
```

```kotlin
@Composable
fun XrAdaptiveLayout() {
    val windowInfo = currentWindowAdaptiveInfo()

    // XR expanded layout uses spatial panels
    if (windowInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
        && isXrEnvironment()) {
        SpatialPanelLayout()
    } else {
        StandardAdaptiveLayout()
    }
}
```

### Window Size Classes in XR

| Class | Context |
|---|---|
| `COMPACT` | Phone, watch face |
| `MEDIUM` | Tablet, foldable |
| `EXPANDED` | Desktop, large tablet |
| `LARGE` (XR) | Spatial panel at arm's length |
| `EXTRA_LARGE` (XR) | Room-scale environment |

The new `LARGE` and `EXTRA_LARGE` window size classes were added for XR contexts where "window" means a floating spatial panel.

---

## Spatial Panels

In XR, Compose content is rendered on **spatial panels** — virtual rectangular surfaces floating in 3D space:

```kotlin
// Jetpack XR Compose spatial panel
SpatialPanel(
    modifier = Modifier.width(800.dp).height(600.dp),
    depth = 0.5f  // 0.5m away from user
) {
    AppContent()
}
```

Key considerations:
- **Minimum readable size at 1m distance:** ~12sp (maps to larger physical size)
- **Panel size:** 400dp–1200dp wide for arm's-length interaction
- **Depth layers:** Use M3 surface elevation tokens to suggest depth (nearer panels = higher elevation)
- **Shape:** Rounded panel corners (use `ExtraLarge` shape) prevent harsh edges in mixed reality

---

## Navigation in XR

Traditional bottom navigation doesn't work in XR. Recommended patterns:

### Orbital Navigation
Navigation elements orbit around the user's field of view, always accessible:
```kotlin
// Conceptual — implementation via XR SDK
OrbitalNavigation(items = navDestinations)
```

### Spatial NavigationRail
A persistent vertical rail floating to the side of main content:
```kotlin
NavigationRail(modifier = Modifier.fillMaxHeight()) {
    // items
}
```

This is already familiar from tablet layouts and adapts naturally to XR side panels.

### Voice Navigation
XR apps should support voice navigation as a first-class interaction mode. Pair with visual confirmation animations (spring + shape morph on selection).

---

## Motion in XR

Spring physics is critical in XR — elements in space must move according to physical expectations:

- **Spatial spring:** Elements should lag slightly behind head movement (follow with spring)
- **Depth spring:** Content appearing at a new depth springs from the user's gaze point
- **Scale spring:** Elements that come into focus spring-scale to their active size

```kotlin
// Distance-adaptive scaling
val distanceM = 0.8f  // distance in meters
val scaleFactor = 1f + (distanceM - 1f) * 0.5f  // scale with distance

val scale by animateFloatAsState(
    targetValue = scaleFactor,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
)
```

---

## Color in XR

XR introduces new color considerations:
- **Transparency:** Panels often have partially transparent backgrounds to show the real world through them. Use `surface` colors with alpha
- **Environmental lighting:** Real-world lighting affects perceived color. Design with a minimum of 60% chroma reduction headroom
- **Focus/unfocus states:** Active panels use `primary` tinted surfaces; inactive panels dim to `surfaceDim`
- **High contrast mode:** Essential for XR — real-world backgrounds can be highly variable

```kotlin
// Panel with environmental transparency
Surface(
    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
    shape = MaterialTheme.shapes.extraLarge
) {
    /* panel content */
}
```

---

## XR-Specific Shape Usage

In XR, shapes carry additional meaning:
- **Floating action panel** → Pill shape, primary color, spring physics → high prominence
- **Information tooltip** → Rounded shape, surfaceContainerHigh → passive info
- **Warning overlay** → Slightly spikier shape (SoftBurst) + error color → alert

The morphing capability becomes especially powerful in XR:
- A notification bubble morphing from Circle to Cookie9Sided signals it has new information
- A loading spinner morphing through shapes in 3D space creates depth without a progress bar

---

## Status: XR in M3 Expressive (2025–2026)

As of April 2026:
- XR adaptive layout guidelines are published but some APIs remain alpha
- Jetpack XR Compose is in developer preview
- Google's own XR apps (Google Maps XR, YouTube XR) use M3 Expressive color and shape
- Full XR component library: expected with stable Jetpack XR Compose release

**Key resources:**
- Jetpack XR: https://developer.android.com/develop/xr
- Android XR overview: https://developer.android.com/xr
- M3 + XR design guidance: https://m3.material.io/foundations/adaptive-design/overview
