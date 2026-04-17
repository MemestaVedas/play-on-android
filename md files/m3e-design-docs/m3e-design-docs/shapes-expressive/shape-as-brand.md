# Shape as Brand Identity

In M3 Expressive, shape is elevated from a layout detail to a **primary brand tool**. Consistent use of a distinctive shape family makes an app immediately recognizable — before the user reads a label or sees a color.

---

## The Shift: Shape as Brand Signature

Before M3 Expressive, shape in Material Design was mostly about corner radii — how rounded are buttons, cards, dialogs. The scale went from 0dp to fully rounded, and most apps used the defaults.

M3 Expressive adds 35 named polygon shapes and morphing, meaning a brand can now choose:
- A **signature shape** for its primary action buttons (e.g., `Cookie9Sided` instead of `Pill`)
- A **morph destination** that reveals on interaction (e.g., tapping a `Circle` button morphs to `SoftBurst`)
- A **shape family** that appears consistently across all primary surfaces

This is analogous to how some physical brands are recognized purely by silhouette (a Coca-Cola bottle, a Volkswagen hood ornament). The shape becomes the identity before any other element registers.

---

## Choosing a Shape Family

A **shape family** is a set of 2–4 related shapes that appear throughout the app in different contexts but feel visually cohesive. Choose them based on your brand personality:

| Brand personality | Suggested shape family |
|---|---|
| Playful, youthful, fun | Cookie variants, Clover, Sunny, Flower |
| Bold, energetic, sport | Burst, Star, Boom variants |
| Calm, health, wellness | Puffy, Oval, Circle, soft round variants |
| Professional, structured | Hexagon, RoundedSquare, Pill |
| Creative, artistic | Mixed: Ghost, Bun, PuffyDiamond |
| Premium, luxury | Clean Pill, subtle RoundedSquare — less is more |

---

## Shape Hierarchy in a Layout

Just as typography has a scale from Display to Label, shapes can have a hierarchy:

| Level | Context | Recommended shape approach |
|---|---|---|
| Primary FAB | Single most important action | Signature brand shape (e.g., `Cookie9Sided`) |
| Secondary buttons | Main action buttons | Pill (default), or softer variant of brand shape |
| Cards / containers | Content grouping | `ExtraLarge` rounded rectangle |
| Chips | Filters, tags | `Full` (pill) — always |
| Icons | Icon buttons, badges | `Circle` or signature shape |
| Dialogs | Modal content | `ExtraLarge` rounded rectangle |

---

## Shape + Color Pairing

Shapes are amplified by color. A signature shape should appear in the highest-emphasis color role to maximize brand recognition:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BrandFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = MaterialShapes.Cookie9Sided.toShape(),  // brand shape
        containerColor = MaterialTheme.colorScheme.primary,  // highest emphasis color
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Add")
    }
}
```

---

## Shape Consistency Across the App

Once you pick a brand shape, use it **consistently** for the same type of element:

```kotlin
// Define brand shapes as constants — don't scatter MaterialShapes.X calls
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object AppShapes {
    val primaryAction = MaterialShapes.Cookie9Sided.toShape()
    val secondaryAction = MaterialShapes.Pill.toShape()
    val container = RoundedCornerShape(28.dp)
    val card = RoundedCornerShape(16.dp)
    val chip = CircleShape
}

// Use throughout the app
FloatingActionButton(shape = AppShapes.primaryAction) { }
Button(shape = AppShapes.secondaryAction) { }
Card(shape = AppShapes.card) { }
```

---

## Shape in the M3 Theme

You can set default component shapes via the `Shapes` theme object:

```kotlin
// Custom shape theme
val AppShapeTheme = Shapes(
    // Standard M3 scale (used for most components automatically)
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
    full = CircleShape
)

MaterialTheme(shapes = AppShapeTheme) {
    // Components that use MaterialTheme.shapes.* pick up custom values automatically
}
```

Custom polygon shapes (`MaterialShapes.*`) must be applied per-component since they live outside the standard `Shapes` class.

---

## Shape and State Communication

Beyond branding, shape communicates component state. The system convention:

| State | Shape behavior |
|---|---|
| Default / idle | Resting shape (e.g., `Pill` button) |
| Hovered | Slight expansion (handled automatically by ripple/hover) |
| Pressed | Spring-scale down + shape change (e.g., `Pill` → slightly flatter) |
| Selected / active | Morph to "positive" shape (e.g., circle → softBurst) |
| Loading | Continuous morph sequence |
| Error | Shape stays the same — color changes to `error` |
| Disabled | Shape stays the same — alpha reduced to 38% |

The pattern: **shape change = semantic state change; color change = emotional state change**. Use both together for maximum clarity.

---

## Real-World Examples

**Google Androidify (2025 sample app):**
- Camera capture button → `Cookie9Sided` shape, springs on press
- Avatar placeholder → `Clover4Leaf`
- Confirmation screen → morphs to `Sunny` on completion

**Gmail (M3E redesign):**
- Compose FAB → standard `Large` rounded shape
- Navigation items → `secondaryContainer` pill shape on selection

**Google Chrome (M3E redesign):**
- Bookmarked state → star button morphs to a rounded-square container
- Tab groups → squircle (`RoundedSquare`) containers with group color
- Progress bar → pill/fully-rounded ends

---

## Shape Art Direction: The M3 Expressive Aesthetic

The official M3 Expressive art direction guidelines:

1. **Prefer rounded over angular** — Round shapes feel approachable and modern; sharp corners feel dated in the M3E context
2. **Scale matters** — A `Cookie9Sided` shape at 48dp feels playful; the same shape at 200dp feels dramatic and brand-forward
3. **Shapes and text work in harmony** — Rounded shapes pair with rounder letterforms; geometric shapes pair with geometric sans-serifs
4. **Never random** — Every shape choice should be intentional, not a default. If it looks accidental, it probably is
5. **Restraint for complex screens** — In dense layouts, stick to simple `Pill` and `RoundedSquare`; save expressive shapes for hero moments
