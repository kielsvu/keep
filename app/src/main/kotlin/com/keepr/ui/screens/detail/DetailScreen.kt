package com.keepr.ui.screens.detail

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.keepr.ui.components.ConfirmDialog
import com.keepr.ui.components.KeeprButton
import com.keepr.ui.components.KeeprSecondaryButton
import com.keepr.ui.components.ServiceIcon
import com.keepr.ui.theme.*
import com.keepr.utils.ClipboardUtils

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val entry = state.entry

    LaunchedEffect(state.deleted) { if (state.deleted) onDeleted() }
    LaunchedEffect(state.copiedField) {
        state.copiedField?.let {
            snackbar.showSnackbar("$it copied")
            viewModel.clearCopiedField()
        }
    }
    if (entry == null) return

    Box(
        Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = TextSecondary) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    val color by animateColorAsState(
                        if (entry.isFavorite) FavoriteActive else TextSecondary,
                        animationSpec = spring(),
                        label = "favorite"
                    )
                    Icon(if (entry.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder, "Favorite", tint = color)
                }
                IconButton(onClick = { onEdit(entry.id) }) { Icon(Icons.Outlined.Edit, "Edit", tint = TextSecondary) }
            }

            Spacer(Modifier.height(10.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDeep)
                    .padding(21.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ServiceIcon(entry.serviceName, Modifier.size(70.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(entry.serviceName, style = KeeprTypography.displaySmall)
                        Spacer(Modifier.height(5.dp))
                        Text(entry.category, style = KeeprTypography.bodySmall, color = TextSecondary)
                        if (entry.accountLabel.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(entry.accountLabel, style = KeeprTypography.bodySmall, color = TextTertiary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))
            DetailGroup("ACCOUNT") {
                DetailField("Username", entry.username, context, viewModel)
                DetailDivider(entry.username.isNotBlank(), entry.email.isNotBlank() || entry.passwordEncrypted.isNotBlank() || entry.website.isNotBlank())
                DetailField("Email", entry.email, context, viewModel)
                DetailDivider(entry.email.isNotBlank(), entry.passwordEncrypted.isNotBlank() || entry.website.isNotBlank())
                PasswordField("Password", entry.passwordEncrypted, state.passwordVisible, viewModel::togglePasswordVisibility, context) {
                    ClipboardUtils.copyToClipboard(context, "Password", entry.passwordEncrypted)
                    viewModel.onFieldCopied("Password")
                }
                DetailDivider(entry.passwordEncrypted.isNotBlank(), entry.website.isNotBlank())
                DetailField("Website", entry.website, context, viewModel, Icons.Outlined.Language)
            }

            if (entry.notes.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                DetailGroup("NOTES") {
                    Text(entry.notes, style = KeeprTypography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(17.dp))
                }
            }

            Spacer(Modifier.height(28.dp))
            KeeprButton("Edit account", { onEdit(entry.id) }, Modifier.fillMaxWidth(), icon = { Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp)) })
            Spacer(Modifier.height(10.dp))
            KeeprSecondaryButton("Delete account", viewModel::showDeleteConfirm, Modifier.fillMaxWidth(), icon = { Icon(Icons.Outlined.DeleteOutline, null, Modifier.size(18.dp)) })
            Spacer(Modifier.height(34.dp))
        }

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp))
        if (state.showDeleteConfirm) {
            ConfirmDialog(
                title = "Delete account?",
                message = "This account will be permanently removed from your vault.",
                confirmText = "Delete",
                destructive = true,
                onConfirm = viewModel::deleteEntry,
                onDismiss = viewModel::hideDeleteConfirm
            )
        }
    }
}

@Composable
private fun DetailGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = KeeprTypography.labelSmall, color = TextTertiary, modifier = Modifier.padding(start = 3.dp, bottom = 8.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(SurfaceMid)
        ) { content() }
    }
}

@Composable
private fun DetailDivider(show: Boolean, needed: Boolean) {
    if (show && needed) HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(start = 17.dp))
}

@Composable
private fun DetailField(
    label: String,
    value: String,
    context: android.content.Context,
    viewModel: DetailViewModel,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = KeeprTypography.labelMedium, color = TextTertiary)
            Spacer(Modifier.height(5.dp))
            Text(value, style = KeeprTypography.bodyLarge)
        }
        IconButton(onClick = { ClipboardUtils.copyToClipboard(context, label, value); viewModel.onFieldCopied(label) }) {
            Icon(icon ?: Icons.Outlined.ContentCopy, "Copy $label", tint = TextSecondary, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    visible: Boolean,
    onToggle: () -> Unit,
    context: android.content.Context,
    onCopied: () -> Unit
) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = KeeprTypography.labelMedium, color = TextTertiary)
            Spacer(Modifier.height(5.dp))
            AnimatedContent(
                targetState = visible,
                transitionSpec = { fadeIn() + scaleIn(initialScale = 0.98f) togetherWith fadeOut() },
                label = "password_visibility"
            ) { isVisible ->
                Text(
                    if (isVisible) value else "•".repeat(minOf(value.length, 24)),
                    style = KeeprTypography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                )
            }
        }
        IconButton(onClick = onToggle) { Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Toggle password", tint = TextSecondary) }
        IconButton(onClick = { ClipboardUtils.copyToClipboard(context, label, value); onCopied() }) { Icon(Icons.Outlined.ContentCopy, "Copy password", tint = TextSecondary) }
    }
}
