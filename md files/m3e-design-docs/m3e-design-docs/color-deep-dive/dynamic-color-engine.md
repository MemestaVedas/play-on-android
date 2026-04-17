# Dynamic Color Engine

Dynamic Color is the system by which M3 derives a full color scheme automatically from a **seed color** — either extracted from the user's wallpaper (Android 12+) or provided by the app as a brand color.

---

## Seed Color Sources

### 1. Wallpaper Extraction (Android 12+ / API 31+)
Android extracts a prominent color from the user's wallpaper and uses it as the seed. Apps that opt into dynamic color automatically receive a scheme that harmonizes with the user's personal choice.

```kotlin
// Compose — automatic wallpaper-based color
val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val context = LocalContext.current
    if (darkTheme) dynamicDarkColorScheme(context)
    else dynamicLightColorScheme(context)
} else {
    if (darkTheme) DarkColorScheme else LightColorScheme
}
```

### 2. Brand Seed Color (Static)
Apps can provide their own seed color. The Material Theme Builder generates a static `ColorScheme` from any hex input.

```kotlin
// Generated static scheme (from Material Theme Builder export)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF476810),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC7F089),
    // ... all roles pre-computed by algorithm
)
```

### 3. In-App Content Extraction
Some apps extract seed colors from in-app content (album covers, hero images) to create an immersive, content-matched theme. This is common in music and photo apps.

---

## The Seed-to-Scheme Algorithm

1. **Seed → HCT** — Convert seed color to HCT values
2. **HCT → 5 key colors** — Derive primary, secondary, tertiary, neutral, neutral-variant at standardized chroma values for the chosen scheme variant
3. **Key colors → 5 tonal palettes** — Each key color generates 13 tones (0–100)
4. **Tonal palettes → color roles** — Specific tones from each palette are slotted into the ~30 named color roles
5. **Color roles → scheme** — Light and dark variants generated (different tone assignments)

---

## Scheme Variants

M3 supports 9 named scheme variants, each altering how secondary and tertiary hues relate to the primary seed:

| Variant | Description | Use Case |
|---|---|---|
| `TonalSpot` | Standard; secondary = muted primary; tertiary = rotated ~60° | Default for most apps |
| `Vibrant` | High chroma throughout; bold, vivid | Entertainment, games |
| `Expressive` | Max color separation; tertiary is highly contrasting | Brand-forward, creative |
| `Fidelity` | Secondary/tertiary stay close to seed color | Brand color preservation |
| `Content` | Muted; scheme matches content's natural palette | Media, photo, news |
| `Monochrome` | No chroma; pure grayscale | Minimal, accessibility-focused |
| `Neutral` | Near-neutral; very low chroma | Productivity, enterprise |
| `Rainbow` | Tertiary hue rotated ~120° from primary | Creative, high color variety |
| `FruitSalad` | Secondary and tertiary are complementary hues | Fun, playful apps |

### In Compose (via m3color library)
```kotlin
// Using Kyant0/m3color for scheme variants beyond standard M3
implementation("com.github.Kyant0:m3color:2025.4")
```

```kotlin
import com.github.kyant0.m3color.dynamiccolor.MaterialDynamicColors
import com.github.kyant0.m3color.scheme.*

val hct = Hct.fromInt(argbFromHex("#6750A4"))
val scheme = when (selectedStyle) {
    PaletteStyle.Expressive  -> SchemeExpressive(hct, isDark, contrastLevel = 0.0)
    PaletteStyle.FruitSalad  -> SchemeFruitSalad(hct, isDark, contrastLevel = 0.0)
    PaletteStyle.Rainbow     -> SchemeRainbow(hct, isDark, contrastLevel = 0.0)
    PaletteStyle.Vibrant     -> SchemeVibrant(hct, isDark, contrastLevel = 0.0)
    else                     -> SchemeTonalSpot(hct, isDark, contrastLevel = 0.0)
}
```

---

## Contrast Level

The `contrastLevel` parameter shifts all tones to increase or decrease WCAG contrast:

| Level | Value | Effect |
|---|---|---|
| Standard | `0.0` | Default M3 contrast (meets WCAG AA) |
| Medium | `0.5` | Slightly higher contrast |
| High | `1.0` | Maximum contrast (approaches WCAG AAA) |
| Reduced | `-1.0` | Lower contrast (never recommended) |

```kotlin
val highContrastScheme = SchemeTonalSpot(hct, isDark = false, contrastLevel = 1.0)
```

---

## Light vs. Dark Scheme Tone Mapping

The same color roles use different tone values in light vs. dark mode:

| Role | Light mode tone | Dark mode tone | From palette |
|---|---|---|---|
| `primary` | T40 | T80 | Primary |
| `onPrimary` | T100 | T20 | Primary |
| `primaryContainer` | T90 | T30 | Primary |
| `onPrimaryContainer` | T10 | T90 | Primary |
| `secondary` | T40 | T80 | Secondary |
| `surface` | T98 | T6 | Neutral |
| `onSurface` | T10 | T90 | Neutral |
| `background` | T98 | T6 | Neutral |
| `outline` | T50 | T60 | Neutral-variant |
| `error` | T40 | T80 | Error (fixed red) |

---

## Extended Color Roles in M3 Expressive

M3 Expressive enriches the color system with additional surface roles introduced for better container/hierarchy support:

| Role | Description |
|---|---|
| `surfaceDim` | Darker surface variant (shadows, overlays) |
| `surfaceBright` | Lighter surface variant |
| `surfaceContainerLowest` | Faintest container color |
| `surfaceContainerLow` | Light container (cards on background) |
| `surfaceContainer` | Standard container |
| `surfaceContainerHigh` | More visible container |
| `surfaceContainerHighest` | Strongest container (emphasized surfaces) |

These graduated surface containers give designers a fine-grained hierarchy of surface depth without requiring custom colors.

```kotlin
// Using graduated surfaces in Compose
Card(colors = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
)) { /* ... */ }

Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest) {
    // Most prominent surface
}
```
