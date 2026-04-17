# SplitButton (M3 Expressive)

`SplitButton` is a **new M3 Expressive component** consisting of two zones: a **leading action button** (triggers a primary action) and a **trailing menu button** (opens a dropdown). The trailing button morphs shape when the menu is open, providing a visual signal of state.

> **Status:** `@ExperimentalMaterial3ExpressiveApi` — requires opt-in

---

## Design Spec

- Same 5 recommended sizes as regular buttons: XS, S, M (default), L, XL
- Four color styles: `Elevated`, `Filled`, `Tonal`, `Outlined`
- The trailing (dropdown trigger) section spins and changes shape on activation
- Use alongside other buttons of the same size for visual consistency
- Not interchangeable with `Button` + separate `DropdownMenu` — the split button has a specific UX meaning (primary + secondary action grouping)

---

## Basic Usage

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BasicSplitButton() {
    var expanded by remember { mutableStateOf(false) }

    SplitButton(
        leadingButton = {
            SplitButtonDefaults.LeadingButton(onClick = { /* primary action */ }) {
                Text("Save")
            }
        },
        trailingButton = {
            SplitButtonDefaults.TrailingButton(
                onClick = { expanded = !expanded },
                checked = expanded
            )
        }
    )
}
```

---

## With Dropdown Menu

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplitButtonWithMenu() {
    var expanded by remember { mutableStateOf(false) }

    Box {
        SplitButton(
            leadingButton = {
                SplitButtonDefaults.LeadingButton(
                    onClick = { /* save to default location */ }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Save")
                }
            },
            trailingButton = {
                SplitButtonDefaults.TrailingButton(
                    onClick = { expanded = !expanded },
                    checked = expanded
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Save as Draft") },
                onClick = { expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Save and Publish") },
                onClick = { expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Save to Cloud") },
                onClick = { expanded = false }
            )
        }
    }
}
```

---

## Tonal Variant

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TonalSplitButton() {
    var expanded by remember { mutableStateOf(false) }

    SplitButton(
        leadingButton = {
            SplitButtonDefaults.LeadingButton(
                onClick = { },
                colors = SplitButtonDefaults.leadingButtonTonalColors()
            ) {
                Text("Export")
            }
        },
        trailingButton = {
            SplitButtonDefaults.TrailingButton(
                onClick = { expanded = !expanded },
                checked = expanded,
                colors = SplitButtonDefaults.trailingButtonTonalColors()
            )
        }
    )
}
```

---

## Outlined Variant

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OutlinedSplitButton() {
    var expanded by remember { mutableStateOf(false) }

    SplitButton(
        leadingButton = {
            SplitButtonDefaults.LeadingButton(
                onClick = { },
                colors = SplitButtonDefaults.leadingButtonOutlinedColors(),
                border = SplitButtonDefaults.leadingButtonOutlinedBorder()
            ) {
                Text("Share")
            }
        },
        trailingButton = {
            SplitButtonDefaults.TrailingButton(
                onClick = { expanded = !expanded },
                checked = expanded,
                colors = SplitButtonDefaults.trailingButtonOutlinedColors(),
                border = SplitButtonDefaults.trailingButtonOutlinedBorder(checked = expanded)
            )
        }
    )
}
```

---

## Size Variants

```kotlin
// Large SplitButton
SplitButton(
    leadingButton = {
        SplitButtonDefaults.LeadingButton(
            onClick = { },
            contentPadding = SplitButtonDefaults.LeadingButtonLargeContentPadding
        ) { Text("Send") }
    },
    trailingButton = {
        SplitButtonDefaults.TrailingButton(
            onClick = { },
            checked = false,
            modifier = Modifier.size(SplitButtonDefaults.LargeTrailingButtonSize)
        )
    }
)
```

---

## Design Guidelines

- The **leading button** should always perform the most common action without expanding the menu
- The **trailing button** (chevron) expands to offer variations of the primary action, not unrelated actions
- Do NOT use split buttons for navigation or destructive actions
- Pair split buttons with other same-size buttons in the same layout for consistency
- If there is only one possible action, use a regular `Button` instead
