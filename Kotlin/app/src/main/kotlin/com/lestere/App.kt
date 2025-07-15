package com.lestere

import com.lestere.common.HttpRequestCacher
import com.lestere.common.ResponseOutputGroup
import com.lestere.common.ResponseOutputMode
import com.lestere.model.*
import com.lestere.types.HttpJsonTask
import com.lestere.types.HttpNoneBodyTask
import com.lestere.types.HttpRequestJsonRowBody
import com.lestere.utils.formatText
import com.lestere.utils.requestInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import kotlin.io.path.absolutePathString

fun main(args: Array<String>): Unit = runBlocking(Dispatchers.IO) {
    val json = Json {
        isLenient = true // Enable JSON content without quotes
        prettyPrint = true // Formatted JSON output
        ignoreUnknownKeys = true // Ignore unknown keys
    }
    val client = HttpClient(CIO) {
        install(WebSockets) {
            pingIntervalMillis = 15_000
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpRequestRetry) {
            maxRetries = 3
            retryIf { request, response ->
                !request.headers["authorization"].isNullOrEmpty() and (400..499).contains(response.status.value)
            }
            modifyRequest { request -> request.header("retry", true) }
            delayMillis { it * 1000L }
        }

        defaultRequest {
            port = 443
            host = "https://frontend.lifemark-next.orb.local"
            contentType(ContentType.Application.Json)
        }
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                }
            }
        }

        BrowserUserAgent()
        HttpResponseValidator {
            validateResponse { response ->
                val info = response.requestInfo
                if (!(100..300).contains(info.responseStatus)) throw ResponseStatusException(info, response)
            }
            handleResponseExceptionWithRequest { _, _ -> /* Doesn't matter */ }
        }
    }

    /**
     * Http request simples
     * ---
     *
     * | **ResponseType** | **URL**                                                                                                   |
     * | :--------------- | :---------------------------------------------------------------------------------------------------------|
     * | Jpeg             | [https://frontend.lifemark-next.orb.local/api/media/images/5afc709b-b114-4038-af50-45ae8aa2ceb7?size=large](#) |
     * | Json             | [https://api.openf1.org/v1/drivers?driver_number=1](#)                                                         |
     * | Xls              | [https://download.samplelib.com/xls/sample-heavy-1.xls](#)                                                     |
     *
     * ---
     */
    val meta = args.getOrNull(0)?.startsWith("--task-execution") ?: false
    if (meta) {
        val task = HttpNoneBodyTask(
            label = "Max Verstappen in F1",
            url = "https://api.openf1.org/v1/drivers",
            method = HttpMethod.Get,
            mode = ResponseOutputMode.Text.Json,
            queryParameters = mapOf(Pair("driver_number", "1"))
        )
        val statement = task.prepareRequest(client)

        runCatching {
            val response = statement.execute()
            when (task.mode.group) {
                ResponseOutputMode.Text -> HttpRequestCacher
                    .cacheText(response.requestInfo, json.formatText(response.bodyAsText()), task.mode.type)

                ResponseOutputMode.Binary -> HttpRequestCacher
                    .cacheBinary(response.requestInfo, response.bodyAsChannel(), task.mode.type)

                ResponseOutputGroup.General -> HttpRequestCacher
                    .cacheBinary(response.requestInfo, response.bodyAsChannel(), "\\*")
            }
        }.onSuccess { path ->
            println("Response text already written to path: ${path.absolutePathString()}")
        }.onFailure { error ->
            println("Send request to ${task.fullpathUrl} failed: ${error.localizedMessage}")
        }
    }

    runCatching {
        val authServerHost = "localhost:4000"
        val businessServerHost = "localhost:3000"

        // Get user auth token
        val auth = TypescriptOauthImplAuthBody("lestere", "123456")
        val authTokenTaskJsonRowBody = json.encodeToJsonElement(TypescriptOauthImplAuthBody.serializer(), auth)
        val authTokenTask = HttpJsonTask(
            label = "Login in to get auth token",
            url = "https://$authServerHost/login",
            method = HttpMethod.Post,
            mode = ResponseOutputMode.Text.Json,
            body = HttpRequestJsonRowBody(authTokenTaskJsonRowBody)
        )
        val authToken = authTokenTask.prepareRequest(client)
            .execute()
            .body<TypescriptOauthImplAuthResponseToken>()

        // Get user information
        val userInfoTask = HttpNoneBodyTask(
            label = "Get user info",
            url = "https://$authServerHost/info",
            method = HttpMethod.Get,
            mode = ResponseOutputMode.Text.Json,
            headers = mapOf(Pair("authorization", authToken.token))
        )
        val user = userInfoTask.prepareRequest(client)
            .execute()
            .body<TypescriptOauthImplUser>()

        // Websocket connection session
        val websocketUrl = "wss://$businessServerHost/chat?authorization=${user.token}"
        val session = client.webSocketSession(websocketUrl)

        // Websocket active signal
        var socketActive = isActive

        // Websocket automatically serialization convert
        val messageFlow = MutableSharedFlow<TypescriptOauthImplChatSocketMessage>()

        // Process websocket frame
        @OptIn(ExperimentalSerializationApi::class)
        suspend fun frameProcessor(frame: Frame) {
            val message = runCatching {
                when (frame) {
                    is Frame.Close -> {
                        socketActive = false
                        println("Connection lost cause ${frame.readReason()?.message ?: "No reason"}")
                    }

                    is Frame.Binary -> {
                        val input = frame.readBytes().inputStream()
                        json.decodeFromStream<TypescriptOauthImplChatSocketMessage>(input)
                    }

                    is Frame.Text -> json.decodeFromString<TypescriptOauthImplChatSocketMessage>(frame.readText())
                    else -> Unit
                }
            }.getOrDefault(Unit)
            if (message is TypescriptOauthImplChatSocketMessage) {
                session.sendSerialized(message)
            }
        }

        // Websocket close frame watcher
        val receiveJob = launch {
            session.incoming.receiveAsFlow().collect(::frameProcessor)
        }
        // Job for message collect
        val messageJob = launch {
            messageFlow.collect { message ->
                println("${message.username}(${message.userId}): ${message.message}")
            }
        }

        // Commandline operation
        while (socketActive) {
            print("Enter new message: ")
            val message = readlnOrNull()
            if (message === null) {
                continue
            } else if (arrayOf("quit", "exit").contains(message.trim())) {
                socketActive = false
            } else {
                session.sendSerialized(TypescriptOauthImplChatSocketMessage(user.id, user.username, message))
            }
        }

        // Close message collect and websocket connection
        receiveJob.cancel()
        messageJob.cancel()
        session.close(CloseReason(CloseReason.Codes.NORMAL, "Just wanna close connection"))
    }.onSuccess {
        println("Websocket connection flow work good!")
    }.onFailure { error ->
        println("Websocket connection flow failed cause: $error")
    }
}
