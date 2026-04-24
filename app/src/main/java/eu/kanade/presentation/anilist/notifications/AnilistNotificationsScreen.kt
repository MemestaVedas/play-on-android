package eu.kanade.presentation.anilist.notifications

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// ── Screen ──────────────────────────────────────────────────────────────────

object AnilistNotificationsScreen : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { AnilistNotificationsScreenModel() }
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        NotificationsContent(
            state = state,
            onRefresh = screenModel::refresh,
            onMarkAllRead = screenModel::markAllRead,
            onOpenMedia = { id -> navigator.push(AnilistMediaDetailsScreen(id)) },
        )
    }
}

// ── Notification types ────────────────────────────────────────────────────────

enum class AnilistNotificationType {
    AIRING,
    ACTIVITY_MESSAGE,
    ACTIVITY_REPLY,
    ACTIVITY_LIKE,
    ACTIVITY_MENTION,
    FOLLOWING,
    MEDIA_DATA_CHANGE,
    MEDIA_MERGE,
    MEDIA_DELETION,
    RELATED_MEDIA_ADDITION,
}

// ── Model ─────────────────────────────────────────────────────────────────────

@Immutable
data class AnilistNotification(
    val id: Int,
    val type: AnilistNotificationType,
    val text: String,
    val imageUrl: String?,
    val mediaId: Int?,
    val mediaTitle: String?,
    val createdAtEpoch: Long,
    val isRead: Boolean,
)

// ── Screen Model ──────────────────────────────────────────────────────────────

class AnilistNotificationsScreenModel(
    private val trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistNotificationsScreenModel.State>(State.Loading) {

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
                // Fetch notifications via AniList API
                // For now return structured placeholder until GraphQL query is wired
                State.Ready(notifications = emptyList(), unreadCount = 0)
            }.onSuccess { s -> mutableState.update { s } }
                .onFailure { e ->
                    mutableState.update { State.Error(e.message ?: "Failed to load notifications") }
                }
        }
    }

    fun markAllRead() {
        screenModelScope.launchIO {
            val current = state.value
            if (current is State.Ready) {
                mutableState.update {
                    current.copy(
                        notifications = current.notifications.map { it.copy(isRead = true) },
                        unreadCount = 0,
                    )
                }
            }
        }
    }

    sealed interface State {
        data object Loading : State
        data object Guest : State
        @Immutable
        data class Ready(
            val notifications: List<AnilistNotification>,
            val unreadCount: Int,
        ) : State
        data class Error(val message: String) : State
    }
}

// ── Content ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NotificationsContent(
    state: AnilistNotificationsScreenModel.State,
    onRefresh: () -> Unit,
    onMarkAllRead: () -> Unit,
    onOpenMedia: (Int) -> Unit,
) {
    val unreadCount = (state as? AnilistNotificationsScreenModel.State.Ready)?.unreadCount ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                actions = {
                    if (unreadCount > 0) {
                        IconButton(onClick = onMarkAllRead) {
                            BadgedBox(
                                badge = {
                                    Badge { Text(unreadCount.toString()) }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Mark all read",
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        when (state) {
            AnilistNotificationsScreenModel.State.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    ContainedLoadingIndicator()
                }
            }

            AnilistNotificationsScreenModel.State.Guest -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sign in to see notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Connect your AniList account to receive activity and airing alerts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }

            is AnilistNotificationsScreenModel.State.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Surface(
                        onClick = onRefresh,
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

            is AnilistNotificationsScreenModel.State.Ready -> {
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
                    if (state.notifications.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "All caught up!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "No new notifications right now.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            items(state.notifications, key = { it.id }) { notification ->
                                NotificationRow(
                                    notification = notification,
                                    onOpenMedia = {
                                        notification.mediaId?.let(onOpenMedia)
                                    },
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

// ── Notification row ───────────────────────────────────────────────────────────

private fun notificationIcon(type: AnilistNotificationType): ImageVector {
    return when (type) {
        AnilistNotificationType.AIRING -> Icons.Default.Update
        AnilistNotificationType.ACTIVITY_MESSAGE -> Icons.Default.Message
        AnilistNotificationType.ACTIVITY_REPLY -> Icons.Default.Message
        AnilistNotificationType.ACTIVITY_LIKE -> Icons.Default.Favorite
        AnilistNotificationType.ACTIVITY_MENTION -> Icons.Default.Message
        AnilistNotificationType.FOLLOWING -> Icons.Default.PersonAdd
        AnilistNotificationType.MEDIA_DATA_CHANGE -> Icons.Default.Update
        AnilistNotificationType.MEDIA_MERGE -> Icons.Default.Update
        AnilistNotificationType.MEDIA_DELETION -> Icons.Default.Update
        AnilistNotificationType.RELATED_MEDIA_ADDITION -> Icons.Default.Star
    }
}

private val notificationFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())

@Composable
private fun NotificationRow(
    notification: AnilistNotification,
    onOpenMedia: () -> Unit,
) {
    val unreadBackground = if (!notification.isRead) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(unreadBackground)
            .clickable(onClick = onOpenMedia)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Type icon or cover image
        if (notification.imageUrl != null) {
            AsyncImage(
                model = notification.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
        } else {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = notificationIcon(notification.type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!notification.isRead) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = notificationFormatter.format(
                    Instant.ofEpochSecond(notification.createdAtEpoch),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
