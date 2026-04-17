# Material Color Utilities Library

`material-color-utilities` is Google's open-source library implementing the HCT color space and M3 dynamic color algorithm. It is the engine behind Material Theme Builder and dynamic color on Android.

**GitHub:** https://github.com/material-foundation/material-color-utilities  
**npm:** `@material/material-color-utilities`

---

## Available Implementations

| Platform | Package / Import |
|---|---|
| JavaScript / TypeScript | `@material/material-color-utilities` |
| Dart / Flutter | `material_color_utilities` (pub.dev) |
| Java / Kotlin (Android) | Bundled in `com.google.android.material:material` |
| Python | `materialyoucolor` (community port) |
| Swift (iOS) | Community ports available |

---

## Core Modules

### HCT
The fundamental color type — convert from/to ARGB:

```typescript
import { Hct, argbFromHex, hexFromArgb } from "@material/material-color-utilities";

const seedArgb = argbFromHex("#6750A4");
const hct = Hct.fromInt(seedArgb);

console.log(hct.hue);     // e.g., 270.4
console.log(hct.chroma);  // e.g., 48.3
console.log(hct.tone);    // e.g., 40.5

// Round-trip
const backToArgb = hct.toInt();
const hex = hexFromArgb(backToArgb); // "#6750A4"
```

### TonalPalette
Generate the 13-tone palette from a seed:

```typescript
import { TonalPalette, argbFromHex, hexFromArgb } from "@material/material-color-utilities";

const palette = TonalPalette.fromInt(argbFromHex("#6750A4"));

// Get specific tones
const t10 = hexFromArgb(palette.tone(10));   // very dark purple
const t40 = hexFromArgb(palette.tone(40));   // primary (light mode)
const t80 = hexFromArgb(palette.tone(80));   // primary (dark mode)
const t90 = hexFromArgb(palette.tone(90));   // primaryContainer (light mode)
```

### DynamicScheme + MaterialDynamicColors
Generate a complete color role map:

```typescript
import {
    Hct, SchemeTonalSpot, MaterialDynamicColors,
    argbFromHex, hexFromArgb
} from "@material/material-color-utilities";

const hct = Hct.fromInt(argbFromHex("#6750A4"));

// Light mode, standard contrast
const lightScheme = new SchemeTonalSpot(hct, false, 0.0);
// Dark mode, standard contrast
const darkScheme = new SchemeTonalSpot(hct, true, 0.0);

// Extract color roles
const primary = hexFromArgb(MaterialDynamicColors.primary.getArgb(lightScheme));
const onPrimary = hexFromArgb(MaterialDynamicColors.onPrimary.getArgb(lightScheme));
const primaryContainer = hexFromArgb(MaterialDynamicColors.primaryContainer.getArgb(lightScheme));
const secondary = hexFromArgb(MaterialDynamicColors.secondary.getArgb(lightScheme));
const surface = hexFromArgb(MaterialDynamicColors.surface.getArgb(lightScheme));
const surfaceContainerLow = hexFromArgb(MaterialDynamicColors.surfaceContainerLow.getArgb(lightScheme));
```

### Available Color Role Keys in MaterialDynamicColors

```typescript
MaterialDynamicColors.primary
MaterialDynamicColors.onPrimary
MaterialDynamicColors.primaryContainer
MaterialDynamicColors.onPrimaryContainer
MaterialDynamicColors.primaryFixed
MaterialDynamicColors.primaryFixedDim
MaterialDynamicColors.onPrimaryFixed
MaterialDynamicColors.onPrimaryFixedVariant
MaterialDynamicColors.secondary
MaterialDynamicColors.onSecondary
MaterialDynamicColors.secondaryContainer
MaterialDynamicColors.onSecondaryContainer
MaterialDynamicColors.secondaryFixed
MaterialDynamicColors.secondaryFixedDim
MaterialDynamicColors.onSecondaryFixed
MaterialDynamicColors.onSecondaryFixedVariant
MaterialDynamicColors.tertiary
MaterialDynamicColors.onTertiary
MaterialDynamicColors.tertiaryContainer
MaterialDynamicColors.onTertiaryContainer
MaterialDynamicColors.tertiaryFixed
MaterialDynamicColors.tertiaryFixedDim
MaterialDynamicColors.onTertiaryFixed
MaterialDynamicColors.onTertiaryFixedVariant
MaterialDynamicColors.error
MaterialDynamicColors.onError
MaterialDynamicColors.errorContainer
MaterialDynamicColors.onErrorContainer
MaterialDynamicColors.background
MaterialDynamicColors.onBackground
MaterialDynamicColors.surface
MaterialDynamicColors.onSurface
MaterialDynamicColors.surfaceVariant
MaterialDynamicColors.onSurfaceVariant
MaterialDynamicColors.inverseSurface
MaterialDynamicColors.inverseOnSurface
MaterialDynamicColors.inversePrimary
MaterialDynamicColors.outline
MaterialDynamicColors.outlineVariant
MaterialDynamicColors.shadow
MaterialDynamicColors.scrim
MaterialDynamicColors.surfaceDim
MaterialDynamicColors.surfaceBright
MaterialDynamicColors.surfaceContainerLowest
MaterialDynamicColors.surfaceContainerLow
MaterialDynamicColors.surfaceContainer
MaterialDynamicColors.surfaceContainerHigh
MaterialDynamicColors.surfaceContainerHighest
```

