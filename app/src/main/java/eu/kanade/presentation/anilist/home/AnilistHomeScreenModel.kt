package eu.kanade.presentation.anilist.home

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.anilist.base.DataResult
import eu.kanade.domain.anilist.interactor.GetAnilistViewerProfile
import eu.kanade.domain.anilist.model.AnilistUserInfo
import eu.kanade.tachiyomi.data.track.TrackerManager
import tachiyomi.core.common.util.lang.launchIO
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AnilistHomeScreenModel(
    private val trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistHomeScreenModel.State>(State.Loading) {

    private val tracker = trackerManager.aniList
    private val getViewerProfile = GetAnilistViewerProfile()

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launchIO {
            mutableState.value = State.Loading
            if (!tracker.isLoggedIn) {
                mutableState.value = State.Guest
                return@launchIO
            }

            mutableState.value = when (val result = getViewerProfile()) {
                is DataResult.Success -> State.Ready(result.data)
                is DataResult.Error -> State.Error(result.message)
                DataResult.Loading -> State.Loading
            }
        }
    }

    sealed interface State {
        data object Loading : State
        data object Guest : State
        data class Ready(val viewer: AnilistUserInfo) : State
        data class Error(val message: String) : State
    }
}
