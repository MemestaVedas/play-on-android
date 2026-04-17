package eu.kanade.tachiyomi.ui.anilist

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
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
            val title = stringResource(MR.strings.label_home)
            val icon = rememberAniListTabPainter()
            return TabOptions(
                index = 0u,
                title = title,
                icon = icon,
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        navigator.push(UpdatesTab)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = androidx.compose.ui.platform.LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(MR.strings.label_home),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    actions = {
                        IconButton(onClick = { navigator.push(UpdatesTab) }) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
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

@Composable
private fun rememberAniListTabPainter(): BitmapPainter {
    val context = LocalContext.current
    return remember(context) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_tracker_anilist)
            ?: throw Resources.NotFoundException()
        val sourceBitmap = drawable.toBitmap()
        val scale = 0.78f
        val targetWidth = (sourceBitmap.width / scale).toInt()
        val targetHeight = (sourceBitmap.height / scale).toInt()
        val outputBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        Canvas(outputBitmap).apply {
            val left = (targetWidth - sourceBitmap.width) / 2f
            val top = (targetHeight - sourceBitmap.height) / 2f
            drawBitmap(sourceBitmap, left, top, null)
        }
        BitmapPainter(outputBitmap.asImageBitmap())
    }
}
