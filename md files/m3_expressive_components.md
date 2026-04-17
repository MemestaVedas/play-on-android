# Material 3 Expressive — Complete Component & API Reference

**Purpose:** Authoritative guide for every AI agent working on PLAY-ON UI. Covers every M3 Expressive composable, token, and migration rule relevant to this app.

**Last Updated:** 2026-04-16  
**Min Dependency:** `androidx.compose.material3:material3:1.4.0-alpha14+`  
**BOM:** `androidx.compose:compose-bom:2025.12.00+`

---

## 0. Quick Rules Table (NEVER Break These)

| ❌ BANNED | ✅ REPLACEMENT | Reason |
|---|---|---|
| `CircularProgressIndicator` | `ContainedLoadingIndicator` or `LoadingIndicator` | M3 Expressive loading pattern |
| `LinearProgressIndicator` | `LoadingIndicator` variants | M3 Expressive |
| hardcoded `tween(...)` durations | `MaterialTheme.motionScheme.*Spec()` | Centralized motion tokens |
| `Toast.makeText()` | `Snackbar` via `SnackbarHostState` | M3 UX consistency |
| Hardcoded colors (`Color(0xFF...)`) | `MaterialTheme.colorScheme.*` tokens | Dynamic color mandate |
| Hardcoded sizes (`16.dp`) | `MaterialTheme.spacing.*` or component defaults | Design system consistency |
| `MaterialTheme` without Expressive | `MaterialExpressiveTheme` | Required for M3E features |
| `SegmentedButton` (old) | `ButtonGroup` / `ToggleButton` patterns | Deprecated in M3E |

Motion note:
- Default to `MaterialTheme.motionScheme` tokens.
- Use raw `spring()` only for intentional expressive bounce or morph interactions where a tokenized spec does not match the interaction intent.

---

## 1. Theming Foundation

### 1.1 MaterialExpressiveTheme

The top-level entry point. **Always use this instead of `MaterialTheme` when using M3E features.**

```kotlin
@Composable
fun AnimeAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = Color(0xFF3D2B8E),
            tertiary = Color(0xFF9C1B5A)
        )
        else -> lightColorScheme(
            primary = Color(0xFF3D2B8E),
            tertiary = Color(0xFF9C1B5A)
        )
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),   // ALWAYS expressive
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
```

**Notes:**
- `MaterialExpressiveTheme` wraps `MaterialTheme` and adds `motionScheme` parameter.
- You still access all `MaterialTheme.*` tokens inside it normally.
- `MotionScheme.expressive()` enables spring physics + bounce for all M3 components automatically.

---

### 1.2 MotionScheme — Physics-Based Animation

**M3 Expressive replaces duration/easing-based animations with spring physics.**

```kotlin
// Access the current motion scheme
val motionScheme = MaterialTheme.motionScheme

// Two spring spec types:
val spatialSpec = motionScheme.defaultSpatialSpec<Dp>()     // For position/size changes
val effectsSpec = motionScheme.defaultEffectsSpec<Float>()  // For opacity/color/alpha

// Two built-in schemes:
MotionScheme.expressive()   // Bouncy, alive — use for all UI in this app
MotionScheme.standard()     // Minimal bounce — for productivity/utility UIs only
```

**How to use spring specs in custom animations:**
```kotlin
// Animate a value using motion scheme spring (NOT tween/keyframes)
val scale by animateFloatAsState(
    targetValue = if (isExpanded) 1.2f else 1.0f,
    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
)

// For transition between states
updateTransition(targetState = uiState, label = "state_transition").also { transition ->
    val offset by transition.animateDp(
        transitionSpec = { MaterialTheme.motionScheme.defaultSpatialSpec() },
        label = "offset"
    ) { state -> if (state.isExpanded) 0.dp else (-56).dp }
}
```

**Rules:**
- **Never use** `tween()`, `spring(stiffness=...)`, or hardcoded duration for UI motion.
- **Always use** `motionScheme.defaultSpatialSpec()` for layout/position changes.
- **Always use** `motionScheme.defaultEffectsSpec()` for alpha/color transitions.
- Override locally only when a specific screen needs a different feel.

---

### 1.3 Color Scheme Tokens

Access via `MaterialTheme.colorScheme.*`:

