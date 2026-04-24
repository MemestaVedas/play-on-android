package eu.kanade.presentation.anilist.profile

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
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

// ── Screen ──────────────────────────────────────────────────────────────────

class AnilistUserProfileScreen(private val userId: Int) : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { AnilistUserProfileScreenModel(userId) }
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        UserProfileContent(
            state = state,
            onBack = navigator::pop,
            onRefresh = screenModel::refresh,
            onOpenMedia = { id -> navigator.push(AnilistMediaDetailsScreen(id)) },
        )
    }
}

// ── Domain models ─────────────────────────────────────────────────────────────

@Immutable
data class UserStats(
    val totalAnime: Int,
    val daysWatched: Float,
    val meanAnimeScore: Float,
    val totalManga: Int,
    val chaptersRead: Int,
    val meanMangaScore: Float,
)

@Immutable
data class FavoriteMedia(
    val id: Int,
    val title: String,
    val coverUrl: String?,
)

// ── Screen Model ──────────────────────────────────────────────────────────────

class AnilistUserProfileScreenModel(
    private val userId: Int,
    private val trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistUserProfileScreenModel.State>(State.Loading) {

    private val tracker = trackerManager.aniList
    private val api by lazy {
        AnilistApi(tracker.client, AnilistInterceptor(tracker, tracker.getPassword()))
    }

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launchIO {
            mutableState.update { State.Loading }
            runCatching {
                // Fetch user profile from AniList — will be wired to real GraphQL later
                State.Ready(
                    userId = userId,
                    username = "User",
                    avatarUrl = null,
                    bannerUrl = null,
                    about = null,
                    stats = UserStats(0, 0f, 0f, 0, 0, 0f),
                    favoriteAnime = emptyList(),
                    favoriteManga = emptyList(),
                    favoriteCharacters = emptyList(),
                )
            }.onSuccess { s -> mutableState.update { s } }
                .onFailure { e ->
                    mutableState.update { State.Error(e.message ?: "Failed to load profile") }
                }
        }
    }

    sealed interface State {
        data object Loading : State
        @Immutable
        data class Ready(
            val userId: Int,
            val username: String,
            val avatarUrl: String?,
            val bannerUrl: String?,
            val about: String?,
            val stats: UserStats,
            val favoriteAnime: List<FavoriteMedia>,
            val favoriteManga: List<FavoriteMedia>,
            val favoriteCharacters: List<FavoriteMedia>,
        ) : State
        data class Error(val message: String) : State
    }
}

// ── Content ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UserProfileContent(
    state: AnilistUserProfileScreenModel.State,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenMedia: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (state) {
                        is AnilistUserProfileScreenModel.State.Ready ->
                            Text(state.username, fontWeight = FontWeight.Bold)
                        else -> Text("Profile")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { padding ->
        when (state) {
            AnilistUserProfileScreenModel.State.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    ContainedLoadingIndicator()
                }
            }

            is AnilistUserProfileScreenModel.State.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Surface(
                        onClick = onRefresh,
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        Text(
                            "Retry",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        )
                    }
                }
            }

            is AnilistUserProfileScreenModel.State.Ready -> {
                ProfileReadyContent(
                    state = state,
                    topPadding = padding.calculateTopPadding(),
                    onOpenMedia = onOpenMedia,
                )
            }
        }
    }
}

// ── Profile ready state ───────────────────────────────────────────────────────

