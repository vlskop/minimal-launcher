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
import androidx.compose.ui.input.pointer.util.VelocityTracker
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
    val openThresholdPx = baseGestureThresholdPx / 16f
    val closeThresholdPx = minOf(drawerHeightPx * 0.16f, with(density) { 160.dp.toPx() })
    val drawerOffset = remember { mutableFloatStateOf(drawerHeightPx) }
    val drawerListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val drawerOpenState = rememberUpdatedState(isDrawerOpen)
    val settleJob = remember { mutableStateOf<Job?>(null) }

    suspend fun animateDrawerTo(target: Float, initialVelocity: Float = 0f) {
        val animatable = Animatable(drawerOffset.floatValue)
        animatable.updateBounds(lowerBound = 0f, upperBound = drawerHeightPx)
        animatable.animateTo(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = 0.86f,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialVelocity = initialVelocity.coerceIn(
                -with(density) { 5000.dp.toPx() },
                with(density) { 5000.dp.toPx() }
            )
        ) {
            drawerOffset.floatValue = value
        }
    }

    suspend fun closeDrawer() {
        settleJob.value?.cancel()
        val closeJob = coroutineScope.launch {
            animateDrawerTo(drawerHeightPx)
            isDrawerOpen = false
        }
        settleJob.value = closeJob
        closeJob.join()
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
            var wasOpenAtStart = drawerOpenState.value
            val velocityTracker = VelocityTracker().apply {
                addPosition(down.uptimeMillis, down.position)
            }
            var totalDrag = 0f
            var totalHorizontalDrag = 0f
            var dragOriginY = 0f
            var dragStartOffset = 0f
            var isDraggingDrawer = false
            var isOpeningGesture = false
            var pointerStillDown = true

            while (pointerStillDown) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                val positionChange = change.positionChangeIgnoreConsumed()
                velocityTracker.addPosition(change.uptimeMillis, change.position)
                val deltaY = positionChange.y
                totalDrag += deltaY
                totalHorizontalDrag += positionChange.x
                if (!isDraggingDrawer) wasOpenAtStart = drawerOpenState.value
                val directionMatches = abs(totalDrag) >= abs(totalHorizontalDrag) * 0.65f
                val hasVerticalIntent =
                    abs(totalDrag) > viewConfiguration.touchSlop && directionMatches

                if (
                    !isDraggingDrawer &&
                    hasVerticalIntent
                ) {
                    if (totalDrag < 0f &&
                        (!wasOpenAtStart || drawerOffset.floatValue > viewConfiguration.touchSlop)
                    ) {
                        isDraggingDrawer = true
                        settleJob.value?.cancel()
                        isOpeningGesture = true
                        if (!wasOpenAtStart) {
                            dragOriginY = totalDrag
                            dragStartOffset = drawerHeightPx * (2f / 3f)
                            drawerOffset.floatValue = dragStartOffset
                            isDrawerOpen = true
                        } else {
                            dragOriginY = totalDrag
                            dragStartOffset = drawerOffset.floatValue
                        }
                    } else if (
                        wasOpenAtStart && totalDrag > 0f &&
                        drawerListState.firstVisibleItemIndex == 0 &&
                        drawerListState.firstVisibleItemScrollOffset == 0
                    ) {
                        isDraggingDrawer = true
                        settleJob.value?.cancel()
                        isOpeningGesture = false
                        dragOriginY = totalDrag
                        dragStartOffset = drawerOffset.floatValue
                    }
                } else if (isDraggingDrawer) {
                    drawerOffset.floatValue =
                        (dragStartOffset + totalDrag - dragOriginY).coerceIn(0f, drawerHeightPx)
                }

                if (isDraggingDrawer) change.consume()
                pointerStillDown = change.pressed
            }

            if (isDraggingDrawer) {
                val releaseVelocity = velocityTracker.calculateVelocity().y
                    .coerceIn(-with(density) { 5000.dp.toPx() }, with(density) { 5000.dp.toPx() })
                val projectedOffset =
                    (drawerOffset.floatValue + releaseVelocity * 0.12f).coerceIn(0f, drawerHeightPx)
                val shouldSettleOpen = if (isOpeningGesture) {
                    projectedOffset <= dragStartOffset - openThresholdPx
                } else {
                    projectedOffset < closeThresholdPx
                }
                val target = if (shouldSettleOpen) 0f else drawerHeightPx
                val settleVelocity = if (shouldSettleOpen) {
                    minOf(releaseVelocity, 0f)
                } else {
                    maxOf(releaseVelocity, 0f)
                }
                settleJob.value = coroutineScope.launch {
                    animateDrawerTo(target, initialVelocity = settleVelocity)
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
                onClose = { closeDrawer() }
            )
        }
    }

}
