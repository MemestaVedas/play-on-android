# Figma Design Kit & Tools Ecosystem

M3 Expressive comes with a comprehensive set of design tools that let designers work with the exact same tokens and components that developers use in code.

---

## Material 3 Design Kit for Figma

The official M3 Design Kit is a Figma Community file that contains:
- All M3 and M3 Expressive components as Figma components
- Color styles mapped to M3 color roles
- Typography styles mapped to all 30 type styles (15 baseline + 15 emphasized)
- Shape styles and the expressive shape library
- Motion annotations and motion token references

**Figma Community link:** https://www.figma.com/community/file/1035203688168086460/material-3-design-kit

**M3 Expressive Shapes Set (separate file):**  
https://www.figma.com/community/file/1510597655879136621/m3-expressive-shapes-set

---

## Material Theme Builder

The Material Theme Builder is the fastest way to generate a complete M3 color scheme from a seed color and export it directly to Compose code.

**URL:** https://m3.material.io/theme-builder

### Features:
- Input a seed color or upload a wallpaper/image for color extraction
- Choose scheme variant (TonalSpot, Vibrant, Expressive, Fidelity, etc.)
- Preview in light and dark modes simultaneously
- Preview on a component library (buttons, cards, navigation, etc.)
- Export to:
  - **Jetpack Compose** — generates `Color.kt` and `Theme.kt`
  - **Android Views XML** — generates theme XML
  - **Flutter** — generates Dart theme
  - **CSS tokens** — for web

### Exported Files (Compose)
```
Color.kt     — all color values for light and dark scheme
Theme.kt     — MaterialTheme composable with ColorScheme, Typography, Shapes
Type.kt      — Typography object (optionally)
```

---

## Material Color Utilities

The open-source library powering Material's color science, available for multiple platforms.

**npm:** `@material/material-color-utilities`
**GitHub:** https://github.com/material-foundation/material-color-utilities

### Available implementations:
| Language | Package |
|---|---|
| JavaScript / TypeScript | `@material/material-color-utilities` |
| Dart / Flutter | `material_color_utilities` |
| Java / Kotlin | `com.google.android.material:material` (bundled) |

### JavaScript Quick Start
```bash
npm install @material/material-color-utilities
```

```typescript
import {
    argbFromHex, hexFromArgb,
    Hct, TonalPalette,
    SchemeTonalSpot, SchemeVibrant, SchemeExpressive,
    MaterialDynamicColors
} from "@material/material-color-utilities";

// Generate a full M3 scheme from a seed
const seedArgb = argbFromHex("#6750A4");
const hct = Hct.fromInt(seedArgb);
const scheme = new SchemeTonalSpot(hct, false, 0.0); // light mode, standard contrast

// Access color roles
const primary = hexFromArgb(MaterialDynamicColors.primary.getArgb(scheme));
const onPrimary = hexFromArgb(MaterialDynamicColors.onPrimary.getArgb(scheme));
const surface = hexFromArgb(MaterialDynamicColors.surface.getArgb(scheme));

console.log({ primary, onPrimary, surface });
```

### Kotlin (Android, beyond standard material library)
For scheme variants beyond standard M3, use the community `m3color` wrapper:
```kotlin
implementation("com.github.Kyant0:m3color:2025.4")
```

---

## HCT Color Picker for Figma

A Figma plugin that uses HCT color math for accessible color adjustments:

- Adjust hue, chroma, and tone while preserving the M3 palette logic
- Shows contrast ratios in real time as you adjust
- Generates M3-compatible tonal palettes from any starting color

**Figma Community:** Search "HCT Color Picker" in Figma's plugin library.

---

## Material Symbols (Icon Library)

Material Symbols is Google's icon library — the successor to Material Icons. It provides variable icons with weight, fill, and optical size axes.

**Website:** https://fonts.google.com/icons?icon.set=Material+Symbols

### Three families:
- **Outlined** — default, clean, versatile
- **Rounded** — soft, friendly (pairs well with M3 Expressive round shapes)
- **Sharp** — angular, defined

### Variable axes:
| Axis | Range | Default | Effect |
|---|---|---|---|
| `FILL` | 0–1 | 0 | 0 = outlined, 1 = filled |
| `wght` | 100–700 | 400 | Icon stroke weight |
| `GRAD` | -25–200 | 0 | Grade/contrast |
| `opsz` | 20–48 | 24 | Optical size matching |

### In Compose
```kotlin
// Add Material Symbols for Compose
implementation("androidx.compose.material:material-icons-core")
implementation("androidx.compose.material:material-icons-extended")  // large set

// Or use Google Fonts Material Symbols with variable font support
implementation("androidx.compose.ui:ui-text-google-fonts")
```

```kotlin
Icon(
    imageVector = Icons.Rounded.Favorite,
    contentDescription = "Favorite"
)
```

---

## M3 Expressive Shape Library for Figma

A dedicated Figma file with all 35 M3 Expressive polygon shapes, ready to use as fills, clips, and masks.

**URL:** https://www.figma.com/community/file/1510597655879136621/m3-expressive-shapes-set

Each shape is provided at multiple sizes with correct proportions for use in icons, avatars, buttons, and decorative elements.

---

## Android Studio Tools

### Compose Preview
Use `@Preview` annotations with theme wrappers to iterate quickly:

```kotlin
@Preview(name = "Light", uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MyComponentPreview() {
    AppTheme {
        MyComponent()
    }
}
```

### Layout Inspector
Android Studio's Layout Inspector supports Compose and shows the live composition tree, including applied styles and modifiers.

### Compose Metrics (Stability Analysis)
```bash
# Add to build.gradle.kts
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-P", "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=build/compose_metrics"
        )
    }
}
```

Run the build to generate stability reports identifying which composables recompose unnecessarily.

---

## Web: Material Web Components

For web implementations of M3, Google provides Material Web Components (MWC):

```bash
npm install @material/web
```

```html
<md-filled-button>Click me</md-filled-button>
<md-outlined-text-field label="Email"></md-outlined-text-field>
```

Design tokens from the Theme Builder export also work with MWC via CSS custom properties.
