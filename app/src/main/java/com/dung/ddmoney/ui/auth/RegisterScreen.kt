package com.dung.ddmoney.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.dung.ddmoney.R
import com.dung.ddmoney.ui.theme.Space1
import com.dung.ddmoney.ui.theme.Space2
import com.dung.ddmoney.ui.theme.Space3
import com.dung.ddmoney.ui.theme.Space4
import com.dung.ddmoney.ui.theme.Space6

@Composable
fun RegisterScreen(
    onRegisterClick: (fullName: String, email: String, password: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    val focusManager = LocalFocusManager.current
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var agreedToTerms by rememberSaveable { mutableStateOf(false) }
    var showEmptyWarning by rememberSaveable { mutableStateOf(false) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }

    val emptyError = stringResource(R.string.register_error_empty)
    val emailError = stringResource(R.string.auth_error_email)
    val passwordAsciiError = stringResource(R.string.auth_error_password_ascii)
    val passwordLengthError = stringResource(R.string.register_error_password_length)
    val passwordMismatchError = stringResource(R.string.register_error_password_mismatch)
    val termsError = stringResource(R.string.register_error_terms)

    val passwordsMatch = password.isEmpty() ||
        confirmPassword.isEmpty() ||
        password == confirmPassword

    fun validateInput(): Boolean {
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
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

        if (password.length < 6) {
            validationError = passwordLengthError
            return false
        }

        if (password != confirmPassword) {
            validationError = passwordMismatchError
            return false
        }

        if (!agreedToTerms) {
            validationError = termsError
            return false
        }

        validationError = null
        return true
    }

    fun submit() {
        focusManager.clearFocus()
        if (validateInput()) {
            onRegisterClick(fullName.trim(), email.trim(), password)
        }
    }

    LaunchedEffect(fullName, email, password, confirmPassword, agreedToTerms) {
        if (
            fullName.isNotBlank() &&
            email.isNotBlank() &&
            password.length >= 6 &&
            password == confirmPassword &&
            agreedToTerms
        ) {
            showEmptyWarning = false
            validationError = null
        }
    }

    AuthScreenContainer(
        title = stringResource(R.string.register_title),
        subtitle = null,
        onBack = onBack,
    ) {
        DDMoneyInputField(
            label = stringResource(R.string.register_name_label),
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = stringResource(R.string.register_name_placeholder),
            leadingIcon = Icons.Outlined.Person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )
        Spacer(Modifier.height(Space4))
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
        Spacer(Modifier.height(Space4))
        DDMoneyInputField(
            label = stringResource(R.string.auth_password_label),
            value = password,
            onValueChange = { password = it },
            placeholder = stringResource(R.string.auth_password_placeholder),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible },
            isError = validationError == passwordAsciiError ||
                validationError == passwordLengthError,
            errorText = when (validationError) {
                passwordAsciiError -> passwordAsciiError
                passwordLengthError -> passwordLengthError
                else -> null
            },
            helperText = stringResource(R.string.register_password_helper),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )
        Spacer(Modifier.height(Space4))
        DDMoneyInputField(
            label = stringResource(R.string.register_confirm_password_label),
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = stringResource(R.string.register_confirm_password_placeholder),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
            isError = !passwordsMatch || validationError == passwordMismatchError,
            errorText = passwordMismatchError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )

        Spacer(Modifier.height(Space4))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = agreedToTerms,
                    enabled = !isLoading,
                    role = Role.Checkbox,
                    onValueChange = { agreedToTerms = it },
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space2),
        ) {
            Checkbox(
                checked = agreedToTerms,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                ),
            )
            Text(
                text = stringResource(R.string.register_terms_text),
                style = MaterialTheme.typography.bodyMedium,
                color = if (validationError == termsError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        val displayError = errorMessage ?: validationError ?: if (showEmptyWarning) emptyError else null
        if (displayError != null && displayError != passwordMismatchError) {
            Spacer(Modifier.height(Space4))
            AuthErrorMessage(message = displayError)
        }

        Spacer(Modifier.height(Space3))
        AuthPrimaryButton(
            text = stringResource(R.string.auth_create_account),
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
        Spacer(Modifier.height(Space3))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading, onClick = onNavigateToLogin)
                .padding(vertical = Space2),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.register_has_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(Space1))
            Text(
                text = stringResource(R.string.auth_login),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
