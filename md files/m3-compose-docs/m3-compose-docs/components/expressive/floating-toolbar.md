# FloatingToolbar (M3 Expressive)

`FloatingToolbar` is a **new M3 Expressive component** designed for **contextual actions**. It floats over content and contains frequently used actions relevant to the current page. It is more versatile than the `BottomAppBar` it partly replaces, supporting more actions and more varied placement.

> **Status:** `@ExperimentalMaterial3ExpressiveApi` — requires opt-in
> **Note:** `BottomAppBar` is now **deprecated** — migrate to `DockedToolbar` or `FloatingToolbar`

---

## Variants

| Variant | Description |
|---|---|
| `FloatingToolbar` | Floats over content (pill-shaped, not docked) |
| `DockedToolbar` | Shorter, docked to bottom of screen — replaces `BottomAppBar` |

Both share similar API shapes but differ in placement and visual presentation.

---

## Design Spec

- Shape: `Full` (pill) by default for `FloatingToolbar`
- Contains `IconButton`s and optionally one `FloatingActionButton`
- Not a navigation component — use `NavigationBar` or `NavigationRail` for navigation
- Suitable for editor surfaces, image viewers, media controls, map pages
- Shorter than the old `BottomAppBar`, reclaiming content space

---

## Basic FloatingToolbar

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BasicFloatingToolbar() {
    FloatingToolbar(
        expanded = true,  // controls whether labels/text are shown
        content = {
            IconButton(onClick = { /* undo */ }) {
                Icon(Icons.Filled.Undo, contentDescription = "Undo")
            }
            IconButton(onClick = { /* redo */ }) {
                Icon(Icons.Filled.Redo, contentDescription = "Redo")
            }
            IconButton(onClick = { /* bold */ }) {
                Icon(Icons.Filled.FormatBold, contentDescription = "Bold")
            }
            IconButton(onClick = { /* italic */ }) {
                Icon(Icons.Filled.FormatItalic, contentDescription = "Italic")
            }
        }
    )
}
```

---

## FloatingToolbar with FAB

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingToolbarWithFab() {
    FloatingToolbar(
        expanded = true,
        floatingActionButton = {
            FloatingActionButton(onClick = { /* primary action */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        },
        content = {
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.FilterList, contentDescription = "Filter")
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Sort, contentDescription = "Sort")
            }
        }
    )
}
```

---

## DockedToolbar (replaces BottomAppBar)

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DockedToolbarScreen() {
    Scaffold(
        bottomBar = {
            DockedToolbar(
                floatingActionButton = {
                    FloatingActionButton(onClick = { }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Compose")
                    }
                },
                content = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { paddingValues ->
        // screen content
    }
}
```

---

## Animating Toolbar Visibility

The `FloatingToolbar` works well with scroll state to show/hide contextually:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScrollAwareFloatingToolbar() {
    val listState = rememberLazyListState()
    val isScrollingUp by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 ||
            listState.firstVisibleItemScrollOffset < 100 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState) {
            items(50) { Text("Item $it", modifier = Modifier.padding(16.dp)) }
        }

        AnimatedVisibility(
            visible = isScrollingUp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            FloatingToolbar(expanded = true, content = {
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.BookmarkBorder, contentDescription = "Bookmark")
                }
            })
        }
    }
}
```

---

## Migrating from BottomAppBar

`BottomAppBar` is **deprecated** in M3 Expressive. Migration path:

| Old | New |
|---|---|
| `BottomAppBar` with FAB | `DockedToolbar` with `floatingActionButton` slot |
| `BottomAppBar` without FAB | `DockedToolbar` (content only) |
| Contextual action bar | `FloatingToolbar` |

**Before:**
```kotlin
BottomAppBar(
    actions = {
        IconButton(onClick = {}) { Icon(Icons.Filled.Menu, null) }
    },
    floatingActionButton = {
        FloatingActionButton(onClick = {}) { Icon(Icons.Filled.Add, null) }
    }
)
```

**After:**
```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
DockedToolbar(
    floatingActionButton = {
        FloatingActionButton(onClick = {}) { Icon(Icons.Filled.Add, null) }
    },
    content = {
        IconButton(onClick = {}) { Icon(Icons.Filled.Menu, null) }
    }
)
```

---

## Design Guidelines

- `FloatingToolbar` = contextual actions (editor, viewer, map); appears above content
- `DockedToolbar` = persistent actions at the bottom of a screen; docked to edge
- Neither replaces `NavigationBar` — they serve different UX purposes
- Keep toolbar actions to 3–5 items; use `MoreVert` overflow for additional options
