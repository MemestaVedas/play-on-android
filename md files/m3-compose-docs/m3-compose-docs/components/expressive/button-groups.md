# ButtonGroup (M3 Expressive)

`ButtonGroup` is a **new M3 Expressive component** that groups buttons together with shared shape, motion, and width behavior. When a button in the group is pressed or selected, others react — expanding, contracting, or changing shape in response. It replaces patterns of manually stacked buttons.

> **Status:** `@ExperimentalMaterial3ExpressiveApi` — requires opt-in

---

## Design Spec

- Groups 2–5 buttons (recommended; more reduces clarity)
- Works with all button types and sizes (XS → XL)
- The selected/pressed button expands; others contract
- Outer container shape is typically `Full` (pill); inner buttons have dynamic corner treatment
- Supports both **single-select** (radio group) and **multi-select** semantics
- Use `Modifier.align()` within `ButtonGroupScope` for fine control

---

## Basic Usage

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BasicButtonGroup() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    ButtonGroup {
        listOf("Day", "Week", "Month").forEachIndexed { index, label ->
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { selectedIndex = index }
            ) {
                Text(label)
            }
        }
    }
}
```

---

## Connected Button Group (Visual)

Connected button groups render without gaps between buttons and with shared outer corners:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConnectedButtonGroup() {
    var selected by remember { mutableStateOf(setOf(0)) }

    ButtonGroup(
        style = ButtonGroupDefaults.connectedStyle()  // no gaps, shared shape
    ) {
        listOf("Bold", "Italic", "Underline").forEachIndexed { index, label ->
            ToggleButton(
                checked = index in selected,
                onCheckedChange = {
                    selected = if (index in selected)
                        selected - index else selected + index
                }
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
```

---

## Icon-Only Button Group

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IconButtonGroup() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val icons = listOf(Icons.Filled.FormatAlignLeft, Icons.Filled.FormatAlignCenter, Icons.Filled.FormatAlignRight)
    val contentDescriptions = listOf("Align left", "Align center", "Align right")

    ButtonGroup {
        icons.forEachIndexed { index, icon ->
            IconToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { selectedIndex = index }
            ) {
                Icon(imageVector = icon, contentDescription = contentDescriptions[index])
            }
        }
    }
}
```

---

## With Size Variants

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SizedButtonGroup() {
    ButtonGroup(
        modifier = Modifier.fillMaxWidth()
    ) {
        // XS button
        Button(
            onClick = { },
            modifier = Modifier.buttonGroupItem(),
            contentPadding = ButtonDefaults.ExtraSmallButtonContentPadding,
            shape = ButtonDefaults.extraSmallShape
        ) { Text("XS") }

        // Regular button
        Button(
            onClick = { },
            modifier = Modifier.buttonGroupItem()
        ) { Text("Regular") }

        // Large button
        Button(
            onClick = { },
            modifier = Modifier.buttonGroupItem(),
            contentPadding = ButtonDefaults.LargeButtonContentPadding,
            shape = ButtonDefaults.largeShape
        ) { Text("Large") }
    }
}
```

---

## Modifier.align in ButtonGroupScope

Added in alpha09, `Modifier.align` in `ButtonGroupScope` allows controlling alignment of individual items:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlignedButtonGroup() {
    ButtonGroup {
        Button(
            onClick = { },
            modifier = Modifier.align(Alignment.Top)
        ) { Text("Top") }

        Button(onClick = { }) { Text("Center") }

        Button(
            onClick = { },
            modifier = Modifier.align(Alignment.Bottom)
        ) { Text("Bottom") }
    }
}
```

---

## Design Guidelines

- Use `ButtonGroup` to replace a row of manually spaced `Button` composables when the actions are semantically related
- For navigation (e.g., a tab bar replacement), prefer `NavigationBar` or a segmented control
- Limit groups to 5 items max to avoid overcrowding; for more options use a `DropdownMenu` or `FABMenu`
- Do **not** mix filled and outlined buttons inside the same group — keep a single button variant per group

---

## Accessibility

- Each button in the group should have a meaningful `contentDescription` if icon-only
- `ToggleButton` correctly communicates `checked` state to accessibility services
- Minimum touch target is enforced by `Modifier.minimumInteractiveComponentSize`
