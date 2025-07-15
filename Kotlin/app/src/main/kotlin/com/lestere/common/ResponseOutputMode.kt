package com.lestere.common

import com.lestere.model.IllegalResponseOutputParameterException
import com.lestere.utils.findAllNestedInstances

/**
 * The mode for http request response than record writter.
 *
 *  @param group [ResponseOutputGroup] The content stream type of response data
 *  @param type [String] The content type of response data
 */
sealed class ResponseOutputMode private constructor(val group: ResponseOutputGroup, val type: String) {

    /**
     * Quuick init, group is `General`
     *
     * @param type [String] The content type of response data
     */
    private constructor(type: String) : this(ResponseOutputGroup.General, type)

    override fun toString(): String = "${this::class.qualifiedName}(${group.name}:$type)"

    /**
     * Unspecific type, no specific is general group
     */
    object Any : ResponseOutputMode("*")

    companion object {
        private val implementations: List<ResponseOutputMode> by lazy { ResponseOutputMode::class.findAllNestedInstances() }

        /**
         * Parses a string representing into a [ResponseOutputMode] instance.
         *
         * @param value [String] Matched type string
         */
        @Throws(IllegalResponseOutputParameterException::class)
        fun parse(value: String): ResponseOutputMode = implementations.find {
            it.type.lowercase() == value.lowercase()
        } ?: throw IllegalResponseOutputParameterException(value)

        @JvmStatic
        fun main(args: Array<String>) {
            println(parse("json"))
        }
    }

    data object Text : ResponseOutputGroup() {

        object Txt : ResponseOutputMode(type, "txt")
        object Json : ResponseOutputMode(type, "json")
        object Javascript : ResponseOutputMode(type, "javascript")
    }

    data object Binary : ResponseOutputGroup() {

        object Jpeg : ResponseOutputMode(type, "jpeg")
        object Png : ResponseOutputMode(type, "png")
        object Docx : ResponseOutputMode(type, "docx")
        object Xls : ResponseOutputMode(type, "xls")
    }
}
