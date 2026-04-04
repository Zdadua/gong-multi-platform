package com.sky31.gongmultiplatform.network

import com.sky31.gongmultiplatform.SystemGlobalConfig
import com.sky31.gongmultiplatform.model.bearerTokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.HttpSendPipeline
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * client provider
 */
object HttpClientProvider {
    val client: HttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            install(Resources)

            install(Logging) {
                level = LogLevel.HEADERS
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        if(!bearerTokenStorage.isEmpty()) {
                            bearerTokenStorage.last()
                        } else {
                            BearerTokens("", "")
                        }
                    }

                    sendWithoutRequest { request ->
                        request.url.encodedPath != "/login"
                    }
                }
            }

            defaultRequest {
                url {
                    protocol = URLProtocol.Companion.HTTP
                    host = SystemGlobalConfig.HOST
                }
            }
        }.apply {
            sendPipeline.intercept(HttpSendPipeline.Monitoring) {
                val authHeader = context.headers[HttpHeaders.Authorization]
                val path = context.url.encodedPath
                println("最终请求的 Authorization: $authHeader")
                println(path)
                proceed()
            }
        }
}