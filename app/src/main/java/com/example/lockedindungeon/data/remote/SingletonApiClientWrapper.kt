package com.example.lockedindungeon.data.remote

import com.example.lockedindungeon.BuildConfig
import com.example.lockedindungeon.api.AuthControllerApi
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import com.example.lockedindungeon.data.model.ApiResult
import com.example.lockedindungeon.dto.LoginResponse
import com.example.lockedindungeon.dto.RefreshRequest
import com.example.lockedindungeon.utils.IdentityManager
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import org.openapitools.client.infrastructure.ApiClient
import javax.inject.Inject
import javax.inject.Singleton


//Ini yang jalan sebelum request dikirim
class AuthRequestInterceptor() : Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = IdentityManager.accessToken

        val requestBuilder = original.newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}


// Ini hanya akan ketrigger apabila user kena 401
class AuthResponseInterceptor(
    val datastore : AppDatastoreRepository,
    val authApi : AuthControllerApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Step 1: Avoid infinite loops if the refresh request itself returns 401
        if (response.request.url.encodedPath.contains("/auth/refresh")) {
            return null
        }

        // Step 2: Ensure we don't retry continuously if token was already updated
        val currentToken = IdentityManager.accessToken
        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        if (!currentToken.isNullOrEmpty() && currentToken != requestToken) {
            // Retry original request with the already-refreshed token
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        } else {
            // Perform synchronous token refresh
            synchronized(this) {
                when(val tokenResponse = refreshToken()){
                    is ApiResult.Error -> return response.request
                    is ApiResult.Success<LoginResponse> -> {
                        IdentityManager.accessToken = tokenResponse.data.accessToken
                        saveRefreshToken(tokenResponse.data.refreshToken)

                        return response.request.newBuilder().header("Authorization", "Bearer ${tokenResponse.data.accessToken}").build()
                    }
                }
            }

        }
    }


    fun refreshToken() : ApiResult<LoginResponse> =
        synchronized(this){
            runBlocking {
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
        }

    fun saveRefreshToken(refreshToken: String?){
        synchronized(this){
            runBlocking {
                datastore.setRefreshToken(refreshToken)
            }
        }
    }
}

val logging = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    redactHeader("Authorization")
    redactHeader("Cookie")
}


@Singleton
class SingletonApiClientWrapper @Inject constructor(
    val datastore: AppDatastoreRepository,
)
{
    //    intercept request utk masukin header

    private val unauthenticatedApiClient = ApiClient(baseUrl = BuildConfig.apiBaseUrl)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthRequestInterceptor())
        .addInterceptor(logging)
        .authenticator(AuthResponseInterceptor(datastore, unauthenticatedApiClient.createService(
            AuthControllerApi::class.java)))


    public val authenticatedApiClient = ApiClient(baseUrl = BuildConfig.apiBaseUrl, okHttpClient)
}