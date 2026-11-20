package com.example.lockedindungeon.data.remote

import android.util.Log
import com.example.lockedindungeon.BuildConfig
import com.example.lockedindungeon.api.AuthControllerApi
import com.example.lockedindungeon.data.local.repositories.AppDatastoreRepository
import com.example.lockedindungeon.data.model.ApiResult
import com.example.lockedindungeon.dto.LoginResponse
import com.example.lockedindungeon.dto.RefreshRequest
import com.example.lockedindungeon.utils.auth.IdentityManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import org.openapitools.client.infrastructure.ApiClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


//Ini yang jalan sebelum request dikirim
class AuthRequestInterceptor() : Interceptor{
    private val TAG : String = "AuthRequestInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = IdentityManager.accessToken
        val hasToken = !token.isNullOrEmpty()

        Log.d(TAG, "--> ${original.method} ${original.url}")
        Log.d(TAG, "    Authorization: ${if (hasToken) "Bearer ${maskToken(token)}" else "none"}")

        val requestBuilder = original.newBuilder()
        if (hasToken) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())
        val tookMs = response.receivedResponseAtMillis - response.sentRequestAtMillis

        Log.d(TAG, "<-- ${response.code} ${original.method} ${original.url.encodedPath} (${tookMs}ms)")

        return response
    }
}


// Ini hanya akan ketrigger apabila user kena 401
class AuthResponseInterceptor(
    val datastore : AppDatastoreRepository,
    val authApi : AuthControllerApi
) : Authenticator {

    private val TAG : String = "AuthResponseInterceptor"

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        Log.w(TAG, "401 on ${response.request.method} $path, handling authentication")

        // Step 1: Avoid infinite loops if the refresh request itself returns 401
        if (path.contains("/auth/refresh")) {
            Log.w(TAG, "failure came from the refresh endpoint itself, giving up")
            return null
        }

        // Step 2: Ensure we don't retry continuously if token was already updated
        val currentToken = IdentityManager.accessToken
        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        Log.d(TAG, "token in IdentityManager : ${maskToken(currentToken)}")
        Log.d(TAG, "token sent by failed call : ${maskToken(requestToken)}")

        if (!currentToken.isNullOrEmpty() && currentToken != requestToken) {
            Log.d(TAG, "token was already refreshed by another call, retrying without refreshing again")
            // Retry original request with the already-refreshed token
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }

        // Perform synchronous token refresh
        synchronized(this) {
            Log.d(TAG, "refreshing the access token")
            return when(val tokenResponse = refreshToken()){
                is ApiResult.Error -> {
                    // returning the request here would retry with the expired token and loop forever
                    Log.w(TAG, "refresh failed (${tokenResponse.message}), giving up")
                    null
                }
                is ApiResult.Success<LoginResponse> -> {
                    Log.d(TAG, "refresh ok, retrying the failed call with ${maskToken(tokenResponse.data.accessToken)}")
                    IdentityManager.accessToken = tokenResponse.data.accessToken
                    // saving the refresh token also refreshes IdentityManager.isLoggedIn
                    saveRefreshToken(tokenResponse.data.refreshToken)

                    response.request.newBuilder().header("Authorization", "Bearer ${tokenResponse.data.accessToken}").build()
                }
            }
        }
    }


    fun refreshToken() : ApiResult<LoginResponse> =
        synchronized(this){
            runBlocking {
                val token = datastore.refreshToken.first()
                Log.d(TAG, "stored refresh token: ${maskToken(token)}")
                if(token.isNullOrEmpty()) {
                    Log.w(TAG, "no refresh token stored, cannot refresh")
                    // no refresh token means there is no session left, whatever the access token says
                    IdentityManager.isLoggedIn.value = false
                    return@runBlocking ApiResult.Error("No refresh token")
                }

                runCatching {
                    Log.d(TAG, "calling POST /auth/refresh")
                    authApi.refresh(RefreshRequest(token)).execute()
                }.fold(
                    onSuccess = { res ->
                        val body = res.body()
                        Log.d(TAG, "refresh answered ${res.code()}")
                        if(body != null && res.isSuccessful){
                            ApiResult.Success(res.code(), body)
                        }else if(res.code() == 401){
                            // refresh token rejected: the session is over, clear both sides
                            Log.w(TAG, "refresh token rejected, clearing the session")
                            IdentityManager.accessToken = null
                            saveRefreshToken(null)
                            ApiResult.Error("Refresh token invalid")
                        }else{
                            ApiResult.Error("Refresh Token Error")
                        }
                    },
                    onFailure = {
                        error ->
                        Log.w(TAG, "refresh call failed: ${error.message}")
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
            syncLoginState(datastore)
        }
    }
}

val logging = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    redactHeader("Authorization")
    redactHeader("Cookie")
}

/** Enough of a token to tell two of them apart, not enough to be a leaked credential in logcat. */
private fun maskToken(token : String?) : String =
    if (token.isNullOrEmpty()) "none" else "${token.take(8)}...${token.takeLast(4)} (len ${token.length})"

private const val AUTH_TAG = "AuthState"

/**
 * Keeps [IdentityManager.isLoggedIn] in sync with the two tokens the session actually needs.
 *
 * Logged in only when there is an access token in memory *and* a refresh token on disk: with just
 * one of them the next call cannot be authenticated or renewed. Reads the store directly because
 * the refresh token is persisted, while the access token lives only in [IdentityManager].
 */
public fun syncLoginState(datastore : AppDatastoreRepository) {
    val accessToken = IdentityManager.accessToken
    val refreshToken = runBlocking { datastore.refreshToken.first() }

    val hasAccessToken = !accessToken.isNullOrEmpty()
    val hasRefreshToken = !refreshToken.isNullOrEmpty()
    val loggedIn = hasAccessToken && hasRefreshToken

    if (IdentityManager.isLoggedIn.value != loggedIn) {
        Log.d(
            AUTH_TAG,
            "isLoggedIn -> $loggedIn (access=${maskToken(accessToken)}, refresh=${maskToken(refreshToken)})"
        )
        IdentityManager.isLoggedIn.value = loggedIn
    }
}


@Singleton
class SingletonApiClientWrapper @Inject constructor(
    val datastore: AppDatastoreRepository,
)
{
    // dipakai buat /auth/refresh, sengaja tanpa AuthRequestInterceptor + Authenticator supaya tidak
    // bisa rekursi. logging tetap dipasang biar panggilan refresh-nya kelihatan di logcat.
    private val unauthenticatedApiClient = ApiClient(
        baseUrl = BuildConfig.apiBaseUrl,
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
    )

    private val okHttpClient = OkHttpClient.Builder()
        // a call that never answers otherwise blocks the thread that runs the Authenticator
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(AuthRequestInterceptor())
        .addInterceptor(logging)
        .authenticator(AuthResponseInterceptor(datastore, unauthenticatedApiClient.createService(
            AuthControllerApi::class.java)))


    public val authenticatedApiClient = ApiClient(baseUrl = BuildConfig.apiBaseUrl, okHttpClient)
}