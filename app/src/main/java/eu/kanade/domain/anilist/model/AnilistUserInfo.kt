package eu.kanade.domain.anilist.model

data class AnilistUserInfo(
    val id: Int,
    val name: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val aboutHtml: String?,
    val profileColor: String?,
    val titleLanguage: String?,
    val scoreFormat: String?,
    val siteUrl: String?,
)
