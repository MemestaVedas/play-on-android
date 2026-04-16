package eu.kanade.tachiyomi.data.track.anilist

import android.net.Uri
import androidx.core.net.toUri
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.network.okHttpClient
import eu.kanade.tachiyomi.data.database.models.anime.AnimeTrack
import eu.kanade.tachiyomi.data.database.models.manga.MangaTrack
import eu.kanade.tachiyomi.data.track.anilist.apollo.AiringOnMyListQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.DeleteMediaListMutation
import eu.kanade.tachiyomi.data.track.anilist.apollo.UnreadNotificationCountQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.UpdateEntryMutation
import eu.kanade.tachiyomi.data.track.anilist.apollo.UserActivityQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.UserMediaListQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.UserStatsAnimeOverviewQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.UserStatsMangaOverviewQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.ViewerSettingsQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.ViewerUserInfoQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.type.ActivitySort
import eu.kanade.tachiyomi.data.track.anilist.apollo.type.FuzzyDateInput
import eu.kanade.tachiyomi.data.track.anilist.apollo.type.MediaListSort
import eu.kanade.tachiyomi.data.track.anilist.apollo.type.MediaListStatus
import eu.kanade.tachiyomi.data.track.anilist.apollo.type.MediaType
import eu.kanade.tachiyomi.data.track.anilist.dto.ALOAuth
import eu.kanade.tachiyomi.data.track.anilist.dto.ALSearchResult
import eu.kanade.tachiyomi.data.track.anilist.dto.ALUserListEntryQueryResult
import eu.kanade.tachiyomi.data.track.model.AnimeTrackSearch
import eu.kanade.tachiyomi.data.track.model.MangaTrackSearch
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.network.jsonMime
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import tachiyomi.core.common.util.lang.withIOContext
import uy.kohesive.injekt.injectLazy
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.minutes
import tachiyomi.domain.track.anime.model.AnimeTrack as DomainAnimeTrack
import tachiyomi.domain.track.manga.model.MangaTrack as DomainMangaTrack

class AnilistApi(val client: OkHttpClient, interceptor: AnilistInterceptor) {

    private val json: Json by injectLazy()

    private val authClient = client.newBuilder()
        .addInterceptor(interceptor)
        .rateLimit(permits = 85, period = 1.minutes)
        .build()

    val apolloClient = ApolloClient.Builder()
        .serverUrl(API_URL)
        .okHttpClient(authClient)
        .build()

    private fun throwIfApolloFailed(errors: List<com.apollographql.apollo.api.Error>?) {
        if (!errors.isNullOrEmpty()) {
            throw Exception(errors.first().message)
        }
    }

    suspend fun addLibManga(track: MangaTrack): MangaTrack {
        return withIOContext {
            val response = apolloClient.mutation(
                UpdateEntryMutation(
                    mediaId = Optional.present(track.remote_id.toInt()),
                    progress = Optional.present(track.last_chapter_read.toInt()),
                    status = Optional.present(MediaListStatus.safeValueOf(track.toApiStatus())),
                    `private` = Optional.present(track.private),
                ),
            ).execute()
            throwIfApolloFailed(response.errors)
            track
        }
    }

    suspend fun updateLibManga(track: MangaTrack): MangaTrack {
        return withIOContext {
            val response = apolloClient.mutation(
                UpdateEntryMutation(
                    mediaId = Optional.present(track.remote_id.toInt()),
                    progress = Optional.present(track.last_chapter_read.toInt()),
                    status = Optional.present(MediaListStatus.safeValueOf(track.toApiStatus())),
                    score = Optional.present(track.score),
                    startedAt = Optional.present(createDateInput(track.started_reading_date)),
                    completedAt = Optional.present(createDateInput(track.finished_reading_date)),
                    `private` = Optional.present(track.private),
                ),
            ).execute()
            throwIfApolloFailed(response.errors)
            track
        }
    }

    suspend fun deleteLibManga(track: DomainMangaTrack) {
        withIOContext {
            val listId = track.libraryId ?: return@withIOContext
            val response = apolloClient.mutation(
                DeleteMediaListMutation(mediaListEntryId = Optional.present(listId.toInt())),
            ).execute()
            throwIfApolloFailed(response.errors)
        }
    }

