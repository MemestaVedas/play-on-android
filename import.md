# AniHyou → PLAY-ON! Master Integration Blueprint

## Open Questions

> [!IMPORTANT]
> **Decisions needed before starting implementation:**

1. **Bottom nav vs side drawer**: Should the AniList section be a new bottom nav tab (6th tab) or accessible from the "More" screen? A 6th tab may feel crowded on mobile.
Ans. It should replcae the updates section in the nav bar and the updates section button should be placed at the top right of the Anilist page with the same icon. this will be a simple change in the routing and the UI a little.

2. **Theme integration**: Should the AniList screens use PLAY-ON's existing 20+ theme system, or should they have their own `MaterialKolor` dynamic theming? Recommend: use PLAY-ON's theme for consistency.
Ans. Use the Material 3 dynamic theming. Only keep the dynamic and the default theme even from PLAY-ON!s side. rest all delete

3. **Offline support**: AniHyou uses Apollo's normalized cache for offline viewing. Since we're dropping Apollo, should we implement SQLite caching for AniList data or go network-only?
Ans. Use apollo itself, Do not drop apollo

4. **Markdown rendering**: AniHyou uses `multiplatform-markdown-renderer`. PLAY-ON already has `compose-webview` for rendering HTML. Should we add the markdown renderer library or convert to WebView?
Ans. You choose the best recommended option which is bound to improve the experience of the user.

5. **Should the existing AniList tracker UI be merged** with the new AniList media list, or should they remain separate? (Tracker = syncs episode progress; Media List = full AniList collection browser)
Ans. Keep them separate for now and later we will think of integrating them together. right now the working of the app is the main thing.

## Executive Summary

This plan ports every feature from **AniHyou-android** (a pure AniList client built with Apollo GraphQL, Koin DI, and Navigation 3) into **PLAY-ON!** (an Aniyomi/Mihon fork with Voyager navigation, Injekt DI, and extension-based source architecture). The apps have fundamentally different architectures, so this is a **translation + integration**, not a copy-paste.

> [!IMPORTANT]
> **PLAY-ON! already has a partial AniList tracker** at `data/track/anilist/`. AniHyou's value lies in its **first-class AniList UI** — social feeds, activity details, character/staff/studio pages, user profiles with stats, forum threads, reviews, airing calendar, explore/search with advanced filters, and an airing widget. These are all features PLAY-ON! lacks.

---

## Architectural Differences

| Aspect | AniHyou | PLAY-ON! |
|---|---|---|
| **Navigation** | Jetpack Navigation 3 (`rememberNavBackStack`, `TopLevelBackStack`) | Voyager (`Navigator`, `Screen`, `Tab`) |
| **DI Framework** | Koin (`koinInject`, `viewModel()`, modules) | Injekt (`Injekt.get()`, `injectLazy()`) |
| **Networking** | Apollo GraphQL (`.graphql` schema files, generated types) | OkHttp + custom REST/GraphQL via `AnilistApi.kt` |
| **State Management** | `UiStateViewModel` / `PagedUiStateViewModel` with `DataResult<T>` | Voyager `ScreenModel` with custom state flows |
| **Data Layer** | DataStore Preferences + Apollo normalized cache | SQLDelight + SharedPreferences + OkHttp cache |
| **Theming** | `MaterialKolor` dynamic palette from seed color | Custom `BaseColorScheme` subclasses (20+ themes) |
| **Module Structure** | Multi-module: `core/{base,common,domain,model,network,resources,ui}` + `feature/*` (18 feature modules) | Multi-module: `app`, `core/{archive,common}`, `data`, `domain`, `i18n`, `presentation-{core,widget}`, `source-{api,local}` |
| **Build System** | Version catalog `libs.versions.toml`, AGP 9.1.0, Kotlin 2.3.10 | Custom `buildSrc` convention plugins (`mihon.android.*`), multiple version catalogs |
| **Image Loading** | Coil 3 | Coil 3 (shared ✓) |
| **Min SDK** | 26 | 26 (shared ✓) |

---

## Reconciliation Strategy

For each ported feature, we will:

1. **Convert Koin → Injekt**: Replace `koinInject()` with `Injekt.get()`, `by viewModel()` with `rememberScreenModel { }` or `injectLazy()`.
2. **Convert Navigation 3 → Voyager**: Each AniHyou `*View.kt` composable becomes a Voyager `Screen` object with a `Content()` override.
3. **Unify AniList networking on Apollo GraphQL**: Add Apollo to PLAY-ON, import AniHyou's schema + operations, and migrate the existing `AnilistApi.kt` tracker paths to Apollo so all AniList features share one stack and one cache.
4. **Reuse PLAY-ON's existing models** where they overlap (e.g., `ALAnime`, `ALManga`, `ALUser`).
5. **Place new UI code** under `app/src/main/java/eu/kanade/presentation/anilist/` to keep the namespace clean.
6. **Place new domain/data code** under `app/src/main/java/eu/kanade/domain/anilist/` and `app/src/main/java/eu/kanade/tachiyomi/data/track/anilist/`.

