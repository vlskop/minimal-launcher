package com.example.minimallauncher.launcher.drawer

import androidx.compose.foundation.Image
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minimallauncher.core.model.AppInfo
import com.example.minimallauncher.launcher.FavoriteAppsViewModel
import com.example.minimallauncher.launcher.home.AppViewModel
import java.text.Collator
import kotlinx.coroutines.launch

private data class DrawerPalette(
    val background: Color,
    val text: Color,
    val secondaryText: Color,
    val searchSurface: Color,
    val iconTint: Color,
    val favorite: Color
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppDrawerScreen(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = viewModel(),
    listState: LazyListState,
    drawerOffset: State<Float>,
    drawerHeightPx: Float,
    onClose: suspend () -> Unit
) {
    val apps by viewModel.apps.collectAsState()
    val favoriteViewModel: FavoriteAppsViewModel = viewModel()
    val favorites by favoriteViewModel.favorites.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val locale = LocalLocale.current.platformLocale
    val colorScheme = MaterialTheme.colorScheme
    val palette = DrawerPalette(
        background = colorScheme.background,
        text = colorScheme.onBackground,
        secondaryText = colorScheme.onSurfaceVariant,
        searchSurface = colorScheme.surfaceVariant,
        iconTint = colorScheme.onSurfaceVariant,
        favorite = colorScheme.tertiary
    )

    var searchQuery by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }
    var searchImeWasVisible by remember { mutableStateOf(false) }
    var selectedCategoryKey by remember { mutableStateOf<String?>(null) }
    val imeVisible = WindowInsets.isImeVisible

    LaunchedEffect(searchFocused, imeVisible) {
        when {
            !searchFocused -> searchImeWasVisible = false
            imeVisible -> searchImeWasVisible = true
            searchImeWasVisible -> focusManager.clearFocus()
        }
    }

    val categorizedApps = remember(apps, context, locale) {
        apps.map { app -> categoryFor(context, app) to app }
    }
    val filteredApps = categorizedApps.filter { (_, app) ->
        app.name.contains(searchQuery, ignoreCase = true)
    }
    val collator = remember(locale) { Collator.getInstance(locale) }
    val groupedApps = filteredApps
        .groupBy { (category, _) -> category.label.normalizedCategoryKey(locale) }
        .values
        .map { group -> group.first().first to group.map { it.second } }
        .sortedWith { left, right -> collator.compare(left.first.label, right.first.label) }
    val selectedGroup = groupedApps.firstOrNull { (category, _) ->
        category.label.normalizedCategoryKey(locale) == selectedCategoryKey
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer {
                    val opaqueStartOffset = drawerHeightPx * (2f / 3f)
                    alpha = ((opaqueStartOffset - drawerOffset.value) / opaqueStartOffset)
                        .coerceIn(0f, 1f)
                }
                .background(palette.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.height(24.dp))
            DrawerCategoriesContent(
                modifier = Modifier.weight(1f),
                groups = groupedApps,
                favorites = favorites,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onClearSearch = {
                    searchQuery = ""
                    focusManager.clearFocus()
                },
                onSearchFocusChange = { searchFocused = it },
                listState = listState,
                palette = palette,
                onOpenCategory = { category ->
                    focusManager.clearFocus()
                    selectedCategoryKey = category.label.normalizedCategoryKey(locale)
                },
                onLaunch = { app ->
                    coroutineScope.launch {
                        onClose()
                        context.packageManager.getLaunchIntentForPackage(app.packageName)
                            ?.let(context::startActivity)
                    }
                },
                onToggleFavorite = favoriteViewModel::toggleFavorite
            )
        }
    }

    BackHandler(enabled = selectedGroup == null) {
        if (searchFocused) {
            focusManager.clearFocus()
        } else {
            coroutineScope.launch { onClose() }
        }
    }

    if (selectedGroup != null) {
        val categorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedCategoryKey = null },
            sheetState = categorySheetState,
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
private fun DrawerCategoriesContent(
    modifier: Modifier = Modifier,
    groups: List<Pair<AppCategory, List<AppInfo>>>,
    favorites: List<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    listState: LazyListState,
    palette: DrawerPalette,
    onOpenCategory: (AppCategory) -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        AppSearchField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            onClear = onClearSearch,
            onFocusChange = onSearchFocusChange,
            palette = palette,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp)
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
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp)
                    .heightIn(max = 658.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 8.dp)
            ) {
                items(groups, key = { it.first.label }) { (category, categoryApps) ->
                    CategoryRow(
                        category = category,
                        apps = categoryApps,
                        favorites = favorites,
                        palette = palette,
                        onClick = { onOpenCategory(category) },
                        onLaunch = onLaunch,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
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
}

@Composable
private fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    palette: DrawerPalette,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
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
            textStyle = TextStyle(color = palette.text, fontSize = 15.sp, lineHeight = 20.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth()) {
                    if (value.isEmpty() && !isFocused) {
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
            modifier = Modifier
                .weight(1f)
                .onFocusChanged {
                    isFocused = it.isFocused
                    onFocusChange(it.isFocused)
                }
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
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        val previewApps = apps.take(4)
        val previewWidth = if (previewApps.isEmpty()) {
            0.dp
        } else {
            44.dp * previewApps.size + 8.dp * (previewApps.size - 1)
        }
        val previewStart = (maxWidth - 36.dp - previewWidth).coerceAtLeast(0.dp)
        Text(
            text = category.label,
            color = palette.text,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.4).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.TopStart)
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = previewStart, top = 42.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            previewApps.forEach { app ->
                AppIcon(
                    app = app,
                    favorite = app.packageName in favorites,
                    palette = palette,
                    onClick = { onLaunch(app) },
                    onLongClick = { onToggleFavorite(app.packageName) }
                )
            }
        }

        Text(
            text = apps.size.toString(),
            color = palette.secondaryText,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.End,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 43.dp)
                .width(28.dp)
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
                        maxLines = 1,
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

private fun String.normalizedCategoryKey(locale: java.util.Locale): String =
    trim().lowercase(locale)

private fun Color.luminance(): Float =
    0.2126f * red + 0.7152f * green + 0.0722f * blue
