# PLAY-ON! — Exhaustive AniHyou Feature Porting Blueprint

This document acts as the **source of truth** for porting the `AniHyou-android` repository (a rich Kotlin/Jetpack Compose AniList client) into the unified **PLAY-ON!** architecture. 

Unlike older reference apps like Otraku or Dartotsu which required complete Dart-to-Kotlin rewrites, AniHyou provides readily usable Kotlin models, ViewModels, and Compose UI trees. However, these must be heavily adapted to fit PLAY-ON's **Material 3 Expressive (M3E)** design system, **Hilt** dependency injection, and **Clean Architecture** patterns.

---

## 1. Architectural Alignment: AniHyou ➔ PLAY-ON!

Before porting any specific feature, the underlying frameworks must be normalized. AniHyou uses several patterns that conflict with the PLAY-ON standard.

### 1.1 Dependency Injection (Koin ➔ Hilt)
AniHyou uses Koin for lightweight dependency injection. PLAY-ON (following Mihon/Animiru standards) universally uses Hilt/Dagger.
*   **Actionable Translation**: 
    Every Koin `module { factory { ... } single { ... } }` in AniHyou must be translated to a Hilt `@Module` class annotated with `@InstallIn(SingletonComponent::class)`.
    *Ref Example*:
    ```kotlin
    // AniHyou (Koin)
    val networkModule = module {
        single { ApolloClient.Builder().serverUrl("...").build() }
    }
    
    // PLAY-ON! (Hilt)
    @Module
    @InstallIn(SingletonComponent::class)
    object NetworkModule {
        @Provides
        @Singleton
        fun provideApolloClient(): ApolloClient {
            return ApolloClient.Builder().serverUrl("...").build()
        }
    }
    ```
*   **ViewModels**: Replace Koin's `org.koin.androidx.compose.koinViewModel()` with Hilt's `@HiltViewModel` annotation and `androidx.hilt.navigation.compose.hiltViewModel()`.

### 1.2 Motion & Animation (Tween ➔ Spring)
AniHyou uses standard `tween()` and `keyframes()` animations. PLAY-ON strictly enforces M3E's physics-based spring constraints.
*   **Actionable Translation**: 
    Search the AniHyou source for any instance of `tween(durationMillis = ...)`. Replace these universally with `spring()` physics.
    *Ref Example*:
    ```kotlin
    // AniHyou source
    animateFloatAsState(targetValue = x, animationSpec = tween(300))
    
    // PLAY-ON! target
    animateFloatAsState(targetValue = x, animationSpec = spring(
        dampingRatio = Spring.DampingRatioNoBouncy, 
        stiffness = Spring.StiffnessMedium
    ))
    ```

### 1.3 Theming & Colors (Hardcoded ➔ MaterialExpressive)
AniHyou occasionally relies on hardcoded HEX constraints or specific non-material semantic names.
*   **Actionable Translation**: 
    Ensure every `@Composable` uses `MaterialTheme.colorScheme` tokens. No instances of `Color(0xFF...)` are permitted inside UI component trees.

---

## 2. Directory & Package Restructuring

AniHyou organizes by flat feature folders (`feature/explore`, `feature/login`). PLAY-ON groups by architectural layer then feature (`ui/browse`, `ui/tracker`, `data/remote`, `domain/usecase`).

| AniHyou Package | PLAY-ON! Target Destination | Description |
| :--- | :--- | :--- |
| `core/network` | `data/track/anilist/`, `app/src/main/graphql` | Move `.graphql` schema and queries here. |
| `core/model` | `domain/anilist/model/` | Map Apollo-generated DTOs to these clean domain classes. |
| `feature/usermedialist` | `ui/tracker/list/` | The core AniList tracking screen (Watching/Reading). |
| `feature/explore/search`| `ui/browse/search/` | Unified search across local library and AniList. |
| `feature/mediadetails` | `ui/detail/` | Manga/Anime information screen logic. |
| `feature/activitydetails`| `ui/tracker/feed/` | User's AniList social feed (Otraku replacement). |
| `feature/profile` | `ui/tracker/profile/` | AniList user profile & stats. |