| Token | PLAY-ON Use Case |
|---|---|
| `primary` | Primary CTA buttons (Play, Resume, Add to Library) |
| `onPrimary` | Text/icons on primary buttons |
| `primaryContainer` | Active nav destination, selected chip backgrounds |
| `onPrimaryContainer` | Text on primary containers |
| `secondary` | Tracker controls (score, status) |
| `secondaryContainer` | Tracking status chips |
| `tertiary` | Episode count badges, "New" indicators, AniList score |
| `tertiaryContainer` | Alert backgrounds |
| `surface` | Default card/screen background |
| `surfaceContainer` | Elevated cards, bottom sheets |
| `surfaceContainerHigh` | Dialogs, modals |
| `error` | Network failures, sync errors, destructive actions |
| `outline` | Borders, dividers |
| `outlineVariant` | Subtle separators |

---

### 1.4 Shape System (M3E Expanded)

M3E adds **35+ shape tokens** including polygon-based morphable shapes.

```kotlin
// Access via MaterialTheme.shapes.*
MaterialTheme.shapes.extraSmall    // 4.dp corner
MaterialTheme.shapes.small         // 8.dp corner
MaterialTheme.shapes.medium        // 12.dp corner
MaterialTheme.shapes.large         // 16.dp corner
MaterialTheme.shapes.extraLarge    // 28.dp corner
MaterialTheme.shapes.full          // Circle/pill (fully rounded)

// M3E Expressive shapes (require @ExperimentalMaterial3ExpressiveApi)
// Use MaterialShapes for morphable polygon shapes
val shapes = MaterialShapes  // Object containing all M3E polygon shapes
```

**For PLAY-ON cards (media covers):** Use `MaterialTheme.shapes.medium` (12dp)  
**For FABs:** Use `MaterialTheme.shapes.large` (16dp)  
**For chips/tags:** Use `MaterialTheme.shapes.full` (pill)  
**For buttons:** Use `MaterialTheme.shapes.full` (standard M3E pill buttons)

---

### 1.5 Typography System

Access via `MaterialTheme.typography.*`:

```kotlin
// Display styles (anime hero titles, feature text)
MaterialTheme.typography.displayLarge    // 57sp
MaterialTheme.typography.displayMedium   // 45sp
MaterialTheme.typography.displaySmall    // 36sp

// Headlines (section titles, media titles)
MaterialTheme.typography.headlineLarge   // 32sp
MaterialTheme.typography.headlineMedium  // 28sp
MaterialTheme.typography.headlineSmall   // 24sp

// Titles (card titles, screen titles)
MaterialTheme.typography.titleLarge      // 22sp — use for media title in detail
MaterialTheme.typography.titleMedium     // 16sp — card title, list item primary
MaterialTheme.typography.titleSmall      // 14sp — badge labels, chip text

// Body (description, synopsis, read-through text)
MaterialTheme.typography.bodyLarge       // 16sp
MaterialTheme.typography.bodyMedium      // 14sp — default body text
MaterialTheme.typography.bodySmall       // 12sp — metadata, captions

// Labels (compact UI, buttons)
MaterialTheme.typography.labelLarge      // 14sp — button text
MaterialTheme.typography.labelMedium     // 12sp — chip labels
MaterialTheme.typography.labelSmall      // 11sp — badges, tiny labels
```

---

## 2. M3 Expressive — New Components

> All require `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`

### 2.1 ContainedLoadingIndicator ✅ (replaces CircularProgressIndicator)

**Always use this instead of `CircularProgressIndicator`.**

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingState() {
    // Full-screen loading
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ContainedLoadingIndicator()
    }
}

// With custom container color
ContainedLoadingIndicator(
    modifier = Modifier.size(64.dp),
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    polygons = ContainedLoadingIndicatorDefaults.WavyPolygons
)
```

**Variants:**
- `ContainedLoadingIndicator` — Animated shape inside a colored container
- `LoadingIndicator` — Shape-only without container (for inline use)
- `LinearWavyProgressIndicator` — Horizontal wavy loading bar (replaces `LinearProgressIndicator`)

```kotlin
// Inline usage (e.g., button loading state)
LoadingIndicator(
    modifier = Modifier.size(24.dp),
    color = MaterialTheme.colorScheme.onPrimary
)

