package com.example.lockedindungeon.viewmodels

/** Form state shared by the login and signup screens. */
data class AuthFormState(
    val username : String = "",
    val password : String = "",
    val isLoading : Boolean = false,
    val errorMessage : String? = null,
    val successMessage : String? = null
)