---

## Priority Ordering

| Phase | Features | Rationale |
|---|---|---|
| **P0 — Foundation** | AniList API layer expansion, data models, preferences | Everything depends on these |
| **P1 — Core Screens** | User Profile, Media Details, User Media List | Most-used features |
| **P2 — Discovery** | Explore/Search, Seasonal Anime, Charts, Calendar | Browse & discover content |
| **P3 — Social** | Activity Feed, Activity Details, Notifications | Community features |
| **P4 — Detail Pages** | Character, Staff, Studio, Review, Thread details | Deep-link & browse targets |
| **P5 — Edit & Settings** | Edit Media Sheet, AniList Settings, Custom Lists | User management |
| **P6 — Extras** | Airing Widget, Notification Worker, WearOS (skip) | Polish |

---

## Phase 0 — Foundation

### New Dependencies to Add

> [!WARNING]
> **Use Apollo GraphQL as the single AniList networking layer.** This is a one-time migration cost that gives normalized caching, better type safety, and avoids maintaining parallel AniList networking systems.

Add to PLAY-ON's `libs.versions.toml`:

```toml
# Add Apollo (runtime + plugin aliases) along with UI support libs used by AniHyou:
apollo-runtime = { group = "com.apollographql.apollo", name = "apollo-runtime", version = "<pin-version>" }
apollo-normalized-cache-sqlite = { group = "com.apollographql.apollo", name = "apollo-normalized-cache-sqlite", version = "<pin-version>" }
placeholder-material3 = { group = "io.github.fornewid", name = "placeholder-material3", version = "2.0.0" }
markdown-renderer = { group = "com.mikepenz", name = "multiplatform-markdown-renderer-android", version = "0.39.2" }
markdown-renderer-m3 = { group = "com.mikepenz", name = "multiplatform-markdown-renderer-m3", version = "0.39.2" }
```

These are used by AniHyou for shimmer loading placeholders and markdown rendering (user bios, activity text, thread comments).

---

### AniHyou `core/base/` → PLAY-ON Foundation

**Source files (6 files):**
- `Constants.kt` — API URLs, client IDs
- `DataResult.kt` — `sealed class DataResult<T>` (Loading / Success / Error)
- `PagedResult.kt` — Pagination wrapper
- `extensions/FlowExt.kt`, `ListExt.kt` — Utility extensions
- `event/UiEvent.kt`, `PagedEvent.kt` — Base event interfaces
- `state/UiState.kt`, `PagedUiState.kt` — Base state classes

**Integration target:** `app/src/main/java/eu/kanade/domain/anilist/base/`

**What to do:**
- Port `DataResult<T>` as-is — it's a clean sealed class with no external dependencies.
- Port `PagedResult<T>` — used by all list screens.
- Port `UiState` / `PagedUiState` — adapt to work with Voyager's `ScreenModel` instead of Koin's ViewModel.
- **Constants**: Merge into PLAY-ON's existing `AnilistUtils.kt` / `Anilist.kt` which already define `BASE_URL`, `CLIENT_ID`, etc.
- **Extensions**: Port to `eu.kanade.tachiyomi.util.anilist.*`.

---

### AniHyou `core/model/` → PLAY-ON Data Models

**Source files (65 files)** covering:
- `activity/` — `ActivityTypeGrouped`, `GenericActivity`, `ListActivity`, `MessageActivity`, `TextActivity`
- `character/` — `Character`, `CharacterRole`
- `genre/` — `GenresAndTags`, `SelectableGenre`
- `media/` — `AnimeSeason`, `AnimeThemes`, `ChartType`, `CountryOfOrigin`, `ListType`, `Media`, `MediaCharactersAndStaff`, `MediaFormat`, `MediaList`, `MediaListSort`, `MediaListStatus`, `MediaRank`, `MediaRelation`, `MediaRelationsAndRecommendations`, `MediaSort`, `MediaSource`, `MediaStatus`, `MediaType`, `StreamingEpisode`
- `notification/` — `GenericNotification`, `NotificationInterval`, `NotificationTypeGroup`
- `review/` — `Review`
- `staff/` — `Staff`, `StaffMediaGrouped`, `StaffRole`
- `stats/` — `Stat`, `StatDistributionType`, overview models (6 files), `genres/GenreStat`
- `thread/` — `ChildComment`
- `user/` — `UserInfo`, `UserMediaListSort`, `UserStaffNameLanguage`, `UserTitleLanguage`
- Root: `AppColorMode`, `CurrentListType`, `DeepLink`, `DefaultTab`, `HomeTab`, `ItemsPerRow`, `ListStyle`, `ScoreFormat`, `SearchType`, `Theme`

