package eu.kanade.presentation.anilist.home

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.data.track.TrackerManager
import tachiyomi.core.common.util.lang.launchIO
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AnilistHomeScreenModel(
    private val trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistHomeScreenModel.State>(State.Loading) {

    private val tracker = trackerManager.aniList

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launchIO {
            mutableState.value = State.Loading
            mutableState.value = if (tracker.isLoggedIn) {
                State.Ready
            } else {
                State.Guest
            }
        }
    }

    sealed interface State {
        data object Loading : State
        data object Guest : State
        data object Ready : State
        data object Error : State
    }
}
