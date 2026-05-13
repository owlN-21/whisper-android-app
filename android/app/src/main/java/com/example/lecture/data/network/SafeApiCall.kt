package com.example.lecture.data.network

import retrofit2.Response
import java.io.IOException

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): NetworkResult<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            val body = response.body()

            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error(
                    message = "Empty response body",
                    code = response.code()
                )
            }
        } else {
            NetworkResult.Error(
                message = response.errorBody()?.string() ?: "Unknown server error",
                code = response.code()
            )
        }
    } catch (exception: IOException) {
        NetworkResult.Error(
            message = "Network error: ${exception.message}"
        )
    } catch (exception: Exception) {
        NetworkResult.Error(
            message = "Unexpected error: ${exception.message}"
        )
    }
}