    suspend fun addLibAnime(track: AnimeTrack): AnimeTrack {
        return withIOContext {
            val response = apolloClient.mutation(
                UpdateEntryMutation(
                    mediaId = Optional.present(track.remote_id.toInt()),
                    progress = Optional.present(track.last_episode_seen.toInt()),
                    status = Optional.present(MediaListStatus.safeValueOf(track.toApiStatus())),
                    `private` = Optional.present(track.private),
                ),
            ).execute()
            throwIfApolloFailed(response.errors)
            track
        }
    }

    suspend fun updateLibAnime(track: AnimeTrack): AnimeTrack {
        return withIOContext {
            val response = apolloClient.mutation(
                UpdateEntryMutation(
                    mediaId = Optional.present(track.remote_id.toInt()),
                    progress = Optional.present(track.last_episode_seen.toInt()),
                    status = Optional.present(MediaListStatus.safeValueOf(track.toApiStatus())),
                    score = Optional.present(track.score),
                    startedAt = Optional.present(createDateInput(track.started_watching_date)),
                    completedAt = Optional.present(createDateInput(track.finished_watching_date)),
                    `private` = Optional.present(track.private),
                ),
            ).execute()
            throwIfApolloFailed(response.errors)
            track
        }
    }

    suspend fun deleteLibAnime(track: DomainAnimeTrack) {
        return withIOContext {
            val listId = track.libraryId ?: return@withIOContext
            val response = apolloClient.mutation(
                DeleteMediaListMutation(mediaListEntryId = Optional.present(listId.toInt())),
            ).execute()
            throwIfApolloFailed(response.errors)
        }
    }

    suspend fun search(search: String): List<MangaTrackSearch> {
        return withIOContext {
            val query = """
            |query Search(${'$'}query: String) {
                |Page (perPage: 50) {
                    |media(search: ${'$'}query, type: MANGA, format_not_in: [NOVEL]) {
                        |id
                        |staff {
                            |edges {
                                |role
                                |id
                                |node {
                                    |name {
                                        |full
                                        |userPreferred
                                        |native
                                    |}
                                |}
                            |}
                        |}
                        |title {
                            |userPreferred
                        |}
                        |coverImage {
                            |large
                        |}
                        |format
                        |status
                        |chapters
                        |description
                        |startDate {
                            |year
                            |month
                            |day
                        |}
                        |averageScore
                    |}
                |}
            |}
            |
            """.trimMargin()
            val payload = buildJsonObject {
                put("query", query)
                putJsonObject("variables") {
                    put("query", search)
                }
            }
            with(json) {
                authClient.newCall(
                    POST(
                        API_URL,
                        body = payload.toString().toRequestBody(jsonMime),
                    ),
                )
                    .awaitSuccess()
                    .parseAs<ALSearchResult>()
                    .data.page.media
                    .map { it.toALManga().toTrack() }
            }
        }
    }

    suspend fun searchAnime(search: String): List<AnimeTrackSearch> {
        return withIOContext {
            val query = """
            |query Search(${'$'}query: String) {
                |Page (perPage: 50) {
                    |media(search: ${'$'}query, type: ANIME) {
                        |id
                        |studios {
                            |edges {
                                |isMain
                                |node {
                                    |name
                                |}
                            |}
                        |}
                        |title {
                            |userPreferred
                        |}
                        |coverImage {
                            |large
                        |}
                        |format
                        |status
                        |episodes
                        |description
                        |startDate {
                            |year
                            |month
                            |day
                        |}
                        |averageScore
                    |}
                |}
            |}
            |
            """.trimMargin()
            val payload = buildJsonObject {
                put("query", query)
                putJsonObject("variables") {
                    put("query", search)
                }
            }
            with(json) {
                authClient.newCall(
                    POST(
                        API_URL,
                        body = payload.toString().toRequestBody(jsonMime),
                    ),
                )
                    .awaitSuccess()
                    .parseAs<ALSearchResult>()
                    .data.page.media
                    .map { it.toALAnime().toTrack() }
            }
        }
    }

