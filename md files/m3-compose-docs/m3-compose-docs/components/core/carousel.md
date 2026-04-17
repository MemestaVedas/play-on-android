# Carousel (Core M3)

The M3 Carousel displays a horizontally (or vertically) scrollable list of items with expressive sizing — featured items are larger, and adjacent items are partially visible to hint at scrollability.

---

## Variants (as of material3 1.4.0 / 1.5.x)

| Variant | Description | Status |
|---|---|---|
| `HorizontalMultiBrowseCarousel` | Multiple items visible, variable sizes | **Stable** (1.4.0+) |
| `HorizontalUncontainedCarousel` | Items overflow the container | **Stable** (1.4.0+) |
| `HorizontalCenteredHeroCarousel` | Center item hero-sized, smaller on sides | **Stable** (1.4.0) |
| Multi-aspect via lazy grids | Create multi-aspect carousels using LazyGrid | **Alpha** (1.5.0-alpha11) |

---

## HorizontalMultiBrowseCarousel

Shows several items at once with a hero item that's wider:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiBrowseCarousel() {
    val items = listOf(
        CarouselItem(R.drawable.img1, "Forest"),
        CarouselItem(R.drawable.img2, "Mountains"),
        CarouselItem(R.drawable.img3, "Ocean"),
        CarouselItem(R.drawable.img4, "Desert"),
        CarouselItem(R.drawable.img5, "City"),
    )

    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { items.size },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        preferredItemWidth = 180.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { index ->
        val item = items[index]
        Box(
            modifier = Modifier
                .height(200.dp)
                .maskClip(MaterialTheme.shapes.extraLarge)
        ) {
            AsyncImage(
                model = item.imageRes,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}

data class CarouselItem(val imageRes: Int, val title: String)
```

---

## HorizontalUncontainedCarousel

Items scroll freely and can extend beyond the container edge:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UncontainedCarousel() {
    HorizontalUncontainedCarousel(
        state = rememberCarouselState { 8 },
        modifier = Modifier.fillMaxWidth(),
        itemWidth = 160.dp,
        itemSpacing = 8.dp
    ) { index ->
        Card(
            modifier = Modifier
                .height(200.dp)
                .maskClip(MaterialTheme.shapes.large)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Item ${index + 1}")
            }
        }
    }
}
```

---

## HorizontalCenteredHeroCarousel (New in 1.4.0)

The center item is displayed larger (hero), with smaller items on either side:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenteredHeroCarousel() {
    HorizontalCenteredHeroCarousel(
        state = rememberCarouselState { 5 },
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        itemSpacing = 8.dp
    ) { index ->
        ElevatedCard(
            modifier = Modifier
                .fillMaxHeight()
                .maskClip(MaterialTheme.shapes.extraLarge)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Slide ${index + 1}",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}
```

---

## Carousel State

```kotlin
// Basic state (item count only)
val state = rememberCarouselState { items.size }

// With initial index
val state = rememberCarouselState(initialItem = 2) { items.size }

// Programmatically scroll
val coroutineScope = rememberCoroutineScope()
Button(onClick = {
    coroutineScope.launch {
        state.animateScrollToItem(3)
    }
}) {
    Text("Go to item 4")
}
```

---

## maskClip Modifier

The `maskClip` modifier is the correct way to clip carousel items — it respects the carousel's internal clipping behavior (unlike `Modifier.clip` which can conflict):

```kotlin
Box(
    modifier = Modifier
        .maskClip(MaterialTheme.shapes.extraLarge) // use this instead of .clip()
) { /* item content */ }
```

---

## Design Guidelines

- Use carousels for browsable collections of 4–12 items (images, cards, media)
- Always use `maskClip` instead of `clip` for item clipping in carousels
- Provide visible overflow (partial item at edge) to signal scrollability
- `HorizontalMultiBrowseCarousel` is best for content libraries (articles, photos, products)
- `HorizontalCenteredHeroCarousel` is best for featured content / onboarding slides
- `HorizontalUncontainedCarousel` is best for chips-like browsable items (categories, tags)
- Include meaningful `contentDescription` on all carousel item images
