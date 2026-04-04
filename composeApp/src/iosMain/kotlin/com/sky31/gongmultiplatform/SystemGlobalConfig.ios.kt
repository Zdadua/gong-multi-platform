package com.sky31.gongmultiplatform

import platform.Foundation.NSProcessInfo

actual object SystemGlobalConfig {
    private fun requiredEnv(name: String): String {
        return NSProcessInfo.processInfo.environment[name] as? String
            ?: error("Missing required environment variable: $name")
    }

    actual val HOST: String = requiredEnv("SYSTEM_HOST")
    actual val UPDATE_HOST: String = requiredEnv("SYSTEM_UPDATE_HOST")
    actual val WEB_HOST: String = requiredEnv("SYSTEM_WEB_HOST")
    actual val MAX_RETRY_TIMES: Int = requiredEnv("SYSTEM_MAX_RETRY_TIMES").toInt()
    actual val RETRY_INTERVAL: Long = requiredEnv("SYSTEM_RETRY_INTERVAL").toLong()
}