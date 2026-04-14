package eu.kanade.presentation.anilist.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.apollographql.apollo.api.Optional
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.track.anilist.AnilistApi
import eu.kanade.tachiyomi.data.track.anilist.AnilistInterceptor
import eu.kanade.tachiyomi.data.track.anilist.apollo.CharacterDetailsQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.CharacterMediaQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.MediaCharactersAndStaffQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.MediaDetailsQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.StaffDetailsQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.StaffMediaQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.StudioDetailsQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.UserFavoritesAnimeQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.UserFavoritesCharacterQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.UserFavoritesMangaQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.UserFavoritesStaffQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.UserFavoritesStudioQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.type.MediaType
import eu.kanade.tachiyomi.data.track.TrackerManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.update
import tachiyomi.core.common.util.lang.launchIO
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AnilistMediaDetailsScreen(private val mediaId: Int) : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { AnilistMediaDetailsScreenModel(mediaId) }
        val state by screenModel.state.collectAsState()

        when (val currentState = state) {
            AnilistMediaDetailsScreenModel.State.Loading -> LoadingScreen()
            is AnilistMediaDetailsScreenModel.State.Error -> ErrorScreen(currentState.message, screenModel::refresh)
            is AnilistMediaDetailsScreenModel.State.Ready -> MediaDetailsContent(
                details = currentState.details,
                credits = currentState.credits,
                onRetry = screenModel::refresh,
            )
        }
    }
}

class AnilistCharacterDetailsScreen(private val characterId: Int) : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { AnilistCharacterDetailsScreenModel(characterId) }
        val state by screenModel.state.collectAsState()

        when (val currentState = state) {
            AnilistCharacterDetailsScreenModel.State.Loading -> LoadingScreen()
            is AnilistCharacterDetailsScreenModel.State.Error -> ErrorScreen(currentState.message, screenModel::refresh)
            is AnilistCharacterDetailsScreenModel.State.Ready -> CharacterDetailsContent(
                details = currentState.details,
                credits = currentState.credits,
                onRetry = screenModel::refresh,
            )
        }
    }
}

class AnilistStaffDetailsScreen(private val staffId: Int) : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { AnilistStaffDetailsScreenModel(staffId) }
        val state by screenModel.state.collectAsState()

        when (val currentState = state) {
            AnilistStaffDetailsScreenModel.State.Loading -> LoadingScreen()
            is AnilistStaffDetailsScreenModel.State.Error -> ErrorScreen(currentState.message, screenModel::refresh)
            is AnilistStaffDetailsScreenModel.State.Ready -> StaffDetailsContent(
                details = currentState.details,
                credits = currentState.credits,
                onRetry = screenModel::refresh,
            )
        }
    }
}

class AnilistStudioDetailsScreen(private val studioId: Int) : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { AnilistStudioDetailsScreenModel(studioId) }
        val state by screenModel.state.collectAsState()

        when (val currentState = state) {
            AnilistStudioDetailsScreenModel.State.Loading -> LoadingScreen()
            is AnilistStudioDetailsScreenModel.State.Error -> ErrorScreen(currentState.message, screenModel::refresh)
            is AnilistStudioDetailsScreenModel.State.Ready -> StudioDetailsContent(
                details = currentState.details,
                onRetry = screenModel::refresh,
            )
        }
    }
}

class AnilistFavoritesScreen(private val userId: Int) : Screen() {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { AnilistFavoritesScreenModel(userId) }
        val state by screenModel.state.collectAsState()

