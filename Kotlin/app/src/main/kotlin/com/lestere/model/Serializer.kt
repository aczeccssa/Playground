package com.lestere.model

import com.lestere.common.ResponseOutputGroup
import com.lestere.common.ResponseOutputMode
import com.lestere.types.HttpRequestJsonRowBody
import com.lestere.types.HttpRequestMultiPartFormDataRowBody
import com.lestere.types.HttpRequestNoneBody
import com.lestere.types.HttpRequestRowBodyType
import io.ktor.http.HttpMethod
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.JsonElement

object HttpMethodAsStringSerializer : KSerializer<HttpMethod> {
    // Serial names of descriptors should be unique, this is why we advise including app package in the name.
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("io.ktor.http.HttpMethod", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: HttpMethod) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): HttpMethod {
        val string = decoder.decodeString()
        return HttpMethod.parse(string)
    }
}

object ResponseOutputModeSerializer : KSerializer<ResponseOutputMode> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.lestere.common.ResponseOutputMode") {
        element<String>("group")
        element<String>("type")
    }

    override fun serialize(encoder: Encoder, value: ResponseOutputMode) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.group.name)
            encodeStringElement(descriptor, 1, value.type)
        }
    }

    override fun deserialize(decoder: Decoder): ResponseOutputMode {
        return decoder.decodeStructure(descriptor) {
            var group: ResponseOutputGroup? = null
            var type: String? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> group = ResponseOutputGroup.parse(decodeStringElement(descriptor, 0))
                    1 -> type = decodeStringElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            // Validate and match instance
            requireNotNull(group) { "group cannot be null" }
            requireNotNull(type) { "type cannot be null" }

            val result = ResponseOutputMode.parse(type)
            require(result.group == group) { "Resolved group not matched" }
            result
        }
    }
}

object HttpRequestMultiPartFormDataRowBodySerializer : KSerializer<HttpRequestMultiPartFormDataRowBody> {

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.lestere.types.MultiPartFormDataHttpRequestRowBody") {
            element<HttpRequestNoneBody>("contentType")
            element<List<String>>("row")
        }

    override fun serialize(encoder: Encoder, value: HttpRequestMultiPartFormDataRowBody) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.contentType.name)
            encodeSerializableElement(descriptor, 1, ListSerializer(String.serializer()), value.row)
        }
    }

    override fun deserialize(decoder: Decoder): HttpRequestMultiPartFormDataRowBody {
        return decoder.decodeStructure(descriptor) {
            var contentType: HttpRequestNoneBody? = null
            var row: List<String>? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> contentType = decodeSerializableElement(descriptor, 0, HttpRequestNoneBody.serializer())
                    1 -> row = decodeSerializableElement(descriptor, 1, ListSerializer(String.serializer()))
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            // Validate and create instance
            requireNotNull(contentType) { "contentType cannot be null" }
            requireNotNull(row) { "content cannot be null" }

            HttpRequestMultiPartFormDataRowBody(row)
        }
    }
}

object HttpRequestJsonRowBodySerializer : KSerializer<HttpRequestJsonRowBody> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.lestere.types.HttpRequestJsonRowBodySerializer") {
            element<HttpRequestRowBodyType>("contentType")
            element<JsonElement>("row")
        }

    override fun serialize(encoder: Encoder, value: HttpRequestJsonRowBody) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.contentType.name)
            encodeSerializableElement(descriptor, 1, JsonElement.serializer(), value.row)
        }
    }

    override fun deserialize(decoder: Decoder): HttpRequestJsonRowBody {
        return decoder.decodeStructure(descriptor) {
            var contentType: HttpRequestRowBodyType? = null
            var row: JsonElement? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> contentType = decodeSerializableElement(descriptor, 0, HttpRequestRowBodyType.serializer())
                    1 -> row = decodeSerializableElement(descriptor, 1, JsonElement.serializer())
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            // Validate and create instance
            requireNotNull(contentType) { "contentType cannot be null" }
            requireNotNull(row) { "content cannot be null" }

            HttpRequestJsonRowBody(row)
        }
    }

}

@Suppress("EXTERNAL_SERIALIZER_USELESS")
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = HttpRequestNoneBody::class)
object HttpRequestNoneBodySerializer : KSerializer<HttpRequestNoneBody> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.lestere.types.HttpRequestJsonRowBodySerializer") {
            element<HttpRequestRowBodyType>("contentType")
            element<Unit>("row")
        }

    override fun serialize(encoder: Encoder, value: HttpRequestNoneBody) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.contentType.name)
            encodeNullableSerializableElement(descriptor, 1, JsonElement.serializer(), null)
        }
    }

    override fun deserialize(decoder: Decoder): HttpRequestNoneBody = HttpRequestNoneBody()

}
