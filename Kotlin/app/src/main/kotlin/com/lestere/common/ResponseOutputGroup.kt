package com.lestere.common

import com.lestere.model.IllegalResponseOutputParameterException
import com.lestere.utils.findAllInstances

sealed class ResponseOutputGroup {
    val name: String = this::class.simpleName ?: throw RuntimeException("Unknown class ${this::class.qualifiedName}")

    protected val type: ResponseOutputGroup by lazy { this }

    companion object {
        private val implementions by lazy { ResponseOutputGroup::class.findAllInstances() }

        @Throws(IllegalResponseOutputParameterException::class)
        fun parse(value: String): ResponseOutputGroup = implementions.find {
            it.name.lowercase().split("[.$]".toRegex()).last() == value.lowercase().trim()
        } ?: throw IllegalResponseOutputParameterException(value)
    }

    data object General : ResponseOutputGroup()
}
