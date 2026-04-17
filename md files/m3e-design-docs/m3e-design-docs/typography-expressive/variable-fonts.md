# Variable Fonts in M3 Expressive

M3 Expressive moves beyond static font weights and styles to embrace **variable fonts** — a single font file with adjustable axes that allow continuous variation in weight, width, and other properties. The result is richer, more dynamic typography that can animate and respond to interaction.

---

## The M3 Expressive Font Family

Google's M3 Expressive uses five official fonts, each with a specific purpose:

| Font | Type | Best for |
|---|---|---|
| **Roboto Flex** | Variable (primary) | Display, Headline, Title — expressive, animated type |
| **Roboto** | Static + variable | Body, Label — highly readable at small sizes |
| **Roboto Serif** | Variable | Long-form editorial content, literary feel |
| **Noto Sans** | Static | Multilingual/internationalized apps, fallback |
| **Google Sans** | Static (Google internal) | Used in Google's own apps (not publicly distributed as a brand font) |

---

## Roboto Flex — The Expressive Workhorse

**Roboto Flex** is a variable font with an exceptional range of axes, making it the centerpiece of M3 Expressive typography. It includes over 900 glyphs with Latin, Greek, and Cyrillic support.

### Registered Axes in Roboto Flex

| Axis tag | Name | Range | Default |
|---|---|---|---|
| `wght` | Weight | 100–1000 | 400 |
| `wdth` | Width | 25–151 | 100 |
| `opsz` | Optical size | 8–144 | 14 |
| `GRAD` | Grade | -200–150 | 0 |
| `slnt` | Slant | -10–0 | 0 |
| `XTRA` | Counter width | 323–603 | 468 |
| `XOPQ` | Thick stroke | 27–175 | 96 |
| `YOPQ` | Thin stroke | 25–135 | 79 |
| `YTLC` | Lowercase height | 416–570 | 514 |
| `YTUC` | Uppercase height | 528–760 | 712 |
| `YTAS` | Ascender | 649–854 | 750 |
| `YTDE` | Descender | -305–-98 | -203 |
| `YTFI` | Figure height | 560–788 | 738 |

For M3 Expressive, the primary axes to use are `wght`, `wdth`, and `opsz`.

---

## Setting Up Roboto Flex in Compose

### Download Font
Roboto Flex is available on Google Fonts: https://fonts.google.com/specimen/Roboto+Flex

Add it to `res/font/roboto_flex.ttf` or use the Google Fonts Compose integration.

### Via Google Fonts API
```kotlin
implementation("androidx.compose.ui:ui-text-google-fonts")
```

```kotlin
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val RobotoFlexFont = GoogleFont("Roboto Flex")

val RobotoFlexFamily = FontFamily(
    Font(
        googleFont = RobotoFlexFont,
        fontProvider = provider,
        weight = FontWeight.Normal,
        style = FontStyle.Normal
    )
)
```

### Via Local File
```kotlin
val RobotoFlexFamily = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        weight = FontWeight.W400,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400f),
            FontVariation.width(100f),
            FontVariation.opticalSizing(14f)
        )
    )
)
```

---

## Optical Sizing (`opsz`)

Optical sizing automatically adjusts stroke contrast and letter spacing based on the intended display size. At small sizes, letters become slightly wider and strokes more uniform for legibility. At large display sizes, strokes can be more dramatic.

```kotlin
// Small body text — optical size 12 for legibility
val bodySmallStyle = TextStyle(
    fontFamily = RobotoFlexFamily,
    fontSize = 12.sp,
    fontVariationSettings = "'opsz' 12"  // matches font size for optimal rendering
)

// Large display text — optical size 57 for drama
val displayLargeStyle = TextStyle(
    fontFamily = RobotoFlexFamily,
    fontSize = 57.sp,
    fontVariationSettings = "'opsz' 57"
)
```

In Compose, use `FontVariation.opticalSizing(fontSize.value)` to automatically match optical size to font size.

---

## Animating Font Weight (Expressive Interaction)

A key M3 Expressive technique: **animating `wght` axis on interaction** to give text a "press" or "bounce" feel.

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeightAnimatedLabel(text: String, isPressed: Boolean) {
    val weight by animateIntAsState(
        targetValue = if (isPressed) 700 else 400,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
    )

    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
            fontVariationSettings = FontVariation.Settings(
                FontVariation.weight(weight.toFloat())
            )
        )
    )
}
```

This is used in Google's Androidify sample and recommended for interactive labels, active navigation items, and pressed buttons.

---

## Width Axis for Stretch Effects

The `wdth` axis (25–151) allows stretching text horizontally. At default (100), the font is standard width. Reducing narrows it; increasing stretches it.

```kotlin
// Condensed variant (useful for tight spaces)
val condensedStyle = TextStyle(
    fontVariationSettings = FontVariation.Settings(
        FontVariation.weight(500f),
        FontVariation.width(75f)  // narrower than default
    )
)

// Wide/stretched variant (expressive display use)
val wideStyle = TextStyle(
    fontVariationSettings = FontVariation.Settings(
        FontVariation.weight(400f),
        FontVariation.width(130f)
    )
)
```

---

## Grade (`GRAD`) for Display Contrast

Grade adjusts the visual weight of strokes without changing the font's metrics (spacing stays the same). Use it for:
- High-contrast vs. low-contrast display environments
- Subpixel adjustment (different screen densities)

```kotlin
// Higher grade = bolder strokes at same metrics
val highContrastDisplay = TextStyle(
    fontVariationSettings = FontVariation.Settings(
        FontVariation.weight(400f),
        FontVariation.Setting('G', 'R', 'A', 'D', 100f)  // GRAD axis
    )
)
```

---

## Compose FontVariation API

```kotlin
// Full example — Roboto Flex with multiple axes
Text(
    text = "Expressive Text",
    style = TextStyle(
        fontFamily = RobotoFlexFamily,
        fontSize = 32.sp,
        fontVariationSettings = FontVariation.Settings(
            FontVariation.weight(650f),       // wght: between medium and bold
            FontVariation.width(110f),         // wdth: slightly wider
            FontVariation.opticalSizing(32f)  // opsz: matches display size
        )
    )
)
```

---

## Design Guidelines for Variable Fonts

- Use `opsz` at every font size for best rendering — make it equal to the `fontSize` value in sp
- Reserve weight animation for **primary interactive moments** (button press, item selection)
- Do not animate both weight and width simultaneously — choose one expressive axis per interaction
- Ensure animated weight changes stay within accessible contrast bounds — going from `wght 400` to `wght 700` should not change color/background
- Provide static fallbacks for devices without variable font support (older APIs)
