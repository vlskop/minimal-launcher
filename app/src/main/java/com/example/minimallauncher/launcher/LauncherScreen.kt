package com.example.minimallauncher.launcher

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment

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

    val homeSwipeModifier = if (isDrawerOpen) {
        Modifier
    } else {
        Modifier.pointerInput(Unit) {
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
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(homeSwipeModifier)
    ) {
        HomeScreen(appViewModel = appViewModel)

        AnimatedVisibility(
            visible = isDrawerOpen,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(280, easing = LinearOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(180)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(220, easing = FastOutLinearInEasing)
            ) + fadeOut(animationSpec = tween(140))
        ) {
            AppDrawerScreen(
                viewModel = appViewModel,
                onClose = { isDrawerOpen = false }
            )
        }
    }

}
