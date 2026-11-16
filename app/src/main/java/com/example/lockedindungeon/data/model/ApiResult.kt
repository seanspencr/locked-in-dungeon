package com.example.lockedindungeon.data.model

import retrofit2.Response


sealed interface ApiResult<out T> {
    data class Success<T>(val code : Int, val data: T) : ApiResult<T>
    data class Error(val message: String) : ApiResult<Nothing>

    companion object {
        fun <T> mapFromResponse(response: Response<T>): ApiResult<T> {
            if (response.isSuccessful) {
                response.body()?.let {
                    return Success(code = response.code(), data = it)
                }
                return Error("Body null : ${response.code()}")
            } else {
                return Error("Error in calling API, code : ${response.code()}")
            }
        }

        /**
         * Like [mapFromResponse], but a 2xx without a payload is a success carrying null.
         *
         * Endpoints that answer `200`/`204` with an empty body (unfollow and friends) would
         * otherwise come back as `Error("Body null : 204")` even though the server already applied
         * the change, and callers that revert their optimistic update on error would undo a write
         * that actually happened.
         */
        fun <T> mapFromResponseAllowingEmptyBody(response: Response<T>): ApiResult<T?> {
            if (!response.isSuccessful) {
                return Error("Error in calling API, code : ${response.code()}")
            }
            return Success(code = response.code(), data = response.body())
        }
    }
}