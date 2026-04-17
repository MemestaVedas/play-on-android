# M3 Expressive on Wear OS

M3 Expressive launched on Wear OS alongside the Pixel Watch 4 in August 2025, bringing spring animations, shape morphing, variable fonts, and richer color to round-screen wearables.

---

## Key Principles for Round Screens

Wear OS M3 Expressive is specifically designed for circular displays — it does not simply port the phone design language:

1. **Embrace the round form factor** — use the full circular canvas; don't treat it like a small square
2. **Glanceability over density** — users look at their watch for 1–2 seconds; every screen must communicate its purpose instantly
3. **Edge-to-edge components** — buttons and tiles extend to the screen edge for easier tapping
4. **Spring motion follows the curve** — animations follow the natural circular arc of the display

---

## New Wear OS Components

### Edge-Hugging Button
The signature M3E Wear OS component. A button with a curved bottom edge that perfectly follows the circular display border.

```kotlin
// Wear Compose
implementation("androidx.wear.compose:compose-material3:1.0.0-alpha29")
```

```kotlin
@Composable
fun EdgeHuggingButton(onClick: () -> Unit) {
    EdgeButton(
        onClick = onClick,
        colors = ButtonDefaults.filledButtonColors()
    ) {
        Text("Confirm")
    }
}
```

The button's animation on appearance: it slides in from the bottom and morphs into the curved edge shape — a signature M3E transition.

### TransformingLazyColumn
A scrolling list that follows the display's circular edges — items shrink as they approach the top and bottom bezels:

```kotlin
@Composable
fun TransformingList() {
    val scrollState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = scrollState) {
        TransformingLazyColumn(state = scrollState) {
            items(count = 20) { index ->
                ListItem(
                    modifier = Modifier.transformedHeight(this, scrollState),
                    headlineContent = { Text("Item $index") }
                )
            }
        }
    }
}
```

### ScrollIndicator
Automatically included when using `ScreenScaffold` — shows position in the list as a subtle arc on the right edge of the display.

### 3-Slot Tile PrimaryLayout
A new tile layout designed for at-a-glance information at different watch sizes:

```kotlin
// Tile layout (in Tiles API, not Compose)
PrimaryLayout.Builder(context)
    .setResponsiveContentInsetEnabled(true)
    .setPrimaryLabelContent(Text.Builder(context, "Steps Today").build())
    .setContent(/* main content */)
    .setSecondaryLabelContent(Text.Builder(context, "8,432 / 10,000").build())
    .build()
```

---

## Color System on Wear OS

The Wear OS M3E color system uses **two seed colors** (unlike mobile which uses one):

- **Primary seed** → primary, secondary palettes
- **Tertiary seed** (from watch face) → tertiary palette

Both seeds come from the watch face selection, enabling complete system-wide color harmony between the watch face, tiles, and apps.

```kotlin
// Wear OS dynamic color (from watch face)
val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    dynamicColorScheme(
        context = context,
        isDark = true  // Wear OS is always dark-mode-first
    )
} else {
    defaultColorScheme()
}
```

### Always Dark-First
Unlike mobile, Wear OS UI is **dark-mode primary**. The active light backgrounds of phone M3 are inverted — dark backgrounds preserve battery on OLED displays and are more readable outdoors.

---

## Typography on Wear OS

Wear OS M3E uses a **21-style type scale** (expanded from mobile's 15):

| Group | Styles | Notes |
|---|---|---|
| Display | Large, Medium, Small | Scaled down vs. phone; optical sizing critical |
| Headline | Large, Medium, Small | Primary heading styles |
| Title | Large, Medium, Small | Section headers in apps |
| Body | Large, Medium, Small | Standard content text; all scale with font size settings |
| Label | Large, Medium, Small | Chips, buttons |
| Arc | Large, Medium, Small | Text following circular arc (e.g., watch complications) |
| Numeral | Large, Medium, Small | Numerical displays (time, steps, BPM) — tabular spacing |

### Arc Text
```kotlin
// Curved text following watch display arc
CurvedText(
    text = "12 Steps",
    style = MaterialTheme.typography.arcLarge,
    angularDirection = CurvedDirection.Angular.CounterClockwise
)
```

### Numeral Text
Numeral styles use **tabular mono spacing** to prevent number digits jumping as values change:

```kotlin
Text(
    text = heartRate.toString(),
    style = MaterialTheme.typography.numeralLarge,
    // tabular spacing is built into the style
)
```

---

## Variable Fonts on Wear OS

M3 Expressive enables variable font animations on Wear OS for interactive feedback:

```kotlin
// Animate font weight on step goal completion
val targetWeight = if (goalReached) 700f else 400f
val weight by animateFloatAsState(
    targetValue = targetWeight,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
)

Text(
    text = "$stepCount",
    style = MaterialTheme.typography.numeralLarge.copy(
        fontVariationSettings = FontVariation.Settings(
            FontVariation.weight(weight)
        )
    )
)
```

---

## Spring Motion on Wear OS

The same spring physics system from mobile applies to Wear OS. Key adaptations:
- Animations must complete quickly — 300ms is the practical maximum for wearable interactions
- Use `fastSpatialSpec()` more often than on mobile
- Shape morphs for PIN entry: digits expand/spring when tapped
- Media controls: play/pause button morphs between triangle and double-bar

```kotlin
// Pin entry button spring
val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.9f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
)
```

---

## Tiles (Glanceable Surfaces)

Wear OS tiles are not Compose — they use the Tiles API. M3 Expressive for tiles:

- Dynamic color from watch face is applied automatically to opted-in tiles
- Use `ResponsiveLayout` for consistent sizing across watch sizes
- Tile templates: progress tile, media tile, status tile, action tile

```kotlin
// Opt in to dynamic color for tiles
TileService.requestUpdate(context)
// In onTileRequest, return a Tile with dynamic color enabled
Tile.Builder()
    .setResourcesVersion("1")
    .setTileTimeline(
        TimelineBuilders.Timeline.fromLayoutElement(layout)
    )
    // Dynamic color flag
    .build()
```

---

## Battery Life Considerations

M3E on Wear OS is designed to be battery-neutral:
- Spring animations are GPU-accelerated and efficient
- OLED dark backgrounds reduce pixel power vs. light themes
- `LoadingIndicator` shape morphing is optimized to not exceed normal animation CPU budget
- Google reports "up to 10% battery improvement" from internal Wear OS 6 optimizations

Key developer guidance:
- Avoid looping animations when the watch display is ambient/off
- Stop animations in `onAmbientModeChanged` callbacks
- Use `awake/non-ambient` state to gate heavy animations

---

## Resources

- Wear OS M3E design guidance: https://developer.android.com/design/ui/wear/guides/get-started/apply
- Wear Compose Material3: https://developer.android.com/develop/ui/compose/wear
- Wear OS blog (August 2025): https://android-developers.googleblog.com/2025/08/introducing-material-3-expressive-for-wear-os.html
