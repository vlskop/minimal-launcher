package com.example.minimallauncher.launcher.drawer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minimallauncher.launcher.home.AppViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape


@Composable
fun AppDrawerScreen(
    viewModel: AppViewModel = viewModel()
) {
    val apps = viewModel.apps.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember {
        mutableStateOf("")
    }

    val filteredApps = apps.value.filter { app ->
        app.name.contains(
            searchQuery,
            ignoreCase = true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Поиск приложений...")
            },
            leadingIcon = {
                Text(
                    text = "⌕",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                        }
                    ) {
                        Text(
                            text = "x",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            },
            singleLine = true
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(filteredApps) { app ->
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

                    Text(text = app.name)
                }
            }
        }
    }
}