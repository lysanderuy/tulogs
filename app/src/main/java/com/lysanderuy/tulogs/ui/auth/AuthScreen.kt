package com.lysanderuy.tulogs.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lysanderuy.tulogs.ui.theme.Amber500
import com.lysanderuy.tulogs.ui.theme.Error500
import com.lysanderuy.tulogs.ui.theme.Ink700
import com.lysanderuy.tulogs.ui.theme.Ink900
import com.lysanderuy.tulogs.ui.theme.Ink950
import com.lysanderuy.tulogs.ui.theme.Mist600
import com.lysanderuy.tulogs.ui.theme.Paper50
import com.lysanderuy.tulogs.ui.theme.TuLogsType

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onModeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isSignUp = uiState.mode == AuthMode.SIGN_UP

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink950)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Paper50)) { append("Tu") }
                    withStyle(SpanStyle(color = Amber500)) { append("Logs") }
                },
                style = TuLogsType.statusHeadline
            )
            Text(
                text = if (isSignUp) "Create an account to start logging." else "Sign in to keep your logs.",
                style = TuLogsType.statusSub,
                modifier = Modifier.padding(top = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AuthTextField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "you@email.com",
                    keyboardType = KeyboardType.Email
                )
                AuthTextField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = if (isSignUp) "Minimum 8 characters" else "••••••••",
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = TuLogsType.captionText.copy(color = Error500)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (uiState.isLoading) Amber500.copy(alpha = 0.5f) else Amber500)
                        .clickable(enabled = !uiState.isLoading) {
                            if (isSignUp) onSignUp(email, password) else onSignIn(email, password)
                        }
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        Text(
                            text = if (isSignUp) "Creating Account..." else "Signing In...",
                            style = TuLogsType.captionText.copy(color = Ink950)
                        )
                    } else {
                        Text(
                            text = if (isSignUp) "Create Account" else "Sign In",
                            style = TuLogsType.captionText.copy(color = Ink950)
                        )
                    }
                }
            }

            Text(
                text = buildAnnotatedString {
                    if (isSignUp) {
                        append("Already have an account? ")
                        withStyle(SpanStyle(color = Amber500)) { append("Sign in") }
                    } else {
                        append("New to TuLogs? ")
                        withStyle(SpanStyle(color = Amber500)) { append("Create an account") }
                    }
                },
                style = TuLogsType.captionText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .clickable(onClick = onModeToggle)
            )
        }
    }
}

@Composable
private fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label.uppercase(), style = TuLogsType.monoLabel)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Ink900)
                .border(width = 1.dp, color = Ink700, shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = TuLogsType.factValue.copy(color = Mist600, fontSize = 15.sp))
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = TuLogsType.factValue.copy(color = Paper50, fontSize = 15.sp),
                        visualTransformation = if (isPassword && !passwordVisible) {
                            PasswordVisualTransformation()
                        } else {
                            VisualTransformation.None
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        cursorBrush = SolidColor(Amber500),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (isPassword) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Mist600,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { passwordVisible = !passwordVisible }
                    )
                }
            }
        }
    }
}