// Progress-aware loading bar
LinearWavyProgressIndicator(
    progress = { downloadProgress },
    modifier = Modifier.fillMaxWidth()
)
```

---

### 2.2 ButtonGroup — Connected Button Groups

Replaces old `SegmentedButton`. Use for grouped actions with physics animations.

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaActionGroup(
    onWatchClick: () -> Unit,
    onReadClick: () -> Unit,
    onTrackClick: () -> Unit
) {
    // Maximum recommended: 3 buttons in a group
    ButtonGroup(
        modifier = Modifier.fillMaxWidth()
    ) {
        // First button — expanded on press, neighbors compress
        Button(
            onClick = onWatchClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Watch")
        }
        Button(
            onClick = onReadClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Rounded.MenuBook, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Read")
        }
        Button(
            onClick = onTrackClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Rounded.Bookmark, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Track")
        }
    }
}
```

**Notes:**
- `ButtonGroup` provides automatic press-expand/compress bump animation.
- Child buttons keep total group width constant on press.
- Each child should be a standard M3 `Button`, `FilledTonalButton`, or `OutlinedButton`.

---

### 2.3 ToggleButton — Single/Multi Select Patterns

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatusFilterGroup(
    selectedStatus: AnimeStatus,
    onStatusSelected: (AnimeStatus) -> Unit
) {
    // Single-select toggle group (replaces SegmentedButton for status filters)
    val statuses = listOf(
        AnimeStatus.WATCHING, AnimeStatus.COMPLETED, AnimeStatus.PLAN_TO_WATCH
    )
    ButtonGroup {
        statuses.forEach { status ->
            ToggleButton(
                checked = selectedStatus == status,
                onCheckedChange = { onStatusSelected(status) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = status.displayName)
            }
        }
    }
}
```

---

### 2.4 FloatingToolbar

Contextual toolbar that floats over content. Perfect for reader/player overlay controls.

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReaderToolbar(
    isVisible: Boolean,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onBookmark: () -> Unit
) {
    // Horizontal floating toolbar
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it } + fadeIn(
            animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
        ),
        exit = slideOutVertically { it } + fadeOut(
            animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
        )
    ) {
        FloatingToolbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            IconButton(onClick = onPreviousChapter) {
                Icon(Icons.Rounded.SkipPrevious, "Previous")
            }
            IconButton(onClick = onNextChapter) {
                Icon(Icons.Rounded.SkipNext, "Next")
            }
            IconButton(onClick = onBookmark) {
                Icon(Icons.Rounded.Bookmark, "Bookmark")
            }
        }
    }
}

// Vertical variant for player side controls
FloatingToolbar(
    orientation = Orientation.Vertical
) { /* icon buttons */ }
```

---

### 2.5 WideNavigationRail / ModalWideNavigationRail

Expanded navigation rail for tablets and foldables.

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TabletShell(
    navController: NavController,
    currentTab: AppTab
) {
    PermanentNavigationDrawer(
        drawerContent = {
            WideNavigationRail(
                header = {
                    // App logo / user avatar
                    AppLogo()
                }
            ) {
                AppTab.entries.forEach { tab ->
                    WideNavigationRailItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = currentTab == tab,
                        onClick = { navController.navigate(tab.route) }
                    )
                }
            }
        }
    ) {
        // Main content
    }
}

// Modal variant (drawer-like, dismissible)
ModalWideNavigationRail(
    expanded = isDrawerOpen,
    onDismissRequest = { isDrawerOpen = false }
) {
    // Same WideNavigationRailItem content
}
```

---

### 2.6 SplitButton

Primary action + secondary menu combined. Great for "Play" + "Download" combos.

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaySplitButton(
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    onStream: () -> Unit
) {
    SplitButton(
        leadingButton = {
            Button(onClick = onPlay) {
                Icon(Icons.Rounded.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Play")
            }
        },
        trailingButton = {
            // Opens dropdown menu
            FilledIconButton(onClick = { /* toggle menu */ }) {
                Icon(Icons.Rounded.ArrowDropDown, null)
            }
        }
    )
    // Manage DropdownMenu separately
}
```

---

