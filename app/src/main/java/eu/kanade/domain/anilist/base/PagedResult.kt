package eu.kanade.domain.anilist.base

sealed class PagedResult<out T> {
    data object Loading : PagedResult<Nothing>()

    data class Success<T>(
        val list: List<T>,
        val currentPage: Int?,
        val hasNextPage: Boolean,
    ) : PagedResult<T>()

    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : PagedResult<Nothing>()
}
