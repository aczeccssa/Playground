package com.lestere.ksp.utils

import com.google.devtools.ksp.symbol.KSAnnotation
import com.lestere.ksp.currying.Currying
import java.io.OutputStream
import kotlin.reflect.KClass

internal fun OutputStream.writeText(text: String) = write(text.toByteArray())

internal fun String.capitalizeFirstChar() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}

internal fun String.loadTemplate(placeholder: String, value: String) = replace("{{$placeholder}}", value)

internal fun Sequence<KSAnnotation>.filterAnnotations(type: KClass<*>) = filter {
    type.simpleName?.let { target -> it.shortName.asString().endsWith(target) } ?: false
}