---

## Generating All Scheme Variants (JavaScript)

```typescript
import {
    Hct, argbFromHex,
    SchemeTonalSpot, SchemeVibrant, SchemeExpressive,
    SchemeFidelity, SchemeContent, SchemeMonochrome,
    SchemeNeutral, SchemeRainbow, SchemeFruitSalad
} from "@material/material-color-utilities";

const hct = Hct.fromInt(argbFromHex("#6750A4"));
const isDark = false;
const contrast = 0.0; // 0.0 = standard, 0.5 = medium, 1.0 = high

const schemes = {
    tonalSpot:   new SchemeTonalSpot(hct, isDark, contrast),
    vibrant:     new SchemeVibrant(hct, isDark, contrast),
    expressive:  new SchemeExpressive(hct, isDark, contrast),
    fidelity:    new SchemeFidelity(hct, isDark, contrast),
    content:     new SchemeContent(hct, isDark, contrast),
    monochrome:  new SchemeMonochrome(hct, isDark, contrast),
    neutral:     new SchemeNeutral(hct, isDark, contrast),
    rainbow:     new SchemeRainbow(hct, isDark, contrast),
    fruitSalad:  new SchemeFruitSalad(hct, isDark, contrast),
};
```

---

## Color Harmony: Blending Arbitrary Colors

The library includes a `Blend` module to harmonize arbitrary brand colors with a seed:

```typescript
import { Blend, argbFromHex, hexFromArgb } from "@material/material-color-utilities";

const brandColor = argbFromHex("#FF6B00");  // orange brand color
const seedColor = argbFromHex("#6750A4");   // purple seed

// Shift brand color's hue toward the seed, preserving chroma and tone
const harmonized = Blend.harmonize(brandColor, seedColor);
console.log(hexFromArgb(harmonized)); // slightly shifted orange, more compatible
```

This is how M3's "harmonize" feature works for third-party library icons and fixed brand elements — shifting them just enough to feel cohesive without losing their identity.

---

## Quantize + Score: Extracting Colors from Images

```typescript
import {
    QuantizerCelebi, Score, argbFromRgb, hexFromArgb
} from "@material/material-color-utilities";

// imagePixels: number[] — array of ARGB pixel values from image
const result = QuantizerCelebi.quantize(imagePixels, 128);
const ranked = Score.score(result);

// ranked[0] is the most suitable seed color for M3
const seedColor = hexFromArgb(ranked[0]);
console.log(`Best seed: ${seedColor}`);
```

This is the algorithm used by Android's wallpaper color extraction on API 31+.

---

## Kotlin / Android Usage

On Android, the HCT utilities are bundled within `com.google.android.material:material`:

```kotlin
import com.google.material.colorscience.hct.Hct
import com.google.material.colorscience.palettes.TonalPalette
import com.google.material.colorscience.quantize.QuantizerCelebi
import com.google.material.colorscience.score.Score

// Extract from bitmap
val pixels = IntArray(bitmap.width * bitmap.height)
bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

val quantized = QuantizerCelebi.quantize(pixels, 128)
val scored = Score.score(quantized)
val seedArgb = scored[0]

val hct = Hct.fromInt(seedArgb)
val palette = TonalPalette.fromHct(hct)
val t80 = palette.tone(80)
```

For advanced scheme variants beyond standard M3, use the community library:
```kotlin
implementation("com.github.Kyant0:m3color:2025.4")
```