### 2.7 FAB Menu (Extended FAB with grouped actions)

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryFabMenu(
    onAddManga: () -> Unit,
    onAddAnime: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // FAB menu group using standard FAB + AnimatedVisibility pattern
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(visible = expanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(onClick = onAddManga) {
                    Icon(Icons.Rounded.MenuBook, "Add Manga")
                }
                SmallFloatingActionButton(onClick = onAddAnime) {
                    Icon(Icons.Rounded.Movie, "Add Anime")
                }
            }
        }
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = if (expanded) Icons.Rounded.Close else Icons.Rounded.Add,
                contentDescription = "Menu"
            )
        }
    }
}
```

---

## 3. Standard M3 Components — Usage Guide for PLAY-ON

### 3.1 Buttons

```kotlin
// PRIMARY — highest emphasis (Watch, Play, Start Reading)
Button(
    onClick = onPrimaryAction,
    shape = MaterialTheme.shapes.full  // pill shape in M3E
) {
    Icon(icon, contentDescription = null)
    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
    Text("Resume Episode 5")
}

// FILLED TONAL — medium emphasis (Add to Library, Download)
FilledTonalButton(onClick = onAddToLibrary) {
    Text("Add to Library")
}

// ELEVATED — for surfaces (secondary contextual actions)
ElevatedButton(onClick = onAction) {
    Text("View on AniList")
}

// OUTLINED — low-emphasis, paired with primary
OutlinedButton(onClick = onCancel) {
    Text("Cancel")
}

// TEXT — lowest emphasis (inline links, navigation)
TextButton(onClick = onViewAll) {
    Text("View All")
}
```

---

### 3.2 Icon Buttons

```kotlin
// Standard icon button (borderless)
IconButton(onClick = onBookmark) {
    Icon(Icons.Rounded.BookmarkBorder, "Bookmark")
}

// Filled icon button (high emphasis)
FilledIconButton(onClick = onPlay) {
    Icon(Icons.Rounded.PlayArrow, "Play")
}

// Filled tonal icon button (medium emphasis)
FilledTonalIconButton(onClick = onDownload) {
    Icon(Icons.Rounded.Download, "Download")
}

// Outlined icon button
OutlinedIconButton(onClick = onShare) {
    Icon(Icons.Rounded.Share, "Share")
}

// Toggle icon button
IconToggleButton(
    checked = isBookmarked,
    onCheckedChange = { onBookmarkToggle(it) }
) {
    Icon(
        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
        contentDescription = "Bookmark"
    )
}
```

---

### 3.3 Cards

```kotlin
// FILLED card (default, for library items)
Card(
    onClick = onCardClick,
    modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(2f / 3f),  // portrait cover ratio
    shape = MaterialTheme.shapes.medium
) {
    // Cover image + title
}

// ELEVATED card (for featured/hero content)
ElevatedCard(
    onClick = onCardClick,
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
) {
    // Featured anime card
}

// OUTLINED card (for secondary widgets)
OutlinedCard(
    modifier = Modifier.fillMaxWidth()
) {
    // Stats widget, tracking summary
}
```

---

### 3.4 Chips

```kotlin
// ASSIST chip — for actions in context (e.g., "Open AniList", genre links)
AssistChip(
    onClick = onOpenAniList,
    label = { Text("AniList") },
    leadingIcon = { Icon(Icons.Rounded.OpenInNew, null, Modifier.size(18.dp)) }
)

// FILTER chip — togglable genre/status filters ⭐ most used in PLAY-ON
FilterChip(
    selected = isSelected,
    onClick = { onFilterSelected(genre) },
    label = { Text(genre) },
    leadingIcon = if (isSelected) {
        { Icon(Icons.Rounded.Check, null, Modifier.size(18.dp)) }
    } else null
)

// INPUT chip — removable tags (selected categories list)
InputChip(
    selected = true,
    onClick = { onCategoryRemove(category) },
    label = { Text(category) },
    trailingIcon = { Icon(Icons.Rounded.Close, "Remove", Modifier.size(18.dp)) }
)

