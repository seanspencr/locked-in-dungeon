package com.example.lockedindungeon.data.remote

import com.example.lockedindungeon.api.UserControllerApi
import com.example.lockedindungeon.data.model.ApiResult
import com.example.lockedindungeon.dto.ModifySettingsRequest
import com.example.lockedindungeon.dto.UserSettings
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepository @Inject constructor(
    val apiClientWrapper: SingletonApiClientWrapper
) {

    private val userApi : UserControllerApi = apiClientWrapper.authenticatedApiClient.createService(
        UserControllerApi::class.java)
    public fun searchUserByUsername(username : String){
        TODO("blom ada backend")
    }

    public fun findUserByUsername(username : String){
        searchUserByUsername(username)
    }

    public fun getUserSettings(): ApiResult<UserSettings?> {
        val response = userApi.getUserSettings().execute()
        return ApiResult.mapFromResponse(response)
    }

    public fun updateSettings(req : ModifySettingsRequest): ApiResult<UserSettings?> {
        val response = userApi.modifySettings(req).execute()
        return ApiResult.mapFromResponse(response)
    }
}