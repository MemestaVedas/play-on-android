# M3 Expressive – Gap Analysis & Improvements
## Specific to the Merged Aniyomi + AniHyou-android Codebase

This document covers the **eight gaps** identified in the existing `refresh.md` migration guide. Each gap includes: what it is, why it matters for this specific codebase, where it applies, and concrete before/after code.

## M3 Expressive Visual Language Baseline (Add This Before Implementation)

Use this as the non-negotiable style baseline so migration work does not stop at API replacement.

### 1) Color Role Strategy (No Literal UI Colors)

- App-level surfaces: `surface`, `surfaceContainer`, `surfaceContainerLow`, `surfaceContainerHigh`
- Emphasis surfaces: `primaryContainer` and `secondaryContainer` only for selected/highlighted state
- Critical actions: `errorContainer` + `onErrorContainer`
- Disable direct `Color(0x...)` in feature UI unless rendering content media (poster/artwork)

Rule:
- If a composable has interaction state (`selected`, `pressed`, `active`), its color must come from `MaterialTheme.colorScheme` role mapping.

### 2) Shape Rhythm (Large, Confident, Consistent)

M3 Expressive reads best when shapes are intentionally larger and consistent by role:

- Page-level containers (sections, grouped settings): `extraLarge`
- Row-level containers (list rows, activity cards): `large`
- Small utilities (chips, badges, mini affordances): `small` to `medium`
- Hero/contextual action surfaces (toolbar, media header blocks): `extraLargeIncreased`

Rule:
- Never mix more than 3 distinct corner radii on one screen.
- Prefer tokenized `MaterialTheme.shapes.*` over inline `RoundedCornerShape(...)`.

### 3) Motion Personality (Spring First)

Expressive language in this app should feel responsive and spatial, not linear/fade-heavy:

- State transitions: `MaterialTheme.motionScheme.defaultSpatialSpec()`
- Quick icon/content swaps: `fastEffectsSpec()`
- Layout expansion/collapse: `defaultSpatialSpec()`
- Avoid custom `tween(...)` unless no tokenized spec exists

Rule:
- Every major interaction should communicate one of: hierarchy change, state confirmation, or continuity.

### 4) Interaction Hierarchy (One Hero Action Per Surface)

Per surface, define:
- 1 primary action (prominent container color + strongest shape)
- 1 to 2 secondary actions (tonal/neutral)
- overflow actions (vertical menu or split trailing action)

If more than one action appears primary, reduce one to tonal or move it into split/overflow.

### 5) Typography Expression (Not Just Size)

- Selection and active states should use variable font weight axis where available
- Dense metadata should use lower emphasis roles (`onSurfaceVariant`) with stable optical readability
- Avoid mixing many static bold overrides; use role + state-driven emphasis

### 6) Expressive Acceptance Checks

A screen is considered M3 Expressive-ready only if all are true:

- No hardcoded UI literals (`Color(0x...)`, ad-hoc corner shapes) outside theme/content-art cases
- Container hierarchy is visible at a glance (section > row > control)
- Primary action is obvious within 1 second
- Motion on interaction uses motionScheme token(s), not arbitrary timing
- Menus and contextual actions use grouping (gap/divider/split) where intent differs

---

## Gap 1 — AniHyou-Specific Components Have No Migration Inventory

### The Problem

The existing `refresh.md` treats the merged app as a single codebase with a single component tree. It is not. Aniyomi and AniHyou have distinct UI layers, separate module roots, and different design patterns. The AniHyou side has never been audited against M3E in the current guide.

### Why It Matters

The AniHyou screens are the primary surfaces users interact with for AniList tracking. They carry AniHyou's own dialog patterns, card styles, and list rows — all of which bypass Aniyomi's `presentation-core` wrappers and will be missed entirely if you only migrate Aniyomi's component tree.

### AniHyou Component Inventory (Needs Migration)

