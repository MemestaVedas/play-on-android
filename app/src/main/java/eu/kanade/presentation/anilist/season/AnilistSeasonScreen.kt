package eu.kanade.presentation.anilist.season

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import java.time.LocalDate

// ── Seasons ───────────────────────────────────────────────────────────────────

enum class AnimeSeason(val label: String) {
    WINTER("Winter ❄️"),
    SPRING("Spring 🌸"),
    SUMMER("Summer ☀️"),
    FALL("Fall 🍂"),
}

fun currentSeason(): AnimeSeason {
    return when (LocalDate.now().monthValue) {
        in 1..3 -> AnimeSeason.WINTER
        in 4..6 -> AnimeSeason.SPRING
        in 7..9 -> AnimeSeason.SUMMER
        else -> AnimeSeason.FALL
    }
}

// ── Media entry ───────────────────────────────────────────────────────────────

@Immutable
data class SeasonMediaEntry(
    val id: Int,
    val title: String,
    val coverUrl: String?,
    val genres: List<String>,
    val meanScore: Int?,
    val episodes: Int?,
    val status: String?,
    val format: String?,
)

// ── Screen ────────────────────────────────────────────────────────────────────

class AnilistSeasonScreen(
    private val initialYear: Int = LocalDate.now().year,
    private val initialSeason: AnimeSeason = currentSeason(),
) : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            AnilistSeasonScreenModel(initialYear, initialSeason)
        }
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        SeasonContent(
            state = state,
            onPreviousSeason = screenModel::previousSeason,
            onNextSeason = screenModel::nextSeason,
            onRefresh = screenModel::refresh,
            onOpenMedia = { id -> navigator.push(AnilistMediaDetailsScreen(id)) },
            onBack = navigator::pop,
        )
    }
}

// ── Screen Model ──────────────────────────────────────────────────────────────

class AnilistSeasonScreenModel(
    private var year: Int,
    private var season: AnimeSeason,
    private val trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistSeasonScreenModel.State>(
    State.Loading(year, season)
) {

    private val tracker = trackerManager.aniList
    private val api by lazy {
        AnilistApi(tracker.client, AnilistInterceptor(tracker, tracker.getPassword()))
    }

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launchIO {
            mutableState.update { State.Loading(year, season) }
            runCatching {
                // Will be replaced with real AniList seasonal GraphQL call
                State.Ready(
                    year = year,
                    season = season,
                    media = emptyList(),
                )
            }.onSuccess { s -> mutableState.update { s } }
                .onFailure { e ->
                    mutableState.update { State.Error(year, season, e.message ?: "Failed") }
                }
        }
    }

    fun previousSeason() {
        val allSeasons = AnimeSeason.entries
        val idx = allSeasons.indexOf(season)
        if (idx == 0) {
            season = allSeasons.last()
            year--
        } else {
            season = allSeasons[idx - 1]
        }
        refresh()
    }

    fun nextSeason() {
        val allSeasons = AnimeSeason.entries
        val idx = allSeasons.indexOf(season)
        if (idx == allSeasons.lastIndex) {
            season = allSeasons.first()
            year++
        } else {
            season = allSeasons[idx + 1]
        }
        refresh()
    }

    sealed interface State {
        val year: Int
        val season: AnimeSeason

        data class Loading(override val year: Int, override val season: AnimeSeason) : State
        @Immutable
        data class Ready(
            override val year: Int,
            override val season: AnimeSeason,
            val media: List<SeasonMediaEntry>,
        ) : State
        data class Error(
            override val year: Int,
            override val season: AnimeSeason,
            val message: String,
        ) : State
    }
}

// ── Content ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SeasonContent(
    state: AnilistSeasonScreenModel.State,
    onPreviousSeason: () -> Unit,
    onNextSeason: () -> Unit,
    onRefresh: () -> Unit,
    onOpenMedia: (Int) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.season.label,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = state.year.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onPreviousSeason) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous season")
                    }
                    IconButton(onClick = onNextSeason) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next season")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        when (state) {
            is AnilistSeasonScreenModel.State.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    ContainedLoadingIndicator()
                }
            }

            is AnilistSeasonScreenModel.State.Error -> {
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

            is AnilistSeasonScreenModel.State.Ready -> {
                val pullRefreshState = rememberPullToRefreshState()
                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = onRefresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    state = pullRefreshState,
                    indicator = {
                        PullToRefreshDefaults.LoadingIndicator(
                            state = pullRefreshState,
                            isRefreshing = false,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    },
                ) {
                    if (state.media.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "No anime this season yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.media, key = { it.id }) { entry ->
                                SeasonMediaCard(
                                    entry = entry,
                                    onClick = { onOpenMedia(entry.id) },
                                )
                            }
                            item { Spacer(modifier = Modifier.height(84.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ── Season media card ─────────────────────────────────────────────────────────

@Composable
private fun SeasonMediaCard(
    entry: SeasonMediaEntry,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column {
            AsyncImage(
                model = entry.coverUrl,
                contentDescription = entry.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            )

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                val meta = listOfNotNull(
                    entry.format,
                    entry.episodes?.let { "$it ep" },
                    entry.meanScore?.let { "${it}%" },
                ).joinToString(" · ")

                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (entry.genres.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        entry.genres.take(2).forEach { genre ->
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(genre, style = MaterialTheme.typography.labelSmall)
                                },
                                modifier = Modifier.height(24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
