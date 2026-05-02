package com.example.lecture.data.repository

import com.example.lecture.data.remote.api.UserApi
import com.example.lecture.data.local.UserSessionStorage
import com.example.lecture.data.remote.dto.CreateUserRequest
import com.example.lecture.data.remote.dto.UserResponseDto


class UserRepository(
    private val userApi: UserApi,
    private val userSessionStorage: UserSessionStorage
) {

    suspend fun loginOrCreateUser(email: String): Result<UserResponseDto> {
        return try {
            val getResponse = userApi.getUserByEmail(email)

            if (getResponse.isSuccessful) {
                val user = getResponse.body()
                if (user != null) {
                    userSessionStorage.saveUserId(user.id)
                    return Result.success(user)
                }
            }

            if (getResponse.code() == 404) {
                val createResponse = userApi.createUser(CreateUserRequest(email))

                if (createResponse.isSuccessful) {
                    val createdUser = createResponse.body()
                    if (createdUser != null) {
                        userSessionStorage.saveUserId(createdUser.id)
                        return Result.success(createdUser)
                    }
                }

                return Result.failure(Exception("Не удалось создать пользователя"))
            }

            Result.failure(Exception("Не удалось получить пользователя. Код: ${getResponse.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSavedUserId(): Long? {
        return userSessionStorage.getUserId()
    }

    fun logout() {
        userSessionStorage.clearUserId()
    }
}