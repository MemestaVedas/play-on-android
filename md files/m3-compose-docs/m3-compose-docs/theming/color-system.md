# Color System

## Color Roles

M3 defines color through **roles**, not hardcoded values. Each role has a semantic meaning:

| Role | Usage |
|---|---|
| `primary` | Key action buttons, active states |
| `onPrimary` | Content (icon/text) on top of `primary` |
| `primaryContainer` | Less prominent primary-colored surfaces |
| `onPrimaryContainer` | Content on `primaryContainer` |
| `secondary` | Supporting / complementary actions |
| `secondaryContainer` | Navigation item selected state |
| `tertiary` | Accent, third-brand color |
| `tertiaryContainer` | Tertiary-tinted surfaces |
| `error` / `onError` | Error states |
| `surface` | Card and sheet backgrounds |
| `surfaceVariant` | Slightly tinted surface (e.g., chip background) |
| `surfaceTint` | Elevation overlay tint (replaces M2 shadow) |
| `outline` | Borders, dividers |
| `outlineVariant` | Subtle borders |
| `background` | App background |
| `scrim` | Modal overlay |

M3 Expressive extends the palette with richer **tertiary** token support for more vibrant branded themes.

---

## Color Scheme in Compose

```kotlin
// Access in any composable
val colors = MaterialTheme.colorScheme

Text(
    text = "Hello",
    color = colors.onSurface
)

Surface(color = colors.primaryContainer) {
    // content drawn on primaryContainer
}
```

### Creating a Custom Color Scheme

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    // ... etc.
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    // ... etc.
)
```

---

## Dynamic Color (Material You)

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

**Important:** Always provide a static fallback. Dynamic color requires API 31+ (Android 12).

---

## Elevation & Surface Tint

In M3, elevation is expressed with a **surface tint** (primary color overlay) rather than a drop shadow. The tint opacity increases with elevation level.

```kotlin
// Cards at elevation 1 get a slight primary tint
Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
) {
    // the surface inside is automatically tinted
}
```

---

## Checking Contrast

M3 color roles are pre-paired to ensure WCAG AA contrast (4.5:1 for body text):

- `primary` / `onPrimary` ✅
- `primaryContainer` / `onPrimaryContainer` ✅
- `surface` / `onSurface` ✅

Avoid using arbitrary pairings like `primary` text on `secondary` — use the defined `on*` role for each container color.
