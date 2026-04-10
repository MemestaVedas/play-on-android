package eu.kanade.presentation.anilist.explore

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.anilist.base.PagedResult
import eu.kanade.domain.anilist.interactor.SearchAnilistMediaSimple
import eu.kanade.domain.anilist.model.AnilistSimpleMedia
import eu.kanade.tachiyomi.data.track.anilist.apollo.type.MediaType
import tachiyomi.core.common.util.lang.launchIO

class AnilistExploreScreenModel : StateScreenModel<AnilistExploreScreenModel.State>(State()) {

    private val searchMedia = SearchAnilistMediaSimple()

    fun updateQuery(query: String) {
        mutableState.value = mutableState.value.copy(query = query)
    }

    fun search() {
        val query = state.value.query.trim()
        if (query.isBlank()) {
            mutableState.value = state.value.copy(error = "Enter a search query")
            return
        }

        screenModelScope.launchIO {
            mutableState.value = state.value.copy(isLoading = true, error = null)

            when (
                val result = searchMedia(
                    query = query,
                    mediaType = MediaType.ANIME,
                )
            ) {
                is PagedResult.Success -> {
                    mutableState.value = state.value.copy(
                        isLoading = false,
                        items = result.list,
                        error = null,
                    )
                }
                is PagedResult.Error -> {
                    mutableState.value = state.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
                PagedResult.Loading -> {
                    mutableState.value = state.value.copy(isLoading = true)
                }
            }
        }
    }

    data class State(
        val query: String = "",
        val isLoading: Boolean = false,
        val items: List<AnilistSimpleMedia> = emptyList(),
        val error: String? = null,
    )
}