| Component | Location (AniHyou module) | M3E Migration Action |
|---|---|---|
| Score dialog (star/smiley/point) | `ui/media/edit/` | Replace custom dialog shape with `MaterialTheme.shapes.extraLarge`; use `ConnectedButtonGroup` for score type selector |
| Media detail sheet | `ui/media/details/` | Adopt expressive container pattern; standardize header shape to `extraLargeIncreased` |
| Character / staff cards | `ui/character/`, `ui/staff/` | Tokenize corner radius; use `tonal elevation` instead of shadow |
| Activity feed items | `ui/social/` | Replace ad-hoc row containers with M3E container components; use `surfaceContainer` role |
| Calendar/schedule view | `ui/calendar/` | Apply expressive shape tokens to day cells; migrate selection highlight to `primaryContainer` role |
| Notification/activity items | `ui/notifications/` | Apply `BadgedBox` for unread counts; tokenize item container shape |
| Media list rows (watching/reading) | `ui/medialist/` | Adopt M3E container pattern per row; migrate status color literals to semantic tokens |
| Stats charts | `ui/profile/stats/` | Migrate chart color fills to `primary`/`secondary`/`tertiary` colorScheme roles |
| Profile header | `ui/profile/` | Replace `profileColor` hex literals (see Gap 2 cross-reference); use expressive large shape for avatar container |
| Season browser | `ui/explore/season/` | Use `FilterChip` or `ConnectedButtonGroup` for season/year/format pickers |

### Migration Rule for AniHyou Screens

Treat every screen under the AniHyou module as its own migration target in Phase 3. Add a dedicated route cluster to the implementation steps:

```
Phase 3 — Screen clusters:
  1. Library + detail routes        (Aniyomi-origin)
  2. Browse + search routes         (Aniyomi-origin)
  3. Updates + history routes       (Aniyomi-origin)
  4. Tracker/AniList routes         (Aniyomi-origin)
  5. Settings and dialog routes     (Aniyomi-origin)
  6. AniList media detail + edit    (AniHyou-origin) ← ADD THIS
  7. AniList profile + social       (AniHyou-origin) ← ADD THIS
  8. AniList explore + calendar     (AniHyou-origin) ← ADD THIS
  9. AniList stats + notifications  (AniHyou-origin) ← ADD THIS
```

---

## Gap 2 — Vertical Menus (M3E New Pattern) Not Addressed

### The Problem

The existing guide does not mention M3E's redesigned vertical menu component, which was released post-IO 2025. This directly replaces the standard `DropdownMenu` usage throughout the app.

### What M3E Vertical Menus Add

- Rounded corners on the menu container itself (not just items)
- Selection state styling per item
- Two new layout options: **with divider** and **with gap**
- "Gap" groups related actions visually without a full-width divider line — more expressive than dividers and makes relationships between items clearer
- Refined submenu entry/exit motion using spring specs

### Where This Applies in the Codebase

Heavy menu users in this merged app:

| Screen / Component | Current Pattern | M3E Target |
|---|---|---|
| Library item long-press | `DropdownMenu` with flat items | Vertical menu with gap grouping (library actions vs. tracker actions) |
| Episode list overflow | `DropdownMenu` | Vertical menu with divider between playback and download actions |
| Browse source filter | Custom bottom sheet filter | Vertical menu + `FilterChip` groups for compact filter |
| AniList media list overflow | `DropdownMenu` per row | Vertical menu with gap (edit / delete / share) |
| Settings screen sections | Flat `Column` of items | Vertical menu containers for logical setting groups |
| Reader/player overflow | `DropdownMenu` | Vertical menu with gap separating navigation from settings actions |

### Before / After

**Before:**
```kotlin
DropdownMenu(
    expanded = expanded,
    onDismissRequest = { expanded = false },
) {
    DropdownMenuItem(text = { Text("Mark as read") }, onClick = { ... })
    DropdownMenuItem(text = { Text("Add to category") }, onClick = { ... })
    Divider()
    DropdownMenuItem(text = { Text("Delete download") }, onClick = { ... })
    DropdownMenuItem(text = { Text("Share") }, onClick = { ... })
}
```

