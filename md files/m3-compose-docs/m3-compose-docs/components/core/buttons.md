# Buttons (Core M3)

M3 defines five button variants based on emphasis level, plus icon buttons.

---

## Button Variants

| Variant | Use Case | Composable |
|---|---|---|
| Filled | Highest emphasis, primary action | `Button` |
| Filled Tonal | Medium-high emphasis, secondary | `FilledTonalButton` |
| Elevated | Medium emphasis, alternative to tonal | `ElevatedButton` |
| Outlined | Medium emphasis, alongside filled | `OutlinedButton` |
| Text | Lowest emphasis, inline actions | `TextButton` |

---

## Basic Usage

```kotlin
// Filled (primary)
Button(onClick = { }) {
    Text("Filled")
}

// Filled Tonal
FilledTonalButton(onClick = { }) {
    Text("Tonal")
}

// Elevated
ElevatedButton(onClick = { }) {
    Text("Elevated")
}

// Outlined
OutlinedButton(onClick = { }) {
    Text("Outlined")
}

// Text
TextButton(onClick = { }) {
    Text("Text")
}
```

---

## Buttons with Icons

```kotlin
Button(onClick = { }) {
    Icon(
        imageVector = Icons.Filled.Send,
        contentDescription = null,
        modifier = Modifier.size(ButtonDefaults.IconSize)
    )
    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
    Text("Send")
}
```

---

## Size Variants (M3 Expressive)

M3 Expressive adds XS → XL size variants:

```kotlin
// Extra Small
Button(
    onClick = { },
    contentPadding = ButtonDefaults.ExtraSmallButtonContentPadding,
    shape = ButtonDefaults.extraSmallShape
) { Text("XS") }

// Large
Button(
    onClick = { },
    contentPadding = ButtonDefaults.LargeButtonContentPadding,
    shape = ButtonDefaults.largeShape
) { Text("Large") }
```

---

## Disabled State

```kotlin
Button(
    onClick = { },
    enabled = false
) {
    Text("Disabled")
}
```

---

## Customizing Colors

```kotlin
Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    )
) { Text("Custom Color") }
```

---

## Icon Buttons

```kotlin
// Standard
IconButton(onClick = { }) {
    Icon(Icons.Filled.Favorite, contentDescription = "Favorite")
}

// Filled
FilledIconButton(onClick = { }) {
    Icon(Icons.Filled.Add, contentDescription = "Add")
}

// Filled Tonal
FilledTonalIconButton(onClick = { }) {
    Icon(Icons.Filled.Bookmark, contentDescription = "Bookmark")
}

// Outlined
OutlinedIconButton(onClick = { }) {
    Icon(Icons.Filled.Share, contentDescription = "Share")
}

// Toggle
var toggled by remember { mutableStateOf(false) }
IconToggleButton(checked = toggled, onCheckedChange = { toggled = it }) {
    Icon(
        imageVector = if (toggled) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
        contentDescription = "Toggle favorite"
    )
}
```

---

## Floating Action Button

```kotlin
// Standard FAB
FloatingActionButton(onClick = { }) {
    Icon(Icons.Filled.Add, contentDescription = "Add")
}

// Small FAB
SmallFloatingActionButton(onClick = { }) {
    Icon(Icons.Filled.Add, contentDescription = "Add")
}

// Large FAB
LargeFloatingActionButton(onClick = { }) {
    Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(36.dp))
}

// Extended FAB
ExtendedFloatingActionButton(
    onClick = { },
    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
    text = { Text("New Note") }
)

// Extended FAB with expansion
var expanded by remember { mutableStateOf(true) }
ExtendedFloatingActionButton(
    onClick = { },
    expanded = expanded,
    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
    text = { Text("Create") }
)
```

---

## Design Guidelines

- Use at most **one** filled button per section; it represents the single primary action
- Pair filled + text, or filled + outlined — avoid two filled buttons side by side
- Button labels: sentence case, 1–3 words, no punctuation
- Minimum touch target: 48x48dp (enforced automatically by Compose M3)
- When using buttons in a row, sort by emphasis: Filled → Tonal → Outlined → Text (left to right for LTR)
