package com.lestere.model

import com.lestere.common.ResponseOutputMode
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import java.lang.Exception
import java.lang.IllegalArgumentException

class ResponseStatusException(info: RequestInfo, response: HttpResponse) :
    ResponseException(response, "Response failed cause status ${info.responseStatus} in ${info.duration}ms.")

class IllegalResponseOutputParameterException(inputString: String) : IllegalArgumentException() {
    override val message: String = "Unresolved parameter value $inputString is illegal."
}

class UnsupportedResponseOutputMode(mode: ResponseOutputMode) : Exception() {
    override val message: String = "Unsupported response output mode: ${mode.type}"
}
