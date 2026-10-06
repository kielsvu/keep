package com.keepr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.dp
import com.keepr.ui.theme.Background
import com.keepr.ui.theme.SurfaceRaised
import com.keepr.ui.theme.TextPrimary

@Composable
fun KeeprToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val trackColor by animateColorAsState(
        targetValue = if (checked) TextPrimary else SurfaceRaised,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMedium),
        label = "toggle_track"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) Background else TextPrimary,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMedium),
        label = "toggle_thumb"
    )
    val thumbX by animateDpAsState(
        targetValue = if (checked) 25.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
        label = "toggle_position"
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (checked) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMedium),
        label = "toggle_scale"
    )
    Box(
        modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(CircleShape)
            .background(trackColor)
            .toggleable(
                value = checked,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,
                onValueChange = onCheckedChange
            )
    ) {
        Box(
            Modifier
                .size(24.dp)
                .offset(x = thumbX, y = 4.dp)
                .scale(thumbScale)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}
