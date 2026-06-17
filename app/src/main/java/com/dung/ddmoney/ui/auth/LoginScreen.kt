package com.dung.ddmoney.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.dung.ddmoney.R
import com.dung.ddmoney.ui.theme.Space1
import com.dung.ddmoney.ui.theme.Space2
import com.dung.ddmoney.ui.theme.Space3
import com.dung.ddmoney.ui.theme.Space4
import com.dung.ddmoney.ui.theme.Space6

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onGoogleSignInClick: () -> Unit = {},
    onBack: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    val focusManager = LocalFocusManager.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showEmptyWarning by rememberSaveable { mutableStateOf(false) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }

    val emptyError = stringResource(R.string.login_error_empty)
    val emailError = stringResource(R.string.auth_error_email)
    val passwordAsciiError = stringResource(R.string.auth_error_password_ascii)

    fun validateInput(): Boolean {
        if (email.isBlank() || password.isBlank()) {
            showEmptyWarning = true
            validationError = null
            return false
        }
        showEmptyWarning = false

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        if (!email.matches(emailRegex)) {
            validationError = emailError
            return false
        }

        val asciiRegex = "^[\\x20-\\x7E]+\$".toRegex()
        if (!password.matches(asciiRegex)) {
            validationError = passwordAsciiError
            return false
        }

        validationError = null
        return true
    }

    fun submit() {
        focusManager.clearFocus()
        if (validateInput()) {
            onLoginClick(email.trim(), password)
        }
    }

    LaunchedEffect(email, password) {
        if (email.isNotBlank() && password.isNotBlank()) {
            showEmptyWarning = false
            validationError = null
        }
    }

    AuthScreenContainer(
        title = stringResource(R.string.login_title),
        subtitle = stringResource(R.string.login_subtitle),
        onBack = onBack,
    ) {
        DDMoneyInputField(
            label = stringResource(R.string.auth_email_label),
            value = email,
            onValueChange = { email = it },
            placeholder = stringResource(R.string.auth_email_placeholder),
            leadingIcon = Icons.Outlined.Email,
            isError = validationError == emailError,
            errorText = emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )
        Spacer(Modifier.height(Space2))
        DDMoneyInputField(
            label = stringResource(R.string.auth_password_label),
            value = password,
            onValueChange = { password = it },
            placeholder = stringResource(R.string.auth_password_placeholder),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible },
            trailingLabel = stringResource(R.string.login_forgot_password),
            onTrailingLabelClick = { /* Forgot-password flow is not implemented yet. */ },
            isError = validationError == passwordAsciiError,
            errorText = passwordAsciiError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )

        val displayError = errorMessage ?: validationError ?: if (showEmptyWarning) emptyError else null
        if (displayError != null) {
            Spacer(Modifier.height(Space4))
            AuthErrorMessage(message = displayError)
        }

        Spacer(Modifier.height(Space6))
        AuthPrimaryButton(
            text = stringResource(R.string.auth_login),
            onClick = { submit() },
            isLoading = isLoading,
        )
        Spacer(Modifier.height(Space6))
        AuthDivider()
        Spacer(Modifier.height(Space6))
        GoogleSignInButton(
            onClick = onGoogleSignInClick,
            enabled = !isLoading,
        )
        Spacer(Modifier.height(Space4))
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.login_terms_prefix).trim())
                append(" ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(stringResource(R.string.auth_terms))
                }
                append(" ")
                append(stringResource(R.string.login_terms_join).trim())
                append(" ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(stringResource(R.string.auth_privacy))
                }
                append(".")
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Space3))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading, onClick = onNavigateToRegister)
                .padding(vertical = Space2),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.login_no_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(Space1))
            Text(
                text = stringResource(R.string.auth_create_account),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
