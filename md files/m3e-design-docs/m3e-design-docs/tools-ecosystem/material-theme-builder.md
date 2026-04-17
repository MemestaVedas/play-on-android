# Material Theme Builder Reference

The Material Theme Builder at https://m3.material.io/theme-builder is the canonical tool for generating M3 color schemes. This document covers the full workflow, export format, and how to customize the output.

---

## Workflow

### Step 1: Choose a Color Input Method

**Option A — Single seed color:**
Pick one hex color. The algorithm generates primary, secondary, tertiary, neutral, and error palettes automatically with harmonious relationships.

**Option B — Custom palette:**
Specify up to four colors: Primary, Secondary, Tertiary, Neutral. Useful for strict brand color requirements.

**Option C — Wallpaper/image:**
Upload an image. The tool extracts the most prominent color and uses it as the primary seed.

### Step 2: Choose a Scheme Variant
From the Style dropdown, select:
- **Tonal Spot** (default) — balanced, versatile
- **Vibrant** — high chroma, bold
- **Expressive** — max color contrast across accents
- **Fidelity** — matches seed closely

### Step 3: Adjust Contrast Level
Slide between standard (default), medium, and high contrast. High contrast satisfies WCAG AAA requirements.

### Step 4: Preview
Switch between light and dark modes. Preview on components: buttons, cards, text fields, navigation, chips, dialogs.

### Step 5: Export

---

## Export Format (Compose)

The builder generates three files:

### Color.kt
```kotlin
// All color values for light and dark schemes
val md_theme_light_primary = Color(0xFF476810)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFC7F089)
val md_theme_light_onPrimaryContainer = Color(0xFF102000)
val md_theme_light_secondary = Color(0xFF55624C)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
// ... all light roles

val md_theme_dark_primary = Color(0xFFACD370)
val md_theme_dark_onPrimary = Color(0xFF213600)
// ... all dark roles

val seed = Color(0xFF476810)
```

### Theme.kt
```kotlin
private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    outline = md_theme_light_outline,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_primary,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    // ... all dark roles
)

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        }
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
```

---

## Adding Extended Surfaces to Exported Theme

The Theme Builder's export includes the core roles. Add the graduated surface containers manually:

```kotlin
private val LightColorScheme = lightColorScheme(
    // ... existing roles from export ...

    // Add M3 Expressive surface containers (not always in older exports)
    surfaceContainerLowest = md_theme_light_surfaceContainerLowest,
    surfaceContainerLow = md_theme_light_surfaceContainerLow,
    surfaceContainer = md_theme_light_surfaceContainer,
    surfaceContainerHigh = md_theme_light_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_light_surfaceContainerHighest,
    surfaceDim = md_theme_light_surfaceDim,
    surfaceBright = md_theme_light_surfaceBright,
)
```

If the export doesn't include these values, calculate them manually using the HCT tone system or use the Theme Builder's latest version which includes them.

---

## Adding MotionScheme to Theme

The exported Theme.kt doesn't include `motionScheme` — add it manually:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    expressiveMotion: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = /* ... as exported ... */

    MaterialExpressiveTheme(  // Use MaterialExpressiveTheme instead of MaterialTheme
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        motionScheme = if (expressiveMotion)
            MotionScheme.expressive()
        else
            MotionScheme.standard(),
        content = content
    )
}
```

---

## Multi-Brand Themes

For apps supporting multiple brand themes (e.g., light, dark, and a "premium" variant):

```kotlin
enum class AppThemeVariant { Default, Premium, Monochrome }

@Composable
fun AppTheme(
    variant: AppThemeVariant = AppThemeVariant.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (variant) {
        AppThemeVariant.Default -> if (darkTheme) DarkColorScheme else LightColorScheme
        AppThemeVariant.Premium -> if (darkTheme) PremiumDarkColorScheme else PremiumLightColorScheme
        AppThemeVariant.Monochrome -> if (darkTheme) MonoDarkColorScheme else MonoLightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = AppTypography) { content() }
}
```

Each `*ColorScheme` would be generated from the Theme Builder with a different seed or variant.

---

## CSS Tokens Export

For web/design token consumers, the Theme Builder exports CSS:

```css
:root {
  --md-sys-color-primary: #476810;
  --md-sys-color-on-primary: #ffffff;
  --md-sys-color-primary-container: #c7f089;
  --md-sys-color-on-primary-container: #102000;
  /* ... all roles ... */
}

[data-theme="dark"] {
  --md-sys-color-primary: #acd370;
  --md-sys-color-on-primary: #213600;
  /* ... dark overrides ... */
}
```

These CSS variables integrate directly with Material Web Components.
