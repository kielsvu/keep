package com.keepr.ui.screens.settings

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.keepr.BuildConfig
import com.keepr.data.repository.AutoLockTimeout
import com.keepr.ui.components.ConfirmDialog
import com.keepr.ui.components.KeeprToggle
import com.keepr.ui.components.pressScale
import com.keepr.ui.theme.*

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit, onChangePin: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var autoLockExpanded by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri -> uri?.let(viewModel::exportBackup) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(viewModel::importBackup) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(Modifier.fillMaxSize().background(Background).statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = TextSecondary) }
                Column(Modifier.weight(1f)) {
                    Text("Settings", style = KeeprTypography.headlineLarge)
                    Text("Security, privacy, and vault preferences", style = KeeprTypography.bodySmall, color = TextSecondary)
                }
            }

            Column(
                Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState()).padding(top = 12.dp, bottom = 32.dp)
            ) {
                SettingSection("SECURITY") {
                    SettingRow(Icons.Outlined.Lock, "Change PIN", "Update the vault passcode", onClick = onChangePin)
                    DividerLine()
                    SettingToggle(Icons.Outlined.Fingerprint, "Biometric unlock", "Use your fingerprint or face when available.", state.biometricEnabled) { viewModel.toggleBiometric(context, it) }
                    DividerLine()
                    Box {
                        SettingRow(Icons.Outlined.Timer, "Auto-lock", "Lock after ${state.autoLockTimeout.displayName}") { autoLockExpanded = true }
                        DropdownMenu(expanded = autoLockExpanded, onDismissRequest = { autoLockExpanded = false }) {
                            AutoLockTimeout.entries.forEach { timeout ->
                                DropdownMenuItem(
                                    text = { Text(timeout.displayName) },
                                    onClick = { viewModel.setAutoLock(timeout); autoLockExpanded = false }
                                )
                            }
                        }
                    }
                    DividerLine()
                    SettingToggle(Icons.Outlined.ScreenshotMonitor, "Block screenshots", "Keep sensitive screens out of screenshots.", state.screenshotProtection, viewModel::setScreenshotProtection)
                }

                Spacer(Modifier.height(20.dp))
                SettingSection("VAULT") {
                    SettingRow(Icons.Outlined.FileUpload, "Export vault", "Create an encrypted backup") { exportLauncher.launch("keepj_backup_${System.currentTimeMillis()}.kbk") }
                    DividerLine()
                    SettingRow(Icons.Outlined.FileDownload, "Import vault", "Restore from a backup") { importLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                    DividerLine()
                    SettingRow(Icons.Outlined.DeleteOutline, "Clear vault", "Permanently remove every account", destructive = true) { viewModel.showClearConfirm() }
                }

                Spacer(Modifier.height(20.dp))
                SettingSection("ABOUT") {
                    SettingRow(Icons.Outlined.Info, "Version", BuildConfig.VERSION_NAME, showChevron = false) {}
                    DividerLine()
                    SettingRow(Icons.Outlined.PrivacyTip, "Privacy policy", "Read how your data is handled") {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://keepr.app/privacy")))
                    }
                }
            }
        }

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp))
        if (state.showClearConfirm) {
            ConfirmDialog(
                title = "Clear vault?",
                message = "All saved accounts will be permanently deleted.",
                confirmText = "Clear vault",
                destructive = true,
                onConfirm = viewModel::clearVault,
                onDismiss = viewModel::hideClearConfirm
            )
        }
    }
}

@Composable
private fun SettingSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(label, style = KeeprTypography.labelSmall, color = TextTertiary, modifier = Modifier.padding(start = 3.dp, bottom = 8.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(SurfaceMid).then(Modifier),
            content = content
        )
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(start = 64.dp))
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String?,
    showChevron: Boolean = true,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .pressScale(0.992f, interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(if (destructive) ErrorContainer else SurfaceRaised),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (destructive) ErrorColor else TextSecondary, modifier = Modifier.size(19.dp))
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, style = KeeprTypography.bodyMedium, color = if (destructive) ErrorColor else TextPrimary)
            if (value != null) Text(value, style = KeeprTypography.bodySmall, color = TextSecondary)
        }
        if (showChevron) Icon(Icons.Outlined.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun SettingToggle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(SurfaceRaised), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(19.dp))
        }
        Column(Modifier.weight(1f).padding(start = 12.dp, end = 10.dp)) {
            Text(title, style = KeeprTypography.bodyMedium)
            Text(subtitle, style = KeeprTypography.bodySmall, color = TextSecondary)
        }
        KeeprToggle(checked = checked, onCheckedChange = onChange)
    }
}
