# Cards (Core M3)

M3 cards are surfaces that group related content and actions. Three variants are available, differing in visual elevation and container treatment.

---

## Variants

| Variant | Composable | Appearance |
|---|---|---|
| Elevated | `Card` (default) / `ElevatedCard` | Drop shadow, no border |
| Filled | `Card` with filled colors | Tonal surface fill, no shadow |
| Outlined | `OutlinedCard` | Border, no fill or shadow |

---

## Basic Cards

```kotlin
// Elevated card (default)
ElevatedCard(
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Card Title", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Card body text here.", style = MaterialTheme.typography.bodyMedium)
    }
}

// Filled card
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    modifier = Modifier.fillMaxWidth()
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Filled Card")
    }
}

// Outlined card
OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Outlined Card")
    }
}
```

---

## Clickable Card

```kotlin
ElevatedCard(
    onClick = { /* navigate or expand */ },
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Article,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Article Title", style = MaterialTheme.typography.titleMedium)
            Text(
                "2 min read",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

---

## Media Card

```kotlin
ElevatedCard(
    modifier = Modifier
        .width(200.dp)
        .padding(8.dp)
) {
    AsyncImage(
        model = "https://example.com/image.jpg",
        contentDescription = "Card image",
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentScale = ContentScale.Crop
    )
    Column(modifier = Modifier.padding(12.dp)) {
        Text("Media Title", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "Subtitle text",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row {
            TextButton(onClick = { }) { Text("Action 1") }
            TextButton(onClick = { }) { Text("Action 2") }
        }
    }
}
```

---

## Selectable / Checkable Card

```kotlin
var checked by remember { mutableStateOf(false) }

Card(
    onClick = { checked = !checked },
    colors = CardDefaults.cardColors(
        containerColor = if (checked)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface
    ),
    border = if (checked)
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    else null,
    modifier = Modifier.fillMaxWidth()
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null  // handled by card click
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("Selectable option")
    }
}
```

---

## Card Elevation

```kotlin
ElevatedCard(
    elevation = CardDefaults.cardElevation(
        defaultElevation = 6.dp,
        pressedElevation = 8.dp,
        focusedElevation = 8.dp,
        hoveredElevation = 8.dp
    )
) { /* content */ }
```

---

## M3 Expressive: Cards with Containers

In M3 Expressive design language, cards are used as **containers** to group related content more explicitly. Key patterns:

```kotlin
// Container grouping pattern (M3 Expressive)
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    shape = MaterialTheme.shapes.extraLarge,  // more rounded corners for expressive feel
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Group Header", style = MaterialTheme.typography.titleMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        // grouped items inside
    }
}
```

---

## Design Guidelines

- **Elevated card**: Use for most content cards; conveys interactivity clearly
- **Filled card**: Use for content that should feel part of the background surface
- **Outlined card**: Use when you need clear grouping without elevation (e.g., settings sections)
- Cards should not be nested inside other cards
- Avoid more than 2 action buttons at the bottom of a card; prefer 0–1 primary action
- In M3 Expressive, prefer `extraLarge` corner radius for cards to emphasize containment