// SUGGESTION chip — quick-add suggestions
SuggestionChip(
    onClick = onSuggestionAccept,
    label = { Text("Completed") }
)
```

---

### 3.5 Navigation Bar (Phone)

```kotlin
@Composable
fun AppNavigationBar(
    currentRoute: String,
    onNavigate: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp
    ) {
        AppTab.entries.forEach { tab ->
            NavigationBarItem(
                icon = {
                    BadgedBox(
                        badge = {
                            if (tab.hasUpdates) Badge { Text("${tab.updateCount}") }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentRoute == tab.route) tab.selectedIcon else tab.icon,
                            contentDescription = tab.label
                        )
                    }
                },
                label = { Text(tab.label) },
                selected = currentRoute == tab.route,
                onClick = { onNavigate(tab) },
                alwaysShowLabel = false  // Only show label when selected
            )
        }
    }
}
```

---

### 3.6 Navigation Rail (Tablet/Foldable)

```kotlin
@Composable
fun AppNavigationRail(
    currentRoute: String,
    onNavigate: (AppTab) -> Unit
) {
    NavigationRail(
        header = {
            // App logo at top of rail
            IconButton(onClick = { /* expand to drawer */ }) {
                Icon(Icons.Rounded.Menu, "Menu")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(Modifier.fillMaxHeight().weight(1f))
        AppTab.entries.forEach { tab ->
            NavigationRailItem(
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                selected = currentRoute == tab.route,
                onClick = { onNavigate(tab) }
            )
        }
        Spacer(Modifier.fillMaxHeight().weight(1f))
    }
}
```

---

### 3.7 Top App Bar

```kotlin
// SMALL — standard screens (library, browse)
TopAppBar(
    title = { Text("Library") },
    navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
        }
    },
    actions = {
        IconButton(onClick = onSearch) {
            Icon(Icons.Rounded.Search, "Search")
        }
        IconButton(onClick = onFilter) {
            Icon(Icons.Rounded.FilterList, "Filter")
        }
    },
    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
)

// LARGE — detail screens (collapses on scroll)
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
LargeTopAppBar(
    title = { Text(mediaTitle) },
    navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
        }
    },
    scrollBehavior = scrollBehavior,
    modifier = Modifier.nestedScrollConnection(scrollBehavior.nestedScrollConnection)
)

// MEDIUM — settings, secondary screens
MediumTopAppBar(
    title = { Text("Settings") },
    navigationIcon = { /* back button */ },
    scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
)

// CENTERED — for symmetric screens
CenterAlignedTopAppBar(
    title = { Text("Discover") }
)
```

---

### 3.8 Bottom Sheets

```kotlin
// MODAL bottom sheet — for filter, sort, tracking options
@Composable
fun TrackingBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onStatusChange: (TrackStatus) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        ) {
            Text(
                text = "Update Status",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            // Status options
            TrackStatus.entries.forEach { status ->
                ListItem(
                    headlineContent = { Text(status.displayName) },
                    trailingContent = {
                        RadioButton(selected = currentStatus == status, onClick = { onStatusChange(status) })
                    },
                    modifier = Modifier.clickable { onStatusChange(status) }
                )
            }
        }
    }
}

// Usage
val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
var showSheet by remember { mutableStateOf(false) }

if (showSheet) {
    TrackingBottomSheet(
        sheetState = sheetState,
        onDismiss = { showSheet = false },
        onStatusChange = viewModel::updateStatus
    )
}
```

---

### 3.9 Dialog

```kotlin
// Standard confirmation dialog
AlertDialog(
    onDismissRequest = onDismiss,
    icon = { Icon(Icons.Rounded.Delete, null) },
    title = { Text("Remove from Library?") },
    text = { Text("This will also delete all downloaded chapters.") },
    confirmButton = {
        Button(onClick = onConfirm) { Text("Remove") }
    },
    dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cancel") }
    },
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    shape = MaterialTheme.shapes.extraLarge
)