        when (val currentState = state) {
            AnilistFavoritesScreenModel.State.Loading -> LoadingScreen()
            is AnilistFavoritesScreenModel.State.Error -> ErrorScreen(currentState.message, screenModel::refresh)
            is AnilistFavoritesScreenModel.State.Ready -> FavoritesContent(
                details = currentState,
                onRetry = screenModel::refresh,
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            onClick = onRetry,
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(
                text = "Retry",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}

private class AnilistMediaDetailsScreenModel(
    private val mediaId: Int,
    trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistMediaDetailsScreenModel.State>(State.Loading) {

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
                coroutineScope {
                    val details = async {
                        api.apolloClient.query(MediaDetailsQuery(Optional.present(mediaId))).execute()
                    }
                    val credits = async {
                        api.apolloClient.query(MediaCharactersAndStaffQuery(Optional.present(mediaId))).execute()
                    }
                    val detailsResponse = details.await()
                    val creditsResponse = credits.await()

                    val errorMessage = detailsResponse.errors?.firstOrNull()?.message
                        ?: creditsResponse.errors?.firstOrNull()?.message
                    if (errorMessage != null) {
                        throw Exception(errorMessage)
                    }

                    State.Ready(
                        details = detailsResponse.data,
                        credits = creditsResponse.data,
                    )
                }
            }.onSuccess { newState -> mutableState.update { newState } }
                .onFailure { throwable ->
                    mutableState.update { State.Error(throwable.message ?: "Unable to load AniList") }
                }
        }
    }

    sealed interface State {
        data object Loading : State

        data class Ready(
            val details: MediaDetailsQuery.Data?,
            val credits: MediaCharactersAndStaffQuery.Data?,
        ) : State

        data class Error(val message: String) : State
    }
}

private class AnilistCharacterDetailsScreenModel(
    private val characterId: Int,
    trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistCharacterDetailsScreenModel.State>(State.Loading) {

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
                coroutineScope {
                    val details = async {
                        api.apolloClient.query(CharacterDetailsQuery(Optional.present(characterId))).execute()
                    }
                    val credits = async {
                        api.apolloClient.query(CharacterMediaQuery(Optional.present(characterId), Optional.present(1), Optional.present(24))).execute()
                    }
                    val detailsResponse = details.await()
                    val creditsResponse = credits.await()
                    val errorMessage = detailsResponse.errors?.firstOrNull()?.message
                        ?: creditsResponse.errors?.firstOrNull()?.message
                    if (errorMessage != null) {
                        throw Exception(errorMessage)
                    }

                    State.Ready(detailsResponse.data, creditsResponse.data)
                }
            }.onSuccess { newState -> mutableState.update { newState } }
                .onFailure { throwable ->
                    mutableState.update { State.Error(throwable.message ?: "Unable to load AniList") }
                }
        }
    }

    sealed interface State {
        data object Loading : State
        data class Ready(
            val details: CharacterDetailsQuery.Data?,
            val credits: CharacterMediaQuery.Data?,
        ) : State
        data class Error(val message: String) : State
    }
}

private class AnilistStaffDetailsScreenModel(
    private val staffId: Int,
    trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistStaffDetailsScreenModel.State>(State.Loading) {

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
                coroutineScope {
                    val details = async {
                        api.apolloClient.query(StaffDetailsQuery(Optional.present(staffId))).execute()
                    }
                    val credits = async {
                        api.apolloClient.query(StaffMediaQuery(Optional.present(staffId), Optional.present(false), Optional.present(1), Optional.present(24))).execute()
                    }
                    val detailsResponse = details.await()
                    val creditsResponse = credits.await()
                    val errorMessage = detailsResponse.errors?.firstOrNull()?.message
                        ?: creditsResponse.errors?.firstOrNull()?.message
                    if (errorMessage != null) {
                        throw Exception(errorMessage)
                    }

                    State.Ready(detailsResponse.data, creditsResponse.data)
                }
            }.onSuccess { newState -> mutableState.update { newState } }
                .onFailure { throwable ->
                    mutableState.update { State.Error(throwable.message ?: "Unable to load AniList") }
                }
        }
    }

    sealed interface State {
        data object Loading : State
        data class Ready(
            val details: StaffDetailsQuery.Data?,
            val credits: StaffMediaQuery.Data?,
        ) : State
        data class Error(val message: String) : State
    }
}

