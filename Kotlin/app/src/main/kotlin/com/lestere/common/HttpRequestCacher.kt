package com.lestere.common

import com.lestere.model.RequestInfo
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.serialization.json.Json
import java.io.IOException
import java.lang.RuntimeException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.text.toByteArray

object HttpRequestCacher {
    private val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val directory: Path = Paths.get(".cache")

    init {
        runCatching {
            if (directory.exists() and directory.isDirectory()) return@runCatching
            directory.createDirectory()
        }.onFailure { error ->
            throw RuntimeException(error)
        }
    }

    @Throws(IOException::class)
    private fun generateNewDirPath(): Path = run {
        val id = UUID.randomUUID().toString().replace("-", "").lowercase()
        val dirPath = directory.resolve(id)
        dirPath.createDirectory()
        dirPath
    }

    @Throws(IOException::class)
    suspend fun cacheBinary(requestInfo: RequestInfo, channel: ByteReadChannel, extensionName: String): Path = run {
        val dirPath = generateNewDirPath()
        // Build full file path
        val responseFullPath = dirPath.resolve("response.${extensionName}")
        val requestInfoFullPath = dirPath.resolve("request.json")

        // Write to cache file
        val writeChannel = responseFullPath.toFile().writeChannel()
        channel.copyAndClose(writeChannel)
        val requestInfoText = json.encodeToString(requestInfo)
        Files.writeString(requestInfoFullPath, requestInfoText)

        // Return response pathname
        responseFullPath
    }

    @Throws(IOException::class)
    suspend fun cacheText(requestInfo: RequestInfo, response: String, extensionName: String): Path = run {
        val channel = response.toByteArray()
            .inputStream()
            .toByteReadChannel(Dispatchers.Unconfined + Job(), KtorDefaultPool)
        cacheBinary(requestInfo, channel, extensionName)
    }
}