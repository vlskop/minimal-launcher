package com.example.minimallauncher.launcher.home

import android.widget.Space
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            favorites.value
                .mapNotNull { packageName ->
                    apps.value.find { app ->
                        app.packageName == packageName
                    }
                }
                .take(5)
                .forEach { app ->
                Column(
                    modifier = Modifier.clickable {
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
                        modifier = Modifier.size(48.dp)
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