**After (M3E Vertical Menu with gap):**
```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
DropdownMenu(
    expanded = expanded,
    onDismissRequest = { expanded = false },
    shape = MaterialTheme.shapes.large,
) {
    // Group 1 — content actions
    DropdownMenuItem(text = { Text("Mark as read") }, onClick = { ... })
    DropdownMenuItem(text = { Text("Add to category") }, onClick = { ... })

    DropdownMenuItemGap() // ← M3E gap separator (not a full-width divider)

    // Group 2 — destructive / secondary actions
    DropdownMenuItem(text = { Text("Delete download") }, onClick = { ... })
    DropdownMenuItem(text = { Text("Share") }, onClick = { ... })
}
```

### Rule for This Codebase

Audit every `DropdownMenu` call. Apply this decision matrix:

- 2–4 flat actions with no logical grouping → plain vertical menu, no gap/divider
- Actions with clear groupings (e.g., content vs. destructive) → vertical menu **with gap**
- Actions with many sub-categories (e.g., filter menus) → vertical menu **with divider** between categories

---

## Gap 3 — Short vs Tall Bottom Bar Decision Is Unresolved

### The Problem

M3E switched back to a short bottom navigation bar from the taller Material You style. The existing guide does not flag this as a required decision, leaving it ambiguous for the agent.

### Why It Is a Real Decision, Not Just a Style Choice

The tall vs short bar affects:
- How much content is visible below the fold on library/list screens
- The touch target size of nav items (tall bar = larger targets)
- Whether labels are always-visible or icon-only
- Tablet/foldable rail behavior (not affected by this change)

### Recommendation for This App

Use the **short bottom bar** to align with M3E spec and maximize content area on dense library/list screens. This app shows media grids and long episode lists — every dp counts.

However: apply the short bar **only on phone**. Keep the existing rail behavior on tablet unchanged.

### Implementation

**Step 1 — Reduce nav bar height:**
```kotlin
// presentation-core/.../components/material/NavigationBar.kt

NavigationBar(
    modifier = Modifier
        .windowInsetsPadding(WindowInsets.navigationBars)
        .height(56.dp), // ← M3E short bar: 56dp (was 80dp in Material You)
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    // nav items
}
```

**Step 2 — Confirm label behavior stays "always show"** (M3E short bar still shows labels, unlike icon-only rail):
```kotlin
NavigationBarItem(
    selected = selected,
    onClick = onClick,
    icon = { Icon(icon, contentDescription = null) },
    label = { Text(label) }, // ← keep label; short bar still renders it
    alwaysShowLabel = true,   // ← explicit, do not remove
)
```

**Step 3 — Add this to the migration checklist:**
```
☐ Bottom bar height reduced to 56dp on phone
☐ Rail behavior on tablet unchanged
☐ Label visibility confirmed on all 4–5 destinations
☐ Bottom bar background uses surfaceContainer (not surface)
```

---

## Gap 4 — Floating Toolbar Pattern Not Included

### The Problem

M3E introduced the **floating toolbar** as a distinct component — separate from the app bar and bottom nav bar. It is meant for contextual, page-level actions relevant to the current content, not for app-level navigation.

The existing guide has no mention of this pattern, and this app has several surfaces that are a natural fit for it.

### Where Floating Toolbar Applies in This App

| Surface | Current Pattern | M3E Floating Toolbar Use |
|---|---|---|
| Video player (Aniyomi) | Custom overlay control bar | Floating toolbar for: subtitles, quality, next episode, lock |
| Manga reader (Aniyomi) | Custom overlay toolbar | Floating toolbar for: chapter nav, reading mode, brightness |
| Episode list (Aniyomi) | Top app bar with action icons | Floating toolbar for: mark-all-read, filter, sort (contextual) |
| AniList media detail | FAB + sheet actions | Floating toolbar for: edit status, set score, add to list |
| AniList profile stats | Top bar actions | Floating toolbar for: chart type toggle, time range filter |

### Before / After — Video Player Controls

