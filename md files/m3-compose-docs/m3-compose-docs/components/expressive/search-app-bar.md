# SearchAppBar (M3 Expressive)

The **Search App Bar** is a new M3 Expressive variant of the top app bar that integrates a search field directly. It moves the hamburger/navigation button and profile avatar **outside** the search pill, making the search container itself a clean, focused input area.

> **Status:** `@ExperimentalMaterial3ExpressiveApi` — requires opt-in

The standard `SearchBar` / `DockedSearchBar` composables also gained expressive updates — see the expanded variants below.

---

## Variants

| Component | Description |
|---|---|
| `SearchBar` | Full-screen expanding search (existing, updated) |
| `DockedSearchBar` | Inline docked search (existing, updated) |
| `ExpandedDockedSearchBarWithGap` | New expressive variant with gap/padding from edges (alpha09) |
| `ExpandedFullScreenContainedSearchBar` | New full-screen contained variant (alpha11) |

---

## Basic SearchBar

The existing `SearchBar` now has expressive visual updates (thicker pill, navigation outside):

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicSearchBar() {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { query = it },
                onSearch = { active = false },
                expanded = active,
                onExpandedChange = { active = it },
                placeholder = { Text("Search") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                } else null
            )
        },
        expanded = active,
        onExpandedChange = { active = it }
    ) {
        // Search results content
        LazyColumn {
            items(suggestedResults) { result ->
                ListItem(
                    headlineContent = { Text(result) },
                    modifier = Modifier.clickable {
                        query = result
                        active = false
                    }
                )
            }
        }
    }
}
```

---

## SearchAppBar Pattern (Expressive Style)

In M3 Expressive apps (like Gmail, Drive), the SearchAppBar moves navigation elements outside the pill:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveSearchAppBar(
    onNavigationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Navigation icon OUTSIDE the search pill (M3 Expressive style)
        IconButton(onClick = onNavigationClick) {
            Icon(Icons.Filled.Menu, contentDescription = "Navigation")
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Search pill takes remaining space
        DockedSearchBar(
            modifier = Modifier.weight(1f),
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { active = false },
                    expanded = active,
                    onExpandedChange = { active = it },
                    placeholder = { Text("Search in app") }
                )
            },
            expanded = active,
            onExpandedChange = { active = it }
        ) {
            // search results
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Profile avatar OUTSIDE the search pill (M3 Expressive style)
        IconButton(onClick = onProfileClick) {
            Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
        }
    }
}
```

---

## ExpandedDockedSearchBarWithGap

New in alpha09 — adds configurable spacing/gaps from screen edges:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GappedSearchBar() {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    ExpandedDockedSearchBarWithGap(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { query = it },
                onSearch = { active = false },
                expanded = active,
                onExpandedChange = { active = it },
                placeholder = { Text("Search") }
            )
        },
        expanded = active,
        onExpandedChange = { active = it }
    ) {
        // results
    }
}
```

---

## SearchBar with Keyboard Options

The `SearchBar.InputField` now supports `keyboardOptions` and `lineLimits` (alpha07):

```kotlin
SearchBarDefaults.InputField(
    query = query,
    onQueryChange = { query = it },
    onSearch = { },
    expanded = active,
    onExpandedChange = { active = it },
    keyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Search,
        keyboardType = KeyboardType.Text
    )
)
```

---

## Design Guidelines

- Use `SearchBar` for full-screen search-centric screens (e.g., a dedicated search screen)
- Use `DockedSearchBar` or the expressive SearchAppBar pattern for search within a primary screen
- Move navigation (hamburger) and profile avatar **outside** the pill in M3 Expressive style
- Only show the search bar actively expanded when the user taps into it — collapse on back gesture
- `SearchBar` automatically handles predictive back (dismisses on back press)
