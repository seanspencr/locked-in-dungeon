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
class LoginViewmodel @Inject constructor(
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

    fun login(onSuccess : () -> Unit) {
        val current = _state.value
        val validation = when {
            current.username.isBlank() -> "Username cannot be empty"
            current.password.isBlank() -> "Password cannot be empty"
            else -> null
        }
        if (validation != null) {
            _state.value = current.copy(errorMessage = validation)
            return
        }

        _state.value = current.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch {
            when (val result = authRepository.login(current.username.trim(), current.password)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        password = "",
                        successMessage = result.data.message ?: "Logged in as ${current.username.trim()}"
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
