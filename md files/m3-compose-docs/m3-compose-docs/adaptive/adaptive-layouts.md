# Adaptive Layouts

Compose Material3 Adaptive helps you build UIs that work across phones, foldables, tablets, and large screens. The adaptive libraries automatically respond to window size and fold state.

---

## Dependencies

```kotlin
// build.gradle.kts
dependencies {
    implementation("androidx.compose.material3.adaptive:adaptive:1.3.0-alpha09")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.3.0-alpha09")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.3.0-alpha09")
    // Optional: Navigation3 integration
    implementation("androidx.compose.material3.adaptive:adaptive-navigation3:1.0.0-alpha03")
}
```

---

## Window Size Classes

The foundation of adaptive layout decisions:

```kotlin
// Get current window size class
val windowSizeClass = calculateWindowSizeClass(this)

// Use in a Composable
@Composable
fun AdaptiveContent() {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> PhoneLayout()
        WindowWidthSizeClass.MEDIUM -> TabletLayout()
        WindowWidthSizeClass.EXPANDED -> DesktopLayout()
    }
}
```

---

## ListDetailPaneScaffold

The most common adaptive layout pattern — a list on the left, detail on the right (or full-screen on phones).

```kotlin
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ListDetailScreen() {
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()

    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                LazyColumn {
                    items(10) { index ->
                        ListItem(
                            headlineContent = { Text("Item $index") },
                            modifier = Modifier.clickable {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, index)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                val index = navigator.currentDestination?.contentKey as? Int
                if (index != null) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Detail for Item $index", style = MaterialTheme.typography.headlineMedium)
                        // detailed content
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Select an item")
                    }
                }
            }
        }
    )
}
```

---

## SupportingPaneScaffold

Two-pane layout with a main pane and a supporting/contextual pane:

```kotlin
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SupportingPaneExample() {
    val navigator = rememberSupportingPaneScaffoldNavigator()

    SupportingPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        mainPane = {
            AnimatedPane {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Main Content", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        navigator.navigateTo(SupportingPaneScaffoldRole.Supporting)
                    }) {
                        Text("Open Supporting Pane")
                    }
                }
            }
        },
        supportingPane = {
            AnimatedPane {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Supporting Info", style = MaterialTheme.typography.titleLarge)
                    // contextual content
                }
            }
        }
    )
}
```

---

## NavigationSuiteScaffold

Automatically picks `NavigationBar`, `NavigationRail`, or `NavigationDrawer` based on window size:

```kotlin
implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
```

```kotlin
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
fun AdaptiveNavApp() {
    var selected by remember { mutableIntStateOf(0) }
    val items = listOf("Home" to Icons.Filled.Home, "Search" to Icons.Filled.Search, "Profile" to Icons.Filled.Person)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            items.forEachIndexed { index, (label, icon) ->
                item(
                    icon = { Icon(icon, contentDescription = label) },
                    label = { Text(label) },
                    selected = selected == index,
                    onClick = { selected = index }
                )
            }
        }
    ) {
        // screen content
    }
}
```

---

## Fold-Aware Layout

```kotlin
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun FoldAwareLayout() {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isFolded = adaptiveInfo.windowPosture.isTabletop

    if (isFolded) {
        // Tabletop mode (hinge in the middle)
        Column {
            Box(modifier = Modifier.weight(1f)) { /* top half */ }
            HorizontalDivider()
            Box(modifier = Modifier.weight(1f)) { /* bottom half */ }
        }
    } else {
        // Normal layout
        Box(modifier = Modifier.fillMaxSize()) { /* full content */ }
    }
}
```

---

## Pane Preferred Height

Control the preferred heights of panes for flexible layout (added in adaptive 1.2.0):

```kotlin
ListDetailPaneScaffold(
    // ...
    listPane = {
        AnimatedPane(
            modifier = Modifier.preferredWidth(320.dp)  // preferred width hint
        ) { /* list content */ }
    }
)
```

---

## Edge-to-Edge and Margins (adaptive 1.3.0+)

`ListDetailPaneScaffold` and `SupportingPaneScaffold` now support margins and edge-to-edge:

```kotlin
ListDetailPaneScaffold(
    directive = navigator.scaffoldDirective,
    value = navigator.scaffoldValue,
    // Margins applied automatically in 1.3.0+
    listPane = { AnimatedPane { /* ... */ } },
    detailPane = { AnimatedPane { /* ... */ } }
)
```

---

## Design Guidelines

- Always test on phone, foldable, and tablet form factors
- Use `NavigationSuiteScaffold` instead of hardcoding `NavigationBar`
- Use `ListDetailPaneScaffold` for any list → detail navigation pattern
- Respond to fold state for media apps and productivity apps
- Target minimum touch targets: 48×48dp across all form factors
- Use `AnimatedPane` for smooth pane transitions in adaptive scaffolds