// Custom dialog with full control
Dialog(onDismissRequest = onDismiss) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp
    ) {
        // Custom content
    }
}
```

---

### 3.10 Snackbar (replaces Toast)

```kotlin
// In your Screen composable
val snackbarHostState = remember { SnackbarHostState() }

Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
) { paddingValues ->
    // Screen content

    // Show from ViewModel event (never from coroutine directly in UI)
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
        }
    }
}
```

---

### 3.11 Scaffold

Every screen must wrap content in `Scaffold`.

```kotlin
@Composable
fun LibraryScreen(viewModel: LibraryViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                scrollBehavior = scrollBehavior,
                actions = { /* filter, search */ }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onFabClick) {
                Icon(Icons.Rounded.Add, "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        // Content with padding to avoid overlapping system UI
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            contentPadding = paddingValues
        ) { /* ... */ }
    }
}
```

---

### 3.12 Search Bar

```kotlin
// DOCKED search bar (top of screen, no expansion)
DockedSearchBar(
    inputField = {
        SearchBarDefaults.InputField(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            onSearch = viewModel::onSearch,
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            placeholder = { Text("Search anime, manga...") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = viewModel::clearSearch) {
                        Icon(Icons.Rounded.Clear, "Clear")
                    }
                }
            }
        )
    },
    expanded = isExpanded,
    onExpandedChange = { isExpanded = it }
) {
    // Search suggestions / results
    LazyColumn {
        items(suggestions) { suggestion ->
            ListItem(
                headlineContent = { Text(suggestion.title) },
                leadingContent = { /* thumbnail */ },
                modifier = Modifier.clickable { viewModel.onSuggestionSelect(suggestion) }
            )
        }
    }
}
```

---

### 3.13 Progress Indicators

```kotlin
// Determinate — show a specific progress value
// Use LinearWavyProgressIndicator (M3E)
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
LinearWavyProgressIndicator(
    progress = { episodeProgress },   // 0.0f–1.0f
    modifier = Modifier.fillMaxWidth().height(4.dp),
    color = MaterialTheme.colorScheme.primary,
    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
)

// Indeterminate — for unknown duration
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
ContainedLoadingIndicator(
    modifier = Modifier.align(Alignment.Center)
)

// Inline indeterminate (small, inside buttons or list items)
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
LoadingIndicator(
    modifier = Modifier.size(24.dp),
    color = MaterialTheme.colorScheme.onPrimaryContainer
)
```

---

### 3.14 Slider

```kotlin
// Episode volume / brightness control
Slider(
    value = currentProgress,
    onValueChange = viewModel::onSeek,
    valueRange = 0f..1f,
    modifier = Modifier.fillMaxWidth(),
    colors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary
    )
)

// Rating slider (0–10)
Slider(
    value = score,
    onValueChange = { viewModel.onScoreChange(it) },
    valueRange = 0f..10f,
    steps = 9  // 0.0, 1.0, 2.0... 10.0
)
```

---

### 3.15 Pull-to-Refresh

```kotlin
val pullToRefreshState = rememberPullToRefreshState()

PullToRefreshBox(
    isRefreshing = uiState.isRefreshing,
    onRefresh = viewModel::refresh,
    state = pullToRefreshState,
    indicator = {
        // Use default Expressive indicator
        PullToRefreshDefaults.Indicator(
            state = pullToRefreshState,
            isRefreshing = uiState.isRefreshing,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
) {
    LazyColumn { /* content */ }
}
```

---

### 3.16 Tabs

```kotlin
// SCROLLABLE tabs (for media detail: Info / Episodes / Similar / Reviews)
val tabTitles = listOf("Info", "Episodes", "Similar", "Reviews")
var selectedTabIndex by remember { mutableIntStateOf(0) }

Column {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        edgePadding = 16.dp,
        divider = {}
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            )
        }
    }
    HorizontalDivider()
    // Tab content
    when (selectedTabIndex) {
        0 -> MediaInfoTab()
        1 -> EpisodesTab()
        2 -> SimilarTab()
        3 -> ReviewsTab()
    }
}
```

---

### 3.17 Carousel (Media Browsing)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturedCarousel(items: List<Anime>) {
    val carouselState = rememberCarouselState { items.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = 200.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { index ->
        val item = items[index]
        CarouselItemSurface(
            modifier = Modifier
                .fillMaxHeight()
                .maskClip(MaterialTheme.shapes.medium)
        ) {
            // Cover image + title overlay
            AsyncImage(
                model = item.coverUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
```

---

### 3.18 List Item

```kotlin
// Standard list item (e.g., episode list)
ListItem(
    headlineContent = { Text("Episode 1 — The Beginning") },
    supportingContent = { Text("23 min • Jan 1, 2024") },
    leadingContent = {
        // Episode thumbnail
        AsyncImage(
            model = episode.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(width = 80.dp, height = 56.dp)
                .clip(MaterialTheme.shapes.small)
        )
    },
    trailingContent = {
        // Watched indicator
        if (episode.isWatched) {
            Icon(
                Icons.Rounded.CheckCircle,
                "Watched",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    },
    modifier = Modifier.clickable(onClick = { onEpisodeClick(episode) }),
    colors = ListItemDefaults.colors(
        containerColor = if (episode.isWatched)
            MaterialTheme.colorScheme.surfaceContainerLowest
        else MaterialTheme.colorScheme.surface
    )
)
```