---

## 3. Exhaustive Feature Porting Roadmap

The migration will be executed in 6 distinct phases to ensure the app compiles at every step.

### Phase 1: Authentication & Core Graph Integration
**Goal**: Get AniHyou's Apollo Client communicating inside PLAY-ON and handle OAuth.
1.  **Dependencies**: Introduce `libs.apollo.runtime` and `libs.apollo.normalized.cache` to `app/build.gradle.kts`.
2.  **GraphQL Transfer**: Copy all `.graphql` query files from `AniHyou-android/core/network/src/main/graphql` to PLAY-ON's `data/remote/anilist/graphql`.
3.  **Apollo Client**: Replicate AniHyou's `ApolloClient` configuration, specifically handling the AniList API rate limits and interceptors. Integrate this provider into PLAY-ON's `TrackerManager.kt`.
4.  **Login UI**: Migrate `feature/login` composables to `ui/tracker/anilist/AnilistLoginScreen.kt`. Extract token logic to intercept and save within the PLAY-ON credentials datastore.

### Phase 2: The Unified Media List (`UserMediaList`)
**Goal**: Port AniHyou's robust user watchlist and readlist to replace the basic Mihon tracking views.
1.  **ViewModel Port**: Refactor `UserMediaListViewModel.kt` (all 400+ lines of it).
    *   Change the base class from `PagedUiStateViewModel` to a standard Hilt `ViewModel`.
    *   Migrate StateFlows: Map `UserMediaListUiState` directly into PLAY-ON's standardized screen states.
2.  **Sorting & Filtering**: AniHyou contains complex logic for AniList sorting (e.g., `MediaListSort.MEDIA_TITLE_ROMAJI_DESC`). Migrate this to a bottom sheet unified under `ui/tracker/components/TrackerSortBottomSheet.kt`.
3.  **Plus One Action**: Map AniHyou's `onClickPlusOne(increment: Int, entry: CommonMediaListEntry)` to PLAY-ON's `TrackerManager.updateEntry()` logic to instantly sync watch progress locally and remotely.

### Phase 3: Explore & Browse UI (`ExploreView` & `SearchView`)
**Goal**: Make AniList the backing discovery engine for PLAY-ON's "Browse" tab.
1.  **Search Bar Migration**: Refactor `feature/explore/search/ExploreSearchBar.kt`. Strip out standard Material TopAppBars and implement M3E `DockedSearchBar` or `SearchBar` components.
2.  **Seasonal Anime Component**: Port `feature/explore/season/SeasonView.kt` to the PLAY-ON "Updates" and "Browse" tabs, ensuring users see the current airing season by default instead of a blank local library.
3.  **Media Charts**: Migrate `feature/explore/charts/MediaChartListView.kt` (Trending, Popular, Highest Rated).

### Phase 4: Social Features (Activities, Reviews, Threads)
**Goal**: Unify media consumption with community interaction via AniHyou's rich social tabs.
1.  **Activity Feed**: 
    *   Extract `feature/activitydetails`.
    *   Create a dedicated Tab in the PLAY-ON Navigation Shell for "Social" or embed it as a sub-tab in "Tracker".
    *   Map `ActivityDetailsViewModel.kt` to load following/global feeds.
2.  **Reviews**: 
    *   Extract `feature/reviewdetails`.
    *   Embed this in a new `Pager` tab within PLAY-ON's `MediaDetailScreen`.
3.  **Forum Threads**: 
    *   Port `feature/thread` logic to allow reading AniList forum threads directly when viewing an Anime/Manga detail pane.