**Integration target:** `app/src/main/java/eu/kanade/domain/anilist/model/`

**What to do:**
- Port these as **new model classes**. They don't conflict with existing PLAY-ON models.
- PLAY-ON already has `ALAnime`, `ALManga` etc. in `data/track/anilist/dto/`. Create **mapping extensions** between the two model families.
- The `media/` subpackage models mirror AniList's GraphQL schema — they're needed for the new UI screens.
- Enums like `MediaFormat`, `MediaStatus` can reuse PLAY-ON's tracker mapping where applicable.

---

### AniHyou `core/network/` → Migrate PLAY-ON AniList Networking to Apollo

**Source files:**
- `NetworkModule.kt` — Apollo client setup (PORT and adapt to Injekt)
- `NetworkVariables.kt` — Access token holder
- `ApiModule.kt` — Koin module for API classes
- `api/ActivityApi.kt`, `CharacterApi.kt`, `FavoriteApi.kt`, `LikeApi.kt`, `MalApi.kt`, `MediaApi.kt`, `MediaListApi.kt`, `NotificationsApi.kt`, `ReviewApi.kt`, `StaffApi.kt`, `StudioApi.kt`, `ThreadApi.kt`, `UserApi.kt`
- `api/model/` — DTO extensions
- `api/response/` — Error handling
- **77 `.graphql` files** defining all queries/mutations

**Integration target:** `app/src/main/java/eu/kanade/tachiyomi/data/track/anilist/` + Apollo GraphQL source set in PLAY-ON

**What to do:**

1. **Copy AniHyou schema + 77 operations** into PLAY-ON's Apollo source set and enable Apollo codegen.
2. **Configure ApolloClient in Injekt** using PLAY-ON's existing OkHttp client/interceptors (`AnilistInterceptor`, logging, etc.).
3. **Group APIs by domain:**
   - `AnilistActivityApi` — activity feed, activity details, publish activity
   - `AnilistMediaApi` — search, charts, seasonal, media details, stats
   - `AnilistSocialApi` — user profile, followers, favorites, notifications
   - `AnilistForumApi` — threads, comments
   - `AnilistCharacterStaffApi` — character/staff/studio details
4. **Migrate tracker operations** from legacy `AnilistApi.kt` into Apollo operations so tracker + viewer share one networking stack.
5. **Delete the legacy raw-query `AnilistApi.kt` path** after parity checks pass.

> [!IMPORTANT]
> AniHyou already relies on Apollo generated types from `.graphql` files. Reusing this model in PLAY-ON avoids manual query-string maintenance and reduces long-term complexity.

---

### AniHyou `core/domain/` → PLAY-ON Repository Layer

**Source files (18 files):**
- `DataStoreExt.kt`, `DataStoreModule.kt` — Preferences access
- `RepositoryModule.kt` — Koin DI registration
- `repository/BaseNetworkRepository.kt` — Base class with error handling
- `repository/ActivityRepository.kt` — Activity CRUD
- `repository/CharacterRepository.kt` — Character details
- `repository/DefaultPreferencesRepository.kt` — App preferences
- `repository/FavoriteRepository.kt` — Toggle favorites
- `repository/LikeRepository.kt` — Toggle likes
- `repository/ListPreferencesRepository.kt` — List display preferences
- `repository/LoginRepository.kt` — OAuth flow
- `repository/MediaListRepository.kt` — User media lists (most complex, 206 lines)
- `repository/MediaRepository.kt` — Media search/details
- `repository/NotificationRepository.kt` — Notifications
- `repository/ReviewRepository.kt` — Reviews
- `repository/SearchRepository.kt` — Global search
- `repository/StaffRepository.kt` — Staff details
- `repository/StudioRepository.kt` — Studio details
- `repository/ThreadRepository.kt` — Forum threads
- `repository/UserRepository.kt` — User profiles

**Integration target:** `app/src/main/java/eu/kanade/domain/anilist/interactor/`

**What to do:**
- Convert each repository into an **Injekt-registered interactor** following PLAY-ON's pattern.
- Reuse the base repository pattern with Apollo flows and normalized cache-backed pagination.
- `LoginRepository` — PLAY-ON already has AniList OAuth in `Anilist.kt`. **Reuse it.** Do NOT create a duplicate login flow.
- `MediaListRepository` — The most critical. Port the `incrementProgress()`, `updateEntry()`, `deleteEntry()` logic, mapping to PLAY-ON's existing tracker update path.
- `DefaultPreferencesRepository` / `ListPreferencesRepository` — Map to PLAY-ON's `PreferencesHelper` pattern using `SharedPreferences` backed by its preference DSL.

