package eu.kanade.tachiyomi.ui.anilist

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.presentation.anilist.home.AnilistHomeScreen
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.updates.UpdatesTab
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

data object AniListTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            return TabOptions(
                index = 2u,
                title = "AniList",
                icon = painterResource(R.drawable.ic_tracker_anilist),
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        navigator.push(UpdatesTab)
    }

    @Composable
    override fun Content() {
        val context = androidx.compose.ui.platform.LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("AniList") },
                    actions = {
                        IconButton(onClick = { navigator.push(UpdatesTab) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_updates_outline_24dp),
                                contentDescription = stringResource(MR.strings.label_recent_updates),
                            )
                        }
                    },
                )
            },
        ) {
            AnilistHomeScreen.Content()
        }

        LaunchedEffect(Unit) {
            (context as? MainActivity)?.ready = true
        }
    }
}
