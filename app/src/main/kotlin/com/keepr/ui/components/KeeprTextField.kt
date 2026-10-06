package com.keepr.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.keepr.ui.theme.*

@Composable
fun KeeprTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: (@Composable (() -> Unit))? = null,
    trailingIcon: (@Composable (() -> Unit))? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth(),

        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions.copy(imeAction = if (singleLine) ImeAction.Next else ImeAction.Default),
        singleLine = singleLine,
        supportingText = supportingText?.let { { Text(it) } },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TextPrimary,
            unfocusedBorderColor = BorderDefault,
            focusedContainerColor = SurfaceRaised,
            unfocusedContainerColor = SurfaceMid,
            cursorColor = TextPrimary,
            focusedLabelColor = TextPrimary,
            unfocusedLabelColor = TextSecondary,
            focusedLeadingIconColor = TextPrimary,
            unfocusedLeadingIconColor = TextSecondary,
            focusedTrailingIconColor = TextPrimary,
            unfocusedTrailingIconColor = TextSecondary
        ),
        textStyle = KeeprTypography.bodyLarge
    )
}