---

### AniHyou `core/resources/` → PLAY-ON Resources

**Source files:**
- `Color.kt`, `ColorUtils.kt` — Theme colors
- **127 drawable XML files** — Material icons
- `values/strings.xml` + 18 language translations
- Widget layouts, mipmap, themes

**Integration target:** Merge into PLAY-ON's existing resources

**What to do:**
- **Icons**: PLAY-ON already uses Material Icons via Compose `Icons.*`. Only port icons AniHyou uses that PLAY-ON doesn't have (streaming service logos: Spotify, Apple Music, Deezer, YouTube Music).
- **Strings**: Create a new `strings_anilist.xml` resource file under PLAY-ON's `i18n` module for AniList-specific strings.
- **Colors**: Port AniHyou's AniList status colors (watching/reading/completed/etc.) as extension val properties.
- **Widget resources**: Port to `presentation-widget/` module (Phase 6).

---

### AniHyou `core/ui/` → PLAY-ON Shared Composables

**Source files (68 files)** including:
- `common/` — `BottomDestination`, navigation utilities, `TabRowItem`
- `composables/activity/` — `ActivityFeedItem`, `ActivityItem`
- `composables/character/` — `CharacterVoiceActorsSheet`
- `composables/chip/` — `ChipWithMenu`, `ChipWithRange`
- `composables/common/` — Dialogs, buttons, chips, checkboxes, progress indicators
- `composables/list/` — `DiscoverLazyRow`, `HorizontalListHeader`
- `composables/markdown/` — `DefaultMarkdownText`, `MarkdownEditor`, format helpers
- `composables/media/` — `AiringAnimeHorizontalItem`, `MediaItemHorizontal`, `MediaItemVertical`, `MediaPoster`, `VideoThumbnailItem`
- `composables/person/` — `PersonImage`, `PersonItemHorizontal`, `PersonItemSmall`, `PersonItemVertical`
- `composables/post/` — `PostItem`
- `composables/scores/` — Rating views (5-star, smiley, slider)
- `composables/sheet/` — Bottom sheets
- `composables/stats/` — Stats bars
- `composables/webview/` — WebView wrappers
- `theme/` — `ColorScheme.kt`, `Theme.kt`
- `utils/` — Date, image, locale, markdown, notification, string, translate utils

**Integration target:** `app/src/main/java/eu/kanade/presentation/anilist/components/`

**What to do:**
- **Do NOT port** `BottomDestination`, navigation utilities, `Theme.kt` — PLAY-ON has its own.
- **Port** all `composables/media/*`, `person/*`, `activity/*`, `scores/*`, `stats/*`, `markdown/*` — these are the visual building blocks for AniList screens.
- **Adapt** `DefaultScaffold`, `TabRowWithPager` to use PLAY-ON's existing `Scaffold` from `tachiyomi.presentation.core`.
- **Port** `FullScreenImageView` — useful for viewing character/cover art.
- **Port** `Preferences.kt` composables — settings UI for AniList preferences.
- Port `placeholder-material3` shimmer loading indicators — add the dependency.

---

## Phase 1 — Core Screens

### Feature: User Profile (`feature/profile/`)

**Source files (31 files):**
- Root: `ProfileView.kt`, `ProfileViewModel.kt`, `ProfileEvent.kt`, `ProfileUiState.kt`, `ProfileInfoType.kt`
- `about/UserAboutView.kt` — User bio with markdown rendering
- `activity/UserActivityView.kt` — User's activity timeline
- `favorites/` — `UserFavoritesView.kt`, ViewModel, Event, UiState, `FavoritesType.kt`
- `social/` — `UserSocialView.kt` (followers/following), ViewModel, Event, UiState, `UserSocialType.kt`
- `stats/` — `UserStatsView.kt`, ViewModel, Event, UiState, `UserStatType.kt`
  - `composables/` — `DistributionTypeChips`, `MediaTypeChips`, `PositionalStatItemView`
  - `genres/GenresStatsView.kt`, `overview/OverviewStatsView.kt`, `staff/StaffStatsView.kt`, `studios/StudiosStatsView.kt`, `tags/TagsStatsView.kt`, `voiceactors/VoiceActorsStatsView.kt`

**Integration target:** `app/src/main/java/eu/kanade/presentation/anilist/profile/`

**What to do:**
1. Create `AnilistProfileScreen : Screen` (Voyager) wrapping `ProfileView`.
2. Convert `ProfileViewModel` → `AnilistProfileScreenModel : ScreenModel`.
3. Wire to new `AnilistUserApi` for data fetching.
4. Add navigation entry: accessible from the AniList tab in PLAY-ON's home, and from user avatars throughout the app.
5. Sub-screens (About, Activity, Favorites, Social, Stats) become nested composables within the profile screen's tab pager.

