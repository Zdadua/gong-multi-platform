package com.sky31.gongmultiplatform.network.repository

import com.sky31.gongmultiplatform.network.api.AuthApiImpl
import com.sky31.gongmultiplatform.network.dto.AuthDto
import com.sky31.gongmultiplatform.util.NetworkResult
import com.sky31.gongmultiplatform.util.authMsgMap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode

class AuthRepositoryImpl(
    client: HttpClient
): AuthRepository {
    private val api = AuthApiImpl(client)

    override suspend fun login(username: String, password: String): NetworkResult<AuthDto> {
        try {
            val response = api.login(username, password)
            val code = response.status

            return when(code) {
                HttpStatusCode.OK -> {
                    val body = response.body<AuthDto>()

                    NetworkResult.Success(
                        code = code,
                        data = body
                    )
                }

                HttpStatusCode.Conflict,
                HttpStatusCode.ServiceUnavailable,
                HttpStatusCode.GatewayTimeout,
                HttpStatusCode.Forbidden,
                HttpStatusCode.Unauthorized ->
                    NetworkResult.Error(
                        code = code,
                        message = authMsgMap[code] ?: "Unknown error"
                    )

                else ->
                    NetworkResult.Error(
                        code = code,
                        message = response.toString()
                    )
            }
        } catch (e: Exception) {
            return NetworkResult.Error(
                message = e.message ?: "Unknown error",
                exception = e
            )
        }

    }
}