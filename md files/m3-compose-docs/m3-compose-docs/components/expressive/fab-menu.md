# FAB Menu / ExpandableFab (M3 Expressive)

The **FAB Menu** (also referred to as `ExpandableFab` in the API) adds multiple action options to a Floating Action Button. It replaces the old **speed dial** pattern and any stacked small FABs. It uses contrasting colors and large touch targets to focus attention on available actions.

> **Status:** `@ExperimentalMaterial3ExpressiveApi` — requires opt-in

---

## Design Spec

- Replaces: speed dial FABs, stacked small FABs
- Sub-actions expand from the FAB using shape transitions and spring animation
- The main FAB icon morphs (e.g., rotates) to indicate the expanded state
- Sub-action buttons use `SecondaryFab` or `TertiaryFab` styling for visual hierarchy
- Supports vertical (default) and horizontal expansion directions
- Recommended: 2–5 sub-actions; for more, use a bottom sheet

---

## Basic Usage

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BasicFabMenu() {
    var expanded by remember { mutableStateOf(false) }

    ExpandableFab(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        fab = {
            Icon(
                imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                contentDescription = if (expanded) "Close menu" else "Open menu"
            )
        },
        items = {
            SmallFloatingActionButton(
                onClick = { /* action 1 */ expanded = false }
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Take photo")
            }
            SmallFloatingActionButton(
                onClick = { /* action 2 */ expanded = false }
            ) {
                Icon(Icons.Filled.AttachFile, contentDescription = "Attach file")
            }
            SmallFloatingActionButton(
                onClick = { /* action 3 */ expanded = false }
            ) {
                Icon(Icons.Filled.Link, contentDescription = "Insert link")
            }
        }
    )
}
```

---

## With Labels

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LabeledFabMenu() {
    var expanded by remember { mutableStateOf(false) }

    ExpandableFab(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        fab = {
            Icon(
                imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Edit,
                contentDescription = "Compose"
            )
        },
        items = {
            ExpandableFabItem(
                icon = { Icon(Icons.Filled.Drafts, contentDescription = null) },
                label = { Text("Draft") },
                onClick = { expanded = false }
            )
            ExpandableFabItem(
                icon = { Icon(Icons.Filled.Send, contentDescription = null) },
                label = { Text("Send") },
                onClick = { expanded = false }
            )
            ExpandableFabItem(
                icon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                label = { Text("Schedule") },
                onClick = { expanded = false }
            )
        }
    )
}
```

---

## Horizontal Expansion

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HorizontalFabMenu() {
    var expanded by remember { mutableStateOf(false) }

    ExpandableFab(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        direction = ExpandableFabDirection.Horizontal,
        fab = {
            Icon(Icons.Filled.Share, contentDescription = "Share")
        },
        items = {
            SmallFloatingActionButton(onClick = { }) {
                Icon(Icons.Filled.Email, contentDescription = "Email")
            }
            SmallFloatingActionButton(onClick = { }) {
                Icon(Icons.Filled.Message, contentDescription = "Message")
            }
        }
    )
}
```

---

## Scaffold Integration

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScreenWithFabMenu() {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExpandableFab(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it },
                fab = {
                    Icon(
                        imageVector = if (menuExpanded) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = "Actions"
                    )
                },
                items = {
                    ExpandableFabItem(
                        icon = { Icon(Icons.Filled.Image, null) },
                        label = { Text("Image") },
                        onClick = { menuExpanded = false }
                    )
                    ExpandableFabItem(
                        icon = { Icon(Icons.Filled.VideoCall, null) },
                        label = { Text("Video") },
                        onClick = { menuExpanded = false }
                    )
                }
            )
        }
    ) { paddingValues ->
        // screen content
    }
}
```

---

## Replacing Speed Dial

If you previously had a **speed dial** pattern (stacked small FABs appearing on main FAB tap):

**Before (old pattern):**
```kotlin
// Don't do this in M3 Expressive
Column {
    if (expanded) {
        SmallFloatingActionButton(onClick = {}) { Icon(Icons.Filled.A, null) }
        SmallFloatingActionButton(onClick = {}) { Icon(Icons.Filled.B, null) }
    }
    FloatingActionButton(onClick = { expanded = !expanded }) {
        Icon(Icons.Filled.Add, null)
    }
}
```

**After (M3 Expressive):**
```kotlin
ExpandableFab(expanded = expanded, onExpandedChange = { expanded = it }, ...) {
    // items as shown above
}
```

---

## Design Guidelines

- Always animate the main FAB icon to indicate state (e.g., `Add` → `Close`)
- Use 2–5 sub-actions; if you need more, use a modal bottom sheet
- Label sub-actions with concise text (1–2 words)
- The FABMenu should only contain actions relevant to the current screen context
- Position in the bottom-right corner (default scaffold behavior)
