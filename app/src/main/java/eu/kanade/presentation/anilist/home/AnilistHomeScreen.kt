package eu.kanade.presentation.anilist.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import coil3.compose.AsyncImage
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.track.anilist.AnilistApi
import java.time.Duration
import java.time.Instant

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
    val uriHandler = LocalUriHandler.current
    val notifyToggles = remember { mutableStateMapOf<Int, Boolean>() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            TopHeaderCard(
                dashboard = dashboard,
                onRetry = onRetry,
                onOpenProfile = {
                    dashboard.viewer.siteUrl?.let { uriHandler.openUri(it) }
                },
            )
        }

        item {
            SectionHeader(
                title = "Currently Watching",
                actionLabel = "View All",
                onActionClick = {
                    dashboard.viewer.siteUrl
                        ?.let { "$it/animelist" }
                        ?.let { uriHandler.openUri(it) }
                },
            )
        }

        item {
            WatchingCarousel(
                media = dashboard.airingMedia,
                onOpenItem = { media -> openMedia(uriHandler, media.id, media.mediaType) },
            )
        }

        item {
            SectionHeader(
                title = "Currently Reading",
                actionLabel = "View All",
                onActionClick = {
                    dashboard.viewer.siteUrl
                        ?.let { "$it/mangalist" }
                        ?.let { uriHandler.openUri(it) }
                },
            )
        }

        item {
            ReadingCarousel(
                entries = dashboard.readingMedia,
                onOpenItem = { media -> openMedia(uriHandler, media.id, media.mediaType) },
            )
        }

        item {
            SectionHeaderWithPager(title = "Upcoming Episodes")
        }

        item {
            UpcomingSection(
                items = dashboard.airingMedia.take(5),
                isNotified = { id -> notifyToggles[id] == true },
                onToggleNotify = { id ->
                    notifyToggles[id] = !(notifyToggles[id] ?: false)
                },
                onOpenItem = { media -> openMedia(uriHandler, media.id, media.mediaType) },
            )
        }

        item {
            SectionHeader(title = "Personal Statistics")
        }

        item {
            StatisticsSection(dashboard = dashboard)
        }

        item {
            SectionHeaderTabs(title = "Community Feed")
        }

        item {
            CommunityFeedSection(
                dashboard = dashboard,
                onOpenActivityMedia = { activity ->
                    val mediaId = activity.mediaId ?: return@CommunityFeedSection
                    openMedia(uriHandler, mediaId, activity.mediaType)
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

@Composable
private fun TopHeaderCard(
    dashboard: AnilistApi.HomeDashboard,
    onRetry: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val viewer = dashboard.viewer
    val accent = profileContainerColor(viewer.profileColor).copy(alpha = 0.56f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accent),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Editorial Expressive",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = onOpenProfile) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Open AniList profile",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = viewer.avatarUrl,
                    contentDescription = viewer.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = viewer.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = buildList {
                            viewer.titleLanguage?.let { add(it.lowercase().replaceFirstChar(Char::titlecase)) }
                            viewer.scoreFormat?.let { add(it) }
                        }.joinToString(" | "),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip(label = "${dashboard.unreadNotifications} unread alerts")
                StatusChip(label = "AniList linked")
            }

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        actionLabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(enabled = onActionClick != null) { onActionClick?.invoke() },
            )
        }
    }
}