    suspend fun findLibManga(track: MangaTrack, userid: Int): MangaTrack? {
        return withIOContext {
            val query = """
            |query (${'$'}id: Int!, ${'$'}manga_id: Int!) {
                |Page {
                    |mediaList(userId: ${'$'}id, type: MANGA, mediaId: ${'$'}manga_id) {
                        |id
                        |status
                        |scoreRaw: score(format: POINT_100)
                        |progress
                        |private
                        |startedAt {
                            |year
                            |month
                            |day
                        |}
                        |completedAt {
                            |year
                            |month
                            |day
                        |}
                        |media {
                            |id
                            |title {
                                |userPreferred
                            |}
                            |coverImage {
                                |large
                            |}
                            |format
                            |status
                            |chapters
                            |description
                            |startDate {
                                |year
                                |month
                                |day
                            |}
                            |staff {
                                |edges {
                                    |role
                                    |id
                                    |node {
                                        |name {
                                            |full
                                            |userPreferred
                                            |native
                                        |}
                                    |}
                                |}
                            |}
                        |}
                    |}
                |}
            |}
            |
            """.trimMargin()
            val payload = buildJsonObject {
                put("query", query)
                putJsonObject("variables") {
                    put("id", userid)
                    put("manga_id", track.remote_id)
                }
            }
            with(json) {
                authClient.newCall(
                    POST(
                        API_URL,
                        body = payload.toString().toRequestBody(jsonMime),
                    ),
                )
                    .awaitSuccess()
                    .parseAs<ALUserListEntryQueryResult>()
                    .data.page.mediaList
                    .map { it.toALUserManga() }
                    .firstOrNull()
                    ?.toTrack()
            }
        }
    }

    suspend fun findLibAnime(track: AnimeTrack, userid: Int): AnimeTrack? {
        return withIOContext {
            val query = """
            |query (${'$'}id: Int!, ${'$'}anime_id: Int!) {
                |Page {
                    |mediaList(userId: ${'$'}id, type: ANIME, mediaId: ${'$'}anime_id) {
                        |id
                        |status
                        |scoreRaw: score(format: POINT_100)
                        |progress
                        |private
                        |startedAt {
                            |year
                            |month
                            |day
                        |}
                        |completedAt {
                            |year
                            |month
                            |day
                        |}
                        |media {
                            |id
                            |title {
                                |userPreferred
                            |}
                            |coverImage {
                                |large
                            |}
                            |format
                            |status
                            |episodes
                            |description
                            |startDate {
                                |year
                                |month
                                |day
                            |}
                            |studios {
                                |edges {
                                    |isMain
                                    |node {
                                        |name
                                    |}
                                |}
                            |}
                        |}
                    |}
                |}
            |}
            |
            """.trimMargin()
            val payload = buildJsonObject {
                put("query", query)
                putJsonObject("variables") {
                    put("id", userid)
                    put("anime_id", track.remote_id)
                }
            }
            with(json) {
                authClient.newCall(
                    POST(
                        API_URL,
                        body = payload.toString().toRequestBody(jsonMime),
                    ),
                )
                    .awaitSuccess()
                    .parseAs<ALUserListEntryQueryResult>()
                    .data.page.mediaList
                    .map { it.toALUserAnime() }
                    .firstOrNull()
                    ?.toTrack()
            }
        }
    }

    suspend fun getLibManga(track: MangaTrack, userId: Int): MangaTrack {
        return findLibManga(track, userId) ?: throw Exception("Could not find manga")
    }

    suspend fun getLibAnime(track: AnimeTrack, userId: Int): AnimeTrack {
        return findLibAnime(track, userId) ?: throw Exception("Could not find anime")
    }

    fun createOAuth(token: String): ALOAuth {
        return ALOAuth(token, "Bearer", System.currentTimeMillis() + 31536000000, 31536000000)
    }

    suspend fun accessToken(code: String): ALOAuth {
        return withIOContext {
            with(json) {
                val response = client.newCall(accessTokenRequest(code))
                    .awaitSuccess()
                    .parseAs<JsonObject>()

                val accessToken = response["access_token"]?.jsonPrimitive?.content
                    ?: throw Exception("AniList token response missing access_token")
                val tokenType = response["token_type"]?.jsonPrimitive?.content ?: "Bearer"
                val expiresIn = response["expires_in"]?.jsonPrimitive?.longOrNull ?: 31536000L
                val expires = (System.currentTimeMillis() / 1000) + expiresIn

                ALOAuth(
                    accessToken = accessToken,
                    tokenType = tokenType,
                    expires = expires,
                    expiresIn = expiresIn,
                )
            }
        }
    }

