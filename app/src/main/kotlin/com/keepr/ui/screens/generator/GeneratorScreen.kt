package com.keepr.ui.screens.generator

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.keepr.ui.components.KeeprToggle
import com.keepr.ui.components.KeeprButton
import com.keepr.ui.components.KeeprSecondaryButton
import com.keepr.ui.theme.*
import com.keepr.utils.PasswordStrength

@Composable
fun GeneratorScreen(viewModel: GeneratorViewModel, onBack: () -> Unit, onUsePassword: ((String) -> Unit)? = null) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    Column(Modifier.fillMaxSize().background(Background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = TextSecondary) }
            Column(Modifier.weight(1f)) {
                Text("Password generator", style = KeeprTypography.headlineLarge)
                Text("Make something difficult to guess.", style = KeeprTypography.bodySmall, color = TextSecondary)
            }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(14.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(SurfaceMid).padding(20.dp)
            ) {
                Text("GENERATED PASSWORD", style = KeeprTypography.labelSmall, color = TextTertiary)
                Spacer(Modifier.height(12.dp))
                AnimatedContent(
                    targetState = state.generatedPassword,
                    transitionSpec = { fadeIn() + scaleIn(initialScale = 0.97f) togetherWith fadeOut() },
                    label = "generated_password"
                ) { password ->
                    Text(
                        password,
                        style = KeeprTypography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { clipboard.setText(AnnotatedString(state.generatedPassword)) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = SurfaceRaised, contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, null, Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)); Text("Copy")
                    }
                    FilledTonalButton(
                        onClick = viewModel::regenerate,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = TextPrimary, contentColor = Background)
                    ) {
                        Icon(Icons.Outlined.Refresh, null, Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)); Text("New")
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            StrengthBar(state.strength)
            Spacer(Modifier.height(25.dp))

            Text("Length", style = KeeprTypography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = state.options.length.toFloat(),
                    onValueChange = { viewModel.onLength(it.toInt()) },
                    valueRange = 8f..48f,
                    steps = 39,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = TextPrimary, activeTrackColor = TextPrimary, inactiveTrackColor = SurfaceRaised)
                )
                Spacer(Modifier.width(12.dp))
                Surface(shape = RoundedCornerShape(11.dp), color = SurfaceMid) {
                    Text("${state.options.length}", style = KeeprTypography.labelLarge, modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp))
                }
            }

            Spacer(Modifier.height(15.dp))
            GeneratorOption("Uppercase letters", state.options.uppercase, viewModel::onUppercase)
            GeneratorOption("Lowercase letters", state.options.lowercase, viewModel::onLowercase)
            GeneratorOption("Numbers", state.options.numbers, viewModel::onNumbers)
            GeneratorOption("Symbols", state.options.symbols, viewModel::onSymbols)

            if (onUsePassword != null) {
                Spacer(Modifier.height(24.dp))
                KeeprButton("Use this password", { onUsePassword(state.generatedPassword) }, Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun GeneratorOption(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(17.dp), color = SurfaceMid) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = KeeprTypography.bodyLarge, modifier = Modifier.weight(1f))
            KeeprToggle(checked = checked, onCheckedChange = onChange)
        }
    }
    Spacer(Modifier.height(7.dp))
}

@Composable
private fun StrengthBar(strength: PasswordStrength) {
    val progress = when (strength) {
        PasswordStrength.VERY_WEAK -> .2f
        PasswordStrength.WEAK -> .4f
        PasswordStrength.FAIR -> .6f
        PasswordStrength.STRONG -> .8f
        PasswordStrength.VERY_STRONG -> 1f
    }
    val label = when (strength) {
        PasswordStrength.VERY_WEAK -> "Very weak"
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.FAIR -> "Fair"
        PasswordStrength.STRONG -> "Strong"
        PasswordStrength.VERY_STRONG -> "Very strong"
    }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceMid).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Strength", style = KeeprTypography.labelMedium, modifier = Modifier.weight(1f))
            AnimatedContent(targetState = label, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "strength_label") { Text(it, style = KeeprTypography.labelMedium, color = TextPrimary) }
        }
        Spacer(Modifier.height(11.dp))
        LinearProgressIndicator(progress = { progress }, Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(10.dp)), color = TextPrimary, trackColor = SurfaceRaised)
    }
}