@Composable
private fun WatchingCarousel(
    media: List<AnilistApi.HomeAiringMedia>,
    onOpenItem: (AnilistApi.HomeAiringMedia) -> Unit,
) {
    if (media.isEmpty()) {
        Text(
            text = "No currently watching entries found yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        return
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(end = 8.dp),
    ) {
        items(media, key = { it.id }) { item ->
            WatchingCard(media = item, onClick = { onOpenItem(item) })
        }
    }
}

@Composable
private fun WatchingCard(
    media: AnilistApi.HomeAiringMedia,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(258.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AsyncImage(
                model = media.coverImageUrl,
                contentDescription = media.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(14.dp)),
            )

            LinearProgressIndicator(
                progress = { watchProgress(media.progress, media.totalEpisodes) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

            Text(
                text = media.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = watchSubtitle(media),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ReadingCarousel(
    entries: List<AnilistApi.HomeReadingMedia>,
    onOpenItem: (AnilistApi.HomeReadingMedia) -> Unit,
) {
    if (entries.isEmpty()) {
        Text(
            text = "No currently reading entries found yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        return
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(end = 8.dp),
    ) {
        items(entries, key = { it.id }) { item ->
            Column(
                modifier = Modifier
                    .width(112.dp)
                    .clickable { onOpenItem(item) },
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    AsyncImage(
                        model = item.coverImageUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f),
                    )
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp),
                )

                Text(
                    text = readingSubtitle(item),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SectionHeaderWithPager(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
            }
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next")
            }
        }
    }
}

@Composable
private fun UpcomingSection(
    items: List<AnilistApi.HomeAiringMedia>,
    isNotified: (Int) -> Boolean,
    onToggleNotify: (Int) -> Unit,
    onOpenItem: (AnilistApi.HomeAiringMedia) -> Unit,
) {
    val weekDays = listOf(
        "Monday" to "May 15",
        "Tuesday" to "May 16",
        "Wednesday" to "May 17",
        "Thursday" to "May 18",
        "Friday" to "May 19",
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(weekDays) { (day, date) ->
                val selected = day == "Monday"
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (items.isEmpty()) {
            Text(
                text = "No upcoming episodes available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        items.forEachIndexed { index, media ->
            val faded = index == items.lastIndex
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier
                    .alpha(if (faded) 0.74f else 1f)
                    .clickable { onOpenItem(media) },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = approximateClock(media, index),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(60.dp),
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Episode ${media.nextEpisode ?: "-"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    val notified = isNotified(media.id)
                    Surface(
                        shape = CircleShape,
                        color = if (notified) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.clickable { onToggleNotify(media.id) },
                    ) {
                        Icon(
                            imageVector = if (notified) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = if (notified) "Disable reminder" else "Enable reminder",
                            modifier = Modifier.padding(8.dp),
                            tint = if (notified) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsSection(dashboard: AnilistApi.HomeDashboard) {
    val animeStats = dashboard.animeStats
    val mangaStats = dashboard.mangaStats

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                            ),
                        ),
                    )
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TOTAL WATCH TIME",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = formatMinutesWatchTime(animeStats.minutesWatched),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = "${animeStats.episodesWatched ?: 0} episodes watched",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "VOLUMES READ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = (mangaStats.volumesRead ?: 0).toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "${mangaStats.chaptersRead ?: 0} chapters read",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { ((mangaStats.chaptersRead ?: 0) / 200f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp)),
                )
            }
        }

        val breakdown = dashboard.animeStats.formatBreakdown.take(5)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "FORMAT BREAKDOWN",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (breakdown.isEmpty()) {
                    Text(
                        text = "No format data available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val maxValue = breakdown.maxOf { it.value }.coerceAtLeast(1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 74.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        breakdown.forEachIndexed { index, item ->
                            val ratio = item.value.toFloat() / maxValue.toFloat()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height((ratio * 70f).dp)
                                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                    .background(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.tertiary,
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.error,
                                        )[index],
                                    ),
                            )
                        }
                    }
                    Text(
                        text = breakdown.joinToString("   ") { it.label.replace("_", " ").take(8) },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderTabs(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Recent",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Global",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CommunityFeedSection(
    dashboard: AnilistApi.HomeDashboard,
    onOpenActivityMedia: (AnilistApi.HomeActivity) -> Unit,
) {
    if (dashboard.activityFeed.isEmpty()) {
        Text(
            text = "No recent activity found for this profile.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        dashboard.activityFeed.take(4).forEach { item ->
            ActivityCard(activity = item, onOpenMedia = { onOpenActivityMedia(item) })
        }
    }
}

@Composable
private fun ActivityCard(
    activity: AnilistApi.HomeActivity,
    onOpenMedia: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.clickable(enabled = activity.mediaId != null, onClick = onOpenMedia),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = activity.userAvatarUrl,
                    contentDescription = activity.userName,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape),
                )
                Text(
                    text = buildActivityHeadline(activity),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = relativeTime(activity.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!activity.text.isNullOrBlank()) {
                Text(
                    text = trimHtml(activity.text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Likes",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = " ${activity.likes}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "Comments ${activity.replies}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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

private fun openMedia(uriHandler: UriHandler, mediaId: Int, mediaType: String?) {
    val url = if (mediaType.equals("MANGA", ignoreCase = true)) {
        AnilistApi.mangaUrl(mediaId.toLong())
    } else {
        AnilistApi.animeUrl(mediaId.toLong())
    }
    uriHandler.openUri(url)
}

private fun formatMinutesWatchTime(minutes: Int?): String {
    val value = minutes ?: return "0h"
    val days = value / (60 * 24)
    val hours = (value / 60) % 24
    return if (days > 0) "${days}d ${hours}h" else "${hours}h"
}

private fun readingSubtitle(media: AnilistApi.HomeReadingMedia): String {
    val progress = media.progress ?: 0
    val total = media.totalChapters
    return if (total != null && total > 0) {
        "Chapter $progress/$total"
    } else {
        "Chapter $progress"
    }
}

private fun watchProgress(progress: Int?, total: Int?): Float {
    val p = progress ?: return 0.2f
    val t = total ?: return (p / (p + 2f)).coerceIn(0.12f, 0.96f)
    if (t <= 0) return 0.2f
    return (p.toFloat() / t.toFloat()).coerceIn(0f, 1f)
}

private fun watchSubtitle(media: AnilistApi.HomeAiringMedia): String {
    val episodeText = media.nextEpisode?.let { "Episode $it" } ?: "Episode -"
    val remaining = media.timeUntilAiringSeconds?.let(::formatTimeUntilAiring) ?: "Airing soon"
    return "$episodeText | $remaining"
}

private fun approximateClock(media: AnilistApi.HomeAiringMedia, index: Int): String {
    val hours = ((media.timeUntilAiringSeconds ?: ((index + 1) * 3600)) / 3600)
        .coerceAtLeast(1)
    val base = 17 + (hours % 6)
    val minute = if (index % 2 == 0) "30" else "00"
    return "$base:$minute"
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

private fun buildActivityHeadline(activity: AnilistApi.HomeActivity): String {
    val title = activity.mediaTitle?.let { " $it" }.orEmpty()
    return "${activity.userName} ${activity.action}$title"
}

private fun relativeTime(createdAtSeconds: Int?): String {
    val timestamp = createdAtSeconds?.toLong() ?: return "Recently"
    val now = Instant.now().epochSecond
    val delta = (now - timestamp).coerceAtLeast(0)
    return when {
        delta < 60 -> "Just now"
        delta < 3600 -> "${delta / 60}m ago"
        delta < 86400 -> "${delta / 3600}h ago"
        delta < 604800 -> "${delta / 86400}d ago"
        else -> "${delta / 604800}w ago"
    }
}

private fun trimHtml(value: String): String {
    return value
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .trim()
}
