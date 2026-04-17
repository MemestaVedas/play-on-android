# Shape System

## Shape Scale

M3 defines a shape scale from `None` to `Full`. Components are assigned a default shape from this scale.

| Token | Corner Radius | Example Components |
|---|---|---|
| `None` | 0dp | — |
| `ExtraSmall` | 4dp | `TextField`, `Tooltip` |
| `Small` | 8dp | `Chip`, `Button` (default) |
| `Medium` | 12dp | `Card`, `DropdownMenu` |
| `Large` | 16dp | `NavigationDrawer`, `Sheet` |
| `ExtraLarge` | 28dp | `Dialog`, `FAB` (large) |
| `Full` | 50% (fully rounded) | `FAB`, `Chip` (some variants) |

M3 Expressive expanded this to **35 new shape tokens**, including shapes beyond the basic rounded-rectangle — squircles, clipped corners, and custom polygon shapes.

---

## Accessing Shapes in Compose

```kotlin
val shapes = MaterialTheme.shapes

Box(
    modifier = Modifier
        .clip(shapes.medium)
        .background(MaterialTheme.colorScheme.primaryContainer)
)
```

### Custom Shape Theme

```kotlin
val MyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

MaterialTheme(shapes = MyShapes) { /* ... */ }
```

---

## Shape Morphing (M3 Expressive)

Shape morphing allows shapes to **animate smoothly between two shapes** — a core M3 Expressive feature. It's used extensively in `ButtonGroup`, `SplitButton`, and the `LoadingIndicator`.

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphingShape() {
    var toggled by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (toggled) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
    )

    // MorphShape interpolates between two shapes
    val morphShape = remember(progress) {
        MorphShape(
            startShape = RoundedCornerShape(50),  // circle-like
            endShape = RoundedCornerShape(8.dp),
            progress = progress
        )
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(morphShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { toggled = !toggled }
    )
}
```

---

## Fully Rounded Corners

In M3 Expressive, `Full` is the canonical token for fully rounded corners. Previously this was hardcoded as 50% of the component size; now it uses the `full` token which always renders a perfect pill/circle regardless of component size.

```kotlin
// Use Full shape (pill-shaped)
Box(modifier = Modifier.clip(CircleShape))         // Compose built-in
Box(modifier = Modifier.clip(MaterialTheme.shapes.full)) // M3 token (preferred in themes)
```

---

## Component Default Shapes

| Component | Default Shape |
|---|---|
| `Button` (all variants) | `Full` |
| `FloatingActionButton` | `Large` |
| `ExtendedFAB` | `Full` |
| `Card` (filled/outlined/elevated) | `Medium` |
| `Chip` (all types) | `Small` |
| `TextField` (filled) | `ExtraSmall` (top corners only) |
| `AlertDialog` | `ExtraLarge` |
| `ModalBottomSheet` | `ExtraLarge` (top) |
| `NavigationBar` | None |
| `Snackbar` | `ExtraSmall` |
| `Tooltip` | `ExtraSmall` |

---

## Shape in M3 Expressive New Components

- `ButtonGroup`: Outer container uses a rounded shape; inner buttons morph shape dynamically when pressed or selected
- `SplitButton`: The menu trigger portion spins and morphs shape when the dropdown opens
- `LoadingIndicator`: Continuously morphs through a sequence of shapes to indicate loading
- `FloatingToolbar`: Uses `Full` (pill) shape by default
- `FABMenu`: Sub-action items expand from the FAB using shape transitions
