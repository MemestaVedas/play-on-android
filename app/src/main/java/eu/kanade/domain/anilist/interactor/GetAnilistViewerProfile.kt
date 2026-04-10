package eu.kanade.domain.anilist.interactor

import com.apollographql.apollo.ApolloClient
import eu.kanade.domain.anilist.base.DataResult
import eu.kanade.domain.anilist.model.AnilistUserInfo
import eu.kanade.tachiyomi.data.track.anilist.apollo.ViewerUserInfoQuery
import uy.kohesive.injekt.injectLazy

class GetAnilistViewerProfile {

    private val apolloClient: ApolloClient by injectLazy()

    suspend operator fun invoke(): DataResult<AnilistUserInfo> {
        return try {
            val response = apolloClient.query(ViewerUserInfoQuery()).execute()
            val viewer = response.data?.Viewer
                ?: return DataResult.Error(response.errors?.firstOrNull()?.message ?: "Unable to fetch viewer")

            DataResult.Success(
                AnilistUserInfo(
                    id = viewer.id,
                    name = viewer.name,
                    avatarUrl = viewer.avatar?.large,
                    bannerUrl = viewer.bannerImage,
                    aboutHtml = viewer.about,
                    profileColor = viewer.options?.profileColor,
                    titleLanguage = viewer.options?.titleLanguage?.rawValue,
                    scoreFormat = viewer.mediaListOptions?.scoreFormat?.rawValue,
                    siteUrl = viewer.siteUrl,
                ),
            )
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Unexpected error", e)
        }
    }
}
