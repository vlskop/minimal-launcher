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
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.runtime.*
import androidx.compose.runtime.MutableFloatState
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
import kotlinx.coroutines.Job
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
    val baseGestureThresholdPx = minOf(drawerHeightPx * 0.22f, with(density) { 160.dp.toPx() })
    val gestureThresholdPx = baseGestureThresholdPx / 8f
    val drawerOffset = remember { mutableFloatStateOf(drawerHeightPx) }
    val drawerListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val drawerOpenState = rememberUpdatedState(isDrawerOpen)
    val settleJob = remember { mutableStateOf<Job?>(null) }

    suspend fun animateDrawerTo(target: Float) {
        Animatable(drawerOffset.floatValue).animateTo(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = 0.86f,
                stiffness = Spring.StiffnessMediumLow
            )
        ) {
            drawerOffset.floatValue = value
        }
    }

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
            var totalHorizontalDrag = 0f
            var dragOriginY = 0f
            var isDraggingDrawer = false
            var pointerStillDown = true

            while (pointerStillDown) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                val positionChange = change.positionChangeIgnoreConsumed()
                val deltaY = positionChange.y
                totalDrag += deltaY
                totalHorizontalDrag += positionChange.x

                if (
                    !isDraggingDrawer &&
                    abs(totalDrag) > viewConfiguration.touchSlop &&
                    abs(totalDrag) > abs(totalHorizontalDrag)
                ) {
                    if (!wasOpenAtStart && totalDrag < 0f) {
                        isDraggingDrawer = true
                        settleJob.value?.cancel()
                        drawerOffset.floatValue =
                            (drawerHeightPx + totalDrag).coerceIn(0f, drawerHeightPx)
                        isDrawerOpen = true
                    } else if (
                        wasOpenAtStart && totalDrag > 0f &&
                        drawerListState.firstVisibleItemIndex == 0 &&
                        drawerListState.firstVisibleItemScrollOffset == 0
                    ) {
                        isDraggingDrawer = true
                        settleJob.value?.cancel()
                        dragOriginY = totalDrag
                    }
                } else if (isDraggingDrawer && wasOpenAtStart) {
                    drawerOffset.floatValue = (totalDrag - dragOriginY).coerceIn(0f, drawerHeightPx)
                } else if (isDraggingDrawer) {
                    drawerOffset.floatValue =
                        (drawerHeightPx + totalDrag).coerceIn(0f, drawerHeightPx)
                }

                if (isDraggingDrawer) change.consume()
                pointerStillDown = change.pressed
            }

            if (isDraggingDrawer) {
                val shouldClose = wasOpenAtStart && drawerOffset.floatValue >= gestureThresholdPx
                val shouldOpen = !wasOpenAtStart &&
                    drawerOffset.floatValue <= drawerHeightPx - gestureThresholdPx
                val shouldSettleOpen = if (wasOpenAtStart) !shouldClose else shouldOpen
                val target = if (shouldSettleOpen) 0f else drawerHeightPx
                settleJob.value = coroutineScope.launch {
                    animateDrawerTo(target)
                    if (target == drawerHeightPx) isDrawerOpen = false
                }
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
                    IntOffset(x = 0, y = drawerOffset.floatValue.roundToInt())
                },
                onClose = {
                    settleJob.value?.cancel()
                    animateDrawerTo(drawerHeightPx)
                    isDrawerOpen = false
                }
            )
        }
    }

}
