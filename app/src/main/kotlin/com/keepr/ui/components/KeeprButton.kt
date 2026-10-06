package com.keepr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.keepr.ui.theme.*

@Composable
fun KeeprButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .height(52.dp)
            .pressScale(0.985f, interactionSource),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TextPrimary,
            contentColor = Background,
            disabledContainerColor = SurfaceRaised,
            disabledContentColor = TextTertiary
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(9.dp))
        }
        Text(text, style = KeeprTypography.labelLarge)
    }
}

@Composable
fun KeeprSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(50.dp)
            .pressScale(0.985f, interactionSource),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderDefault),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(9.dp))
        }
        Text(text, style = KeeprTypography.labelLarge)
    }
}

@Composable
fun KeeprSurface(
    modifier: Modifier = Modifier,
    color: Color = SurfaceMid,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = color,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(content = content)
    }
}