private class AnilistStudioDetailsScreenModel(
    private val studioId: Int,
    trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistStudioDetailsScreenModel.State>(State.Loading) {

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
                val response = api.apolloClient.query(StudioDetailsQuery(Optional.present(studioId), Optional.present(24))).execute()
                    response.errors?.firstOrNull()?.message?.let { throw Exception(it) }
                    State.Ready(response.data)
            }.onSuccess { newState -> mutableState.update { newState } }
                .onFailure { throwable ->
                    mutableState.update { State.Error(throwable.message ?: "Unable to load AniList") }
                }
        }
    }

    sealed interface State {
        data object Loading : State
        data class Ready(val details: StudioDetailsQuery.Data?) : State
        data class Error(val message: String) : State
    }
}

private class AnilistFavoritesScreenModel(
    private val userId: Int,
    trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistFavoritesScreenModel.State>(State.Loading) {

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
                coroutineScope {
                    val anime = async {
                        api.apolloClient.query(UserFavoritesAnimeQuery(Optional.present(userId), Optional.present(1), Optional.present(24))).execute()
                    }
                    val manga = async {
                        api.apolloClient.query(UserFavoritesMangaQuery(Optional.present(userId), Optional.present(1), Optional.present(24))).execute()
                    }
                    val characters = async {
                        api.apolloClient.query(UserFavoritesCharacterQuery(Optional.present(userId), Optional.present(1), Optional.present(24))).execute()
                    }
                    val staff = async {
                        api.apolloClient.query(UserFavoritesStaffQuery(Optional.present(userId), Optional.present(1), Optional.present(24))).execute()
                    }
                    val studios = async {
                        api.apolloClient.query(UserFavoritesStudioQuery(Optional.present(userId), Optional.present(1), Optional.present(24))).execute()
                    }

                    val animeResponse = anime.await()
                    val mangaResponse = manga.await()
                    val characterResponse = characters.await()
                    val staffResponse = staff.await()
                    val studiosResponse = studios.await()

                    listOf(animeResponse, mangaResponse, characterResponse, staffResponse, studiosResponse)
                        .firstNotNullOfOrNull { it.errors?.firstOrNull()?.message }
                        ?.let { throw Exception(it) }

                    State.Ready(
                        anime = animeResponse.data,
                        manga = mangaResponse.data,
                        characters = characterResponse.data,
                        staff = staffResponse.data,
                        studios = studiosResponse.data,
                    )
                }
            }.onSuccess { newState -> mutableState.update { newState } }
                .onFailure { throwable ->
                    mutableState.update { State.Error(throwable.message ?: "Unable to load AniList") }
                }
        }
    }

    sealed interface State {
        data object Loading : State
        data class Ready(
            val anime: UserFavoritesAnimeQuery.Data?,
            val manga: UserFavoritesMangaQuery.Data?,
            val characters: UserFavoritesCharacterQuery.Data?,
            val staff: UserFavoritesStaffQuery.Data?,
            val studios: UserFavoritesStudioQuery.Data?,
        ) : State
        data class Error(val message: String) : State
    }
}

