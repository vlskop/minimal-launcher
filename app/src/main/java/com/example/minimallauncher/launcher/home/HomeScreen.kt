package com.example.minimallauncher.launcher.home

import android.widget.Space
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.minimallauncher.launcher.FavoriteAppsViewModel
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer


@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
    favoriteViewModel: FavoriteAppsViewModel = viewModel()
) {
    val uiState = homeViewModel.uiState.collectAsStateWithLifecycle()
    val apps = appViewModel.apps.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val favorites = favoriteViewModel.favorites.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .statusBarsPadding()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier.padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.value.time,
                style = MaterialTheme.typography.displayLarge
            )

            Text(
                text = uiState.value.date,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        val favoriteApps = favorites.value
            .mapNotNull { packageName ->
                apps.value.find { app ->
                    app.packageName == packageName
                }
            }
            .take(5)

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(
                items = favoriteApps,
                key = { app -> app.packageName }
            ) { app ->

                val currentIndex = remember(app.packageName) {
                    mutableIntStateOf(
                        favorites.value.indexOf(app.packageName)
                    )
                }

                var isDragging by remember {
                    mutableStateOf(false)
                }

                val scale by animateFloatAsState(
                    targetValue = if (isDragging) 1.2f else 1f,
                    label = "drag_scale"
                )

                val itemWidth = remember {
                    mutableIntStateOf(0)
                }

                val dragDistance = remember {
                    mutableFloatStateOf(0f)
                }

                Column(
                    modifier = Modifier
                        .width(64.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .animateItem()
                        .onSizeChanged {
                            itemWidth.intValue = it.width
                        }
                        .pointerInput(app.packageName) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    isDragging = true

                                    currentIndex.intValue =
                                        favorites.value.indexOf(app.packageName)

                                    dragDistance.floatValue = 0f
                                },
                                onDragEnd = {
                                    isDragging = false
                                    dragDistance.floatValue = 0f
                                },
                                onDragCancel = {
                                    isDragging = false
                                    dragDistance.floatValue = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()

                                    dragDistance.floatValue += dragAmount.x

                                    val threshold = itemWidth.intValue / 2f

                                    if (threshold <= 0f) {
                                        return@detectDragGesturesAfterLongPress
                                    }

                                    if (dragDistance.floatValue > threshold) {
                                        val nextIndex = currentIndex.intValue + 1

                                        if (nextIndex < favorites.value.size) {
                                            favoriteViewModel.moveFavorite(
                                                currentIndex.intValue,
                                                nextIndex
                                            )

                                            currentIndex.intValue = nextIndex
                                            dragDistance.floatValue -= threshold
                                        }
                                    } else if (dragDistance.floatValue < -threshold) {
                                        val previousIndex = currentIndex.intValue - 1

                                        if (previousIndex >= 0) {
                                            favoriteViewModel.moveFavorite(
                                                currentIndex.intValue,
                                                previousIndex
                                            )

                                            currentIndex.intValue = previousIndex
                                            dragDistance.floatValue += threshold
                                        }
                                    }
                                }
                            )
                        }
                        .clickable {
                            val launchIntent = context.packageManager
                                .getLaunchIntentForPackage(app.packageName)

                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = app.icon.toBitmap().asImageBitmap(),
                        contentDescription = app.name,
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = if (isDragging) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                } else {
                                    Color.Transparent
                                },
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    )

                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

}