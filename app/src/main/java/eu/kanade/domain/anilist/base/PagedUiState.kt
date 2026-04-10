package eu.kanade.domain.anilist.base

interface PagedUiState : UiState {
    val currentPage: Int
    val hasNextPage: Boolean
}
