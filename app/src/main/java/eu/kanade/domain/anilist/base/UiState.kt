package eu.kanade.domain.anilist.base

interface UiState {
    val isLoading: Boolean
    val errorMessage: String?
}