### Phase 5: Deep Metadata (Characters, Staff, Studios)
**Goal**: Provide rich Wiki-like context for every Anime and Manga.
1.  **Entities Migration**: Port `feature/characterdetails`, `feature/staffdetails`, and `feature/studiodetails`.
2.  **UI Refactoring**: 
    *   AniHyou uses standard list views for characters. PLAY-ON requires M3E horizontal carousels with highly rounded image shapes (`MaterialTheme.shapes.extraLarge`).
    *   Implement Hero transitions when navigating from an Anime detail screen character icon into the `CharacterDetailScreen`. Use PLAY-ON's `SharedTransitionLayout` context.

### Phase 6: Background Sync & Widgets
**Goal**: Keep AniList data synchronized with local SQL databases silently in the background.
1.  **Workers**: Extract `feature/worker/SyncWorker.kt` from AniHyou.
2.  **Integration**: Merge this logic with PLAY-ON's existing Mihon-based `TrackUpdateWorker.kt`. Ensure that failed updates queue up and retry on network reconnection instead of dropping silently.
3.  **Notifications**: Port `feature/notifications` to group AniList updates (e.g., "Episode 12 aired!") inside PLAY-ON's unified Notification Channels.
4.  **Glance Widgets**: Migrate `feature/widget` to provide Android home screen widgets for "Continue Watching" populated by tracked AniList data.

---

## 4. Specific Refactoring Checklists per Component Type

### 4.1 UI State Classes (`UiState`)
AniHyou heavily utilizes Data classes for state (e.g. `UserMediaListUiState`). 
*   **PLAY-ON Standard**: Wrap these in a sealed interface to properly handle loading and error fallbacks.
    ```kotlin
    sealed interface UserMediaListScreenState {
        object Loading : UserMediaListScreenState
        data class Success(val data: UserMediaListUiState) : UserMediaListScreenState
        data class Error(val message: String) : UserMediaListScreenState
    }
    ```

### 4.2 API Mappers (`*Dto` to `DomainModel`)
AniHyou tends to map Apollo Fragments right at the UI level.
*   **PLAY-ON Standard**: Create dedicated Mappers in the `domain` module.
    ```kotlin
    fun com.axiel7.anihyou.core.network.fragment.CommonMediaListEntry.toDomainTrackEntry(): TrackEntry {
        return TrackEntry(
            mediaId = this.mediaId,
            mediaType = if (this.media?.type == MediaType.ANIME) MediaType.ANIME else MediaType.MANGA,
            tracker = TrackerType.ANILIST,
            status = this.status.toDomain(),
            score = this.score?.toFloat() ?: 0f,
            progress = this.progress ?: 0
        )
    }
    ```

### 4.3 Database Interactions
AniHyou generally queries GraphQL directly with aggressive Apollo caching. PLAY-ON is Offline-First.
*   **PLAY-ON Standard**: Fetched AniList tracking data **must** be mirrored into PLAY-ON's Room Database (`anime_sync` and `manga_sync` tables). AniHyou's repositories need an extra step injected: `fetch from network -> save to Room -> emit flow from Room to UI`.

---

## 5. Potential Roadblocks & Conflict Resolutions

- **Mihon Base Trackers vs AniHyou Models**: PLAY-ON's base (Mihon) has a very tight `Track` schema designed historically for MAL/Kitsu. AniHyou's GraphQL schema is drastically richer. We will need to expand Mihon's base `track` SQLite table to support additional fields (like User ID strings, Start/Completion exact dates, and priority flags).
- **Apollo Versioning**: Need to verify if `AniHyou-android` uses Apollo v3 or v4 and align it with what is currently existing in `libs.versions.toml`. If PLAY-ON lacks Apollo completely, it must be added and synced.
- **Compose Navigation**: AniHyou uses standard `navigation-compose` or `Accompanist`. PLAY-ON utilizes a custom Adaptive Shell / NavigationDrawer architecture for Tablets. All AniHyou routes must be registered via PLAY-ON's unified `NavGraph.kt`. 

---
*End of exhaustive blueprint. Proceed phase by phase per git commit.*
