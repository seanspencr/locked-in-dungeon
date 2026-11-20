package com.example.lockedindungeon.utils.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object IdentityManager {
    var username : String? = null
    var accessToken: String? = null
    var isLoggedIn : MutableStateFlow<Boolean> = MutableStateFlow(false)
}