@Composable
private fun ProfileReadyContent(
    state: AnilistUserProfileScreenModel.State.Ready,
    topPadding: androidx.compose.ui.unit.Dp,
    onOpenMedia: (Int) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Favorites", "Stats")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 84.dp),
    ) {
        // Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topPadding + 100.dp),
            ) {
                if (state.bannerUrl != null) {
                    AsyncImage(
                        model = state.bannerUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                    ),
                                ),
                            ),
                    )
                }
                // Dark overlay at bottom so avatar is visible
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(0.4f)),
                            ),
                        ),
                )
            }
        }

        // Avatar + name
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(72.dp),
                ) {
                    if (state.avatarUrl != null) {
                        AsyncImage(
                            model = state.avatarUrl,
                            contentDescription = state.username,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Column {
                    Text(
                        text = state.username,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "AniList Member",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // About
        if (state.about != null) {
            item {
                Text(
                    text = state.about,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .animateContentSize(),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Quick stats row
        item {
            QuickStatsRow(stats = state.stats)
        }

        // Sticky tabs
        item {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) },
                    )
                }
            }
        }

        // Tab content (inlined to avoid nested scrollable)
        when (selectedTab) {
            0 -> {
                // Favorites preview
                if (state.favoriteAnime.isNotEmpty()) {
                    item {
                        FavoritesSectionHeader("Favorite Anime")
                    }
                    item {
                        FavoritesRow(
                            items = state.favoriteAnime,
                            onOpen = onOpenMedia,
                        )
                    }
                }
                if (state.favoriteManga.isNotEmpty()) {
                    item {
                        FavoritesSectionHeader("Favorite Manga")
                    }
                    item {
                        FavoritesRow(
                            items = state.favoriteManga,
                            onOpen = onOpenMedia,
                        )
                    }
                }
                if (state.favoriteAnime.isEmpty() && state.favoriteManga.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant,
                            )
                            Text(
                                text = "No favorites yet",
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            1 -> {
                // Full favorites list
                if (state.favoriteAnime.isNotEmpty()) {
                    item { FavoritesSectionHeader("Anime (${state.favoriteAnime.size})") }
                    items(state.favoriteAnime) { fav ->
                        FavoriteRow(fav = fav, onOpen = { onOpenMedia(fav.id) })
                        HorizontalDivider(modifier = Modifier.padding(start = 70.dp))
                    }
                }
                if (state.favoriteManga.isNotEmpty()) {
                    item { FavoritesSectionHeader("Manga (${state.favoriteManga.size})") }
                    items(state.favoriteManga) { fav ->
                        FavoriteRow(fav = fav, onOpen = { onOpenMedia(fav.id) })
                        HorizontalDivider(modifier = Modifier.padding(start = 70.dp))
                    }
                }
            }

            2 -> {
                item { DetailedStatsSection(stats = state.stats) }
            }
        }
    }
}

// ── Quick stats ───────────────────────────────────────────────────────────────

@Composable
private fun QuickStatsRow(stats: UserStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        QuickStatItem(label = "Anime", value = stats.totalAnime.toString())
        QuickStatItem(label = "Days", value = String.format("%.1f", stats.daysWatched))
        QuickStatItem(label = "Manga", value = stats.totalManga.toString())
        QuickStatItem(label = "Chapters", value = stats.chaptersRead.toString())
    }
}

@Composable
private fun QuickStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Favorites ─────────────────────────────────────────────────────────────────

@Composable
private fun FavoritesSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun FavoritesRow(
    items: List<FavoriteMedia>,
    onOpen: (Int) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items, key = { it.id }) { fav ->
            Column(
                modifier = Modifier
                    .width(96.dp)
                    .clickable { onOpen(fav.id) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size(width = 96.dp, height = 136.dp),
                ) {
                    AsyncImage(
                        model = fav.coverUrl,
                        contentDescription = fav.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Text(
                    text = fav.title,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    fav: FavoriteMedia,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = fav.coverUrl,
            contentDescription = fav.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 46.dp, height = 66.dp)
                .clip(MaterialTheme.shapes.small),
        )
        Text(
            text = fav.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Detailed stats ────────────────────────────────────────────────────────────

@Composable
private fun DetailedStatsSection(stats: UserStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Anime Stats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        StatCard(label = "Total Anime", value = stats.totalAnime.toString())
        StatCard(label = "Days Watched", value = String.format("%.1f", stats.daysWatched))
        StatCard(label = "Mean Score", value = String.format("%.1f", stats.meanAnimeScore))

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Manga Stats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        StatCard(label = "Total Manga", value = stats.totalManga.toString())
        StatCard(label = "Chapters Read", value = stats.chaptersRead.toString())
        StatCard(label = "Mean Score", value = String.format("%.1f", stats.meanMangaScore))
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
