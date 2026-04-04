package com.sky31.gongmultiplatform.util

import com.sky31.gongmultiplatform.SystemGlobalConfig
import com.sky31.gongmultiplatform.network.response.ApiResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

val authMsgMap = mapOf(
    HttpStatusCode.Unauthorized to "账号或密码错误",
    HttpStatusCode.Forbidden to "请求被拒绝(403)，请检查请求格式或鉴权策略",
    HttpStatusCode.Conflict to "账号未初始化",
    HttpStatusCode.ServiceUnavailable to "教务系统超时",
    HttpStatusCode.GatewayTimeout to "请求超时",
    HttpStatusCode.BadGateway to "网关错误",
)

val getMsgMap = mapOf(
    HttpStatusCode.Unauthorized to "token无效",
    HttpStatusCode.NonAuthoritativeInformation to "资源过期",
    HttpStatusCode.NotFound to "请求资源不存在",
    HttpStatusCode.Locked to "账户锁定",
    HttpStatusCode.ServiceUnavailable to "教务系统超时",
    HttpStatusCode.GatewayTimeout to "请求超时",
)

suspend inline fun <reified T> safeApiCall(
    apiCall: suspend () -> HttpResponse
): NetworkResult<T> {
    try {
        val response = apiCall()
        val code = response.status

        println(code.toString())
        return when(code) {
            HttpStatusCode.OK -> {
                val body = response.body<ApiResponse<T>>()
                return NetworkResult.Success(
                    code = code,
                    data = body.data
                )
            }

            HttpStatusCode.Conflict,
            HttpStatusCode.ServiceUnavailable,
            HttpStatusCode.GatewayTimeout,
            HttpStatusCode.Unauthorized,
            HttpStatusCode.NonAuthoritativeInformation -> {
                NetworkResult.Error(
                    code = code,
                    message = getMsgMap[code] ?: "Unknown error",
                    exception = Exception("")
                )
            }

            else -> NetworkResult.Error(
                code = code,
                message = "未知错误",
                exception = Exception("")
            )
        }

    } catch (e: Exception) {
        return NetworkResult.Error(
            message = e.message ?: "Unknown error",
            exception = e
        )
    }
}

fun codeToDataState(code: HttpStatusCode?): DataState {
    return when(code) {
        HttpStatusCode.Unauthorized -> DataState.Unauthorized
        HttpStatusCode.NonAuthoritativeInformation -> DataState.Expired
        HttpStatusCode.Locked -> DataState.Error("账号被锁定")
        HttpStatusCode.ServiceUnavailable -> DataState.Error("教务系统超时")
        HttpStatusCode.GatewayTimeout -> DataState.Error("请求超时")
        else -> DataState.Error("未知错误")
    }
}

suspend fun safeApiCallsSequential(
    calls: List<suspend () -> DataState>
): List<DataState> = coroutineScope {
    val deferredResults = calls.map { call ->
        async {
            var count = 0
            while (count < SystemGlobalConfig.MAX_RETRY_TIMES) {
                val dataState = call()
                when (dataState) {
                    is DataState.Expired -> {
                        count++
                        delay(SystemGlobalConfig.RETRY_INTERVAL)
                    }

                    is DataState.Unauthorized -> {
                        TokenState.expired()
                        return@async dataState
                    }

                    else -> {
                        return@async dataState
                    }
                }
            }

            DataState.Error("请求超时")
        }
    }

    deferredResults.awaitAll()
}

fun checkResults(results: List<DataState>): Boolean {
    for(result in results) {
        if(result != DataState.Newest) return false
    }

    return true
}

object TokenState {
    private val _isExpired = MutableStateFlow(false)
    val isExpired = _isExpired.asStateFlow()

    fun expired() {
        _isExpired.value = true
    }

    fun refreshed() {
        _isExpired.value = false
    }
}

