# Using Typography to Guide Attention

Typography in M3 Expressive is an active hierarchy tool. The selection and application of type styles should always serve the question: **"Where should the user look first?"**

---

## The Attention Hierarchy Model

Every screen has an implicit reading order. Typography should reinforce the intended order:

```
Display / Headline  → "What is this page about?"
Title               → "What are the sections?"
Body                → "What's the detail?"
Label               → "What are the actions/metadata?"
```

When these roles are correctly applied, users do not need to read everything — they can scan from largest to smallest and extract the critical information in a single pass.

---

## Pairing Sizes for Visual Contrast

M3 Expressive encourages **dramatic size contrast** between adjacent text elements. Minimal size differences create visual ambiguity; large differences create clear hierarchy.

**Good pairings (high contrast):**
- `headlineLarge` + `bodyMedium` (32sp + 14sp = 18sp difference)
- `displaySmall` + `labelLarge` (36sp + 14sp)
- `titleLarge` + `bodySmall` (22sp + 12sp)

**Weak pairings (low contrast):**
- `titleMedium` + `bodyLarge` (16sp + 16sp = 0sp difference)
- `titleSmall` + `bodyMedium` (14sp + 14sp = same size)

When in doubt: make the hierarchy element bigger than feels comfortable. Research shows that dramatic size contrast aids usability without feeling jarring to users.

---

## The Headline-Body Rhythm Pattern

A canonical M3 Expressive layout pattern used in Google's own apps:

```
[headlineMedium or headlineLarge — Emphasized]  → Screen title or section header
[bodyLarge or bodyMedium]                       → Supporting description
                                                  (runs 1–3 lines)
[labelLarge]                                    → Metadata or secondary info
```

Example from Google Drive:
- Folder name → `titleLarge`
- File count → `bodySmall` in `onSurfaceVariant`
- Last modified → `labelSmall` in `onSurfaceVariant`

---

## Display Styles: Short Text Only

Display styles (`displayLarge`, `displayMedium`, `displaySmall`) should **never** wrap to multiple lines. They are for:
- Single-word hero statements ("Hello", "Welcome")
- Short numeric values (timers, counts, prices)
- Splash/onboarding headlines

Max recommended character count: ~20 characters for `displaySmall`, ~12 for `displayLarge`.

---

## Body Text Readability Rules

For any text that runs more than 2 lines:

- Use `bodyLarge` (16sp) or `bodyMedium` (14sp)
- Line height must be ≥ 1.4× font size (built into M3 defaults)
- Maximum line length: 75–90 characters (~40–45 characters on phone)
- Minimum font size: 12sp (`bodySmall`) — never go below this for reading text
- Color: `onSurface` for primary body, `onSurfaceVariant` for supporting

---

## Emphasized Styles in Context

Use emphasized styles (`titleLargeEmphasized`, etc.) for:

| Context | Recommended style |
|---|---|
| Selected tab label | `labelLargeEmphasized` |
| Active navigation item | `labelMediumEmphasized` |
| Selected list item title | `titleMediumEmphasized` |
| Form field label (focused) | `bodyMediumEmphasized` |
| Search result keyword highlight | `bodyMediumEmphasized` |
| Card title (featured/hero card) | `titleLargeEmphasized` |

---

## Semantic Role Assignments for Common Layouts

### App Bar
- Title: `titleLarge`
- Subtitle (if any): `bodyMedium` in `onSurfaceVariant`

### List Item (standard)
- Headline: `bodyLarge` or `titleMedium`
- Supporting text: `bodyMedium` in `onSurfaceVariant`
- Trailing meta: `labelSmall` in `onSurfaceVariant`

### Card
- Title: `titleMedium` or `titleLarge`
- Body: `bodyMedium`
- CTA/Action: `labelLarge`

### Bottom Sheet Header
- Title: `titleLarge`
- Supporting text: `bodyMedium`

### Dialog
- Title: `headlineSmall`
- Body: `bodyMedium`
- Buttons: `labelLarge` (auto-applied by `TextButton`)

### Chip Labels
- All chip types: `labelLarge` (applied automatically)

---

## Color + Typography Pairing

Typography and color work together to establish hierarchy. The most important text should have the strongest color contrast:

| Importance | Type style | Color |
|---|---|---|
| Primary (screen heading) | `headlineMedium` | `onSurface` |
| Secondary (section heading) | `titleMedium` | `onSurface` |
| Body | `bodyMedium` | `onSurface` |
| Supporting | `bodySmall` | `onSurfaceVariant` |
| Metadata/timestamp | `labelSmall` | `onSurfaceVariant` |
| Disabled | any | `onSurface.copy(alpha = 0.38f)` |
| Error | `bodyMedium` | `error` |

---

## Typography Don'ts

- Don't use `fontWeight = FontWeight.Bold` manually when `*Emphasized` styles exist
- Don't use raw `Color.Gray` for secondary text — use `onSurfaceVariant`
- Don't use `fontSize = 10.sp` or smaller — minimum legible size in M3 is 11sp (`labelSmall`)
- Don't use all-caps styling for body or label text — M3 does not use all-caps in its design language (unlike M2)
- Don't mix more than 2 font families on one screen — stick to one primary + one secondary if needed
