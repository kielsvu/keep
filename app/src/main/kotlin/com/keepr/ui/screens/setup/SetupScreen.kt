package com.keepr.ui.screens.setup

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.keepr.ui.components.NumericKeypad
import com.keepr.ui.components.PinDots
import com.keepr.ui.theme.*

@Composable
fun SetupScreen(viewModel: SetupViewModel, onSetupComplete: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
        LaunchedEffect(state.setupComplete) { if (state.setupComplete) onSetupComplete() }

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
            Text("Set up KeepJ", style = KeeprTypography.displaySmall)
            Spacer(Modifier.height(7.dp))
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    (slideInHorizontally(tween(360)) { it / 5 } + fadeIn(tween(280))) togetherWith
                        (slideOutHorizontally(tween(220)) { -it / 8 } + fadeOut(tween(160)))
                },
                label = "setup_step"
            ) { step ->
                Text(
                    if (step == SetupStep.CREATE) "Choose a six-digit PIN for your vault" else "Enter it once more to confirm",
                    style = KeeprTypography.bodyMedium.copy(color = TextSecondary),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(40.dp))
            PinDots(pinLength = state.pin.length, maxLength = 6, hasError = state.error != null)
            Spacer(Modifier.height(15.dp))
            AnimatedContent(
                targetState = state.error,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "setup_error"
            ) { error ->
                Text(
                    error ?: "Your PIN stays on this device.",
                    style = KeeprTypography.bodySmall.copy(color = if (error != null) ErrorColor else TextTertiary),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.weight(1.05f))
            NumericKeypad(
                onDigit = viewModel::onDigit,
                onDelete = viewModel::onDelete,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}