**Before (custom overlay):**
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    PlayerSurface(...)
    AnimatedVisibility(visible = controlsVisible) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp)
        ) {
            // custom row of icon buttons
            Row {
                IconButton(onClick = { toggleSubtitles() }) { ... }
                IconButton(onClick = { openQualityPicker() }) { ... }
                IconButton(onClick = { nextEpisode() }) { ... }
            }
        }
    }
}
```

**After (M3E Floating Toolbar):**
```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
Box(modifier = Modifier.fillMaxSize()) {
    PlayerSurface(...)
    AnimatedVisibility(
        visible = controlsVisible,
        enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
        exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
    ) {
        FloatingToolbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            expanded = controlsVisible,
        ) {
            IconButton(onClick = { toggleSubtitles() }) {
                Icon(Icons.Default.ClosedCaption, contentDescription = "Subtitles")
            }
            IconButton(onClick = { openQualityPicker() }) {
                Icon(Icons.Default.HighQuality, contentDescription = "Quality")
            }
            IconButton(onClick = { nextEpisode() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next Episode")
            }
            IconButton(onClick = { toggleLock() }) {
                Icon(Icons.Default.Lock, contentDescription = "Lock")
            }
        }
    }
}
```

### Rule

Any surface where actions are contextual to the content being viewed (not to app-level navigation) should prefer the floating toolbar over a traditional app bar or custom overlay row.

---

## Gap 5 — Container Component Pattern Not Applied to List Rows

### The Problem

M3E's defining visual pattern in real-world app updates (Gmail, Drive, Google Meet) is placing list row items inside individual containers — giving each row visual separation and grouping clarity. The existing guide covers cards but does not address list rows, which are the dominant UI pattern in both Aniyomi and AniHyou.

### Where List Row Containers Apply

| Screen | Current Row Style | M3E Container Target |
|---|---|---|
| Aniyomi library grid | `Card` with image fill | Keep grid cards; add tonal container for list-view rows |
| Aniyomi updates list | Flat `ListItem` rows | Wrap each row in `surfaceContainer` container with `large` shape |
| Aniyomi history list | Flat `ListItem` rows | Same as updates |
| AniHyou media list | Flat rows with status color | Wrap each row in container; keep status color as leading indicator, not row background |
| AniHyou activity feed | Flat comment/activity rows | Each activity in individual `surfaceContainerLow` container |
| AniHyou notification list | Flat rows | Each notification in container with `unread` state using `primaryContainer` |
| Settings items | Flat preference rows | Group related settings in shared `surfaceContainerLow` container with `extraLarge` shape |

### Before / After — Update List Row

**Before:**
```kotlin
ListItem(
    headlineContent = { Text(manga.title) },
    supportingContent = { Text(chapter.name) },
    leadingContent = {
        AsyncImage(
            model = manga.coverArt,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)),
        )
    },
    trailingContent = { Text(chapter.readAt) },
)
```

**After (M3E container pattern):**
```kotlin
Surface(
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surfaceContainer,
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp),
) {
    ListItem(
        headlineContent = { Text(manga.title) },
        supportingContent = { Text(chapter.name) },
        leadingContent = {
            AsyncImage(
                model = manga.coverArt,
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small),
            )
        },
        trailingContent = { Text(chapter.readAt) },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent, // surface comes from parent
        ),
    )
}
```

### Settings Group Container

**Before:**
```kotlin
Column {
    PreferenceItem("Downloads", ...)
    PreferenceItem("Storage path", ...)
    PreferenceItem("Auto-delete", ...)
}
```

**After:**
```kotlin
Surface(
    shape = MaterialTheme.shapes.extraLarge,
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
) {
    Column {
        PreferenceItem("Downloads", ...)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        PreferenceItem("Storage path", ...)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        PreferenceItem("Auto-delete", ...)
    }
}
```

---

## Gap 6 — Shape Morphing on State Transitions Not Specified

### The Problem

The existing guide mentions shape morph in one sentence under the shape system but gives no concrete guidance. M3E's shape morphing API in Jetpack Compose is a first-class feature and the video player's play/pause button is the single most prominent interaction in this app — a perfect candidate.

### What Shape Morphing Is

Shape morphing allows a composable to animate between two different shapes as a state transition. In M3E, buttons can morph from a rounded rectangle (play) to a squircle (pause) and back, driven by the spring motion system rather than a tween.

### Where Shape Morphing Applies in This App

| Interaction | From Shape | To Shape | Priority |
|---|---|---|---|
| Play → Pause button (player) | Rounded rectangle | Squircle / pill | **High** |
| FAB: Add → Confirm | Circle | Rounded rectangle | **High** |
| Download: idle → active | Rounded rectangle | Pill (progress) | Medium |
| Library filter: off → active | Rounded rectangle | Pill (filled) | Medium |
| AniList status chip: no status → watching | Rounded | Pill | Medium |

### Before / After — Play/Pause Button

**Before:**
```kotlin
IconButton(
    onClick = { isPlaying = !isPlaying },
    modifier = Modifier
        .size(64.dp)
        .background(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
        ),
) {
    Icon(
        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}
```

**After (with shape morphing):**
```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val playShape = RoundedPolygon.pill()
val pauseShape = RoundedPolygon.rectangle(rounding = CornerRounding(0.4f))

val morphProgress by animateFloatAsState(
    targetValue = if (isPlaying) 1f else 0f,
    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
)

val currentShape = remember(morphProgress) {
    Morph(playShape, pauseShape).toShape(morphProgress)
}

Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
        .size(64.dp)
        .clip(currentShape)
        .background(MaterialTheme.colorScheme.primaryContainer)
        .clickable { isPlaying = !isPlaying },
) {
    Crossfade(
        targetState = isPlaying,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
    ) { playing ->
        Icon(
            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (playing) "Pause" else "Play",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(32.dp),
        )
    }
}
```

### Shape Morphing Rules for This Codebase

1. Only morph shapes that are a **direct result of user interaction** — not ambient animations.
2. Always use `MaterialTheme.motionScheme.defaultSpatialSpec()` as the animation spec — never a custom `tween`.
3. Limit morphing to one element per screen at a time to avoid visual chaos.
4. The play/pause button is the highest ROI target — implement it first.

---

## Gap 7 — Variable Font Axes Not Addressed in Typography

### The Problem

M3E uses variable font axes as a core part of its expressive typography system. Roboto Flex supports weight, width, and optical size axes, allowing font style to change in response to state — not just through static weight values. The existing guide's typography section does not mention this.

### What Variable Font Axes Enable

- Weight axis: animate font weight on selection (e.g., nav label going bold when selected)
- Width axis: compress or expand label text for tight containers without truncation
- Optical size: adjust letterform detail at small sizes (score badges, timestamps)

### Where Variable Fonts Apply in This App

| UI Element | Current Approach | M3E Variable Font Approach |
|---|---|---|
| Nav bar labels (selected state) | `fontWeight = FontWeight.Bold` hardcoded | Animate weight axis from 400 → 700 on selection with spring spec |
| Score display (AniHyou) | Static `fontWeight = FontWeight.Bold` | Use weight axis at 800+ for score emphasis |
| Episode count badge | Static small text | Use optical size axis at small sizes for legibility |
| AniList status chips (selected) | Color change only | Add weight axis animation on selection transition |
| App bar title | Static title large | No change needed — title role already conveys hierarchy |

### Setup: Add Roboto Flex to the Project

**Step 1 — Add font to resources:**
```
res/font/roboto_flex.ttf
```

**Step 2 — Declare variable font family:**
```kotlin
// presentation-core/.../theme/Typography.kt