    private fun accessTokenRequest(code: String) = POST(
        OAUTH_URL,
        body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", CLIENT_ID)
            .add("client_secret", CLIENT_SECRET)
            .add("redirect_uri", REDIRECT_URL)
            .add("code", code)
            .build(),
    )

    suspend fun getCurrentUser(): Pair<Int, String> {
        return withIOContext {
            val response = apolloClient.query(ViewerSettingsQuery()).execute()
            throwIfApolloFailed(response.errors)
            val viewer = response.data?.Viewer ?: throw Exception("Unable to fetch AniList viewer")
            val scoreFormat = viewer.userSettings.mediaListOptions?.scoreFormat?.rawValue ?: Anilist.POINT_10
            Pair(viewer.id, scoreFormat)
        }
    }

    suspend fun getHomeDashboard(limit: Int = 6): HomeDashboard {
        return withIOContext {
            coroutineScope {
                val viewerDeferred = async {
                    val response = apolloClient.query(ViewerUserInfoQuery()).execute()
                    throwIfApolloFailed(response.errors)
                    val viewer = response.data?.Viewer ?: throw Exception("Unable to fetch AniList viewer")
                    val userInfo = viewer.userInfo
                    HomeViewer(
                        id = viewer.id,
                        name = userInfo.name,
                        avatarUrl = userInfo.avatar?.large,
                        bannerImageUrl = userInfo.bannerImage,
                        aboutHtml = userInfo.about,
                        profileColor = userInfo.options?.profileColor,
                        titleLanguage = userInfo.options?.titleLanguage?.rawValue,
                        scoreFormat = userInfo.mediaListOptions?.commonMediaListOptions?.scoreFormat?.rawValue,
                        siteUrl = userInfo.siteUrl,
                    )
                }

                val unreadNotificationsDeferred = async {
                    val response = apolloClient.query(UnreadNotificationCountQuery()).execute()
                    throwIfApolloFailed(response.errors)
                    response.data?.Viewer?.unreadNotificationCount ?: 0
                }

                val airingDeferred = async {
                    val response = apolloClient.query(
                        AiringOnMyListQuery(
                            page = Optional.present(1),
                            perPage = Optional.present(limit),
                        ),
                    ).execute()
                    throwIfApolloFailed(response.errors)
                    response.data?.Page?.media.orEmpty()
                        .filterNotNull()
                        .map { media ->
                            val details = media.basicMediaDetails
                            val entry = media.mediaListEntry?.basicMediaListEntry
                            HomeAiringMedia(
                                id = media.id,
                                title = details.title?.userPreferred ?: "Untitled",
                                coverImageUrl = media.coverImage?.large,
                                coverColor = null,
                                meanScore = media.meanScore,
                                nextEpisode = entry?.progress?.plus(1),
                                timeUntilAiringSeconds = media.nextAiringEpisode?.timeUntilAiring,
                                listStatus = entry?.status?.rawValue,
                                progress = entry?.progress,
                                totalEpisodes = details.episodes,
                                mediaType = details.type?.rawValue,
                            )
                        }
                }

                val readingDeferred = async {
                    val response = apolloClient.query(
                        UserMediaListQuery(
                            page = Optional.present(1),
                            perPage = Optional.present(limit),
                            userId = Optional.present(viewerDeferred.await().id),
                            type = Optional.present(MediaType.MANGA),
                            statusIn = Optional.present(listOf(MediaListStatus.CURRENT)),
                            sort = Optional.present(listOf(MediaListSort.UPDATED_TIME_DESC)),
                        ),
                    ).execute()
                    throwIfApolloFailed(response.errors)

                    response.data?.Page?.mediaList.orEmpty()
                        .filterNotNull()
                        .map { entry ->
                            val media = entry.commonMediaListEntry.media
                            HomeReadingMedia(
                                id = media?.id ?: entry.id,
                                title = media?.basicMediaDetails?.title?.userPreferred ?: "Untitled",
                                coverImageUrl = media?.coverImage?.large,
                                progress = entry.commonMediaListEntry.basicMediaListEntry.progress,
                                totalChapters = media?.basicMediaDetails?.chapters,
                                mediaType = media?.basicMediaDetails?.type?.rawValue,
                            )
                        }
                }

                val animeOverviewDeferred = async {
                    val response = apolloClient.query(
                        UserStatsAnimeOverviewQuery(
                            userId = Optional.present(viewerDeferred.await().id),
                        ),
                    ).execute()
                    throwIfApolloFailed(response.errors)

                    val animeStats = response.data?.User?.statistics?.anime
                    HomeAnimeStats(
                        totalAnime = animeStats?.count,
                        episodesWatched = animeStats?.episodesWatched,
                        minutesWatched = animeStats?.minutesWatched,
                        formatBreakdown = animeStats?.formats.orEmpty()
                            .filterNotNull()
                            .mapNotNull { format ->
                                val label = format.format?.rawValue ?: return@mapNotNull null
                                val count = format.count ?: 0
                                HomeBreakdownItem(label = label, value = count)
                            }
                            .sortedByDescending { it.value },
                    )
                }

                val mangaOverviewDeferred = async {
                    val response = apolloClient.query(
                        UserStatsMangaOverviewQuery(
                            userId = Optional.present(viewerDeferred.await().id),
                        ),
                    ).execute()
                    throwIfApolloFailed(response.errors)

                    val mangaStats = response.data?.User?.statistics?.manga
                    HomeMangaStats(
                        totalManga = mangaStats?.count,
                        chaptersRead = mangaStats?.chaptersRead,
                        volumesRead = mangaStats?.volumesRead,
                    )
                }

                val activityDeferred = async {
                    val response = apolloClient.query(
                        UserActivityQuery(
                            page = Optional.present(1),
                            perPage = Optional.present(4),
                            userId = Optional.present(viewerDeferred.await().id),
                            sort = Optional.present(listOf(ActivitySort.ID_DESC)),
                        ),
                    ).execute()
                    throwIfApolloFailed(response.errors)

                    response.data?.Page?.activities.orEmpty()
                        .filterNotNull()
                        .mapNotNull { activity ->
                            when {
                                activity.onListActivity != null -> {
                                    val list = activity.onListActivity.listActivityFragment
                                    HomeActivity(
                                        id = list.id,
                                        userName = list.user?.activityUser?.name ?: "Unknown",
                                        userAvatarUrl = list.user?.activityUser?.avatar?.medium,
                                        action = list.status ?: "updated",
                                        mediaId = list.media?.id,
                                        mediaType = "ANIME",
                                        mediaTitle = list.media?.title?.userPreferred,
                                        mediaCoverUrl = list.media?.coverImage?.medium,
                                        text = null,
                                        createdAt = list.createdAt,
                                        likes = list.likeCount ?: 0,
                                        replies = list.replyCount ?: 0,
                                    )
                                }
                                activity.onTextActivity != null -> {
                                    val text = activity.onTextActivity.textActivityFragment
                                    HomeActivity(
                                        id = text.id,
                                        userName = text.user?.activityUser?.name ?: "Unknown",
                                        userAvatarUrl = text.user?.activityUser?.avatar?.medium,
                                        action = "posted",
                                        mediaId = null,
                                        mediaType = null,
                                        mediaTitle = null,
                                        mediaCoverUrl = null,
                                        text = text.text,
                                        createdAt = text.createdAt,
                                        likes = text.likeCount ?: 0,
                                        replies = text.replyCount ?: 0,
                                    )
                                }
                                activity.onMessageActivity != null -> {
                                    val message = activity.onMessageActivity.messageActivityFragment
                                    HomeActivity(
                                        id = message.id,
                                        userName = message.messenger?.activityUser?.name ?: "Unknown",
                                        userAvatarUrl = message.messenger?.activityUser?.avatar?.medium,
                                        action = "messaged",
                                        mediaId = null,
                                        mediaType = null,
                                        mediaTitle = null,
                                        mediaCoverUrl = null,
                                        text = message.message,
                                        createdAt = message.createdAt,
                                        likes = message.likeCount ?: 0,
                                        replies = message.replyCount ?: 0,
                                    )
                                }
                                else -> null
                            }
                        }
                }

                HomeDashboard(
                    viewer = viewerDeferred.await(),
                    unreadNotifications = unreadNotificationsDeferred.await(),
                    airingMedia = airingDeferred.await(),
                    readingMedia = readingDeferred.await(),
                    animeStats = animeOverviewDeferred.await(),
                    mangaStats = mangaOverviewDeferred.await(),
                    activityFeed = activityDeferred.await(),
                )
            }
        }
    }

