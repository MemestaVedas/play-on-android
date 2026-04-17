# LoadingIndicator (M3 Expressive)

`LoadingIndicator` is a **new M3 Expressive component** that shows loading progress through a continuously morphing shape animation. It is designed for loads expected to complete in **under 5 seconds** and is the replacement for the indeterminate `CircularProgressIndicator` in most use cases.

> **Status:** `@ExperimentalMaterial3ExpressiveApi` — requires opt-in

---

## Design Spec

- Continuously morphs through a sequence of shapes (circle → squircle → rounded square → etc.)
- Used in **pull-to-refresh** behavior built into M3 Expressive
- Should replace: `CircularProgressIndicator` (indeterminate) for short-lived loads
- Keep using `CircularProgressIndicator` (determinate) when you can show real progress %
- Keep using `LinearProgressIndicator` for long-running background operations

---

## Basic Usage

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BasicLoadingIndicator() {
    LoadingIndicator()
}
```

---

## With Custom Color

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColoredLoadingIndicator() {
    LoadingIndicator(
        color = MaterialTheme.colorScheme.secondary
    )
}
```

---

## Conditional Display

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConditionalLoading(isLoading: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            LoadingIndicator()
        } else {
            // actual content
        }
    }
}
```

---

## In PullToRefresh (M3 Expressive)

The `PullToRefreshBox` in M3 Expressive uses `LoadingIndicator` internally:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RefreshableList() {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                delay(2000L) // simulate network call
                isRefreshing = false
            }
        }
    ) {
        LazyColumn {
            items(30) { index ->
                ListItem(headlineContent = { Text("Item $index") })
                HorizontalDivider()
            }
        }
    }
}
```

The `PullToRefreshBox` gained `enabled` and `threshold` parameters in material3 `1.4.0`:

```kotlin
PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = { /* ... */ },
    enabled = true,           // disable pull-to-refresh while already loading
    threshold = 80.dp         // how far to pull before triggering refresh
) { /* content */ }
```

---

## In a Loading Screen

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoadingIndicator(
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading your content…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

---

## When to Use What

| Scenario | Component |
|---|---|
| App/screen loading (< 5s expected) | `LoadingIndicator` |
| Pull-to-refresh | `PullToRefreshBox` (uses LoadingIndicator internally) |
| Known progress (e.g., upload %) | `CircularProgressIndicator(progress = { value })` |
| Long background task (download, sync) | `LinearProgressIndicator` |
| Skeleton loading | Custom shimmer composables (no built-in M3 skeleton) |

---

## Design Guidelines

- Do not use `LoadingIndicator` for loads that may take more than ~5 seconds; users need more feedback (progress bar or skeleton)
- Avoid showing both `LoadingIndicator` and content simultaneously — use `AnimatedContent` for smooth transitions
- Center the indicator in the content area it represents; don't pin it to a corner
- The indicator is self-contained — do not add a spinner or additional text inside it
