# Typography

M3 defines a **type scale** of 15 named text styles organized into five groups (Display, Headline, Title, Body, Label), each in three sizes (Large, Medium, Small).

---

## Type Scale Reference

| Style | Size | Weight | Use Case |
|---|---|---|---|
| `displayLarge` | 57sp | Regular | Hero text, splash screens |
| `displayMedium` | 45sp | Regular | Large feature headings |
| `displaySmall` | 36sp | Regular | Section headings (large screens) |
| `headlineLarge` | 32sp | Regular | Screen titles (large) |
| `headlineMedium` | 28sp | Regular | Screen titles |
| `headlineSmall` | 24sp | Regular | Dialog titles, card headings |
| `titleLarge` | 22sp | Regular | App bar titles, tab labels |
| `titleMedium` | 16sp | Medium (500) | List item primary text |
| `titleSmall` | 14sp | Medium (500) | Overlines, section sub-headings |
| `bodyLarge` | 16sp | Regular | Long-form reading text |
| `bodyMedium` | 14sp | Regular | Body text, descriptions |
| `bodySmall` | 12sp | Regular | Captions, helper text |
| `labelLarge` | 14sp | Medium (500) | Button labels, important small text |
| `labelMedium` | 12sp | Medium (500) | Chip labels, badge text |
| `labelSmall` | 11sp | Medium (500) | Overlines, tiny labels |

---

## Accessing Typography in Compose

```kotlin
// In any composable
Text(
    text = "Screen Title",
    style = MaterialTheme.typography.headlineMedium
)

Text(
    text = "Body text content here.",
    style = MaterialTheme.typography.bodyLarge
)

Text(
    text = "BUTTON",
    style = MaterialTheme.typography.labelLarge
)
```

---

## Customizing the Type Scale

```kotlin
// Type.kt
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    // ... all 15 styles
)

// Pass to MaterialTheme
MaterialTheme(typography = Typography) { ... }
```

---

## Using Custom Fonts

```kotlin
// 1. Add font files to res/font/
// 2. Declare in Kotlin
val MontserratFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

// 3. Use in Typography
val Typography = Typography(
    bodyLarge = TextStyle(fontFamily = MontserratFamily, fontSize = 16.sp)
)
```

### Google Fonts in Compose

```kotlin
implementation("androidx.compose.ui:ui-text-google-fonts")
```

```kotlin
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val Roboto = GoogleFont("Roboto")
val RobotoFamily = FontFamily(
    Font(googleFont = Roboto, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = Roboto, fontProvider = provider, weight = FontWeight.Bold)
)
```

---

## Auto-Sizing Text (1.4.0+)

The `Text` composable supports auto-size behavior to fill a container:

```kotlin
// Auto-size to fit container width in a single line
Text(
    text = "Auto-sized headline",
    maxLines = 1,
    overflow = TextOverflow.Clip,
    style = MaterialTheme.typography.displayLarge.copy(
        fontSize = TextUnit.Unspecified
    )
)
```

---

## Common Text Patterns

```kotlin
// Emphasized label
Text(
    text = "Important",
    style = MaterialTheme.typography.labelLarge,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary
)

// Muted supporting text
Text(
    text = "Last updated 2 hours ago",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)

// Strikethrough
Text(
    text = "Old price",
    style = MaterialTheme.typography.bodyMedium.copy(
        textDecoration = TextDecoration.LineThrough
    )
)

// Max lines with ellipsis
Text(
    text = longDescription,
    style = MaterialTheme.typography.bodyMedium,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis
)
```

---

## Design Guidelines

- Use the type scale tokens — don't hardcode `fontSize` values
- `bodyLarge` / `bodyMedium` for prose; `titleMedium` for list item primary text
- `labelLarge` is the standard for button labels (Material components use it automatically)
- Avoid going below `labelSmall` (11sp) — it becomes unreadable on small screens
- Color for secondary/supporting text: always use `onSurfaceVariant` (never arbitrary opacity)
