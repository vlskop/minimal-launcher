package com.example.minimallauncher.launcher.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.Collator
import java.util.Locale
import com.example.minimallauncher.core.model.AppInfo
import com.example.minimallauncher.launcher.FavoriteAppsViewModel
import com.example.minimallauncher.launcher.home.AppViewModel

private data class DrawerPalette(
    val background: Color,
    val text: Color,
    val secondaryText: Color,
    val searchSurface: Color,
    val divider: Color,
    val iconTint: Color,
    val favorite: Color
)

private val LightDrawerPalette = DrawerPalette(
    background = Color(0xFFF8F9F7),
    text = Color(0xFF202420),
    secondaryText = Color(0xFF838981),
    searchSurface = Color(0xFFECEEEB),
    divider = Color(0xFFE8EBE5),
    iconTint = Color(0xFF626B62),
    favorite = Color(0xFFFFC857)
)

private val DarkDrawerPalette = DrawerPalette(
    background = Color(0xFF121512),
    text = Color(0xFFE1E4DF),
    secondaryText = Color(0xFF9BA397),
    searchSurface = Color(0xFF292E29),
    divider = Color(0xFF303630),
    iconTint = Color(0xFFB0B7AF),
    favorite = Color(0xFFFFD166)
)

@Composable
fun AppDrawerScreen(
    viewModel: AppViewModel = viewModel(),
    onClose: () -> Unit = {}
) {
    val apps by viewModel.apps.collectAsState()
    val favoriteViewModel: FavoriteAppsViewModel = viewModel()
    val favorites by favoriteViewModel.favorites.collectAsState()
    val context = LocalContext.current
    val palette = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        DarkDrawerPalette
    } else {
        LightDrawerPalette
    }

    var searchQuery by remember { mutableStateOf("") }
    var sortByCount by remember { mutableStateOf(false) }
    var selectedCategoryKey by remember { mutableStateOf<String?>(null) }

    val filteredApps = apps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val locale = Locale.getDefault()
    val collator = remember(locale) { Collator.getInstance(locale) }
    val categorizedApps = filteredApps.map { app -> categoryFor(context, app) to app }
    val groupedApps = categorizedApps
        .groupBy { (category, _) -> category.label.normalizedCategoryKey(locale) }
        .values
        .map { grouped -> grouped.first().first to grouped.map { it.second } }
        .sortedWith { first, second -> collator.compare(first.first.label, second.first.label) }
        .let { groups ->
            if (sortByCount) groups.sortedByDescending { it.second.size } else groups
        }
    val selectedGroup = groupedApps.firstOrNull { (category, _) ->
        category.label.normalizedCategoryKey(locale) == selectedCategoryKey
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        DrawerHandle(
            palette = palette,
            onClose = onClose
        )
        AppSearchField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            onClear = { searchQuery = "" },
            palette = palette,
            modifier = Modifier
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 26.dp)
        ) {
            Text(
                text = "Приложения",
                color = palette.text,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-1.1).sp
            )
            Text(
                text = "${filteredApps.size} ${appsPlural(filteredApps.size)} · ${groupedApps.size} категорий",
                color = palette.secondaryText,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ПО КАТЕГОРИЯМ",
                color = palette.secondaryText,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            IconButton(
                onClick = { sortByCount = !sortByCount },
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 0.dp)
            ) {
                Image(
                    painter = painterResource(android.R.drawable.ic_menu_sort_by_size),
                    contentDescription = "Изменить порядок категорий",
                    colorFilter = ColorFilter.tint(palette.secondaryText),
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        if (groupedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ничего не найдено",
                    color = palette.secondaryText,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 8.dp)
            ) {
                items(groupedApps, key = { it.first.name }) { (category, categoryApps) ->
                    CategoryRow(
                        category = category,
                        apps = categoryApps,
                        favorites = favorites,
                        palette = palette,
                        onClick = { selectedCategoryKey = category.label.normalizedCategoryKey(locale) },
                        onLaunch = { app ->
                            context.packageManager.getLaunchIntentForPackage(app.packageName)
                                ?.let(context::startActivity)
                        },
                        onToggleFavorite = favoriteViewModel::toggleFavorite
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Удерживайте значок, чтобы добавить в избранное",
                color = palette.secondaryText,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (selectedGroup != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedCategoryKey = null },
            sheetState = sheetState,
            containerColor = palette.background,
            contentColor = palette.text
        ) {
            CategoryAppsSheet(
                category = selectedGroup.first,
                apps = selectedGroup.second,
                favorites = favorites,
                palette = palette,
                onLaunch = { app ->
                    selectedCategoryKey = null
                    context.packageManager.getLaunchIntentForPackage(app.packageName)
                        ?.let(context::startActivity)
                },
                onToggleFavorite = favoriteViewModel::toggleFavorite
            )
        }
    }
}

@Composable
private fun DrawerHandle(
    palette: DrawerPalette,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .semantics { contentDescription = "Закрыть список приложений" }
            .clickable(role = Role.Button, onClick = onClose)
            .pointerInput(onClose) {
                var dragDistance = 0f
                detectVerticalDragGestures(
                    onDragStart = { dragDistance = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        dragDistance += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        if (dragDistance >= 72.dp.toPx()) onClose()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(palette.secondaryText.copy(alpha = 0.55f))
        )
    }
}

@Composable
private fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    palette: DrawerPalette,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(palette.searchSurface)
            .padding(start = 18.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(android.R.drawable.ic_menu_search),
            contentDescription = null,
            colorFilter = ColorFilter.tint(palette.iconTint),
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = palette.text,
                fontSize = 15.sp,
                lineHeight = 20.sp
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Поиск приложений",
                            color = palette.secondaryText,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
            Image(
                painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                contentDescription = "Очистить поиск",
                colorFilter = ColorFilter.tint(palette.iconTint),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: AppCategory,
    apps: List<AppInfo>,
    favorites: List<String>,
    palette: DrawerPalette,
    onClick: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .clickable(role = Role.Button, onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(143.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = category.label,
                    color = palette.text,
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.4).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${apps.size} ${appsPlural(apps.size)}",
                    color = palette.secondaryText,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 1
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    apps.take(3).forEach { app ->
                        AppIcon(
                            app = app,
                            favorite = app.packageName in favorites,
                            palette = palette,
                            onClick = { onLaunch(app) },
                            onLongClick = { onToggleFavorite(app.packageName) }
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))
            Text(
                text = "›",
                color = palette.secondaryText,
                fontSize = 20.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(14.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.divider)
        )
    }
}

@Composable
private fun CategoryAppsSheet(
    category: AppCategory,
    apps: List<AppInfo>,
    favorites: List<String>,
    palette: DrawerPalette,
    onLaunch: (AppInfo) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = category.label,
            color = palette.text,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 72.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 520.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(apps) { app ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppIcon(
                        app = app,
                        favorite = app.packageName in favorites,
                        palette = palette,
                        onClick = { onLaunch(app) },
                        onLongClick = { onToggleFavorite(app.packageName) }
                    )
                    Text(
                        text = app.name,
                        color = palette.text,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppIcon(
    app: AppInfo,
    favorite: Boolean,
    palette: DrawerPalette,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember(app.packageName) { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = app.icon.toBitmap().asImageBitmap(),
            contentDescription = app.name,
            modifier = Modifier
                .fillMaxSize()
                .indication(
                    interactionSource,
                    CircularRippleIndication(MaterialTheme.colorScheme.primary)
                )
        )
        if (favorite) {
            Text(
                text = "★",
                color = palette.favorite,
                fontSize = 10.sp,
                lineHeight = 11.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(palette.background.copy(alpha = 0.9f), CircleShape)
                    .padding(horizontal = 2.dp)
            )
        }
    }
}

private fun appsPlural(count: Int): String = when {
    count % 100 in 11..14 -> "приложений"
    count % 10 == 1 -> "приложение"
    count % 10 in 2..4 -> "приложения"
    else -> "приложений"
}

private fun String.normalizedCategoryKey(locale: Locale): String =
    trim().lowercase(locale)

private fun Color.luminance(): Float =
    0.2126f * red + 0.7152f * green + 0.0722f * blue
