package com.example.lockedindungeon.data.remote

import android.util.Log
import com.example.lockedindungeon.BuildConfig
import com.example.lockedindungeon.api.AuthControllerApi
import com.example.lockedindungeon.api.UserControllerApi
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import com.example.lockedindungeon.data.model.ApiResult
import com.example.lockedindungeon.dto.LoginRequest
import com.example.lockedindungeon.dto.LoginResponse
import com.example.lockedindungeon.dto.MeResponse
import com.example.lockedindungeon.dto.RefreshRequest
import com.example.lockedindungeon.dto.RegisterFirebaseRequest
import com.example.lockedindungeon.dto.SignupRequest
import com.example.lockedindungeon.dto.SignupResponse
import com.example.lockedindungeon.utils.IdentityManager
import com.example.lockedindungeon.utils.registerFirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import org.openapitools.client.infrastructure.ApiClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.fold


@Singleton
class AuthRepository @Inject constructor(
    val datastore: AppDatastoreRepository,
    val apiClient: ApiClient
) {

    companion object {
        private const val TAG = "AuthRepository"
    }



    private val authApi : AuthControllerApi = apiClient.createService(AuthControllerApi::class.java)
    private val userApi : UserControllerApi = apiClient.createService(UserControllerApi::class.java)



    fun registerFirebaseToServer(fid : String, deviceName : String){
        CoroutineScope(Dispatchers.IO).launch{
            runCatching {
                userApi.registerFirebaseEntity(RegisterFirebaseRequest(fid, deviceName)).execute()
            }.fold(
                onSuccess = {res -> Log.d(AuthRepository.Companion.TAG, res.body().toString())},
                onFailure = {err -> err.message?.let { Log.e(AuthRepository.Companion.TAG, it) } },
            )
        }
    }

    suspend fun login(username : String, password : String) : ApiResult<LoginResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                authApi.login(LoginRequest(username = username, password = password)).execute()
            }.fold(
                onSuccess = { response ->
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        IdentityManager.accessToken = body.accessToken
                        saveRefreshToken(body.refreshToken)
                        tryGetUserInfo()


                        registerFirebaseMessaging { fid, deviceName ->
                            registerFirebaseToServer(fid, deviceName)
                        }

                        ApiResult.Success(response.code(), body)



                    } else {
                        ApiResult.Error("Login error (cant reach server)")
                    }
                },
                onFailure = {
                    error ->
                        error.message?.let { Log.e(TAG, it) }
                        ApiResult.Error("Login error (cant reach server)")

                }
            )
        }

    suspend fun signup(username : String, password : String) : ApiResult<SignupResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                authApi.createUser(SignupRequest(username = username, password = password)).execute()
            }.fold(
                onSuccess = { response ->
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        ApiResult.Success(
                            response.code(),
                            body
                        )
                    } else {
                        ApiResult.Error("Signup Error (cant reach server)")
                    }
                },
                onFailure = { error -> ApiResult.Error("Signup Error (cant reach server") }
            )
        }



    suspend fun tryGetUserInfo() : ApiResult<MeResponse> =
        withContext(Dispatchers.IO){
            runCatching {
                authApi.me().execute()
            }.fold(
                onSuccess = { res ->
                    val body = res.body()
                    Log.d(TAG, body.toString())
                    if(body != null && res.isSuccessful){
                        IdentityManager.username = body.username
                        ApiResult.Success(res.code(), body)
                    }else if(res.code() == 401){
                        ApiResult.Error("Access token invalid")
                    }else{
                        ApiResult.Error("Me Error")
                    }
                },
                onFailure = {
                    ApiResult.Error("Me Error (cant reach server)")
                }
            )
        }

    suspend fun refreshToken() : ApiResult<LoginResponse> =
        withContext(Dispatchers.IO){
            val token = datastore.refreshToken.last()
            if(token.isNullOrEmpty()) ApiResult.Error("No refresh token")

            runCatching {
                authApi.refresh(RefreshRequest(token)).execute()
            }.fold(
                onSuccess = { res ->
                    val body = res.body()
                    if(body != null && res.isSuccessful){
                        ApiResult.Success(res.code(), body)
                    }else if(res.code() == 401){
                        ApiResult.Error("Refresh token invalid")
                    }else{
                        ApiResult.Error("Refresh Token Error")
                    }
                },
                onFailure = {
                    ApiResult.Error("Refresh Token Error (cant reach server)")
                }
            )
        }




    suspend fun saveRefreshToken(refreshToken: String?){
        this.datastore.setRefreshToken(refreshToken)
    }

}