---

### 3.19 Switch & Checkbox

```kotlin
// Switch — for boolean settings
ListItem(
    headlineContent = { Text("Auto-track progress") },
    supportingContent = { Text("Update AniList when 85%+ watched") },
    trailingContent = {
        Switch(
            checked = isAutoTrackEnabled,
            onCheckedChange = viewModel::onAutoTrackToggle
        )
    },
    modifier = Modifier.clickable { viewModel.onAutoTrackToggle(!isAutoTrackEnabled) }
)

// Checkbox — for multi-select lists
Row(
    modifier = Modifier.clickable { onToggle(item) },
    verticalAlignment = Alignment.CenterVertically
) {
    Checkbox(
        checked = isSelected,
        onCheckedChange = { onToggle(item) }
    )
    Spacer(Modifier.width(8.dp))
    Text(item.label)
}
```

---

### 3.20 Date & Time Pickers

```kotlin
// Date picker — for start/finish date in tracking
val datePickerState = rememberDatePickerState(
    initialSelectedDateMillis = trackEntry.startDate
)

DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
        TextButton(onClick = {
            viewModel.onStartDateSet(datePickerState.selectedDateMillis)
            onDismiss()
        }) { Text("OK") }
    },
    dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cancel") }
    }
) {
    DatePicker(state = datePickerState)
}
```

---

### 3.21 Tooltips

```kotlin
// Plain tooltip (label-only)
PlainTooltip(
    tooltip = { Text("Add to Library") }
) {
    IconButton(onClick = onAddToLibrary) {
        Icon(Icons.Rounded.BookmarkAdd, "Add to Library")
    }
}

// Rich tooltip (with title, body, actions)
RichTooltip(
    title = { Text("Download Episode") },
    text = { Text("Downloads for offline viewing. Uses ~250MB storage.") },
    action = {
        TextButton(onClick = onLearnMore) { Text("Learn more") }
    }
) {
    IconButton(onClick = onDownload) {
        Icon(Icons.Rounded.Download, "Download")
    }
}
```

---

### 3.22 Menu & Dropdown

```kotlin
// Standard dropdown menu
Box {
    IconButton(onClick = { menuExpanded = true }) {
        Icon(Icons.Rounded.MoreVert, "More")
    }
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("Mark all watched") },
            onClick = {
                viewModel.markAllWatched()
                menuExpanded = false
            },
            leadingIcon = { Icon(Icons.Rounded.DoneAll, null) }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Delete") },
            onClick = { viewModel.delete(); menuExpanded = false },
            leadingIcon = { Icon(Icons.Rounded.Delete, null) },
            colors = MenuDefaults.itemColors(
                textColor = MaterialTheme.colorScheme.error,
                leadingIconColor = MaterialTheme.colorScheme.error
            )
        )
    }
}
```

---

## 4. Component-to-Screen Mapping for PLAY-ON

| Screen | Primary Components |
|---|---|
| **Home / Dashboard** | `Carousel`, `Card` (ElevatedCard), `ContainedLoadingIndicator` |
| **Library** | `Scaffold`, `TopAppBar` (pinned), `LazyVerticalGrid`, `FilterChip`, `FloatingActionButton` |
| **Browse / Discover** | `DockedSearchBar`, `ScrollableTabRow`, `LazyColumn`, `AssistChip` |
| **Media Detail** | `LargeTopAppBar`, `ScrollableTabRow`, `ButtonGroup`, `SplitButton`, `LinearWavyProgressIndicator` |
| **Episode List** | `ListItem`, `LinearWavyProgressIndicator`, `FilterChip`, `DropdownMenu` |
| **Video Player** | `FloatingToolbar` (overlay), `Slider` (scrubber), `IconButton` controls |
| **Reader** | `FloatingToolbar` (bottom), page gesture surface, `Snackbar` for chapter transitions |
| **Tracker / AniList** | `ScrollableTabRow`, `Card`, `FilterChip`, `ModalBottomSheet` (status update) |
| **Activity Feed** | `LazyColumn`, `ListItem`, `OutlinedCard` |
| **Settings** | `Scaffold`, `MediumTopAppBar`, `Switch` items, `Slider` |
| **Downloads** | `ListItem`, `LinearWavyProgressIndicator`, `IconButton` |
| **Phone Navigation** | `NavigationBar` + `NavigationBarItem` |
| **Tablet Navigation** | `WideNavigationRail` + `WideNavigationRailItem` |

