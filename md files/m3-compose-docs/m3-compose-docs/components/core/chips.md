# Chips (Core M3)

Chips are compact elements that represent attributes, filters, or actions. M3 defines four chip types.

---

## Chip Types

| Type | Composable | Use Case |
|---|---|---|
| Assist | `AssistChip` | Contextual smart actions (e.g., "Set alarm", "Call") |
| Filter | `FilterChip` | Toggle a filter on/off in a set |
| Input | `InputChip` | Represent user input (e.g., tags, email recipients) |
| Suggestion | `SuggestionChip` | Dynamically generated suggestions |

---

## AssistChip

```kotlin
AssistChip(
    onClick = { /* handle action */ },
    label = { Text("Set alarm") },
    leadingIcon = {
        Icon(
            imageVector = Icons.Filled.Alarm,
            contentDescription = null,
            modifier = Modifier.size(AssistChipDefaults.IconSize)
        )
    }
)
```

---

## FilterChip

```kotlin
var selected by remember { mutableStateOf(false) }

FilterChip(
    selected = selected,
    onClick = { selected = !selected },
    label = { Text("Filter") },
    leadingIcon = if (selected) {
        {
            Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = "Selected",
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        }
    } else null
)
```

### Filter Chip Row

```kotlin
val filters = listOf("All", "Unread", "Starred", "Snoozed")
var selectedFilters by remember { mutableStateOf(setOf("All")) }

LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(horizontal = 16.dp)
) {
    items(filters) { filter ->
        FilterChip(
            selected = filter in selectedFilters,
            onClick = {
                selectedFilters = if (filter in selectedFilters)
                    selectedFilters - filter
                else
                    selectedFilters + filter
            },
            label = { Text(filter) }
        )
    }
}
```

---

## InputChip

```kotlin
var tags by remember { mutableStateOf(listOf("Kotlin", "Android")) }

LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(horizontal = 16.dp)
) {
    items(tags) { tag ->
        InputChip(
            selected = false,
            onClick = { /* select */ },
            label = { Text(tag) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove $tag",
                    modifier = Modifier
                        .size(InputChipDefaults.IconSize)
                        .clickable { tags = tags - tag }
                )
            }
        )
    }
}
```

---

## SuggestionChip

```kotlin
val suggestions = listOf("Pizza", "Sushi", "Tacos", "Ramen")

LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(horizontal = 16.dp)
) {
    items(suggestions) { suggestion ->
        SuggestionChip(
            onClick = { /* apply suggestion */ },
            label = { Text(suggestion) }
        )
    }
}
```

---

## Elevated Variants

All chip types have an `Elevated` variant:

```kotlin
ElevatedAssistChip(onClick = { }, label = { Text("Assist") })
ElevatedFilterChip(selected = false, onClick = { }, label = { Text("Filter") })
ElevatedSuggestionChip(onClick = { }, label = { Text("Suggestion") })
```

---

## FilterChip Updates (M3 Expressive — 1.4.0+)

`FilterChip` and `ElevatedFilterChip` gained `contentPadding` and `horizontalSpacing` parameters for expressive sizing:

```kotlin
FilterChip(
    selected = selected,
    onClick = { selected = !selected },
    label = { Text("Expressive Filter") },
    contentPadding = FilterChipDefaults.contentPadding(
        start = 16.dp, end = 16.dp
    ),
    horizontalArrangement = Arrangement.spacedBy(8.dp)  // new in 1.4.0
)
```

---

## Design Guidelines

- **Assist chip**: Use for AI/smart suggestions tied to the current context (reply, call, add event)
- **Filter chip**: Always use in a group; show a checkmark when selected
- **Input chip**: Represents user-entered values; always provide a way to remove
- **Suggestion chip**: Surface 3–5 at a time; auto-generated from context
- Chips are **not** buttons — don't use chips for primary actions
- Keep chip labels short (1–3 words); truncate with ellipsis if needed
- Chips in a horizontal row should use `LazyRow` with horizontal padding for scrollability
