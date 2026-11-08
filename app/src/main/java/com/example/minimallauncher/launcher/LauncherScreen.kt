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

@Composable
fun LauncherScreen() {
    var isDrawerOpen by remember { mutableStateOf(false) }

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
            AppDrawerScreen()
        } else {
            HomeScreen()
        }
    }

}