@Composable
private fun MediaDetailsContent(
    details: MediaDetailsQuery.Data?,
    credits: MediaCharactersAndStaffQuery.Data?,
    onRetry: () -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    val media = details?.Media

    if (media == null) {
        ErrorScreen("Media details were unavailable", onRetry)
        return
    }

    val cast = credits?.Media?.characters?.edges.orEmpty().filterNotNull()
    val staff = credits?.Media?.staff?.edges.orEmpty().filterNotNull()
    val studios = media.studios?.nodes.orEmpty().filterNotNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DetailHero(
                title = media.titleLabel(),
                subtitle = media.mediaMeta(),
                imageUrl = media.bannerImage ?: media.coverImage?.extraLarge ?: media.coverImage?.large,
                coverUrl = media.coverImage?.extraLarge ?: media.coverImage?.large,
                chips = listOfNotNull(
                    media.format?.rawValue,
                    media.status?.rawValue,
                    media.source?.rawValue,
                    media.meanScore?.let { "$it score" },
                ),
                onRetry = onRetry,
            )
        }

        item {
            DetailSection(title = "About") {
                Text(
                    text = media.descriptionText(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            DetailSection(title = "Details") {
                FlowChips(
                    items = listOfNotNull(
                        media.basicMediaDetails.episodes?.let { "$it episodes" },
                        media.basicMediaDetails.chapters?.let { "$it chapters" },
                        media.basicMediaDetails.volumes?.let { "$it volumes" },
                        media.duration?.let { "$it min" },
                        media.favourites?.let { "$it favorites" },
                        media.popularity?.let { "$it popular" },
                    ),
                )
            }
        }

        if (studios.isNotEmpty()) {
            item {
                DetailSection(title = "Studios") {
                    FlowChips(items = studios.map { it.name }) { studioName ->
                        val studio = studios.firstOrNull { it.name == studioName } ?: return@FlowChips
                        navigator.push(AnilistStudioDetailsScreen(studio.id))
                    }
                }
            }
        }

        if (cast.isNotEmpty()) {
            item {
                DetailSection(title = "Characters") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
                        items(cast, key = { it.mediaCharacter.node?.id ?: it.hashCode() }) { edge ->
                            val character = edge.mediaCharacter.node ?: return@items
                            PersonCard(
                                title = character.name?.userPreferred ?: "Untitled",
                                subtitle = edge.mediaCharacter.role?.rawValue ?: "Character",
                                imageUrl = character.image?.medium,
                                onClick = { navigator.push(AnilistCharacterDetailsScreen(character.id)) },
                            )
                        }
                    }
                }
            }
        }

        if (staff.isNotEmpty()) {
            item {
                DetailSection(title = "Staff") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
                        items(staff, key = { it.mediaStaff.node?.id ?: it.hashCode() }) { edge ->
                            val member = edge.mediaStaff.node ?: return@items
                            PersonCard(
                                title = member.name?.userPreferred ?: "Untitled",
                                subtitle = edge.mediaStaff.role ?: "Staff",
                                imageUrl = member.image?.medium,
                                onClick = { navigator.push(AnilistStaffDetailsScreen(member.id)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterDetailsContent(
    details: CharacterDetailsQuery.Data?,
    credits: CharacterMediaQuery.Data?,
    onRetry: () -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    val character = details?.Character

    if (character == null) {
        ErrorScreen("Character details were unavailable", onRetry)
        return
    }

    val relatedMedia = credits?.Character?.media?.edges.orEmpty().filterNotNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DetailHero(
                title = character.nameLabel(),
                subtitle = listOfNotNull(character.gender, character.age, character.bloodType).joinToString(" · "),
                imageUrl = character.image?.large,
                coverUrl = character.image?.large,
                chips = listOfNotNull(
                    character.favourites?.let { "$it favorites" },
                    if (character.isFavourite) "Saved" else null,
                ),
                onRetry = onRetry,
            )
        }

        item {
            DetailSection(title = "About") {
                Text(
                    text = character.descriptionText(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            DetailSection(title = "Media") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
                        items(relatedMedia, key = { it.id ?: it.node?.id ?: it.hashCode() }) { edge ->
                        val media = edge.node ?: return@items
                        MediaCard(
                            title = media.basicMediaDetails.title?.userPreferred ?: "Untitled",
                            subtitle = edge.characterRole?.rawValue ?: "Media",
                            imageUrl = media.coverImage?.large,
                            onClick = { navigator.push(AnilistMediaDetailsScreen(media.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffDetailsContent(
    details: StaffDetailsQuery.Data?,
    credits: StaffMediaQuery.Data?,
    onRetry: () -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    val staff = details?.Staff

    if (staff == null) {
        ErrorScreen("Staff details were unavailable", onRetry)
        return
    }

    val relatedMedia = credits?.Staff?.staffMedia?.edges.orEmpty().filterNotNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DetailHero(
                title = staff.nameLabel(),
                subtitle = listOfNotNull(staff.gender, staff.homeTown, staff.age?.let { "$it years" }).joinToString(" · "),
                imageUrl = staff.image?.large,
                coverUrl = staff.image?.large,
                chips = listOfNotNull(
                    staff.favourites?.let { "$it favorites" },
                    if (staff.isFavourite) "Saved" else null,
                ),
                onRetry = onRetry,
            )
        }

        item {
            DetailSection(title = "About") {
                Text(
                    text = staff.descriptionText(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (staff.primaryOccupations.orEmpty().isNotEmpty()) {
            item {
                DetailSection(title = "Primary Occupations") {
                    FlowChips(items = staff.primaryOccupations.orEmpty().filterNotNull())
                }
            }
        }

        item {
            DetailSection(title = "Media") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
                        items(relatedMedia, key = { it.id ?: it.node?.id ?: it.hashCode() }) { edge ->
                        val media = edge.node ?: return@items
                        MediaCard(
                            title = media.basicMediaDetails.title?.userPreferred ?: "Untitled",
                            subtitle = edge.staffRole ?: "Production credit",
                            imageUrl = media.coverImage?.large,
                            onClick = { navigator.push(AnilistMediaDetailsScreen(media.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioDetailsContent(
    details: StudioDetailsQuery.Data?,
    onRetry: () -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    val studio = details?.Studio

    if (studio == null) {
        ErrorScreen("Studio details were unavailable", onRetry)
        return
    }

    val media = studio.media?.commonStudioMedia?.nodes.orEmpty().filterNotNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DetailHero(
                title = studio.name,
                subtitle = "Studio profile",
                imageUrl = null,
                coverUrl = null,
                chips = listOfNotNull(
                    studio.favourites?.let { "$it favorites" },
                    if (studio.isFavourite) "Saved" else null,
                ),
                onRetry = onRetry,
            )
        }

        item {
            DetailSection(title = "Produced Media") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
                    items(media, key = { it.id }) { node ->
                        MediaCard(
                            title = node.title?.userPreferred ?: "Untitled",
                            subtitle = node.startDate?.year?.toString() ?: "",
                            imageUrl = node.coverImage?.large,
                            onClick = { navigator.push(AnilistMediaDetailsScreen(node.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesContent(
    details: AnilistFavoritesScreenModel.State.Ready?,
    onRetry: () -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    val anime = details?.anime?.User?.favourites?.anime?.nodes.orEmpty().filterNotNull()
    val manga = details?.manga?.User?.favourites?.manga?.nodes.orEmpty().filterNotNull()
    val characters = details?.characters?.User?.favourites?.characters?.nodes.orEmpty().filterNotNull()
    val staff = details?.staff?.User?.favourites?.staff?.nodes.orEmpty().filterNotNull()
    val studios = details?.studios?.User?.favourites?.studios?.nodes.orEmpty().filterNotNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        item { FavoritesSection(title = "Anime", count = anime.size) { FavoriteMediaRow(anime) { navigator.push(AnilistMediaDetailsScreen(it.id)) } } }
        item { FavoritesSection(title = "Manga", count = manga.size) { FavoriteMediaRow(manga) { navigator.push(AnilistMediaDetailsScreen(it.id)) } } }
        item { FavoritesSection(title = "Characters", count = characters.size) { FavoritePersonRow(characters) { navigator.push(AnilistCharacterDetailsScreen(it.id)) } } }
        item { FavoritesSection(title = "Staff", count = staff.size) { FavoritePersonRow(staff) { navigator.push(AnilistStaffDetailsScreen(it.id)) } } }
        item { FavoritesSection(title = "Studios", count = studios.size) { FavoriteStudioRow(studios) { navigator.push(AnilistStudioDetailsScreen(it.id)) } } }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = onRetry,
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = "Refresh favorites",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun FavoritesSection(
    title: String,
    count: Int,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("$count", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        content()
    }
}

@Composable
private fun FavoriteMediaRow(
    entries: List<Any>,
    onClick: (FavoriteMediaEntry) -> Unit,
) {
    val mediaItems = entries.mapNotNull { item ->
        when (item) {
            is UserFavoritesAnimeQuery.Node -> FavoriteMediaEntry(item.id, item.title?.userPreferred ?: "Untitled", item.coverImage?.large)
            is UserFavoritesMangaQuery.Node -> FavoriteMediaEntry(item.id, item.title?.userPreferred ?: "Untitled", item.coverImage?.large)
            else -> null
        }
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
                        items(mediaItems, key = { it.id }) { entry ->
            MediaCard(title = entry.title, subtitle = null, imageUrl = entry.imageUrl, onClick = { onClick(entry) })
        }
    }
}

@Composable
private fun FavoritePersonRow(
    entries: List<Any>,
    onClick: (FavoritePersonEntry) -> Unit,
) {
    val people = entries.mapNotNull { item ->
        when (item) {
            is UserFavoritesCharacterQuery.Node -> FavoritePersonEntry(item.id, item.name?.userPreferred ?: "Untitled", item.image?.large)
            is UserFavoritesStaffQuery.Node -> FavoritePersonEntry(item.id, item.name?.userPreferred ?: "Untitled", item.image?.large)
            else -> null
        }
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
                        items(people, key = { it.id }) { entry ->
            PersonCard(title = entry.title, subtitle = null, imageUrl = entry.imageUrl, onClick = { onClick(entry) })
        }
    }
}

@Composable
private fun FavoriteStudioRow(
    entries: List<UserFavoritesStudioQuery.Node>,
    onClick: (UserFavoritesStudioQuery.Node) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
        items(entries, key = { it.id }) { studio ->
            StudioChip(studio.name, onClick = { onClick(studio) })
        }
    }
}

@Composable
private fun DetailHero(
    title: String,
    subtitle: String,
    imageUrl: String?,
    coverUrl: String?,
    chips: List<String>,
    onRetry: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.96f)),
                        ),
                    ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = title,
                        modifier = Modifier
                            .width(118.dp)
                            .aspectRatio(2f / 3f),
                        contentScale = ContentScale.Crop,
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, maxLines = 2)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                    FlowChips(items = chips)
                    Surface(
                        onClick = onRetry,
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.width(120.dp),
                    ) {
                        Text(
                            text = "Refresh",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun FlowChips(
    items: List<String>,
    onClick: ((String) -> Unit)? = null,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 4.dp)) {
        items(items, key = { it }) { item ->
            Surface(
                onClick = { onClick?.invoke(item) },
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun MediaCard(
    title: String,
    subtitle: String?,
    imageUrl: String?,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
                contentScale = ContentScale.Crop,
            )
        }
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        subtitle?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PersonCard(
    title: String,
    subtitle: String?,
    imageUrl: String?,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(124.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )
        }
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        subtitle?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun StudioChip(
    title: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

private data class FavoriteMediaEntry(
    val id: Int,
    val title: String,
    val imageUrl: String?,
)

private data class FavoritePersonEntry(
    val id: Int,
    val title: String,
    val imageUrl: String?,
)

private fun MediaDetailsQuery.Media.titleLabel(): String {
    return title?.userPreferred ?: basicMediaDetails.title?.userPreferred ?: "Untitled"
}

private fun MediaDetailsQuery.Media.mediaMeta(): String = buildMediaMeta(this)

private fun buildMediaMeta(media: MediaDetailsQuery.Media): String {
    val parts = buildList {
        media.format?.rawValue?.let { add(it) }
        media.status?.rawValue?.let { add(it) }
        media.season?.rawValue?.let { add(it) }
        media.seasonYear?.let { add(it.toString()) }
    }
    return parts.joinToString(" · ")
}

private fun MediaDetailsQuery.Media.descriptionText(): String = trimHtml(description ?: "No description available.")

private fun CharacterDetailsQuery.Character.nameLabel(): String {
    return name?.userPreferred ?: name?.native ?: "Untitled"
}

private fun CharacterDetailsQuery.Character.descriptionText(): String = trimHtml(description ?: "No description available.")

private fun StaffDetailsQuery.Staff.nameLabel(): String {
    return name?.userPreferred ?: name?.native ?: "Untitled"
}

private fun StaffDetailsQuery.Staff.descriptionText(): String = trimHtml(description ?: "No description available.")

private fun trimHtml(value: String): String {
    return value
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .trim()
}
