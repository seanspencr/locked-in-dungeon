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
    }
}