**Conflicts/Notes:**
- PLAY-ON has no concept of "user profile" for AniList — it only uses AniList as a tracker. This is entirely new UI surface.
- The stats views use pie/bar charts — port the custom `HorizontalStatsBar` and `VerticalStatsBar` composables.

---

### Feature: Media Details (`feature/mediadetails/`)

**Source files (18 files):**
- Root: `MediaDetailsView.kt`, `MediaDetailsViewModel.kt`, `MediaDetailsEvent.kt`, `MediaDetailsUiState.kt`, `MediaDetailsType.kt`
- `activity/` — `MediaActivityView.kt`, ViewModel, Event, UiState
- `composables/` — `EpisodeItem`, `FollowingUserItem`, `MediaCharacterStaffView`, `MediaInformationView`, `MediaRelationsView`, `MediaStatsView`, `MusicStreamingSheet`, `ReviewThreadListView`

**Integration target:** `app/src/main/java/eu/kanade/presentation/anilist/mediadetails/`

**What to do:**
1. Create `AnilistMediaDetailsScreen(mediaId: Int) : Screen`.
2. This is **separate from** PLAY-ON's existing `AnimeScreen` / `MangaScreen` which show source-based details. The AniList version shows AniList metadata, community stats, relations, characters, reviews, threads.
3. Navigation: Add a "View on AniList" button to existing `AnimeScreen`/`MangaScreen` that opens this.
4. Also accessible from explore/search results, user lists, activity feed links.

**Conflicts/Notes:**
- `MusicStreamingSheet` links to OP/ED on Spotify, Apple Music, YouTube — nice feature, port as-is.
- `MediaStatsView` shows score distribution and status distribution charts.
- `RelationsView` shows related anime/manga — needs to handle navigation back to either PLAY-ON source screens or AniList detail screens.

---

### Feature: User Media List (`feature/usermedialist/`)

**Source files (15 files):**
- Root: `UserMediaListHostView.kt`, `UserMediaListView.kt`, `UserMediaListViewModel.kt`, `UserMediaListEvent.kt`, `UserMediaListUiState.kt`
- `composables/` — `CompactUserMediaListItem`, `GridUserMediaListItem`, `ListSelectSheet`, `MinimalUserMediaListItem`, `NotesDialog`, `NotesIndicator`, `RandomEntryButton`, `RepeatIndicator`, `SortMenu`, `StandardUserMediaListItem`

**Integration target:** `app/src/main/java/eu/kanade/presentation/anilist/medialist/`

**What to do:**
1. Create `AnilistMediaListScreen : Screen` — shows the user's AniList anime/manga list with status tabs.
2. This supplements PLAY-ON's existing library (which shows locally-tracked entries). The AniList list shows the user's **full AniList collection**.
3. Four list styles: Standard, Compact, Minimal, Grid — port all composables.
4. `RandomEntryButton` — fun feature, selects a random entry from the list.
5. Sorting, filtering by status, custom lists — all supported.

---

## Phase 2 — Discovery

### Feature: Explore & Search (`feature/explore/`)

**Source files (25 files):**
- Root: `ExploreView.kt`, `ExploreSearchBar.kt`
- `search/` — `SearchView.kt`, `SearchViewModel.kt`, `SearchEvent.kt`, `SearchUiState.kt`
  - `composables/` — Country, Date, Duration, Format, Genres, Sort, Sources, Status chips + `PercentageSlider`
  - `genretag/` — `GenresTagsSheet.kt`, ViewModel, Event, UiState, Tab
- `season/` — `SeasonAnimeView.kt`, `SeasonAnimeViewModel.kt`, Event, UiState, `SeasonChartFilterSheet.kt`
- `charts/` — `MediaChartListView.kt`, `MediaChartViewModel.kt`, Event, UiState

**Integration target:** `app/src/main/java/eu/kanade/presentation/anilist/explore/`

**What to do:**
1. Create `AnilistExploreScreen : Screen` — top-level discover page with search bar.
2. `AnilistSearchScreen` — advanced search with all filter chips (genre, tag, year, format, status, country, source, duration, sort).
3. `AnilistSeasonalScreen` — seasonal anime browser with filter sheet.
4. `AnilistChartsScreen` — top anime/manga charts (trending, popular, top rated).
5. The `GenresTagsSheet` is a substantial component — a multi-tab bottom sheet for selecting genres and tags. Port it.

**Notes:**
- The search system queries AniList's API directly — different from PLAY-ON's extension-based source search.
- Consider adding AniList as a "source" option in PLAY-ON's browse tab, or a dedicated AniList tab in the bottom nav.

---

### Feature: Calendar (`feature/calendar/`)

**Source files (6 files):**
- `CalendarView.kt`, `CalendarHostViewModel.kt`, `CalendarViewModel.kt`, `CalendarEvent.kt`, `CalendarUiState.kt`, `CalendarTab.kt`