---

## 5. Adaptive Layout Patterns

### 5.1 WindowSizeClass Usage

```kotlin
@Composable
fun AdaptiveAppShell(content: @Composable (PaddingValues) -> Unit) {
    val windowSizeClass = calculateWindowSizeClass()

    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Phone: NavigationBar at bottom
            Scaffold(
                bottomBar = { AppNavigationBar(/* ... */) }
            ) { content(it) }
        }
        WindowWidthSizeClass.Medium -> {
            // Foldable/small tablet: NavigationRail on side
            Row {
                AppNavigationRail(/* ... */)
                Scaffold { content(it) }
            }
        }
        WindowWidthSizeClass.Expanded -> {
            // Tablet: WideNavigationRail (permanent drawer)
            PermanentNavigationDrawer(
                drawerContent = { AppWideNavigationRail(/* ... */) }
            ) {
                Scaffold { content(it) }
            }
        }
    }
}
```

### 5.2 List-Detail Pattern (Media + Episodes)

```kotlin
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MediaListDetailScreen() {
    val navigator = rememberListDetailPaneScaffoldNavigator<Anime>()

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                MediaListPane(
                    onMediaClick = {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val media = navigator.currentDestination?.contentKey
                if (media != null) {
                    MediaDetailScreen(media = media)
                } else {
                    EmptyDetailPlaceholder()
                }
            }
        }
    )
}
```

---

## 6. State Handling Template

Every screen must handle ALL these states:

```kotlin
@Composable
fun ExampleScreen(viewModel: ExampleViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-time events (errors, navigation)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is Event.ShowError -> snackbarHostState.showSnackbar(event.message)
                is Event.Navigate -> { /* navigation */ }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    ContainedLoadingIndicator()    // NEVER CircularProgressIndicator
                }
            }
            is UiState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = viewModel::retry
                )
            }
            is UiState.Empty -> {
                EmptyScreen(message = "No items found")
            }
            is UiState.Success -> {
                // Actual content
                SuccessContent(
                    data = state.data,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
```

---

## 7. Dependencies

```toml
# In libs.versions.toml

[versions]
composeBom = "2025.12.00"   # or latest
material3 = "1.4.0-alpha14" # minimum for M3 Expressive

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
material3 = { group = "androidx.compose.material3", name = "material3" }
# If using BOM, material3 version is managed automatically
```

```kotlin
// In build.gradle.kts
dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.material3)
}
```

---

## 8. Opt-In Annotations

Add to your composables when using experimental M3E APIs:

```kotlin
// File-level opt-in (preferred for entire screen files)
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

// Or per-composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyScreen() { /* ... */ }

// Or in build.gradle.kts for entire module
kotlinOptions {
    freeCompilerArgs += listOf(
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
    )
}
```

---

## 9. Common Mistakes & Fixes

| Mistake | Fix |
|---|---|
| Using `CircularProgressIndicator()` | Use `ContainedLoadingIndicator()` |
| `animationSpec = tween(300)` | `animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()` |
| `Color(0xFFxxxxxx)` anywhere in UI | `MaterialTheme.colorScheme.primary` (or appropriate token) |
| `fontSize = 16.sp` in Text | `style = MaterialTheme.typography.bodyMedium` |
| `shape = RoundedCornerShape(12.dp)` | `shape = MaterialTheme.shapes.medium` |
| `Toast.makeText(...)` | `snackbarHostState.showSnackbar(...)` |
| Logic in `@Composable` function | Move to ViewModel |
| `collectAsState()` | `collectAsStateWithLifecycle()` |
| Missing padding in Scaffold content | Always pass `paddingValues` from Scaffold lambda |
| `NavigationDrawer` on phone | Use `NavigationBar` on compact, rail/drawer on medium/expanded |
