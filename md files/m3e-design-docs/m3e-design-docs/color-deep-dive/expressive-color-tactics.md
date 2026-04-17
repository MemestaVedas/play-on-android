# Expressive Color Tactics

M3 Expressive uses color as an active hierarchy tool, not just branding. These tactics explain how to wield the color system to create interfaces that are immediately legible and emotionally resonant.

---

## Tactic 1: Primary / Secondary / Tertiary Separation

**The mistake:** Using primary color for everything important.
**The M3E approach:** Let each accent color group serve a distinct semantic tier.

| Tier | Color group | Example use |
|---|---|---|
| Tier 1 — Critical actions | `primary` | Send, Pay, Confirm, Submit |
| Tier 2 — Supporting actions | `secondary` | Filter, Sort, Share, Tag |
| Tier 3 — Special/accent | `tertiary` | Promo badge, featured section header, focus ring |

When all three tiers are visible on a screen, users can immediately distinguish action weight without reading labels. The stronger the chroma separation between primary and tertiary, the faster this hierarchy registers.

---

## Tactic 2: Container Tinting for Emphasis

Use container colors (`primaryContainer`, `secondaryContainer`, `tertiaryContainer`) to visually "charge" a section:

```kotlin
// A selected list item stands out via primaryContainer tint
Card(
    colors = CardDefaults.cardColors(
        containerColor = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerLow
    )
) {
    Text(
        text = item.name,
        color = if (isSelected)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurface
    )
}
```

---

## Tactic 3: Surface Container Depth Hierarchy

Use the 5-step `surfaceContainer*` scale to create spatial depth without elevation shadows:

```kotlin
// Page background
Scaffold(containerColor = MaterialTheme.colorScheme.background) {
    // Cards sit one level above
    Card(colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    )) {
        // Inner content area sits above the card
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            // Dialogs/sheets are the most elevated
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest) { }
        }
    }
}
```

---

## Tactic 4: Inverse Surfaces for Toasts & Tooltips

`inverseSurface` + `inverseOnSurface` create a high-contrast reversed surface — perfect for snackbars, tooltips, and transient messages that need to float above the page:

```kotlin
// Manual snackbar-style surface
Surface(
    color = MaterialTheme.colorScheme.inverseSurface,
    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
    shape = MaterialTheme.shapes.extraSmall
) {
    Text("Action completed", modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
}
```

---

## Tactic 5: Dynamic Color with Brand Identity Preservation

Dynamic color and brand color are not mutually exclusive. The recommended approach:

1. Choose a **seed color** close to your brand primary
2. Let the algorithm generate the full scheme
3. The `primary` role will be a tonal variant of your seed — usually close enough
4. Override only specific roles if needed using `lightColorScheme()` with custom values

For apps that can't compromise on exact brand hex (e.g., licensed brand guidelines): disable dynamic color (`dynamicColor = false`) and always use your static scheme, but still build your static scheme with Material Theme Builder using your brand color as seed.

---

## Tactic 6: Content-Extracted Color

For media apps, extract color from the content being displayed:

```kotlin
// Using Palette API (View-based, commonly used from Compose via side-effect)
val bitmap = /* album art bitmap */
val palette = Palette.from(bitmap).generate()
val dominantSwatch = palette.dominantSwatch

// Convert to M3-compatible seed, then generate scheme
val seedColor = dominantSwatch?.rgb ?: defaultSeedColor
val hct = Hct.fromInt(seedColor)
val contentScheme = SchemeTonalSpot(hct, isDark = darkTheme, contrastLevel = 0.0)

// Apply via CompositionLocalProvider
CompositionLocalProvider(
    LocalColorScheme provides contentScheme.toComposeColorScheme()
) {
    // content-themed UI
}
```

---

## Tactic 7: Fixed Colors for Persistent Branding Elements

`primaryFixed`, `secondaryFixed`, and `tertiaryFixed` (and their `Dim` variants) maintain the same value in both light and dark mode. Use them for:
- Brand logos with a specific background requirement
- Color swatches/chips that need to be consistent across themes
- Category badges that should always match their brand color

```kotlin
// A badge that looks the same in light and dark
Surface(
    color = MaterialTheme.colorScheme.tertiaryFixed,
    shape = CircleShape
) {
    Text(
        "PRO",
        color = MaterialTheme.colorScheme.onTertiaryFixed,
        style = MaterialTheme.typography.labelSmall
    )
}
```

---

## Color Anti-Patterns in M3 Expressive

| Anti-pattern | Why it's wrong | Fix |
|---|---|---|
| Using `primary` for all text highlights | Flattens hierarchy | Use `tertiary` for secondary highlights |
| Custom grey `#888888` for supporting text | Breaks theme and contrast guarantees | Use `onSurfaceVariant` |
| `surface.copy(alpha = 0.5f)` for overlay | Unreliable rendering | Use `surfaceDim` or `scrim` |
| Hardcoding `Color.White` for icon tint | Breaks in dark/light modes | Use `onPrimary` or `onSurface` contextually |
| Ignoring `surfaceContainerLow/High` | Flat visual hierarchy | Use the graduated container scale |
