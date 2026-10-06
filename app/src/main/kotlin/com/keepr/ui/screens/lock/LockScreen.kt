package com.keepr.ui.screens.lock

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.keepr.ui.components.NumericKeypad
import com.keepr.ui.components.PinDots
import com.keepr.ui.theme.*

@Composable
fun LockScreen(viewModel: LockViewModel, onUnlocked: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(state.unlocked) { if (state.unlocked) onUnlocked() }
    LaunchedEffect(state.showBiometricPrompt) {
        if (state.showBiometricPrompt) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            viewModel.launchBiometricPrompt(activity)
        }
    }

    Box(
        Modifier.fillMaxSize().background(Background).statusBarsPadding().navigationBarsPadding()
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.9f))
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(76.dp).background(SurfaceRaised, CircleShape))
                Box(Modifier.size(58.dp).background(SurfaceOverlay, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Lock, null, tint = AccentPurpleLight, modifier = Modifier.size(29.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("KeepJ", style = KeeprTypography.displaySmall)
            Spacer(Modifier.height(7.dp))
            Text("Your vault is locked", style = KeeprTypography.bodyMedium.copy(color = TextSecondary), textAlign = TextAlign.Center)
            Spacer(Modifier.height(40.dp))
            PinDots(pinLength = state.pin.length, maxLength = 6, hasError = state.error != null)
            Spacer(Modifier.height(15.dp))
            AnimatedContent(
                targetState = state.error ?: state.lockedUntilText,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "lock_message"
            ) { message ->
                Text(
                    message ?: "Enter your PIN",
                    style = KeeprTypography.bodySmall.copy(color = if (state.error != null) ErrorColor else TextTertiary),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.weight(1.05f))
            NumericKeypad(
                onDigit = { if (!state.isLockedOut) viewModel.onDigit(it) },
                onDelete = viewModel::onDelete,
                modifier = Modifier.fillMaxWidth(),
                extraAction = if (state.biometricEnabled) {
                    {
                        IconButton(onClick = viewModel::triggerBiometric, modifier = Modifier.size(76.dp)) {
                            Icon(Icons.Outlined.Fingerprint, "Unlock with biometrics", tint = AccentPurple, modifier = Modifier.size(29.dp))
                        }
                    }
                } else null
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}
