package com.example.lockedindungeon.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockedindungeon.data.model.ApiResult
import com.example.lockedindungeon.data.remote.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewmodel @Inject constructor(
    private val authRepository : AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthFormState())
    val state : StateFlow<AuthFormState> = _state

    fun onUsernameChange(username : String) {
        _state.value = _state.value.copy(username = username, errorMessage = null)
    }

    fun onPasswordChange(password : String) {
        _state.value = _state.value.copy(password = password, errorMessage = null)
    }

    fun signup(onSuccess : () -> Unit) {
        val current = _state.value
        val validation = when {
            current.username.isBlank() -> "Username cannot be empty"
            current.password.isBlank() -> "Password cannot be empty"
            current.password.length < 6 -> "Password needs at least 6 characters"
            else -> null
        }
        if (validation != null) {
            _state.value = current.copy(errorMessage = validation)
            return
        }

        _state.value = current.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch {
            when (val result = authRepository.signup(current.username.trim(), current.password)) {
                is ApiResult.Success -> {
                    val createdUsername = result.data.user?.username ?: current.username.trim()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        password = "",
                        successMessage = result.data.message ?: "Account created for $createdUsername"
                    )
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}
