package com.keepr.ui.screens.vault

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keepr.data.model.VaultCategory
import com.keepr.ui.components.KeeprButton
import com.keepr.ui.components.pressScale
import com.keepr.ui.components.ServiceIcon
import com.keepr.ui.components.VaultEntryItem
import com.keepr.ui.theme.*

@Composable
fun VaultScreen(
    viewModel: VaultViewModel,
    onEntryClick: (String) -> Unit,
    onAddEntry: () -> Unit,
    onSettings: () -> Unit,
    onLock: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val showCompactFab by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 || listState.firstVisibleItemScrollOffset > 160 } }
    val (favorites, others) = remember(state.entries) { state.entries.partition { it.isFavorite } }
    val fabWidth by animateDpAsState(if (showCompactFab) 52.dp else 136.dp, animationSpec = tween(180), label = "fab_width")
    val fabHeight by animateDpAsState(if (showCompactFab) 52.dp else 58.dp, animationSpec = tween(180), label = "fab_height")
    val fabCorner by animateDpAsState(if (showCompactFab) 17.dp else 19.dp, animationSpec = tween(180), label = "fab_corner")

    Box(
        Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            item(key = "hero") {
                VaultHeader(onLock = onLock, onSettings = onSettings, count = state.entries.size)
            }
            item(key = "search") { SearchBar(state.searchQuery, viewModel::onSearchQuery) }
            item(key = "categories") { CategoryFilter(state.selectedCategory, viewModel::onCategorySelected) }

            if (state.entries.isEmpty()) {
                item(key = "empty") {
                    EmptyVaultState(
                        hasSearch = state.searchQuery.isNotBlank() || state.selectedCategory != null,
                        onAdd = onAddEntry
                    )
                }
            } else {
                if (favorites.isNotEmpty()) {
                    item(key = "favorites-title") { SectionHeader("Favorites", favorites.size) }
                    itemsIndexed(favorites, key = { _, it -> "favorite-${it.id}" }) { index, entry ->
                        VaultEntryItem(entry, { onEntryClick(entry.id) }, { viewModel.toggleFavorite(entry) })
                    }
                }
                if (others.isNotEmpty()) {
                    item(key = "others-title") { SectionHeader(if (favorites.isEmpty()) "All items" else "Everything else", others.size) }
                    itemsIndexed(others, key = { _, it -> "entry-${it.id}" }) { index, entry ->
                        VaultEntryItem(entry, { onEntryClick(entry.id) }, { viewModel.toggleFavorite(entry) })
                    }
                }
            }
        }

        Box(Modifier.align(Alignment.BottomEnd)) {
            val interactionSource = remember { MutableInteractionSource() }
            FloatingActionButton(
                onClick = onAddEntry,
                interactionSource = interactionSource,
                modifier = Modifier
                    .padding(22.dp)
                    .pressScale(0.97f, interactionSource)
                    .height(fabHeight)
                    .width(fabWidth)
                    .clip(RoundedCornerShape(fabCorner)),
                shape = RoundedCornerShape(fabCorner),
                containerColor = TextPrimary,
                contentColor = Background
            ) {
                AnimatedContent(
                    targetState = showCompactFab,
                    transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(100)) },
                    label = "fab_content"
                ) { compact ->
                    if (compact) {
                        Icon(Icons.Outlined.Add, "Add account")
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Add, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add account", style = KeeprTypography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultHeader(onLock: () -> Unit, onSettings: () -> Unit, count: Int) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDeep)
            .padding(21.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("KEEPJ", style = KeeprTypography.labelSmall.copy(letterSpacing = 2.1.sp), color = TextTertiary)
                Spacer(Modifier.height(9.dp))
                Text("Your vault", style = KeeprTypography.displaySmall)
                Spacer(Modifier.height(5.dp))
                Text(
                    if (count == 0) "Private by design." else "$count ${if (count == 1) "account" else "accounts"} protected here.",
                    style = KeeprTypography.bodySmall,
                    color = TextSecondary
                )
            }
            Row {
                HeaderIconButton(Icons.Outlined.Lock, "Lock vault", onLock)
                Spacer(Modifier.width(2.dp))
                HeaderIconButton(Icons.Outlined.Settings, "Settings", onSettings)
            }
        }
    }
}

@Composable
private fun HeaderIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.size(44.dp).pressScale(0.94f, interactionSource)
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(SurfaceRaised),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, description, tint = TextSecondary, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceMid)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, null, tint = TextTertiary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(11.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
            textStyle = KeeprTypography.bodyLarge.copy(color = TextPrimary),
            cursorBrush = SolidColor(TextPrimary),
            decorationBox = { inner ->
                if (query.isEmpty()) Text("Search accounts", style = KeeprTypography.bodyMedium, color = TextTertiary)
                inner()
            }
        )
        if (query.isNotBlank()) {
            TextButton(onClick = { onQueryChange("") }) {
                Text("Clear", style = KeeprTypography.labelMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun CategoryFilter(selected: String?, onSelect: (String?) -> Unit) {
    val categories = remember { listOf(null) + VaultCategory.entries.map { it.displayName } }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 4.dp)) {
        items(categories, key = { it ?: "all" }) { category ->
            val active = selected == category
            val interactionSource = remember { MutableInteractionSource() }
            Surface(
                modifier = Modifier
                    .pressScale(0.965f, interactionSource)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onSelect(category) },
                shape = RoundedCornerShape(13.dp),
                color = if (active) TextPrimary else SurfaceMid,
                border = if (active) null else BorderStroke(1.dp, BorderSubtle)
            ) {
                Text(
                    category ?: "All",
                    style = KeeprTypography.labelMedium.copy(color = if (active) Background else TextSecondary),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp, start = 2.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = KeeprTypography.labelMedium.copy(color = TextTertiary), modifier = Modifier.weight(1f))
        Text(count.toString(), style = KeeprTypography.labelSmall.copy(color = TextTertiary))
    }
}

@Composable
private fun EmptyVaultState(hasSearch: Boolean, onAdd: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 72.dp, horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(82.dp).clip(RoundedCornerShape(26.dp)).background(SurfaceMid),
            contentAlignment = Alignment.Center
        ) { ServiceIcon("K", Modifier.size(54.dp)) }
        Spacer(Modifier.height(24.dp))
        Text(if (hasSearch) "Nothing found" else "Start your vault", style = KeeprTypography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            if (hasSearch) "Try another search or category." else "Save a login once and keep it close, private, and easy to find.",
            style = KeeprTypography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        if (!hasSearch) {
            Spacer(Modifier.height(26.dp))
            KeeprButton("Add your first account", onAdd, modifier = Modifier.fillMaxWidth())
        }
    }
}
