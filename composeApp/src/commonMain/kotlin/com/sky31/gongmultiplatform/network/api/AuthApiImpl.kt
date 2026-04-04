package com.sky31.gongmultiplatform.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters

class AuthApiImpl(
    private val client: HttpClient
): AuthApi {

    override suspend fun login(username: String, password: String): HttpResponse =
        client.submitForm(
            url = "/login",
            encodeInQuery = false,
            formParameters = Parameters.build {
                append("username", username)
                append("password", password)
            },
            block = {
                headers {
                    append(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
                }
            }
        )
}