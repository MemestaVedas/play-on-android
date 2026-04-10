package eu.kanade.domain.anilist.model

data class AnilistSimpleMedia(
    val id: Int,
    val title: String,
    val coverImageUrl: String?,
    val mediaType: String?,
    val format: String?,
    val status: String?,
    val episodes: Int?,
    val chapters: Int?,
    val meanScore: Int?,
    val siteUrl: String?,
)
