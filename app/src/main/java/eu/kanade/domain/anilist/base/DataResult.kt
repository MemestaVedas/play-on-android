package eu.kanade.domain.anilist.base

sealed class DataResult<out T> {
    data object Loading : DataResult<Nothing>()

    data class Success<T>(
        val data: T,
    ) : DataResult<T>()

    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : DataResult<Nothing>()
}