    private fun createDateInput(dateValue: Long): FuzzyDateInput? {
        if (dateValue == 0L) return null
        val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateValue), ZoneId.systemDefault())
        return FuzzyDateInput(
            year = Optional.present(dateTime.year),
            month = Optional.present(dateTime.monthValue),
            day = Optional.present(dateTime.dayOfMonth),
        )
    }

    private fun createDate(dateValue: Long): JsonObject {
        if (dateValue == 0L) {
            return buildJsonObject {
                put("year", JsonNull)
                put("month", JsonNull)
                put("day", JsonNull)
            }
        }

        val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateValue), ZoneId.systemDefault())
        return buildJsonObject {
            put("year", dateTime.year)
            put("month", dateTime.monthValue)
            put("day", dateTime.dayOfMonth)
        }
    }

    companion object {
        private const val CLIENT_ID = "33523"
        private const val CLIENT_SECRET = "spCWPTMapryGIQwRZ3djJGSKtzCMXB8udNRyDwxX"
        private const val API_URL = "https://graphql.anilist.co/"
        private const val BASE_URL = "https://anilist.co/api/v2/"
        private const val OAUTH_URL = "${BASE_URL}oauth/token"
        private const val BASE_MANGA_URL = "https://anilist.co/manga/"
        private const val BASE_ANIME_URL = "https://anilist.co/anime/"
        private const val REDIRECT_URL = "playon://auth"

        fun mangaUrl(mediaId: Long): String {
            return BASE_MANGA_URL + mediaId
        }

        fun animeUrl(mediaId: Long): String {
            return BASE_ANIME_URL + mediaId
        }

        fun authUrl(): Uri = "${BASE_URL}oauth/authorize".toUri().buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URL)
            .build()
    }

    data class HomeDashboard(
        val viewer: HomeViewer,
        val unreadNotifications: Int,
        val airingMedia: List<HomeAiringMedia>,
        val readingMedia: List<HomeReadingMedia>,
        val animeStats: HomeAnimeStats,
        val mangaStats: HomeMangaStats,
        val activityFeed: List<HomeActivity>,
    )

    data class HomeViewer(
        val id: Int,
        val name: String,
        val avatarUrl: String?,
        val bannerImageUrl: String?,
        val aboutHtml: String?,
        val profileColor: String?,
        val titleLanguage: String?,
        val scoreFormat: String?,
        val siteUrl: String?,
    )

    data class HomeAiringMedia(
        val id: Int,
        val title: String,
        val coverImageUrl: String?,
        val coverColor: String?,
        val meanScore: Int?,
        val nextEpisode: Int?,
        val timeUntilAiringSeconds: Int?,
        val listStatus: String?,
        val progress: Int?,
        val totalEpisodes: Int?,
        val mediaType: String?,
    )

    data class HomeReadingMedia(
        val id: Int,
        val title: String,
        val coverImageUrl: String?,
        val progress: Int?,
        val totalChapters: Int?,
        val mediaType: String?,
    )

    data class HomeAnimeStats(
        val totalAnime: Int?,
        val episodesWatched: Int?,
        val minutesWatched: Int?,
        val formatBreakdown: List<HomeBreakdownItem>,
    )

    data class HomeMangaStats(
        val totalManga: Int?,
        val chaptersRead: Int?,
        val volumesRead: Int?,
    )

    data class HomeBreakdownItem(
        val label: String,
        val value: Int,
    )

    data class HomeActivity(
        val id: Int,
        val userName: String,
        val userAvatarUrl: String?,
        val action: String,
        val mediaId: Int?,
        val mediaType: String?,
        val mediaTitle: String?,
        val mediaCoverUrl: String?,
        val text: String?,
        val createdAt: Int?,
        val likes: Int,
        val replies: Int,
    )
}
