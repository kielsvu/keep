package com.keepr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keepr.data.model.VaultEntry
import com.keepr.ui.theme.*

@Composable
fun ServiceIcon(name: String, modifier: Modifier = Modifier) {
    val letters = remember(name) { name.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifBlank { "?" } }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceRaised),
        contentAlignment = Alignment.Center
    ) {
        Text(letters, style = KeeprTypography.titleMedium, color = TextPrimary)
    }
}

@Composable
fun VaultEntryItem(
    entry: VaultEntry,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val starColor by animateColorAsState(
        if (entry.isFavorite) FavoriteActive else TextTertiary,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "star_color"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(0.982f, interactionSource)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceMid,
        border = BorderStroke(1.dp, if (entry.isFavorite) BorderDefault else BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServiceIcon(entry.serviceName)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.serviceName,
                    style = KeeprTypography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val secondary = remember(entry.accountLabel, entry.email, entry.username) {
                    entry.accountLabel.ifBlank { entry.email.ifBlank { entry.username } }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    secondary.ifBlank { entry.category },
                    style = KeeprTypography.bodySmall,
                    color = if (secondary.isBlank()) TextTertiary else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    if (entry.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (entry.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = starColor,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}
