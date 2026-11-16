package com.example.lockedindungeon.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lockedindungeon.components.AuthForm
import com.example.lockedindungeon.viewmodels.SignupViewmodel

@Composable
fun SignupScreen(
    viewmodel : SignupViewmodel,
    onNavigateToLogin : () -> Unit = {},
    onSignedUp : () -> Unit = {}
) {
    val state by viewmodel.state.collectAsStateWithLifecycle()

    AuthForm(
        title = "Sign up",
        subtitle = "Create an account with a username and a password",
        submitLabel = "Sign up",
        state = state,
        onUsernameChange = viewmodel::onUsernameChange,
        onPasswordChange = viewmodel::onPasswordChange,
        onSubmit = { viewmodel.signup(onSignedUp) },
        switchLabel = "Already have an account? Login",
        onSwitch = onNavigateToLogin
    )
}
