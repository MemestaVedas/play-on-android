# Version Reference

Current as of **April 2026**. Always verify at https://developer.android.com/jetpack/androidx/releases/compose-material3

---

## Compose BOM Version Matrix

| BOM Version | material3 | Notes |
|---|---|---|
| `2025.12.00` | `1.4.0` | **Current stable BOM** |
| `2026.xx.00` | `1.5.0-alpha11+` | Latest alpha (M3 Expressive APIs) |

---

## material3 Release History (Recent)

| Version | Date | Key Additions |
|---|---|---|
| `1.4.0` | Sept 24, 2025 | `MotionScheme` stable, `SecureTextField`, `HorizontalCenteredHeroCarousel`, `VerticalDragHandle`, `PullToRefreshBox` improvements, icon packs removed from transitive deps |
| `1.5.0-alpha07` | Oct 22, 2025 | `SearchBar` `keyboardOptions` + `lineLimits` |
| `1.5.0-alpha08` | Nov 5, 2025 | `TimePickerState` `hourInput`/`minuteInput` |
| `1.5.0-alpha09` | Nov 19, 2025 | Expressive menu updates (toggleable/selectable items, groups), `ExpandedDockedSearchBarWithGap`, `Modifier.align` in `ButtonGroupScope` |
| `1.5.0-alpha10` | Dec 3, 2025 | `StaticSheet`, `MaterialTheme` refactor to single `CompositionLocal`, graduated `motionScheme` from experimental |
| `1.5.0-alpha11` | Dec 17, 2025 | `ExpandedFullScreenContainedSearchBar`, multi-aspect carousel via lazy grids, **expressive list items** stable, multi-browse + uncontained carousel APIs stable, `FilterChip` content padding updates |

---

## material3-adaptive Release History

| Version | Date | Key Additions |
|---|---|---|
| `1.1.0` | Stable | Predictive back for adaptive panes |
| `1.2.0` | Oct 22, 2025 | `preferredHeight` modifier, reflow/levitate strategies |
| `1.3.0-alpha01` | Oct 8, 2025 | Margins + edge-to-edge for `ListDetailPaneScaffold` and `SupportingPaneScaffold` |
| `adaptive-navigation3:1.0.0-alpha03` | Sept 24, 2025 | KMP stubs, Navigation3 integration |

---

## Gradle Dependency Declarations

### Stable (Recommended for production)

```kotlin
// build.gradle.kts
val composeBom = platform("androidx.compose:compose-bom:2025.12.00")
implementation(composeBom)

implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material3:material3-window-size-class")
implementation("androidx.compose.material3:material3-adaptive-navigation-suite")

// Adaptive (separate versioning)
implementation("androidx.compose.material3.adaptive:adaptive:1.2.0")
implementation("androidx.compose.material3.adaptive:adaptive-layout:1.2.0")
implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.2.0")
```

### Alpha (for M3 Expressive)

```kotlin
// Override BOM version for material3 alpha
implementation("androidx.compose.material3:material3:1.5.0-alpha11")

// Adaptive alpha
implementation("androidx.compose.material3.adaptive:adaptive:1.3.0-alpha09")
implementation("androidx.compose.material3.adaptive:adaptive-layout:1.3.0-alpha09")
implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.3.0-alpha09")
```

---

## Kotlin & AGP Compatibility

| Compose material3 | Min Kotlin | Min AGP |
|---|---|---|
| 1.4.0 | 1.9.x | 8.1 |
| 1.5.0-alpha | 2.0.x | 8.5 |

---

## Opt-In Annotations Reference

| Annotation | When Needed |
|---|---|
| `@ExperimentalMaterial3Api` | Experimental core M3 APIs (e.g., `SearchBar`, `ModalBottomSheet` internals) |
| `@ExperimentalMaterial3ExpressiveApi` | All M3 Expressive components |
| `@ExperimentalMaterial3AdaptiveApi` | Adaptive scaffold APIs |
| `@ExperimentalMaterial3AdaptiveNavigationSuiteApi` | `NavigationSuiteScaffold` |

Apply at the call site, function, or file level:

```kotlin
// File level (covers entire file)
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

// Function level
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyScreen() { ... }
```