val RobotoFlex = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.width(100f),
        ),
    )
)
```

**Step 3 — Animate weight axis on nav label selection:**
```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedNavLabel(label: String, selected: Boolean) {
    val fontWeight by animateFloatAsState(
        targetValue = if (selected) 700f else 400f,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
    )

    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(
            fontVariationSettings = "'wght' $fontWeight",
        ),
    )
}
```

**Step 4 — Score display with high-weight axis:**
```kotlin
// AniHyou score display
Text(
    text = score.toString(),
    style = MaterialTheme.typography.headlineMedium.copy(
        fontVariationSettings = "'wght' 800",
        fontFamily = RobotoFlex,
    ),
    color = MaterialTheme.colorScheme.primary,
)
```

### Rules

1. Never set `fontWeight` as a static override on elements that have selection states — use the weight axis instead so the transition can be animated.
2. Apply optical size axis only at `labelSmall` scale and below (timestamps, badges, counts).
3. Do not change font families between static and variable — pick one and apply consistently.

---

## Gap 8 — Split Button Component Not Mapped

### The Problem

The split button is one of the five new M3E components introduced at I/O 2025. It combines a primary action button with a secondary dropdown arrow — making the primary action one tap while exposing variants on a second tap. The existing guide does not include it in the component map.

### Where Split Button Applies in This App

| Surface | Primary Action | Split Options |
|---|---|---|
| Aniyomi: browse source item | Add to library | Add to library / Add to specific category |
| Aniyomi: episode list header | Play (latest episode) | Play from start / Play from last watched / Download all |
| Aniyomi: download button | Download next episode | Download 5 / Download 10 / Download all |
| AniHyou: media detail header | Set as Watching | Set as Watching / Planning / Paused / Dropped |
| AniHyou: edit score sheet | Save score | Save score / Clear score |

### Before / After — Episode List Play Button

**Before:**
```kotlin
Row {
    Button(
        onClick = { playLatestEpisode() },
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Play")
    }

    IconButton(onClick = { showPlayOptions = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "More play options")
    }
}

