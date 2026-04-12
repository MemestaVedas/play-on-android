package eu.kanade.presentation.anilist.home

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.data.track.TrackerManager
import eu.kanade.tachiyomi.data.track.anilist.AnilistApi
import eu.kanade.tachiyomi.data.track.anilist.AnilistInterceptor
import kotlinx.coroutines.flow.update
import tachiyomi.core.common.util.lang.launchIO
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AnilistHomeScreenModel(
    private val trackerManager: TrackerManager = Injekt.get(),
) : StateScreenModel<AnilistHomeScreenModel.State>(State.Loading) {

    private val tracker = trackerManager.aniList
    private val api by lazy {
        AnilistApi(
            tracker.client,
            AnilistInterceptor(tracker, tracker.getPassword()),
        )
    }

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launchIO {
            if (!tracker.isLoggedIn) {
                mutableState.update { State.Guest }
                return@launchIO
            }

            mutableState.update { State.Loading }

            runCatching {
                api.getHomeDashboard()
            }.onSuccess { dashboard ->
                mutableState.update { State.Ready(dashboard) }
            }.onFailure { throwable ->
                mutableState.update { State.Error(throwable.message ?: "Unable to load AniList") }
            }
        }
    }

    sealed interface State {
        data object Loading : State
        data object Guest : State

        @Immutable
        data class Ready(
            val dashboard: AnilistApi.HomeDashboard,
        ) : State

        data class Error(
            val message: String,
        ) : State
    }
}
