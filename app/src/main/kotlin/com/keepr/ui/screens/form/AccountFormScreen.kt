package com.keepr.ui.screens.form

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.keepr.data.model.VaultCategory
import com.keepr.ui.components.KeeprButton
import com.keepr.ui.components.KeeprTextField
import com.keepr.ui.components.ServiceIcon
import com.keepr.ui.theme.*

@Composable
fun AccountFormScreen(
    viewModel: AccountFormViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onOpenGenerator: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    Column(Modifier.fillMaxSize().background(Background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Cancel", color = TextSecondary) }
            Spacer(Modifier.weight(1f))
            AnimatedContent(targetState = state.isEditing, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "form_title") {
                Text(if (it) "Edit account" else "New account", style = KeeprTypography.titleLarge)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = viewModel::save, enabled = state.serviceName.isNotBlank()) { Text("Save", color = if (state.serviceName.isNotBlank()) TextPrimary else TextTertiary) }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(10.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(27.dp)).background(SurfaceMid).padding(19.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ServiceIcon(state.serviceName.ifBlank { "?" }, Modifier.size(58.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (state.isEditing) "Account details" else "A new place for it", style = KeeprTypography.headlineSmall)
                        Spacer(Modifier.height(3.dp))
                        Text("The service name is the only required field.", style = KeeprTypography.bodySmall, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            FormSection("IDENTITY") {
                KeeprTextField(state.serviceName, viewModel::onServiceName, "Service name", placeholder = "e.g. GitHub", leadingIcon = { Icon(Icons.Outlined.Language, null) })
                Spacer(Modifier.height(11.dp))
                KeeprTextField(state.accountLabel, viewModel::onAccountLabel, "Account label", placeholder = "Personal, work, school...")
            }

            Spacer(Modifier.height(20.dp))
            FormSection("SIGN-IN") {
                KeeprTextField(state.username, viewModel::onUsername, "Username", leadingIcon = { Icon(Icons.Outlined.PersonOutline, null) })
                Spacer(Modifier.height(11.dp))
                KeeprTextField(state.email, viewModel::onEmail, "Email", leadingIcon = { Icon(Icons.Outlined.MailOutline, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                Spacer(Modifier.height(11.dp))
                KeeprTextField(
                    state.password,
                    viewModel::onPassword,
                    "Password",
                    leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(if (passwordVisible) "Hide" else "Show", style = KeeprTypography.labelMedium)
                            }
                            IconButton(onClick = onOpenGenerator) { Icon(Icons.Outlined.AutoAwesome, "Generate password") }
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }

            Spacer(Modifier.height(20.dp))
            FormSection("ORGANIZE") {
                Text("Category", style = KeeprTypography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VaultCategory.entries, key = { it.displayName }) { category ->
                        val active = state.category == category.displayName
                        FilterChip(
                            selected = active,
                            onClick = { viewModel.onCategory(category.displayName) },
                            label = { Text(category.displayName) },
                            shape = RoundedCornerShape(13.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TextPrimary,
                                selectedLabelColor = Background,
                                containerColor = SurfaceMid,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
                Spacer(Modifier.height(11.dp))
                KeeprTextField(state.website, viewModel::onWebsite, "Website", placeholder = "https://")
                Spacer(Modifier.height(11.dp))
                KeeprTextField(state.notes, viewModel::onNotes, "Notes", placeholder = "Anything useful to remember", singleLine = false)
            }

            Spacer(Modifier.height(20.dp))
            Surface(shape = RoundedCornerShape(19.dp), color = SurfaceMid) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (state.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder, null, tint = if (state.isFavorite) FavoriteActive else TextSecondary)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("Favorite", style = KeeprTypography.bodyMedium)
                        Text("Keep it at the top of your vault.", style = KeeprTypography.bodySmall, color = TextSecondary)
                    }
                    Switch(checked = state.isFavorite, onCheckedChange = viewModel::onFavorite, colors = SwitchDefaults.colors(checkedThumbColor = Background, checkedTrackColor = TextPrimary))
                }
            }

            Spacer(Modifier.height(22.dp))
            KeeprButton(if (state.isEditing) "Save changes" else "Save account", viewModel::save, Modifier.fillMaxWidth(), enabled = state.serviceName.isNotBlank())
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = KeeprTypography.labelSmall, color = TextTertiary, modifier = Modifier.padding(start = 3.dp, bottom = 9.dp))
        Column(content = content)
    }
}
