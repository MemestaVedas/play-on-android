# HCT Color Space

HCT (Hue, Chroma, Tone) is the perceptual color space that powers all of Material Design 3's color science. It is a custom color model developed by Google that combines the best properties of two established color spaces: **CAM16** (for perceptual hue and chroma accuracy) and **L*a*b*** (for its lightness/tone axis).

---

## Why a New Color Space?

Standard color models (RGB, HSL, HSV) are not perceptually uniform — the same numerical change in value produces visually unequal changes in perceived brightness or saturation depending on the hue. This makes it impossible to reliably guarantee contrast ratios by formula alone.

HCT solves this. A change of N tone units in HCT looks approximately the same across all hues. This makes it mathematically possible to:
- Generate accessible color pairs guaranteed to meet WCAG contrast requirements
- Interpolate between colors in a visually smooth way
- Extract a single seed color and derive a full harmonious palette algorithmically

---

## The Three Axes

### Hue (H)
The perceptual color category: red, yellow, green, cyan, blue, magenta. Measured 0–360° on a circular spectrum (0 = 360). In HCT, hue is based on CAM16's hue calculation, which more closely matches human perception than HSL's hue axis.

```
0°   → Red
60°  → Yellow
120° → Green
180° → Cyan
240° → Blue
300° → Magenta
360° → Red (same as 0)
```

### Chroma (C)
The colorfulness/saturation of a color. Unlike HSL saturation, chroma in HCT has no fixed maximum — it varies by hue and tone, reflecting real optical limits. A color at maximum chroma is as vivid as physically achievable for that hue and tone.

- `Chroma 0` → achromatic (gray)
- `Chroma 4` → near-neutral (used for neutral-variant palette)
- `Chroma 16` → muted (used for secondary palette)
- `Chroma 48` → vibrant (used for primary palette)
- `Chroma 84+` → highly saturated (used for tertiary palette in some schemes)

### Tone (T)
Perceptual lightness, 0–100:
- `T = 0` → pure black
- `T = 100` → pure white
- `T = 40` → medium tone (the default "seed tone" for key colors)
- `T = 80` → light container tone
- `T = 10` → dark on-container tone

Tone corresponds closely to L* in CIELAB, making it the key axis for contrast calculations. WCAG contrast ratio between two tones can be approximated from their difference on the T axis.

---

## Tonal Palettes

From a single seed color, HCT generates a **tonal palette** — a set of 13 discrete tones (0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 99, 100) at the same hue and chroma as the seed.

These 13 tones become the foundation for color roles:

| Tone | Light mode usage | Dark mode usage |
|---|---|---|
| T10 | `onPrimaryContainer` | `primaryContainer` |
| T20 | — | `onPrimary` |
| T30 | — | `primary` |
| T40 | `primary` | — |
| T80 | `primaryContainer` | `onPrimaryContainer` |
| T90 | — | — |
| T95 | — | — |
| T99 | `background` | — |

---

## The Five Key Colors

From a single seed color, M3 derives five key colors, each a variation of the original hue at different chroma levels:

| Key Color | Chroma | Purpose |
|---|---|---|
| Primary | ~48 | Main brand color, key actions |
| Secondary | ~16 | Supporting, less prominent elements |
| Tertiary | ~24 | Contrasting accent, balances primary |
| Neutral | ~4 | Backgrounds, surfaces, low-emphasis text |
| Neutral-variant | ~8 | Surface variants, outline, dividers |

Each key color generates its own 13-tone tonal palette. Color roles draw specific tones from these palettes.

---

## Contrast Guarantee

The relationship between tone values and WCAG contrast is approximately:

- **Tone difference ≥ 50** → contrast ratio ≥ 4.5:1 (meets WCAG AA for body text)
- **Tone difference ≥ 30** → contrast ratio ≥ 3:1 (meets WCAG AA for large text)

Every M3 color role pair is pre-computed to satisfy this. For example:
- `primary` (T40 in light) on `background` (T99) → tone diff 59 → passes AA
- `onPrimary` (T100 in light) on `primary` (T40 in light) → tone diff 60 → passes AA

---

## Using HCT in Code

### Android (material-color-utilities)
```kotlin
// Add to build.gradle.kts
implementation("com.google.android.material:material:1.12.0") // includes HCT

// Or direct:
implementation("com.github.material-foundation:material-color-utilities-android:0.3.1")
```

```kotlin
import com.google.material.colorscience.hct.Hct
import com.google.material.colorscience.palettes.TonalPalette

// Create HCT from a seed color hex
val hct = Hct.fromInt(Color.parseColor("#6750A4"))
println("H=${hct.hue}, C=${hct.chroma}, T=${hct.tone}")

// Generate a tonal palette from seed
val palette = TonalPalette.fromInt(Color.parseColor("#6750A4"))
val tone80 = palette.tone(80) // Get the T80 value (ARGB int)
```

### JavaScript (material-color-utilities npm)
```javascript
import { Hct, TonalPalette, argbFromHex, hexFromArgb } from "@material/material-color-utilities";

const seedArgb = argbFromHex("#6750A4");
const hct = Hct.fromInt(seedArgb);

const palette = TonalPalette.fromInt(seedArgb);
const t80Color = hexFromArgb(palette.tone(80)); // "#D0BCFF"
```

---

## Dynamic Color Scheme Generation

The `DynamicScheme` class generates an entire color scheme from a seed:

```kotlin
val hct = Hct.fromInt(argbFromHex("#6750A4"))
val scheme = SchemeTonalSpot(hct, isDark = false, contrastLevel = 0.0)

// Available scheme variants:
// SchemeTonalSpot  — standard, balanced
// SchemeVibrant    — high chroma, vivid
// SchemeExpressive — wild, max color variation
// SchemeFidelity   — matches seed color closely
// SchemeContent    — muted, content-first
// SchemeMonochrome — no color, grayscale
// SchemeNeutral    — near-neutral
// SchemeRainbow    — rotating hues
// SchemeFruitSalad — secondary/tertiary are complementary hues
```

---

## HCT vs. Other Color Spaces

| Property | RGB | HSL | CIELAB | HCT |
|---|---|---|---|---|
| Perceptually uniform | No | No | Mostly | Yes |
| Can guarantee contrast | No | No | Approximately | Yes |
| Constant hue during interpolation | No | No | Mostly | Yes |
| Used in M3 | No | No | Partially | Yes (primary) |
