package com.sky31.gongmultiplatform

expect object SystemGlobalConfig {
    val HOST: String
    val UPDATE_HOST: String
    val WEB_HOST: String
    val MAX_RETRY_TIMES: Int
    val RETRY_INTERVAL: Long
}