# Progress Indicators (Core M3)

M3 provides two classic progress indicator types plus the new M3 Expressive `LoadingIndicator`.

---

## Indicator Types

| Component | Use Case |
|---|---|
| `CircularProgressIndicator` | Indeterminate loading OR known progress % |
| `LinearProgressIndicator` | Indeterminate or determinate background operations |
| `LoadingIndicator` *(M3 Expressive)* | Short loads < 5s; replaces indeterminate circular in most cases |

---

## CircularProgressIndicator

### Indeterminate (spinning)

```kotlin
CircularProgressIndicator(
    modifier = Modifier.size(48.dp),
    color = MaterialTheme.colorScheme.primary,
    strokeWidth = 4.dp
)
```

### Determinate (known progress)

```kotlin
var progress by remember { mutableFloatStateOf(0f) }

CircularProgressIndicator(
    progress = { progress },  // lambda for state read optimization
    modifier = Modifier.size(48.dp),
    color = MaterialTheme.colorScheme.primary,
    trackColor = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth = 6.dp,
    strokeCap = StrokeCap.Round
)

// Drive progress with a slider or coroutine
Slider(value = progress, onValueChange = { progress = it })
```

---

## LinearProgressIndicator

### Indeterminate

```kotlin
LinearProgressIndicator(
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.primary,
    trackColor = MaterialTheme.colorScheme.surfaceVariant
)
```

### Determinate

```kotlin
var downloadProgress by remember { mutableFloatStateOf(0.4f) }

Column {
    LinearProgressIndicator(
        progress = { downloadProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = "${(downloadProgress * 100).toInt()}% downloaded",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
```

---

## LoadingIndicator (M3 Expressive)

See the dedicated `loading-indicator.md` for full docs. Quick reference:

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuickLoadingIndicator() {
    LoadingIndicator(
        modifier = Modifier.size(64.dp),
        color = MaterialTheme.colorScheme.primary
    )
}
```

**When to use each:**

| Situation | Use |
|---|---|
| Load < 5 seconds expected | `LoadingIndicator` |
| Pull-to-refresh | `PullToRefreshBox` (uses `LoadingIndicator`) |
| Upload/download with % | `CircularProgressIndicator(progress = { })` |
| Streaming/long-running background task | `LinearProgressIndicator` |

---

## Animated Progress

Smooth progress animation with `animateFloatAsState`:

```kotlin
var targetProgress by remember { mutableFloatStateOf(0f) }
val animatedProgress by animateFloatAsState(
    targetValue = targetProgress,
    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
    label = "progress animation"
)

LinearProgressIndicator(
    progress = { animatedProgress },
    modifier = Modifier.fillMaxWidth()
)

Button(onClick = { targetProgress = minOf(targetProgress + 0.1f, 1f) }) {
    Text("Advance")
}
```

---

## Full-Screen Loading State

```kotlin
@Composable
fun FullScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                "Loading…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

---

## Inline Loading in a Button

```kotlin
var isLoading by remember { mutableStateOf(false) }

Button(
    onClick = {
        isLoading = true
        // launch coroutine, reset isLoading when done
    },
    enabled = !isLoading
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
        )
        Spacer(Modifier.width(8.dp))
    }
    Text("Submit")
}
```

---

## Design Guidelines

- Prefer `LoadingIndicator` (M3 Expressive) for transient screen loads — it's more expressive and engaging
- Use `LinearProgressIndicator` at the top of a screen for background syncing (don't block UI)
- Determinate indicators are always preferable when you can calculate progress — they reduce anxiety
- Don't show both a progress indicator and a skeleton simultaneously — pick one approach
- Always set `contentDescription` or pair with a `Text` label for accessibility
