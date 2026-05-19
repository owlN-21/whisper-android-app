package com.example.lecture.data.network

import com.example.lecture.data.network.dto.ErrorResponseDto
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

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
                    message = "Сервер вернул пустой ответ",
                    code = response.code()
                )
            }
        } else {
            NetworkResult.Error(
                message = parseErrorMessage(response),
                code = response.code()
            )
        }
    } catch (exception: SocketTimeoutException) {
        NetworkResult.Error(
            message = "Превышено время ожидания ответа от сервера"
        )
    } catch (exception: IOException) {
        NetworkResult.Error(
            message = "Не удалось подключиться к серверу. Проверьте соединение или доступность backend"
        )
    } catch (exception: Exception) {
        NetworkResult.Error(
            message = exception.message ?: "Произошла неизвестная ошибка"
        )
    }
}

private fun <T> parseErrorMessage(response: Response<T>): String {
    val code = response.code()

    val defaultMessage = when (code) {
        400 -> "Некорректный запрос"
        401 -> "Ошибка авторизации"
        403 -> "Доступ запрещен"
        404 -> "Данные не найдены"
        408 -> "Превышено время ожидания запроса"
        in 500..599 -> "Ошибка сервера. Попробуйте позже"
        else -> "Ошибка запроса. Код: $code"
    }

    val errorBody = response.errorBody()?.string()

    if (errorBody.isNullOrBlank()) {
        return defaultMessage
    }

    return try {
        val errorResponse = Gson().fromJson(errorBody, ErrorResponseDto::class.java)

        when {
            !errorResponse.message.isNullOrBlank() -> errorResponse.message
            !errorResponse.error.isNullOrBlank() -> errorResponse.error
            !errorResponse.code.isNullOrBlank() -> errorResponse.code
            else -> defaultMessage
        }
    } catch (exception: Exception) {
        defaultMessage
    }
}