**Integration target:** `app/src/main/java/eu/kanade/presentation/anilist/calendar/`

**What to do:**
1. Create `AnilistCalendarScreen : Screen` — shows airing schedule grouped by day of week.
2. Each day tab shows anime airing that day with countdown timers.
3. Accessible from the AniList explore section or a dedicated nav entry.

---

## Phase 3 — Social

### Feature: Activity Feed & Details (`feature/home/activity/` + `feature/activitydetails/`)

**Source files (19 files):**

Home activity feed:
- `ActivityFeedView.kt`, `ActivityFeedViewModel.kt`, Event, UiState
- `composables/ActivityFollowingChip.kt`, `ActivityTypeChip.kt`

Activity details:
- `ActivityDetailsView.kt`, `ActivityDetailsViewModel.kt`, Event, UiState
- `composables/ActivityTextView.kt`
- `publish/` — `PublishActivityView.kt`, ViewModel, Event, UiState

**Integration target:** `app/src/main/java/eu/kanade/presentation/anilist/activity/`

**What to do:**
1. `AnilistActivityFeedScreen : Screen` — scrollable feed of followed users' activities.
2. `AnilistActivityDetailsScreen(activityId: Int) : Screen` — single activity with replies.
3. `AnilistPublishActivityScreen : Screen` — compose and post a text activity.
4. Filter chips for activity type (text, list updates, message) and following/global.

---

### Feature: Notifications (`feature/notifications/`)

**Source files (5 files):**
- `NotificationsView.kt`, `NotificationsViewModel.kt`, Event, UiState
- `composables/NotificationItem.kt`

**Integration target:** `app/src/main/java/eu/kanade/presentation/anilist/notifications/`

**What to do:**
1. `AnilistNotificationsScreen : Screen` — shows AniList notifications (airing, activity, thread replies, follows, etc.).
2. AniHyou's `NotificationItem` handles ~15 notification types — port the full renderer.
3. Add a badge counter to the AniList section of the app.

---

### Feature: Home / Discover (`feature/home/`)

**Source files (17 files):**
- Root: `HomeView.kt`, `HomeViewModel.kt`
- `current/` — `CurrentView.kt`, `CurrentViewModel.kt`, Event, UiState, `CurrentListItem.kt`, `CurrentFullListView.kt`
- `discover/` — `DiscoverView.kt`, `DiscoverViewModel.kt`, Event, UiState
  - `content/` — `AiringContent.kt`, `DiscoverMediaContent.kt`, `SeasonAnimeContent.kt`

**Integration target:** `app/src/main/java/eu/kanade/presentation/anilist/home/`

**What to do:**
1. Create an `AnilistHomeScreen` — the AniList-native home page showing:
   - Currently watching/reading with increment buttons
   - Airing soon section
   - Trending anime/manga
   - Seasonal anime preview
   - Activity feed preview
2. This becomes the content of a new "AniList" tab in PLAY-ON's bottom navigation.

---

## Phase 4 — Detail Pages

### Feature: Character Details (`feature/characterdetails/`)

**Source files (7 files):**
- `CharacterDetailsView.kt`, `CharacterDetailsViewModel.kt`, Event, UiState, Tab
- `content/CharacterInfoView.kt`, `content/CharacterMediaView.kt`

**Integration:** `app/src/main/java/eu/kanade/presentation/anilist/character/`
- `AnilistCharacterScreen(characterId: Int) : Screen`

---

### Feature: Staff Details (`feature/staffdetails/`)

**Source files (8 files):**
- `StaffDetailsView.kt`, `StaffDetailsViewModel.kt`, Event, UiState, InfoType
- `content/StaffInfoView.kt`, `StaffCharacterView.kt`, `StaffMediaView.kt`

**Integration:** `app/src/main/java/eu/kanade/presentation/anilist/staff/`
- `AnilistStaffScreen(staffId: Int) : Screen`

---

### Feature: Studio Details (`feature/studiodetails/`)

**Source files (4 files):**
- `StudioDetailsView.kt`, `StudioDetailsViewModel.kt`, Event, UiState

**Integration:** `app/src/main/java/eu/kanade/presentation/anilist/studio/`
- `AnilistStudioScreen(studioId: Int) : Screen`

---

### Feature: Review Details (`feature/reviewdetails/`)

**Source files (4 files):**
- `ReviewDetailsView.kt`, `ReviewDetailsViewModel.kt`, Event, UiState

**Integration:** `app/src/main/java/eu/kanade/presentation/anilist/review/`
- `AnilistReviewScreen(reviewId: Int) : Screen`

---

### Feature: Forum Thread (`feature/thread/`)

