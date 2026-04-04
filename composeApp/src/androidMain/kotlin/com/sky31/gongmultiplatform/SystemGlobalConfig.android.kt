package com.sky31.gongmultiplatform

actual object SystemGlobalConfig {
    actual val HOST: String = BuildConfig.SYSTEM_HOST
    actual val UPDATE_HOST: String = BuildConfig.SYSTEM_UPDATE_HOST
    actual val WEB_HOST: String = BuildConfig.SYSTEM_WEB_HOST
    actual val MAX_RETRY_TIMES: Int = BuildConfig.SYSTEM_MAX_RETRY_TIMES
    actual val RETRY_INTERVAL: Long = BuildConfig.SYSTEM_RETRY_INTERVAL
}