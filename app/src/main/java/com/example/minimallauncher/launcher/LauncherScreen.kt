package com.example.minimallauncher.launcher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.runtime.*
import com.example.minimallauncher.launcher.drawer.AppDrawerScreen
import com.example.minimallauncher.launcher.home.HomeScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.DisposableEffect
import com.example.minimallauncher.launcher.home.AppViewModel
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@Composable
fun LauncherScreen() {
    var isDrawerOpen by remember { mutableStateOf(false) }
    val appViewModel: AppViewModel = viewModel()
    val favoriteViewModel: FavoriteAppsViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    DisposableEffect(context, appViewModel) {
        val packageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

                val receivedIntent = intent ?: return
                val action = receivedIntent.action ?: return

                appViewModel.refreshApps()

                if (
                    action == Intent.ACTION_PACKAGE_REMOVED &&
                    !receivedIntent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                    ) {
                        val packageName = receivedIntent.data?.schemeSpecificPart
                            ?: return
                        favoriteViewModel.removeFavorite(packageName)
                    }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }

        ContextCompat.registerReceiver(
            context,
            packageReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(packageReceiver)
        }

    }

    DisposableEffect(lifecycleOwner, appViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                appViewModel.refreshApps()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    BackHandler(enabled = isDrawerOpen) {
        isDrawerOpen = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isDrawerOpen) {
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial
                    )

                    var totalDrag = 0f

                    do {
                        val event = awaitPointerEvent(
                            PointerEventPass.Initial
                        )

                        val change = event.changes
                            .firstOrNull { it.id == down.id }
                            ?: break

                        totalDrag += change.positionChange().y

                    } while (change.pressed)

                    if (totalDrag < -80f) {
                        isDrawerOpen = true
                    } else if (totalDrag > 80f) {
                        isDrawerOpen = false
                    }
                }
            }
    ) {
        if (isDrawerOpen) {
            AppDrawerScreen(viewModel = appViewModel)
        } else {
            HomeScreen(appViewModel = appViewModel)
        }
    }

}
