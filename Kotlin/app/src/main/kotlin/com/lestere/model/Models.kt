package com.lestere.model

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class RequestInfo(
    val url: String,
    val responseStatus: Int,
    val requestTime: Long,
    val responseTime: Long,
    val duration: Long,
) {
    internal constructor(url: String, statusCode: HttpStatusCode, requestTime: Long, responseTime: Long) :
            this(url, statusCode.value, requestTime, responseTime, responseTime - requestTime)
}

@Serializable
data class TypescriptOauthImplAuthBody(val username: String, val password: String)

@Serializable
data class TypescriptOauthImplAuthResponseToken(val token: String)

@Serializable
data class TypescriptOauthImplChatSocketMessage(val userId: String, val username: String, val message: String)

@Serializable
data class TypescriptOauthImplUser(val id: String, val username: String, val token: String)
