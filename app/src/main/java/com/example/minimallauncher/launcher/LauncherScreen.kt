package com.example.minimallauncher.launcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.consume
import androidx.compose.runtime.*
import com.example.minimallauncher.launcher.drawer.AppDrawerScreen
import com.example.minimallauncher.launcher.home.HomeScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.minimallauncher.launcher.home.AppViewModel
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun LauncherScreen() {
    var isDrawerOpen by remember { mutableStateOf(false) }
    val appViewModel: AppViewModel = viewModel()
    val favoriteViewModel: FavoriteAppsViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val drawerHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val dismissThresholdPx = minOf(drawerHeightPx * 0.22f, with(density) { 160.dp.toPx() })
    val drawerOffset = remember { Animatable(drawerHeightPx) }
    val drawerListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val drawerOpenState = rememberUpdatedState(isDrawerOpen)

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
    val drawerGestureModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Initial
            )
            val wasOpenAtStart = drawerOpenState.value
            var totalDrag = 0f
            var dragOriginY = 0f
            var isDraggingDrawer = false
            var pointerStillDown = true

            while (pointerStillDown) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                val deltaY = change.positionChange().y
                totalDrag += deltaY

                if (!isDraggingDrawer && abs(totalDrag) > viewConfiguration.touchSlop) {
                    if (!wasOpenAtStart && totalDrag < 0f) {
                        isDraggingDrawer = true
                        drawerOffset.snapTo((drawerHeightPx + totalDrag).coerceIn(0f, drawerHeightPx))
                        isDrawerOpen = true
                    } else if (
                        wasOpenAtStart && totalDrag > 0f &&
                        drawerListState.firstVisibleItemIndex == 0 &&
                        drawerListState.firstVisibleItemScrollOffset == 0
                    ) {
                        isDraggingDrawer = true
                        dragOriginY = totalDrag
                    }
                } else if (isDraggingDrawer && wasOpenAtStart) {
                    drawerOffset.snapTo((totalDrag - dragOriginY).coerceIn(0f, drawerHeightPx))
                } else if (isDraggingDrawer) {
                    drawerOffset.snapTo((drawerHeightPx + totalDrag).coerceIn(0f, drawerHeightPx))
                }

                if (isDraggingDrawer) change.consume()
                pointerStillDown = change.pressed
            }

            if (isDraggingDrawer) {
                val shouldClose = wasOpenAtStart && drawerOffset.value >= dismissThresholdPx
                val shouldOpen = !wasOpenAtStart &&
                    drawerOffset.value <= drawerHeightPx - dismissThresholdPx
                val shouldSettleOpen = if (wasOpenAtStart) !shouldClose else shouldOpen
                val target = if (shouldSettleOpen) 0f else drawerHeightPx
                drawerOffset.animateTo(
                    targetValue = target,
                    animationSpec = spring(
                        dampingRatio = 0.86f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                if (target == drawerHeightPx) isDrawerOpen = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(drawerGestureModifier)
    ) {
        HomeScreen(appViewModel = appViewModel)
        if (isDrawerOpen) {
            AppDrawerScreen(
                viewModel = appViewModel,
                listState = drawerListState,
                drawerOffset = drawerOffset,
                drawerHeightPx = drawerHeightPx,
                modifier = Modifier.offset {
                    IntOffset(x = 0, y = drawerOffset.value.roundToInt())
                },
                onClose = {
                    drawerOffset.animateTo(
                        drawerHeightPx,
                        spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow)
                    )
                    isDrawerOpen = false
                }
            )
        }
    }

}
