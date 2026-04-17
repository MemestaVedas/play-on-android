# Color Roles Reference

Color roles are the named semantic slots that components use. They are the "numbers" in a paint-by-number canvas — every UI element maps to a role, not a raw color value. This makes themes swappable and light/dark modes automatic.

---

## Primary Group

| Role | Light tone | Dark tone | Usage |
|---|---|---|---|
| `primary` | T40 | T80 | Key buttons, active states, high-emphasis UI |
| `onPrimary` | T100 | T20 | Text/icons on top of `primary` |
| `primaryContainer` | T90 | T30 | Tinted surfaces related to primary (cards, selected items) |
| `onPrimaryContainer` | T10 | T90 | Content on `primaryContainer` |
| `primaryFixed` | T90 | T90 | Same in both modes — for fixed-color surfaces |
| `primaryFixedDim` | T80 | T80 | Dimmer fixed primary surface |
| `onPrimaryFixed` | T10 | T10 | Content on `primaryFixed` |
| `onPrimaryFixedVariant` | T30 | T30 | Less prominent content on `primaryFixed` |

---

## Secondary Group

| Role | Light tone | Dark tone | Usage |
|---|---|---|---|
| `secondary` | T40 | T80 | Less prominent actions; filter chips |
| `onSecondary` | T100 | T20 | Content on `secondary` |
| `secondaryContainer` | T90 | T30 | Navigation item selected state, chip selected |
| `onSecondaryContainer` | T10 | T90 | Content on `secondaryContainer` |
| `secondaryFixed` | T90 | T90 | Fixed secondary surface |
| `secondaryFixedDim` | T80 | T80 | Dimmer fixed secondary |
| `onSecondaryFixed` | T10 | T10 | Content on `secondaryFixed` |
| `onSecondaryFixedVariant` | T30 | T30 | Less prominent content on `secondaryFixed` |

---

## Tertiary Group

Tertiary gained expanded importance in M3 Expressive for more vibrant, brand-differentiating themes.

| Role | Light tone | Dark tone | Usage |
|---|---|---|---|
| `tertiary` | T40 | T80 | Contrasting accent; input focus, special actions |
| `onTertiary` | T100 | T20 | Content on `tertiary` |
| `tertiaryContainer` | T90 | T30 | Tertiary-tinted surfaces |
| `onTertiaryContainer` | T10 | T90 | Content on `tertiaryContainer` |
| `tertiaryFixed` | T90 | T90 | Fixed tertiary surface |
| `tertiaryFixedDim` | T80 | T80 | Dimmer fixed tertiary |
| `onTertiaryFixed` | T10 | T10 | Content on `tertiaryFixed` |
| `onTertiaryFixedVariant` | T30 | T30 | Less prominent content on `tertiaryFixed` |

---

## Error Group

Error colors use a fixed red tonal palette, independent of the seed color.

| Role | Light tone | Dark tone | Usage |
|---|---|---|---|
| `error` | T40 | T80 | Error state indicators, destructive actions |
| `onError` | T100 | T20 | Content on `error` |
| `errorContainer` | T90 | T30 | Error background surfaces |
| `onErrorContainer` | T10 | T90 | Content on `errorContainer` |

---

## Surface & Background Group

| Role | Light tone | Dark tone | Usage |
|---|---|---|---|
| `background` | T98 | T6 | Page/screen background |
| `onBackground` | T10 | T90 | Text/icons on background |
| `surface` | T98 | T6 | Component surfaces (same as background by default) |
| `onSurface` | T10 | T90 | Content on surface |
| `surfaceVariant` | T90 | T30 | Alternative surface (chip background, etc.) |
| `onSurfaceVariant` | T30 | T80 | Supporting/secondary text/icons |
| `surfaceTint` | T40 | T80 | Elevation overlay color (primary tint) |
| `inverseSurface` | T20 | T90 | Snackbar, tooltip backgrounds |
| `inverseOnSurface` | T95 | T20 | Content on `inverseSurface` |
| `inversePrimary` | T80 | T40 | Primary-like role on inverse surfaces |
| `scrim` | T0 | T0 | Modal overlay/scrim (always dark) |
| `shadow` | T0 | T0 | Drop shadow color |

---

## Graduated Surface Containers (M3 Expressive)

These give a 5-step hierarchy of surface depth:

| Role | Light tone | Dark tone | Usage |
|---|---|---|---|
| `surfaceContainerLowest` | T100 | T4 | Most recessed, faintest |
| `surfaceContainerLow` | T96 | T10 | Light card surfaces |
| `surfaceContainer` | T94 | T12 | Standard card/dialog background |
| `surfaceContainerHigh` | T92 | T17 | More prominent surfaces |
| `surfaceContainerHighest` | T90 | T22 | Most prominent surface (e.g., navigation bar) |
| `surfaceDim` | T87 | T6 | Darkened surface (overlay hints) |
| `surfaceBright` | T98 | T24 | Brightened surface |

---

## Outline Group

| Role | Usage |
|---|---|
| `outline` | Borders, dividers, prominent text field border |
| `outlineVariant` | Subtle dividers, decorative borders |

---

## Usage Rules

**Never mix these:**
- Text `primary` color on a `secondary` background — use `onSecondary`
- Custom opacity hacks to fake contrast — use the defined on-color role
- Using `onSurface` at full opacity for disabled text — use `onSurface.copy(alpha = 0.38f)` for disabled

**Always do this:**
- Pair every container color with its `on*` counterpart for text/icons
- Use `surfaceContainerLow/High` for cards nested in pages, not raw `surface`
- Use `onSurfaceVariant` for secondary/supporting text, not a manual grey

---

## Compose Access

```kotlin
val cs = MaterialTheme.colorScheme

cs.primary
cs.onPrimary
cs.primaryContainer
cs.onPrimaryContainer
cs.secondary
cs.tertiary
cs.error
cs.surface
cs.surfaceContainerLow
cs.surfaceContainerHigh
cs.onSurface
cs.onSurfaceVariant
cs.outline
cs.outlineVariant
cs.inverseSurface
cs.inversePrimary
cs.scrim
```
