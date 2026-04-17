# Navigation Components (Core M3)

M3 provides several navigation components suited to different screen sizes and use cases.

---

## Navigation Hierarchy

| Component | Form Factor | When to Use |
|---|---|---|
| `NavigationBar` | Phone (compact) | 3–5 top-level destinations |
| `NavigationRail` | Tablet / Foldable (medium) | 3–7 destinations, side navigation |
| `NavigationDrawer` (modal/permanent) | Desktop / large tablet | Many destinations, hierarchical nav |
| `NavigationSuite` (adaptive) | All | Auto-selects the right component |

---

## NavigationBar

```kotlin
var selectedItem by remember { mutableIntStateOf(0) }
val items = listOf("Home", "Search", "Library", "Profile")
val icons = listOf(
    Icons.Filled.Home,
    Icons.Filled.Search,
    Icons.Filled.LibraryMusic,
    Icons.Filled.Person
)

NavigationBar {
    items.forEachIndexed { index, item ->
        NavigationBarItem(
            icon = { Icon(icons[index], contentDescription = item) },
            label = { Text(item) },
            selected = selectedItem == index,
            onClick = { selectedItem = index }
        )
    }
}
```

> **Color change in 1.4.0:** The active label/icon color for `NavigationBarItem` changed from `onSurface` to **`secondary`**. Override via `NavigationBarItemDefaults.colors()` if you need the old behavior.

---

## NavigationRail

```kotlin
var selectedItem by remember { mutableIntStateOf(0) }

Row {
    NavigationRail(
        header = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) {
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = { selectedItem = index }
            )
        }
    }

    // Main content area
    Box(modifier = Modifier.weight(1f)) {
        // content
    }
}
```

---

## NavigationDrawer (Modal)

```kotlin
val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
val scope = rememberCoroutineScope()

ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
        ModalDrawerSheet {
            Text("App Name", modifier = Modifier.padding(16.dp))
            HorizontalDivider()
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                label = { Text("Home") },
                selected = true,
                onClick = { scope.launch { drawerState.close() } }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                label = { Text("Settings") },
                selected = false,
                onClick = { scope.launch { drawerState.close() } }
            )
        }
    }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My App") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        // content
    }
}
```

---

## Permanent NavigationDrawer

For large screens where the drawer is always visible:

```kotlin
PermanentNavigationDrawer(
    drawerContent = {
        PermanentDrawerSheet(modifier = Modifier.width(240.dp)) {
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Home, null) },
                label = { Text("Home") },
                selected = true,
                onClick = { }
            )
            // more items...
        }
    }
) {
    // Main content
}
```

---

## Adaptive NavigationSuite (Recommended)

`NavigationSuiteScaffold` automatically switches between `NavigationBar`, `NavigationRail`, and `NavigationDrawer` based on window size class:

```kotlin
implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
```

```kotlin
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
fun AdaptiveApp() {
    var selectedDestination by remember { mutableStateOf(0) }

    val destinations = listOf(
        Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
        Triple("Search", Icons.Filled.Search, Icons.Outlined.Search),
        Triple("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            destinations.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
                item(
                    icon = {
                        Icon(
                            if (selectedDestination == index) selectedIcon else unselectedIcon,
                            contentDescription = label
                        )
                    },
                    label = { Text(label) },
                    selected = selectedDestination == index,
                    onClick = { selectedDestination = index }
                )
            }
        }
    ) {
        // Screen content based on selectedDestination
        when (selectedDestination) {
            0 -> HomeScreen()
            1 -> SearchScreen()
            2 -> ProfileScreen()
        }
    }
}
```

---

## TopAppBar

```kotlin
// Simple TopAppBar
TopAppBar(
    title = { Text("Screen Title") },
    navigationIcon = {
        IconButton(onClick = { /* navigate back */ }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    },
    actions = {
        IconButton(onClick = { }) {
            Icon(Icons.Filled.Search, contentDescription = "Search")
        }
        IconButton(onClick = { }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More")
        }
    }
)

// Large TopAppBar (collapsing)
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

LargeTopAppBar(
    title = { Text("Large Title") },
    scrollBehavior = scrollBehavior
)
// Attach scrollBehavior to the Scaffold and LazyColumn:
Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = { LargeTopAppBar(title = { Text("Title") }, scrollBehavior = scrollBehavior) }
) { ... }
```

---

## Design Guidelines

- **Phone:** `NavigationBar` with 3–5 items; labels always visible
- **Tablet/Foldable:** `NavigationRail` on the side, no labels needed (icons speak)
- **Desktop/large tablet:** `PermanentNavigationDrawer` or `ModalNavigationDrawer`
- Use `NavigationSuiteScaffold` to handle all form factors automatically
- Navigation labels: 1–2 words max; no icons without labels in `NavigationBar`
- Active indicator color: uses `secondaryContainer` by default in M3 (changed from `primary` in M2)
