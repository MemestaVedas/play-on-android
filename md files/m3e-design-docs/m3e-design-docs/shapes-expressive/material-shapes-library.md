# MaterialShapes Library — Complete Catalogue

`MaterialShapes` is a preset collection of named polygon shapes in Jetpack Compose's material3 library. These are the **35 new shapes** added with M3 Expressive. They support smooth morphing between each other via the `Morph` API and are used in `LoadingIndicator`, `ContainedLoadingIndicator`, and anywhere `Shape` or `CornerBasedShape` is accepted.

> **Package:** `androidx.compose.material3.MaterialShapes`
> **Status:** `@ExperimentalMaterial3ExpressiveApi`
> **API:** Each shape is a `RoundedPolygon` which can be converted via `.toShape()`

---

## Using MaterialShapes

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShapedBox() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(MaterialShapes.Cookie9Sided.toShape())
            .background(MaterialTheme.colorScheme.primaryContainer)
    )
}
```

### As a Composable Shape
```kotlin
// clip modifier
Modifier.clip(MaterialShapes.SoftBurst.toShape())

// Surface/Card shape parameter
Surface(shape = MaterialShapes.Clover4Leaf.toShape()) { }

// LoadingIndicator polygon sequence
LoadingIndicator(polygons = listOf(
    MaterialShapes.Circle,
    MaterialShapes.Pill,
    MaterialShapes.RoundedSquare
))
```

---

## Shape Catalogue

### Basic Geometric Shapes

| Name | Description | `MaterialShapes.X` |
|---|---|---|
| Circle | Perfect circle | `Circle` |
| Oval | Non-circular ellipse | `Oval` |
| Pill | Fully rounded rectangle (horizontal) | `Pill` |
| Rounded square | Square with heavily rounded corners | `RoundedSquare` |
| Square | Square with slight corner rounding | `Square` |
| Diamond | Rotated square / rhombus | `Diamond` |
| Pentagon | 5-sided polygon | `Pentagon` |
| Hexagon | 6-sided polygon | `Hexagon` |
| Octagon | 8-sided polygon | `Octagon` |

---

### Cookie / Scalloped Shapes

Shapes with convex scalloped/bumped edges, named by number of "petals":

| Name | Description | `MaterialShapes.X` |
|---|---|---|
| Cookie 4-sided | 4 bumped sides | `Cookie4Sided` |
| Cookie 6-sided | 6 bumped sides | `Cookie6Sided` |
| Cookie 7-sided | 7 bumped sides | `Cookie7Sided` |
| Cookie 9-sided | 9 bumped sides (used in Androidify camera button) | `Cookie9Sided` |
| Cookie 12-sided | 12 bumped sides | `Cookie12Sided` |

---

### Burst / Star-like Shapes

| Name | Description | `MaterialShapes.X` |
|---|---|---|
| 4-pointed Star | Sharp 4-point star | `Star4Pointed` |
| 6-pointed Star | Sharp 6-point star | `Star6Pointed` |
| Burst | Spiky burst / sharp star with many points | `Burst` |
| Soft burst | Burst with rounded points | `SoftBurst` |
| Boom | Explosive burst, more points than Burst | `Boom` |
| Soft boom | Rounded boom shape | `SoftBoom` |
| Sunny | Sun-like shape with rounded rays | `Sunny` |
| Very sunny | Sun with more/longer rays | `VerySunny` |

---

### Clover / Flower Shapes

| Name | Description | `MaterialShapes.X` |
|---|---|---|
| Clover 4-leaf | 4-petal clover | `Clover4Leaf` |
| Clover 8-leaf | 8-petal clover | `Clover8Leaf` |
| Flower | Rounded petal flower | `Flower` |

---

### Puffy / Blobby Shapes

| Name | Description | `MaterialShapes.X` |
|---|---|---|
| Puffy | Rounded, inflated blob | `Puffy` |
| Puffy diamond | Puffy rotated square/diamond | `PuffyDiamond` |
| Ghost | Ghost-like silhouette shape | `Ghost` |

---

### Pixel / Retro Shapes

| Name | Description | `MaterialShapes.X` |
|---|---|---|
| Pixel circle | Pixelated/stepped circle | `PixelCircle` |
| Pixel triangle | Pixelated triangle | `PixelTriangle` |

---

### Organic / Miscellaneous

| Name | Description | `MaterialShapes.X` |
|---|---|---|
| Bun | Bread-bun silhouette | `Bun` |
| Heart | Heart shape | `Heart` |

---

## Total: 35 Shapes

Circle, Oval, Pill, RoundedSquare, Square, Diamond, Pentagon, Hexagon, Octagon, Cookie4Sided, Cookie6Sided, Cookie7Sided, Cookie9Sided, Cookie12Sided, Star4Pointed, Star6Pointed, Burst, SoftBurst, Boom, SoftBoom, Sunny, VerySunny, Clover4Leaf, Clover8Leaf, Flower, Puffy, PuffyDiamond, Ghost, PixelCircle, PixelTriangle, Bun, Heart + 3 additional shapes.

---

## Using Shapes in LoadingIndicator

The `LoadingIndicator` cycles through a list of `RoundedPolygon` shapes. You can customize which shapes it morphs through:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomLoadingIndicator() {
    LoadingIndicator(
        polygons = listOf(
            MaterialShapes.Circle,
            MaterialShapes.Pill,
            MaterialShapes.RoundedSquare,
            MaterialShapes.Sunny
        ),
        color = MaterialTheme.colorScheme.primary
    )
}
```

The default `LoadingIndicatorDefaults.IndeterminateIndicatorPolygons` is a preset list that cycles through shapes with good morphability.

---

## ContainedLoadingIndicator

Adds a colored container background around the morphing shape:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContainedLoading() {
    ContainedLoadingIndicator(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        containerShape = MaterialShapes.Pill.toShape(),
        color = MaterialTheme.colorScheme.primary,
        polygons = listOf(
            MaterialShapes.Circle,
            MaterialShapes.Cookie9Sided
        )
    )
}
```

---

## Converting to Compose Shape

All `MaterialShapes` are `RoundedPolygon` — convert to a Compose `Shape` with `.toShape()`:

```kotlin
val shape: Shape = MaterialShapes.Cookie9Sided.toShape()
// Use anywhere a Shape is expected:
Box(Modifier.clip(shape)) { }
Surface(shape = shape) { }
```

---

## Shape Morphability

Not all shapes morph equally well together. Best morphing pairs (smooth interpolation):
- Circle ↔ Oval ↔ Pill ↔ RoundedSquare (gradual roundness change)
- Circle ↔ Sunny ↔ Burst (petal/spike emergence)
- Clover4Leaf ↔ Clover8Leaf (petal multiplication)
- Cookie4Sided ↔ Cookie9Sided (scallop count change)

Jarring/avoid morphing:
- Heart ↔ PixelCircle (incompatible topology)
- Star6Pointed ↔ Bun (unrelated structures)

For smooth animations, prefer shapes with similar topology (same number of anchor points).
