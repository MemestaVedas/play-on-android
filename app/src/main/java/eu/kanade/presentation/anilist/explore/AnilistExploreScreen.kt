package eu.kanade.presentation.anilist.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import eu.kanade.domain.anilist.model.AnilistSimpleMedia
import eu.kanade.presentation.anilist.details.AnilistMediaDetailsScreen
import eu.kanade.presentation.anilist.season.AnimeSeason
import eu.kanade.presentation.anilist.season.AnilistSeasonScreen
import eu.kanade.presentation.util.Screen
import java.time.LocalDate

object AnilistExploreScreen : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { AnilistExploreScreenModel() }
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val currentYear = LocalDate.now().year

        ExploreContent(
            state = state,
            onQueryChange = screenModel::updateQuery,
            onSearch = screenModel::search,
            onClearQuery = { screenModel.updateQuery("") },
            onOpenMedia = { id -> navigator.push(AnilistMediaDetailsScreen(id)) },
            onOpenSeason = { season -> navigator.push(AnilistSeasonScreen(currentYear, season)) },
        )
    }
}

// ── Data model for the chart/season cards ──────────────────────────────────

private data class ExploreCard(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

private val animeCards = listOf(
    ExploreCard("Top 100 Anime", "All-time ranked", Icons.Default.Star),
    ExploreCard("Trending Now", "This week's hottest", Icons.Default.TrendingUp),
    ExploreCard("Upcoming", "Not yet released", Icons.Default.Star),
    ExploreCard("Currently Airing", "Broadcasting now", Icons.Default.TrendingUp),
    ExploreCard("Top Movies", "Best anime films", Icons.Default.Star),
    ExploreCard("Spring Anime", "Season picks", Icons.Default.Star),
    ExploreCard("Summer Anime", "Season picks", Icons.Default.TrendingUp),
    ExploreCard("Fall Anime", "Season picks", Icons.Default.Star),
    ExploreCard("Winter Anime", "Season picks", Icons.Default.TrendingUp),
)

private val mangaCards = listOf(
    ExploreCard("Top 100 Manga", "All-time ranked", Icons.Default.Star),
    ExploreCard("Trending Manga", "This week's hottest", Icons.Default.TrendingUp),
    ExploreCard("Upcoming Manga", "Not yet released", Icons.Default.Star),
    ExploreCard("Publishing", "Currently active", Icons.Default.TrendingUp),
    ExploreCard("Top Manhwa", "Korean webtoons", Icons.Default.Star),
    ExploreCard("Top Novels", "Light & visual novels", Icons.Default.TrendingUp),
)

// ── Top-level content ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExploreContent(
    state: AnilistExploreScreenModel.State,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearQuery: () -> Unit,
    onOpenMedia: (Int) -> Unit,
    onOpenSeason: (AnimeSeason) -> Unit = {},
) {
    var searchActive by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Search Bar ──────────────────────────────────────────────────────
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    onSearch = { onSearch() },
                    expanded = searchActive,
                    onExpandedChange = { searchActive = it },
                    placeholder = { Text("Search anime, manga, characters…") },
                    leadingIcon = {
                        if (searchActive) {
                            IconButton(onClick = {
                                searchActive = false
                                onClearQuery()
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = onClearQuery) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                )
            },
            expanded = searchActive,
            onExpandedChange = { searchActive = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (searchActive) 0.dp else 16.dp, vertical = 8.dp),
        ) {
            // Search results panel
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ContainedLoadingIndicator()
                }
            }

            state.error?.let { err ->
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    SearchResultRow(
                        item = item,
                        onOpen = { onOpenMedia(item.id) },
                    )
                }
            }
        }

        // ── Browse grid (when not searching) ───────────────────────────────
        if (!searchActive) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Genre chips
                GenreChipRow()

                // Anime section
                SectionLabel("Anime")
                ExploreCardGrid(
                    cards = animeCards,
                    onClick = { card ->
                        when (card.title) {
                            "Spring Anime" -> onOpenSeason(AnimeSeason.SPRING)
                            "Summer Anime" -> onOpenSeason(AnimeSeason.SUMMER)
                            "Fall Anime" -> onOpenSeason(AnimeSeason.FALL)
                            "Winter Anime" -> onOpenSeason(AnimeSeason.WINTER)
                            else -> { /* chart screens — to be wired later */ }
                        }
                    },
                )

                // Manga section
                SectionLabel("Manga")
                ExploreCardGrid(
                    cards = mangaCards,
                    onClick = { /* chart screens */ },
                )

                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

// ── Genre chips ─────────────────────────────────────────────────────────────

private val popularGenres = listOf(
    "Action", "Adventure", "Comedy", "Drama", "Fantasy",
    "Horror", "Mystery", "Romance", "Sci-Fi", "Slice of Life",
    "Sports", "Supernatural", "Thriller",
)

@Composable
private fun GenreChipRow() {
    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(popularGenres) { genre ->
            AssistChip(
                onClick = { /* navigate to genre */ },
                label = { Text(genre) },
            )
        }
    }
}

// ── Section label ───────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
    )
}

// ── 2-column grid of explore cards ─────────────────────────────────────────

@Composable
private fun ExploreCardGrid(
    cards: List<ExploreCard>,
    onClick: (ExploreCard) -> Unit,
) {
    val rows = cards.chunked(2)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { card ->
                    ExploreTileCard(
                        card = card,
                        onClick = { onClick(card) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // If row has only 1 item, fill the other half with space
                if (row.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ExploreTileCard(
    card: ExploreCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = card.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ── Search result row ───────────────────────────────────────────────────────

@Composable
private fun SearchResultRow(
    item: AnilistSimpleMedia,
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
            model = item.coverImageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 44.dp, height = 62.dp)
                .clip(MaterialTheme.shapes.small),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(
                item.format,
                item.status,
                item.meanScore?.let { "${it}%" },
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
