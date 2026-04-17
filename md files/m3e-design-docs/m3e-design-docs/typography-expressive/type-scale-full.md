# Type Scale: Baseline + Emphasized (30 Styles)

M3 Expressive doubles the type scale to **30 named styles**: 15 baseline (same as standard M3) plus 15 new **emphasized** counterparts. Every baseline style has an emphasized twin with higher font weight and subtle adjustments.

---

## Why Emphasized Styles?

Instead of ad-hoc `FontWeight.Bold` overrides, M3 Expressive provides a formal, design-system-approved method for adding emphasis. This means:
- Emphasis is consistent across the app (same weight, same adjustments)
- It's easy to switch between normal and emphasized states (e.g., on selection)
- Figma tokens and code stay in sync

---

## Accessing Emphasized Styles in Compose

```kotlin
// Baseline
MaterialTheme.typography.titleLarge
MaterialTheme.typography.bodyMedium

// Emphasized (new in M3 Expressive)
MaterialTheme.typography.titleLargeEmphasized
MaterialTheme.typography.bodyMediumEmphasized
```

All emphasized styles are `@ExperimentalMaterial3ExpressiveApi`.

---

## Full Type Scale Reference

### Display

| Style | Size | Line height | Weight (Baseline) | Weight (Emphasized) | Letter spacing |
|---|---|---|---|---|---|
| `displayLarge` | 57sp | 64sp | Regular (400) | Bold (700) | -0.25sp |
| `displayMedium` | 45sp | 52sp | Regular (400) | Bold (700) | 0sp |
| `displaySmall` | 36sp | 44sp | Regular (400) | Bold (700) | 0sp |

**Usage:** Hero moments, splash screens, very large feature headings. Emphasized variant used for impactful hero text, marketing copy, animated type-on effects.

---

### Headline

| Style | Size | Line height | Weight (Baseline) | Weight (Emphasized) | Letter spacing |
|---|---|---|---|---|---|
| `headlineLarge` | 32sp | 40sp | Regular (400) | Bold (700) | 0sp |
| `headlineMedium` | 28sp | 36sp | Regular (400) | Bold (700) | 0sp |
| `headlineSmall` | 24sp | 32sp | Regular (400) | SemiBold (600) | 0sp |

**Usage:** Screen titles, section titles, dialog headings. Emphasized variant for screen titles that need strong visual anchoring.

---

### Title

| Style | Size | Line height | Weight (Baseline) | Weight (Emphasized) | Letter spacing |
|---|---|---|---|---|---|
| `titleLarge` | 22sp | 28sp | Regular (400) | Bold (700) | 0sp |
| `titleMedium` | 16sp | 24sp | Medium (500) | Bold (700) | 0.15sp |
| `titleSmall` | 14sp | 20sp | Medium (500) | Bold (700) | 0.1sp |

**Usage:** App bar titles, list item primary text, section sub-headings. Emphasized variant for selected/active item titles, card titles with high importance.

---

### Body

| Style | Size | Line height | Weight (Baseline) | Weight (Emphasized) | Letter spacing |
|---|---|---|---|---|---|
| `bodyLarge` | 16sp | 24sp | Regular (400) | Medium (500) | 0.5sp |
| `bodyMedium` | 14sp | 20sp | Regular (400) | Medium (500) | 0.25sp |
| `bodySmall` | 12sp | 16sp | Regular (400) | Medium (500) | 0.4sp |

**Usage:** Body copy, descriptions, list item supporting text. Emphasized variant should be used sparingly for inline highlights, not entire paragraphs.

---

### Label

| Style | Size | Line height | Weight (Baseline) | Weight (Emphasized) | Letter spacing |
|---|---|---|---|---|---|
| `labelLarge` | 14sp | 20sp | Medium (500) | Bold (700) | 0.1sp |
| `labelMedium` | 12sp | 16sp | Medium (500) | Bold (700) | 0.5sp |
| `labelSmall` | 11sp | 16sp | Medium (500) | Bold (700) | 0.5sp |

**Usage:** Button labels, chip text, small captions. Emphasized variant for active/selected labels, badge text, important status indicators.

---

## Defining Emphasized Styles in Typography

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val AppTypography = Typography(
    // Baseline styles
    titleLarge = TextStyle(
        fontFamily = RobotoFlexFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    // Emphasized counterparts
    titleLargeEmphasized = TextStyle(
        fontFamily = RobotoFlexFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLargeEmphasized = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    // ... all 30 styles
)
```

---

## Switching Between Baseline and Emphasized

A common pattern: emphasize the selected item in a list.

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SelectableListItem(label: String, isSelected: Boolean) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                style = if (isSelected)
                    MaterialTheme.typography.titleMediumEmphasized
                else
                    MaterialTheme.typography.titleMedium
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    )
}
```

---

## Editorial Treatment

M3 Expressive encourages **editorial typography** — intentional pairing of display-scale text with body text to create rhythm:

- Use `displaySmall` or `headlineLarge` for section openers
- Drop to `bodyLarge` for the paragraph immediately following
- The contrast between large and small is itself expressive

This is used in Google apps like Drive (large folder name + small metadata) and Gmail (large sender name + small preview text).

---

## Guidelines for Using Emphasized Styles

- Emphasized ≠ always bolder — use it for state changes (selected, active, focused), not permanent emphasis
- Never use two emphasized styles side by side in the same text block — the contrast is lost
- Display + Headline emphasized → only for single-line hero text, never for body content
- Label emphasized → good for active tab labels, active navigation items, selected chips
- Body emphasized → sparingly — inline keyword in a search result, not entire paragraphs