**Source files (11 files):**
- `ThreadDetailsView.kt`, `ThreadDetailsViewModel.kt`, Event, UiState
- `composables/ChildCommentView.kt`, `ParentThreadView.kt`, `ThreadCommentView.kt`
- `publish/` — `PublishCommentView.kt`, ViewModel, Event, UiState

**Integration:** `app/src/main/java/eu/kanade/presentation/anilist/thread/`
- `AnilistThreadScreen(threadId: Int) : Screen`
- `AnilistPublishCommentScreen : Screen`

---

## Phase 5 — Edit & Settings

### Feature: Edit Media (`feature/editmedia/`)

**Source files (11 files):**
- `EditMediaSheet.kt`, `EditMediaViewModel.kt`, Event, UiState
- `composables/` — `CustomListsDialog`, `DeleteMediaEntryDialog`, `EditMediaDateField`, `EditMediaDatePicker`, `EditMediaProgressRow`, `ScoreView`, `SetScoreDialog`

**Integration:** `app/src/main/java/eu/kanade/presentation/anilist/edit/`

**What to do:**
1. Create `AnilistEditMediaSheet` — a modal bottom sheet for editing AniList list entries.
2. Supports all score formats: 10-point, 100-point, 5-star, smiley, 10-point decimal.
3. Start/complete date pickers, progress tracking, notes, private/hidden toggles.
4. Custom lists management.
5. This is invoked from media details screens and the user media list.

---

### Feature: AniList Settings (`feature/settings/`)

**Source files (15 files):**
- `SettingsView.kt`, `SettingsViewModel.kt`, Event, UiState
- `TranslationsView.kt`
- `composables/CustomColorPreference.kt`, `LanguagePreference.kt`
- `liststyle/` — `ListStyleSettingsView.kt`, ViewModel, Event, UiState
- `customlists/` — `CustomListsView.kt`, ViewModel, Event, UiState

**Integration:** `app/src/main/java/eu/kanade/presentation/more/settings/screen/anilist/`

**What to do:**
1. Add an "AniList" section to PLAY-ON's Settings → Tracking screen.
2. Settings include: title language, staff name language, score format, NSFW toggle, theme customization, list style preferences.
3. List style settings (grid vs list, items per row) for the AniList media list view.

---

### Feature: Login (`feature/login/`)

**Source files (1 file):**
- `LoginView.kt` — OAuth WebView flow

**Integration:** **SKIP** — PLAY-ON already has AniList login via `Anilist.kt`'s `login()` method. Reuse it. Just add a convenience "Login to AniList" button in the new AniList home screen for unauthenticated users.

---

## Phase 6 — Extras

### Feature: Airing Widget (`feature/widget/`)

**Source files (2 files):**
- `AiringWidget.kt` — Glance AppWidget showing next airing anime
- `WidgetTheme.kt`

**Integration:** `presentation-widget/src/main/java/eu/kanade/presentation/widget/anilist/`

