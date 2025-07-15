package com.lestere.types

import com.lestere.model.HttpRequestJsonRowBodySerializer
import com.lestere.model.HttpRequestMultiPartFormDataRowBodySerializer
import com.lestere.model.HttpRequestNoneBodySerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.io.File
import java.nio.file.Paths

abstract class HttpRequestRowBody<T>(open val contentType: HttpRequestRowBodyType, open val row: T) {
    override fun toString(): String = "${this::class.simpleName}(contentType=\"$contentType\",row=$row)"
}

@Serializable
enum class HttpRequestRowBodyType {
    MultiPartFormData, Json, None;
}

@Serializable(HttpRequestMultiPartFormDataRowBodySerializer::class)
class HttpRequestMultiPartFormDataRowBody(row: List<String>) :
    HttpRequestRowBody<List<String>>(HttpRequestRowBodyType.MultiPartFormData, row) {
    val validatableFileList: List<File> = row.asSequence()
        .mapNotNull { runCatching { Paths.get(it).toFile() }.getOrNull() }
        .filter { it.exists() && it.canRead() }
        .toList()
}

@Serializable(HttpRequestJsonRowBodySerializer::class)
class HttpRequestJsonRowBody(row: JsonElement) : HttpRequestRowBody<JsonElement>(HttpRequestRowBodyType.Json, row)

@Serializable(HttpRequestNoneBodySerializer::class)
class HttpRequestNoneBody : HttpRequestRowBody<Unit>(HttpRequestRowBodyType.None, Unit)
