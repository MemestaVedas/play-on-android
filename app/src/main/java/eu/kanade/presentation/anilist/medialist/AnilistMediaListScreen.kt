package eu.kanade.presentation.anilist.medialist

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import eu.kanade.presentation.anilist.details.AnilistMediaDetailsScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.track.TrackerManager
import eu.kanade.tachiyomi.data.track.anilist.AnilistApi
import eu.kanade.tachiyomi.data.track.anilist.AnilistInterceptor
import kotlinx.coroutines.flow.update
import tachiyomi.core.common.util.lang.launchIO
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope

// ── Screen ──────────────────────────────────────────────────────────────────

object AnilistMediaListScreen : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { AnilistMediaListScreenModel() }
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        MediaListContent(
            state = state,
            onRefresh = screenModel::refresh,
            onOpenMedia = { id -> navigator.push(AnilistMediaDetailsScreen(id)) },
        )
    }
}

// ── Screen Model ────────────────────────────────────────────────────────────

class AnilistMediaListScreenModel(
    private val trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistMediaListScreenModel.State>(State.Loading) {

    private val tracker = trackerManager.aniList
    private val api by lazy {
        AnilistApi(tracker.client, AnilistInterceptor(tracker, tracker.getPassword()))
    }

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launchIO {
            if (!tracker.isLoggedIn) {
                mutableState.update { State.Guest }
                return@launchIO
            }
            mutableState.update { State.Loading }
            runCatching {
                val dashboard = api.getHomeDashboard()
                State.Ready(
                    animeList = dashboard.airingMedia.map { m ->
                        MediaListEntry(
                            id = m.id,
                            title = m.title,
                            coverUrl = m.coverImageUrl,
                            progress = m.progress ?: 0,
                            total = m.totalEpisodes,
                            score = null,
                            status = "WATCHING",
                            mediaType = "ANIME",
                            nextEpisode = m.nextEpisode,
                            timeUntilAiring = m.timeUntilAiringSeconds,
                        )
                    },
                    mangaList = dashboard.readingMedia.map { m ->
                        MediaListEntry(
                            id = m.id,
                            title = m.title,
                            coverUrl = m.coverImageUrl,
                            progress = m.progress ?: 0,
                            total = m.totalChapters,
                            score = null,
                            status = "READING",
                            mediaType = "MANGA",
                            nextEpisode = null,
                            timeUntilAiring = null,
                        )
                    },
                )
            }.onSuccess { s -> mutableState.update { s } }
                .onFailure { e -> mutableState.update { State.Error(e.message ?: "Failed to load") } }
        }
    }

    @Immutable
    data class MediaListEntry(
        val id: Int,
        val title: String,
        val coverUrl: String?,
        val progress: Int,
        val total: Int?,
        val score: Float?,
        val status: String,
        val mediaType: String,
        val nextEpisode: Int?,
        val timeUntilAiring: Int?,
    )

    sealed interface State {
        data object Loading : State
        data object Guest : State
        @Immutable
        data class Ready(
            val animeList: List<MediaListEntry>,
            val mangaList: List<MediaListEntry>,
        ) : State
        data class Error(val message: String) : State
    }
}

// ── Status tab labels ────────────────────────────────────────────────────────

private val animeStatusTabs = listOf(
    "Watching", "Completed", "Planning", "Paused", "Dropped",
)

private val mangaStatusTabs = listOf(
    "Reading", "Completed", "Planning", "Paused", "Dropped",
)

// ── List/Grid toggle enum ────────────────────────────────────────────────────

private enum class ListDisplayStyle { LIST, GRID }