if (showPlayOptions) {
    DropdownMenu(...) { /* play from start, download all */ }
}
```

**After (M3E Split Button):**
```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
SplitButton(
    leadingButton = {
        SplitButtonDefaults.LeadingButton(
            onClick = { playLatestEpisode() },
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Play")
        }
    },
    trailingButton = {
        SplitButtonDefaults.TrailingButton(
            onClick = { showPlayOptions = !showPlayOptions },
            checked = showPlayOptions,
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (showPlayOptions) 180f else 0f,
                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "More options",
                modifier = Modifier.rotate(rotation),
            )
        }
    },
)
```

### Before / After — AniList Status Button (AniHyou)

**Before:**
```kotlin
Button(
    onClick = { setWatching() },
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF2196F3), // hardcoded AniList blue
    ),
) {
    Text("Set as Watching")
}
```

**After:**
```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
SplitButton(
    leadingButton = {
        SplitButtonDefaults.LeadingButton(
            onClick = { setStatus(MediaListStatus.CURRENT) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(currentStatus?.label ?: "Set Status")
        }
    },
    trailingButton = {
        SplitButtonDefaults.TrailingButton(
            onClick = { showStatusOptions = !showStatusOptions },
            checked = showStatusOptions,
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Change status")
        }
    },
)

if (showStatusOptions) {
    // M3E vertical menu with gap separating active statuses from terminal ones
    DropdownMenu(
        expanded = showStatusOptions,
        onDismissRequest = { showStatusOptions = false },
        shape = MaterialTheme.shapes.large,
    ) {
        listOf(CURRENT, PLANNING, PAUSED).forEach { status ->
            DropdownMenuItem(
                text = { Text(status.label) },
                leadingIcon = { Icon(status.icon, null) },
                onClick = { setStatus(status); showStatusOptions = false },
            )
        }
        DropdownMenuItemGap()
        listOf(COMPLETED, DROPPED).forEach { status ->
            DropdownMenuItem(
                text = { Text(status.label) },
                leadingIcon = { Icon(status.icon, null) },
                onClick = { setStatus(status); showStatusOptions = false },
            )
        }
    }
}
```

### Decision Rule for Split Button Adoption

Use a split button when **all three** of these are true:
1. There is a clear primary/default action that covers 80%+ of use cases
2. There are 2–5 variant actions that share the same intent
3. Showing all variants at once would clutter the surface

If a surface currently has a primary button + separate `MoreVert` icon button next to it — that is a split button waiting to be implemented.

---

## Updated Migration Checklist (Additions to refresh.md)

Add these items to the master checklist:

```
AniHyou Component Audit
☐ Score dialog migrated to expressive shape + ConnectedButtonGroup
☐ Media detail sheet using expressive container header
☐ Character/staff cards using tokenized corners and tonal elevation
☐ Activity feed rows using surfaceContainer container pattern
☐ Calendar day cells using shape tokens
☐ AniList media list rows using container pattern (not full-row status color)
☐ Stats charts using colorScheme roles for fills
☐ Profile header using semantic token (not hex literal)
☐ Season browser using ConnectedButtonGroup or FilterChip

Vertical Menus
☐ All DropdownMenu instances audited
☐ Gap separator applied where action groups exist
☐ Menu container shape uses MaterialTheme.shapes.large

Navigation Bar Height
☐ Phone bottom bar height set to 56dp
☐ Tablet rail behavior unchanged
☐ Labels remain visible on all destinations

Floating Toolbar
☐ Video player controls migrated to FloatingToolbar
☐ Manga reader overlay migrated to FloatingToolbar
☐ AniList media detail contextual actions use FloatingToolbar

Container Pattern (List Rows)
☐ Updates list rows wrapped in surfaceContainer containers
☐ History list rows wrapped in surfaceContainer containers
☐ AniHyou media list rows wrapped in containers
☐ AniHyou activity feed items in containers
☐ Settings preference groups in surfaceContainerLow containers with extraLarge shape

Shape Morphing
☐ Play/pause button morphs shape on state transition
☐ FAB add→confirm uses shape morph
☐ All shape morphs use motionScheme.defaultSpatialSpec()
☐ Max one morphing element per screen

Variable Fonts
☐ Roboto Flex added to res/font/
☐ Nav bar labels animate weight axis on selection
☐ Score displays use weight axis at 800+
☐ No static fontWeight overrides on elements with selection states

Split Button
☐ Browse source "add" button migrated to SplitButton
☐ Episode list "play" button migrated to SplitButton
☐ Download action migrated to SplitButton
☐ AniList status button migrated to SplitButton
☐ All previous primary+MoreVert pairs audited for SplitButton candidacy
```

---

## Suggested Commit Additions to refresh.md Phase Plan

Insert these commits into the existing Phase 5 (M3E component upgrades):

```
Phase 5b — AniHyou-origin component upgrades
  5b-1: AniHyou score dialog + status button → SplitButton + ConnectedButtonGroup
  5b-2: AniHyou media list rows → container pattern
  5b-3: AniHyou activity feed + notifications → container pattern
  5b-4: AniHyou profile + stats → semantic color tokens + variable font scores
  5b-5: AniHyou season/explore → FilterChip / ConnectedButtonGroup pickers

Phase 5c — Cross-cutting new components
  5c-1: All DropdownMenu → M3E vertical menus with gap/divider
  5c-2: Player + reader overlays → FloatingToolbar
  5c-3: Play/pause + FAB → shape morphing with motionScheme springs
  5c-4: All primary+MoreVert pairs → SplitButton
  5c-5: Nav labels + score displays → variable font weight axis
  5c-6: Bottom nav bar → 56dp short bar on phone
```

---

## Gap 9 — Full Codebase Scan Results (Repo-Verified)

This section converts assumptions into verified findings from a scan of the whole repository.

### 9.1 What was scanned

- Menus: `DropdownMenu`, `DropdownMenuItem`, `ExposedDropdownMenu`
- Shape literals: `RoundedCornerShape`, `CircleShape`, `RectangleShape`
- Hardcoded colors: `Color(0x...`)
- Legacy motion specs: `tween(...)`
- M3E APIs: `MaterialExpressiveTheme`, `MotionScheme.expressive`, `SplitButton`, `ConnectedButtonGroup`, `FloatingToolbar`, `SharedTransitionLayout`, `fontVariationSettings`
- Split-button candidates: `MoreVert`

### 9.2 Verified findings

1. Menu migration is high priority.
- `DropdownMenu`/`DropdownMenuItem` usage is widespread (83 matches in Kotlin).
- Hotspots include:
    - `app/src/main/java/eu/kanade/presentation/components/DropdownMenu.kt`
    - `app/src/main/java/eu/kanade/presentation/components/AppBar.kt`
    - `app/src/main/java/eu/kanade/presentation/components/TabbedDialog.kt`
    - `app/src/main/java/eu/kanade/tachiyomi/ui/download/DownloadsTab.kt`
    - `app/src/main/java/eu/kanade/presentation/entries/**`

2. Hardcoded shape literals are present in feature UI.
- 45 matches for `RoundedCornerShape`/`CircleShape`/`RectangleShape` patterns.
- Notable examples:
    - `app/src/main/java/eu/kanade/presentation/anilist/home/AnilistHomeScreen.kt`
    - `app/src/main/java/eu/kanade/presentation/more/settings/widget/AppThemePreferenceWidget.kt`
    - `app/src/main/java/eu/kanade/presentation/track/**`
    - `app/src/main/java/eu/kanade/tachiyomi/ui/player/controls/**`

3. Hardcoded colors in feature UI still exist.
- 200+ hits for `Color(0x...` (scan capped).
- Many are valid theme definitions under `app/src/main/java/eu/kanade/presentation/theme/colorscheme/**`.
- Actionable non-theme examples include:
    - `app/src/main/java/eu/kanade/presentation/anilist/home/AnilistHomeScreen.kt`
    - `app/src/main/java/eu/kanade/presentation/library/components/CommonEntryItem.kt`

4. Legacy `tween(...)` animation specs are still used in UI.
- 33 matches found.
- Key candidates for motion-token migration:
    - `presentation-core/src/main/java/tachiyomi/presentation/core/components/material/FloatingActionButton.kt`
    - `presentation-core/src/main/java/tachiyomi/presentation/core/components/CircularProgressIndicator.kt`
    - `app/src/main/java/eu/kanade/presentation/components/AdaptiveSheet.kt`
    - `app/src/main/java/eu/kanade/tachiyomi/ui/player/controls/**`

5. M3 Expressive APIs are not yet integrated.
- No matches for:
    - `MaterialExpressiveTheme`
    - `MotionScheme.expressive`
    - `SplitButton`
    - `ConnectedButtonGroup`
    - `FloatingToolbar`
    - `SharedTransitionLayout`
    - `fontVariationSettings`

6. Split-button opportunity is confirmed.
- 8 `MoreVert` usages found, including:
    - `app/src/main/java/eu/kanade/presentation/components/AppBar.kt`
    - `app/src/main/java/eu/kanade/presentation/components/TabbedDialog.kt`
    - `app/src/main/java/eu/kanade/presentation/track/manga/MangaTrackInfoDialogHome.kt`
    - `app/src/main/java/eu/kanade/tachiyomi/ui/player/controls/TopRightPlayerControls.kt`

### 9.3 Priority correction based on scan

Use this order to reduce breakage and rework:

1. Foundation
- Introduce `MaterialExpressiveTheme` + `MotionScheme.expressive` in theme root.

2. Wrappers first
- Migrate shared wrappers in `presentation-core` before feature screens.

3. Menu migration
- Standardize menu shape/grouping because menu usage is high and distributed.

4. Motion token migration
- Replace high-impact `tween(...)` usages in wrappers and player controls.

5. Feature cleanup
- Remove hardcoded shape/color literals in AniList and player surfaces first.

6. New expressive components
- Introduce `SplitButton`, `ConnectedButtonGroup`, and `FloatingToolbar` where interactions justify them.

### 9.4 Acceptance criteria for this scan stage

- `MaterialExpressiveTheme` appears at app theme root.
- `MotionScheme.expressive` appears in theme setup.
- No `tween(...)` remains in shared wrapper components (`presentation-core` material wrappers).
- All `DropdownMenu` callsites have been triaged into: plain / gap / divider variants.
- All `MoreVert` callsites have split-button candidacy decisions documented.
- AniList profile/status color literals are replaced by semantic tokens.