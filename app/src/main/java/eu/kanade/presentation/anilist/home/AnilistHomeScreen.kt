package eu.kanade.presentation.anilist.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import coil3.compose.AsyncImage
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.track.anilist.AnilistApi
import java.time.Duration

object AnilistHomeScreen : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { AnilistHomeScreenModel() }
        val state by screenModel.state.collectAsState()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (val currentState = state) {
                is AnilistHomeScreenModel.State.Loading -> LoadingState()
                is AnilistHomeScreenModel.State.Guest -> GuestState()
                is AnilistHomeScreenModel.State.Error -> ErrorState(
                    message = currentState.message,
                    onRetry = screenModel::refresh,
                )
                is AnilistHomeScreenModel.State.Ready -> HomeContent(
                    dashboard = currentState.dashboard,
                    onRetry = screenModel::refresh,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun GuestState() {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Sign in to AniList to load your dashboard.",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Your tracker login is reused here, so there is no second account flow.",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = { uriHandler.openUri(AnilistApi.authUrl().toString()) },
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text("Log in to AniList")
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "AniList failed to load",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun HomeContent(
    dashboard: AnilistApi.HomeDashboard,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCard(dashboard = dashboard, onRetry = onRetry)
        }

        item {
            SectionHeader(
                title = "Airing soon",
                subtitle = "Your next AniList updates at a glance",
            )
        }

        if (dashboard.airingMedia.isEmpty()) {
            item {
                EmptyStateCard()
            }
        } else {
            items(dashboard.airingMedia, key = { it.id }) { media ->
                AiringCard(media = media)
            }
        }
    }
}

@Composable
private fun HeroCard(
    dashboard: AnilistApi.HomeDashboard,
    onRetry: () -> Unit,
) {
    val viewer = dashboard.viewer
    val accent = profileContainerColor(viewer.profileColor)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accent),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AsyncImage(
                    model = viewer.avatarUrl,
                    contentDescription = viewer.name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.medium),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = viewer.name,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = buildList {
                            viewer.titleLanguage?.let { add(it.lowercase().replaceFirstChar(Char::titlecase)) }
                            viewer.scoreFormat?.let { add(it) }
                        }.joinToString(" | "),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusChip(label = "${dashboard.unreadNotifications} unread")
                StatusChip(label = "AniList linked")
            }

            viewer.aboutHtml?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = stripHtml(it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            OutlinedButton(onClick = onRetry) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Text(
            text = "No airing entries found on your AniList list yet.",
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AiringCard(media: AnilistApi.HomeAiringMedia) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                model = media.coverImageUrl,
                contentDescription = media.title,
                modifier = Modifier
                    .size(width = 88.dp, height = 124.dp)
                    .clip(MaterialTheme.shapes.medium),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = buildString {
                        media.nextEpisode?.let { append("Episode ").append(it) }
                        media.timeUntilAiringSeconds?.let {
                            if (isNotEmpty()) append(" | ")
                            append(formatTimeUntilAiring(it))
                        }
                        media.progress?.let {
                            if (isNotEmpty()) append(" | ")
                            append("Progress ").append(it)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    media.listStatus?.let { StatusChip(label = it) }
                    media.meanScore?.let { StatusChip(label = "$it score") }
                    media.mediaType?.let { StatusChip(label = it.lowercase().replaceFirstChar(Char::titlecase)) }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun profileContainerColor(value: String?) =
    when (value?.lowercase()) {
        "blue" -> MaterialTheme.colorScheme.primaryContainer
        "purple" -> MaterialTheme.colorScheme.tertiaryContainer
        "pink" -> MaterialTheme.colorScheme.tertiaryContainer
        "orange" -> MaterialTheme.colorScheme.secondaryContainer
        "red" -> MaterialTheme.colorScheme.errorContainer
        "green" -> MaterialTheme.colorScheme.secondaryContainer
        "gray" -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

private fun formatTimeUntilAiring(seconds: Int): String {
    val duration = Duration.ofSeconds(seconds.toLong())
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes().coerceAtLeast(0)
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m left"
        hours > 0 -> "${hours}h left"
        minutes > 0 -> "${minutes}m left"
        else -> "Airing soon"
    }
}

private fun stripHtml(value: String): String {
    return value
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]+>"), "")
        .trim()
}
