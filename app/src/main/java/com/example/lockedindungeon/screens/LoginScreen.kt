package com.example.lockedindungeon.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lockedindungeon.components.AuthForm
import com.example.lockedindungeon.viewmodels.LoginViewmodel

@Composable
fun LoginScreen(
    viewmodel : LoginViewmodel,
    onNavigateToSignup : () -> Unit = {},
    onLoggedIn : () -> Unit = {}
) {
    val state by viewmodel.state.collectAsStateWithLifecycle()

    AuthForm(
        title = "Login",
        subtitle = "Sign in to link this device to your account",
        submitLabel = "Login",
        state = state,
        onUsernameChange = viewmodel::onUsernameChange,
        onPasswordChange = viewmodel::onPasswordChange,
        onSubmit = { viewmodel.login(onLoggedIn) },
        switchLabel = "No account yet? Sign up",
        onSwitch = onNavigateToSignup
    )
}