// ── Main content ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaListContent(
    state: AnilistMediaListScreenModel.State,
    onRefresh: () -> Unit,
    onOpenMedia: (Int) -> Unit,
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
    )
    var isAnimeTab by rememberSaveable { mutableStateOf(true) }
    var statusTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var displayStyle by rememberSaveable { mutableStateOf(ListDisplayStyle.LIST) }
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

    if (showFilterSheet) {
        FilterBottomSheet(onDismiss = { showFilterSheet = false })
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("My Lists", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconToggleButton(
                        checked = displayStyle == ListDisplayStyle.GRID,
                        onCheckedChange = {
                            displayStyle = if (it) ListDisplayStyle.GRID else ListDisplayStyle.LIST
                        },
                    ) {
                        Icon(
                            imageVector = if (displayStyle == ListDisplayStyle.GRID)
                                Icons.Default.GridView else Icons.Default.List,
                            contentDescription = "Toggle view",
                        )
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Anime / Manga toggle
            MediaTypeToggle(
                isAnime = isAnimeTab,
                onAnimeClick = { isAnimeTab = true; statusTabIndex = 0 },
                onMangaClick = { isAnimeTab = false; statusTabIndex = 0 },
            )

            // Status tabs
            val statusTabs = if (isAnimeTab) animeStatusTabs else mangaStatusTabs
            PrimaryScrollableTabRow(selectedTabIndex = statusTabIndex) {
                statusTabs.forEachIndexed { index, label ->
                    Tab(
                        selected = statusTabIndex == index,
                        onClick = { statusTabIndex = index },
                        text = { Text(label) },
                    )
                }
            }

            // Content based on state
            when (state) {
                AnilistMediaListScreenModel.State.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        ContainedLoadingIndicator()
                    }
                }

                AnilistMediaListScreenModel.State.Guest -> {
                    GuestPrompt()
                }

                is AnilistMediaListScreenModel.State.Error -> {
                    ErrorPrompt(message = state.message, onRetry = onRefresh)
                }

                is AnilistMediaListScreenModel.State.Ready -> {
                    val pullRefreshState = rememberPullToRefreshState()
                    val list = if (isAnimeTab) state.animeList else state.mangaList

                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize(),
                        state = pullRefreshState,
                        indicator = {
                            PullToRefreshDefaults.LoadingIndicator(
                                state = pullRefreshState,
                                isRefreshing = false,
                                modifier = Modifier.align(Alignment.TopCenter),
                            )
                        },
                    ) {
                        if (list.isEmpty()) {
                            EmptyListPrompt(
                                isAnime = isAnimeTab,
                                statusLabel = statusTabs.getOrElse(statusTabIndex) { "entries" },
                            )
                        } else {
                            when (displayStyle) {
                                ListDisplayStyle.LIST -> MediaListView(
                                    items = list,
                                    isAnime = isAnimeTab,
                                    onOpenMedia = onOpenMedia,
                                )
                                ListDisplayStyle.GRID -> MediaGridView(
                                    items = list,
                                    onOpenMedia = onOpenMedia,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Anime/Manga type toggle ──────────────────────────────────────────────────

@Composable
private fun MediaTypeToggle(
    isAnime: Boolean,
    onAnimeClick: () -> Unit,
    onMangaClick: () -> Unit,
) {
    val animeColor by animateColorAsState(
        targetValue = if (isAnime) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(),
        label = "animeColor",
    )
    val mangaColor by animateColorAsState(
        targetValue = if (!isAnime) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(),
        label = "mangaColor",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.large,
            color = animeColor,
            onClick = onAnimeClick,
        ) {
            Text(
                text = "Anime",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isAnime) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.large,
            color = mangaColor,
            onClick = onMangaClick,
        ) {
            Text(
                text = "Manga",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (!isAnime) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

// ── List view ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaListView(
    items: List<AnilistMediaListScreenModel.MediaListEntry>,
    isAnime: Boolean,
    onOpenMedia: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(items, key = { it.id }) { entry ->
            MediaListRow(
                entry = entry,
                isAnime = isAnime,
                onClick = { onOpenMedia(entry.id) },
                modifier = Modifier.animateItem(),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 86.dp))
        }
        item { Spacer(modifier = Modifier.height(84.dp)) }
    }
}

@Composable
private fun MediaListRow(
    entry: AnilistMediaListScreenModel.MediaListEntry,
    isAnime: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progressFraction = remember(entry.progress, entry.total) {
        val total = entry.total ?: return@remember if (entry.progress > 0) 0.5f else 0f
        if (total <= 0) 0f else (entry.progress.toFloat() / total).coerceIn(0f, 1f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Cover
        Box {
            AsyncImage(
                model = entry.coverUrl,
                contentDescription = entry.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 54.dp, height = 76.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
            // Score badge
            entry.score?.let { score ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(2.dp),
                ) {
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
        }

        // Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            // Airing info for anime
            if (isAnime && entry.nextEpisode != null) {
                Text(
                    text = "Ep ${entry.nextEpisode} airing soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Progress
            val progressLabel = buildString {
                append(entry.progress)
                entry.total?.let { append("/$it") }
                append(if (isAnime) " ep" else " ch")
            }
            Text(
                text = progressLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(MaterialTheme.shapes.small),
            )
        }

        // +1 increment button
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .size(36.dp)
                .clickable { /* increment progress */ },
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increment progress",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

// ── Grid view ────────────────────────────────────────────────────────────────

@Composable
private fun MediaGridView(
    items: List<AnilistMediaListScreenModel.MediaListEntry>,
    onOpenMedia: (Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.id }) { entry ->
            MediaGridCard(
                entry = entry,
                onClick = { onOpenMedia(entry.id) },
            )
        }
        item { Spacer(modifier = Modifier.height(84.dp)) }
    }
}

@Composable
private fun MediaGridCard(
    entry: AnilistMediaListScreenModel.MediaListEntry,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box {
            AsyncImage(
                model = entry.coverUrl,
                contentDescription = entry.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 80f,
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${entry.progress}${entry.total?.let { "/$it" } ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
            entry.score?.let { score ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                ) {
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

// ── Filter bottom sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartialExpansion = false)
    val sortOptions = listOf("Title", "Score", "Progress", "Last Updated", "Average Score")
    var selectedSort by rememberSaveable { mutableIntStateOf(0) }
    var isAscending by rememberSaveable { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Sort & Filter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Sort by",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            sortOptions.forEachIndexed { index, label ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSort = index }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (selectedSort == index) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            HorizontalDivider()

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = isAscending,
                    onClick = { isAscending = true },
                    label = { Text("Ascending") },
                )
                FilterChip(
                    selected = !isAscending,
                    onClick = { isAscending = false },
                    label = { Text("Descending") },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Guest / Error / Empty states ──────────────────────────────────────────────

@Composable
private fun GuestPrompt() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Sign in to AniList",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Your anime & manga list will appear here once you connect your AniList account.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorPrompt(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Failed to load lists",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Surface(
            onClick = onRetry,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(
                text = "Retry",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EmptyListPrompt(isAnime: Boolean, statusLabel: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No ${if (isAnime) "anime" else "manga"} in $statusLabel",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Add some from the Explore tab or the media detail screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
