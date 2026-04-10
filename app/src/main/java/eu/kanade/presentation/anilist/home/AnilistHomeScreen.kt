package eu.kanade.presentation.anilist.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.core.model.rememberScreenModel
import coil3.compose.AsyncImage
import eu.kanade.presentation.anilist.explore.AnilistExploreScreen
import eu.kanade.domain.anilist.model.AnilistUserInfo
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.track.anilist.AnilistApi

object AnilistHomeScreen : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { AnilistHomeScreenModel() }
        val state by screenModel.state.collectAsState()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (state) {
                is AnilistHomeScreenModel.State.Loading -> LoadingState()
                is AnilistHomeScreenModel.State.Guest -> GuestState(onRetry = screenModel::refresh)
                is AnilistHomeScreenModel.State.Ready -> ReadyState(
                    viewer = (state as AnilistHomeScreenModel.State.Ready).viewer,
                    onRefresh = screenModel::refresh,
                )
                is AnilistHomeScreenModel.State.Error -> ErrorState(
                    message = (state as AnilistHomeScreenModel.State.Error).message,
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
private fun GuestState(onRetry: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "AniList is not linked yet.",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Log in to load your AniList profile in-app.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = { uriHandler.openUri(AnilistApi.authUrl().toString()) },
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Log in to AniList")
        }
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text("Refresh")
        }
    }
}

@Composable
private fun ReadyState(
    viewer: AnilistUserInfo,
    onRefresh: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val navigator = LocalNavigator.currentOrThrow

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = viewer.avatarUrl,
                contentDescription = viewer.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = viewer.name,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = listOfNotNull(viewer.titleLanguage, viewer.scoreFormat).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        viewer.aboutHtml?.takeIf { it.isNotBlank() }?.let { about ->
            Text(
                text = stripHtml(about),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
        }

        OutlinedButton(onClick = onRefresh) {
            Text("Refresh")
        }

        OutlinedButton(onClick = { navigator.push(AnilistExploreScreen) }) {
            Text("Explore AniList")
        }

        viewer.siteUrl?.let { url ->
            OutlinedButton(onClick = { uriHandler.openUri(url) }) {
                Text("Open AniList profile")
            }
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
            text = "AniList failed to load.",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Retry")
        }
    }
}

private fun stripHtml(value: String): String {
    return value
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]+>"), "")
        .trim()
}
