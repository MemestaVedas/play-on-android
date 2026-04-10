package eu.kanade.domain.anilist.interactor

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import eu.kanade.domain.anilist.base.PagedResult
import eu.kanade.domain.anilist.model.AnilistSimpleMedia
import eu.kanade.tachiyomi.data.track.anilist.apollo.SearchMediaSimpleQuery
import eu.kanade.tachiyomi.data.track.anilist.apollo.type.MediaType
import uy.kohesive.injekt.injectLazy

class SearchAnilistMediaSimple {

    private val apolloClient: ApolloClient by injectLazy()

    suspend operator fun invoke(
        query: String,
        page: Int = 1,
        perPage: Int = 20,
        mediaType: MediaType = MediaType.ANIME,
    ): PagedResult<AnilistSimpleMedia> {
        return try {
            val response = apolloClient.query(
                SearchMediaSimpleQuery(
                    search = Optional.present(query),
                    page = Optional.present(page),
                    perPage = Optional.present(perPage),
                    type = Optional.present(mediaType),
                ),
            ).execute()

            val pageData = response.data?.Page
                ?: return PagedResult.Error(response.errors?.firstOrNull()?.message ?: "No data")

            val list = pageData.media.orEmpty().filterNotNull().map { media ->
                AnilistSimpleMedia(
                    id = media.id,
                    title = media.title?.userPreferred ?: "Untitled",
                    coverImageUrl = media.coverImage?.large,
                    mediaType = media.type?.rawValue,
                    format = media.format?.rawValue,
                    status = media.status?.rawValue,
                    episodes = media.episodes,
                    chapters = media.chapters,
                    meanScore = media.meanScore,
                    siteUrl = media.siteUrl,
                )
            }

            PagedResult.Success(
                list = list,
                currentPage = pageData.pageInfo?.currentPage,
                hasNextPage = pageData.pageInfo?.hasNextPage ?: false,
            )
        } catch (e: Exception) {
            PagedResult.Error(e.message ?: "Unexpected error", e)
        }
    }
}