**What to do:**
1. Port the Glance widget — it shows upcoming episodes from the user's watching list.
2. Add widget metadata XML and register in `AndroidManifest.xml`.
3. Dependency: `androidx-glance-appwidget` (already in AniHyou's deps, add to PLAY-ON).

---

### Feature: Notification Worker (`feature/worker/`)

**Source files (2 files):**
- `NotificationWorker.kt` — Periodic check for new AniList notifications
- `WorkerModule.kt`

**Integration:** `app/src/main/java/eu/kanade/tachiyomi/data/notification/anilist/`

**What to do:**
1. Port the WorkManager-based periodic notification check.
2. PLAY-ON already uses WorkManager for library updates — follow the same pattern.
3. Register the worker in the app's `DomainModule`.

---

### Feature: WearOS (`wearos/`)

**Source files (22 files)** — Complete Wear OS companion app.

**Integration:** **SKIP entirely.** WearOS is a separate APK module and adds significant build complexity. It can be considered for a future version but is not part of this integration scope.

---

### Feature: Baseline Profile (`baselineprofile/`)

**Source files (2 files)** — Performance benchmarking.

**Integration:** **SKIP.** PLAY-ON has its own benchmark setup. Not relevant for feature porting.

---

## Navigation Integration Plan

### New Bottom Navigation Tab

Add an "AniList" tab to PLAY-ON's `HomeScreen`:

```
HomeScreen.Tab.AniList → AnilistHomeScreen
```

This tab contains the AniList home (discover, currently watching, airing, activity feed). All AniList detail screens are pushed onto the Voyager navigator from this tab.

### Deep Link Support

Add AniList URL handling to `MainActivity.handleIntentAction()`:

```
https://anilist.co/anime/{id} → AnilistMediaDetailsScreen(id)
https://anilist.co/manga/{id} → AnilistMediaDetailsScreen(id)
https://anilist.co/character/{id} → AnilistCharacterScreen(id)
https://anilist.co/staff/{id} → AnilistStaffScreen(id)
https://anilist.co/studio/{id} → AnilistStudioScreen(id)
https://anilist.co/user/{name} → AnilistProfileScreen(name)
```

---

## File Count Summary

| AniHyou Source | Files | Action |
|---|---|---|
| `core/base/` | 9 | Port (adapt to Injekt) |
| `core/model/` | 65 | Port (new models) |
| `core/network/` | 99 | **Port** (Apollo schema/operations + client wiring) |
| `core/domain/` | 18 | Port (repositories → interactors) |
| `core/resources/` | ~140 | Selective merge |
| `core/ui/` | 68 | Selective port |
| `feature/activitydetails/` | 9 | Port |
| `feature/calendar/` | 6 | Port |
| `feature/characterdetails/` | 7 | Port |
| `feature/editmedia/` | 11 | Port |
| `feature/explore/` | 25 | Port |
| `feature/home/` | 17 | Port |
| `feature/login/` | 1 | Skip (reuse existing) |
| `feature/mediadetails/` | 18 | Port |
| `feature/notifications/` | 5 | Port |
| `feature/profile/` | 31 | Port |
| `feature/reviewdetails/` | 4 | Port |
| `feature/settings/` | 15 | Port |
| `feature/staffdetails/` | 8 | Port |
| `feature/studiodetails/` | 4 | Port |
| `feature/thread/` | 11 | Port |
| `feature/usermedialist/` | 15 | Port |
| `feature/widget/` | 2 | Port |
| `feature/worker/` | 2 | Port |
| `app/` (main) | 15 | Selective port |
| `wearos/` | 22 | **Skip** |
| `baselineprofile/` | 2 | **Skip** |
| **Total** | **~712** | **~550 ported, ~140 selective, ~24 skipped** |

---

## Potential Conflicts & Breaking Changes

> [!CAUTION]
> ### Critical Conflicts

1. **Apollo vs OkHttp**: AniHyou's entire data layer is built on Apollo's generated types. Every repository return type references Apollo fragments (`BasicMediaListEntry`, `CommonPage`, etc.). These must be manually replaced with kotlinx.serialization data classes.

2. **Koin vs Injekt DI**: Every `koinInject()`, `by viewModel()`, and Koin `module {}` declaration must be converted to Injekt's `Injekt.get()`, `injectLazy()`, and `addSingletonFactory()`.

3. **Navigation 3 vs Voyager**: AniHyou's `NavBackStack` + `NavActionManager` pattern is completely different from Voyager's `Navigator.push(Screen)`. All inter-screen navigation must be rewritten.

4. **Compose BOM version**: AniHyou uses BOM `2026.02.01` with Material3 `1.5.0-alpha15`. PLAY-ON's Compose version should be verified for compatibility. If PLAY-ON uses an older BOM, some M3 composables may not be available.

5. **Kotlin version**: AniHyou uses Kotlin `2.3.10`. Verify PLAY-ON is on the same or compatible version.

> [!WARNING]
> ### Moderate Risks

6. **Duplicate AniList auth**: PLAY-ON already stores AniList tokens via the tracker system. The new AniList screens must read from the **same token storage** — do not create a second auth flow.

7. **ID type mismatch**: AniHyou uses `Int` for AniList media IDs. PLAY-ON's tracker uses `Long` for `remote_id`. Ensure consistent casting.

8. **Package naming**: AniHyou uses `com.axiel7.anihyou.*`. All code must be repackaged under `eu.kanade.*` to match PLAY-ON's namespace.

9. **ProGuard rules**: AniHyou has specific ProGuard rules for Apollo. Carry forward required Apollo keep rules and validate minified release builds early.

10. **String resource conflicts**: Both apps define some identical string keys (e.g., `app_name`, `settings`). Use prefixed keys (`anilist_*`) for all new strings.

---

## Verification Plan

## Commit & Rollback Strategy

1. Commit after each completed feature slice (AniList tab shell, profile, media details, media list, explore, etc.).
2. Keep each commit single-purpose and reversible.
3. Commit Apollo foundation separately first (version catalog + plugin + schema + generated models).
4. Commit tracker migration (`AnilistApi.kt` to Apollo) as a dedicated checkpoint before removing legacy code.
5. Keep theme reduction and preference migration in separate commits from feature work.

### Build Verification
```bash
./gradlew assembleDebug
```
After each phase, verify the project compiles without errors.

### UI Testing
- Manual navigation through each new screen
- Verify all AniList API calls return correct data
- Test with both authenticated and unauthenticated states

### Integration Testing
- Verify AniList tracker still works after changes
- Verify deep links route correctly
- Verify the airing widget updates
- Test on target device (Poco F1, arm64-v8a)

---

