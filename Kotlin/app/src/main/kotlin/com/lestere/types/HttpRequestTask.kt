package com.lestere.types

import com.lestere.common.ResponseOutputMode
import com.lestere.ksp.currying.Currying
import com.lestere.ksp.currying.CurryingFormat
import com.lestere.model.*
import com.lestere.utils.encodeURLComponent
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.io.File

typealias HttpMultipartFormDataTask = HttpRequestTask<HttpRequestMultiPartFormDataRowBody>

typealias HttpJsonTask = HttpRequestTask<HttpRequestJsonRowBody>

typealias HttpNoneBodyTask = HttpRequestTask<HttpRequestNoneBody>

class HttpRequestTaskBuilder(val builder: HttpRequestBuilder, val task: HttpRequestTask<*>)

@Serializable
data class HttpRequestTask<T : HttpRequestRowBody<*>>
@Currying(format = CurryingFormat.TYPEALIAS)
constructor(
    val label: String,
    val url: String,
    @Serializable(HttpMethodAsStringSerializer::class)
    val method: HttpMethod,
    @Serializable(ResponseOutputModeSerializer::class)
    val mode: ResponseOutputMode = ResponseOutputMode.Any,
    val pathParameters: Map<String, String> = emptyMap(),
    val queryParameters: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val authorization: AuthorizationParameter? = null,
    val body: T? = null,
) {
    val fullpathUrl: Url
        get() {
            var baseUrl = url
            pathParameters.forEach { (key, value) ->
                baseUrl = baseUrl.replace("{$key}", value.encodeURLComponent())
            }
            val queryString = queryParameters.entries.joinToString("&") { (key, value) ->
                "${key.encodeURLComponent()}=${value.encodeURLComponent()}"
            }
            val urlString = if (queryString.isNotEmpty()) "$baseUrl?$queryString" else baseUrl
            return Url(urlString)
        }

    suspend fun prepareRequest(client: HttpClient, builder: HttpRequestTaskBuilder.() -> Unit = {}): HttpStatement =
        client.prepareRequest(fullpathUrl) {
            onDownload(HttpProgressListener.DownloadCommandLineOutputListener(label))
            onUpload(HttpProgressListener.UploadCommandLineOutputListener(label))

            // Set request method and headers
            method = this@HttpRequestTask.method
            this@HttpRequestTask.headers.forEach(::header)
            // Set authorization
            when (authorization?.type) {
                AuthorizationParameterType.Bearer -> this.bearerAuth(authorization.key)
                // TODO: OAuth, JWT
                else -> {}
            }
            // Set request body
            when (val body = this@HttpRequestTask.body) {
                is HttpRequestMultiPartFormDataRowBody -> setBody(MultiPartFormDataContent(formData {
                    body.validatableFileList.forEach { file ->
                        append(file.name, file.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, contentTypeForFile(file))
                            append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        })
                    }
                }, boundary = "WebAppBoundary"))

                is HttpRequestJsonRowBody -> setBody(body.row)
                else -> {}
            }

            // Enable caller custom request
            HttpRequestTaskBuilder(this, this@HttpRequestTask).builder()
        }

    private fun contentTypeForFile(file: File) = when (file.extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "json" -> "application/json"
        "txt" -> "text/plain"
        else -> "application/octet-stream"
    }
}

@Serializable
enum class AuthorizationParameterType {
    Bearer, OAuth;
}

@Serializable
data class AuthorizationParameter(val type: AuthorizationParameterType, val key: String)
