# Expressive List Items (M3 Expressive)

Material expressive list items are available as of **material3 1.5.0-alpha11**. They support expressive interactions and **segmented styling** — visually grouping items within a list using container and divider styling that communicates grouping and hierarchy.

> **Status:** `@ExperimentalMaterial3ExpressiveApi` — requires opt-in

---

## Design Spec

- Built on top of the existing `ListItem` composable with expressive variants
- **Segmented styling**: items at the top, middle, and bottom of a visual group receive different shape treatments (rounded at group boundaries, flat in the middle)
- Supports interaction states (hover, pressed, focused) more expressively than standard `ListItem`
- `ListItemColors` gained additional color fields for finer expressive control

---

## Basic ListItem (standard, updated colors)

The standard `ListItem` gets additional expressive color customization in 1.5.x:

```kotlin
ListItem(
    headlineContent = { Text("Primary text") },
    supportingContent = { Text("Secondary text") },
    leadingContent = {
        Icon(Icons.Filled.Person, contentDescription = null)
    },
    trailingContent = {
        Text("Meta", style = MaterialTheme.typography.labelSmall)
    },
    colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        headlineColor = MaterialTheme.colorScheme.onSurface,
        supportingColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
)
```

---

## Segmented List (Expressive Grouping)

Segmented list items use different corner shapes at the group boundary vs interior:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SegmentedList() {
    val items = listOf("Alpha", "Beta", "Gamma", "Delta")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEachIndexed { index, item ->
            val position = when {
                items.size == 1 -> ListItemPosition.Single
                index == 0 -> ListItemPosition.First
                index == items.lastIndex -> ListItemPosition.Last
                else -> ListItemPosition.Middle
            }

            ExpressiveListItem(
                headlineContent = { Text(item) },
                position = position,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

---

## ExpressiveListItem with Leading Icon

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IconListItems() {
    val settings = listOf(
        Pair(Icons.Filled.Notifications, "Notifications"),
        Pair(Icons.Filled.Lock, "Privacy"),
        Pair(Icons.Filled.Language, "Language"),
        Pair(Icons.Filled.DarkMode, "Appearance")
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        settings.forEachIndexed { index, (icon, label) ->
            val position = when {
                settings.size == 1 -> ListItemPosition.Single
                index == 0 -> ListItemPosition.First
                index == settings.lastIndex -> ListItemPosition.Last
                else -> ListItemPosition.Middle
            }

            ExpressiveListItem(
                headlineContent = { Text(label) },
                leadingContent = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ChevronRight,
                        contentDescription = "Navigate"
                    )
                },
                position = position,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* navigate */ }
            )
        }
    }
}
```

---

## Selectable Expressive List Items

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SelectableExpressiveList() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("Option A", "Option B", "Option C")

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        options.forEachIndexed { index, option ->
            val position = when {
                options.size == 1 -> ListItemPosition.Single
                index == 0 -> ListItemPosition.First
                index == options.lastIndex -> ListItemPosition.Last
                else -> ListItemPosition.Middle
            }

            ExpressiveListItem(
                headlineContent = { Text(option) },
                trailingContent = {
                    RadioButton(
                        selected = selectedIndex == index,
                        onClick = null  // handled by item click
                    )
                },
                position = position,
                colors = if (selectedIndex == index)
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                else ListItemDefaults.colors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )
            )
        }
    }
}
```

---

## ListItem Position Reference

| `ListItemPosition` | Corner Treatment |
|---|---|
| `Single` | All corners rounded (full group) |
| `First` | Top corners rounded, bottom flat |
| `Middle` | All corners flat (or minimal) |
| `Last` | Top corners flat, bottom rounded |

---

## Design Guidelines

- Group list items that belong to the same logical section using segmented styling
- Separate groups with a `Spacer` (8–16dp) rather than a `HorizontalDivider`
- Use `ExpressiveListItem` for settings screens, option lists, and selection panels
- For simple scrolling lists without grouping, the standard `ListItem` is still fine
- Avoid mixing expressive and standard list items in the same group
