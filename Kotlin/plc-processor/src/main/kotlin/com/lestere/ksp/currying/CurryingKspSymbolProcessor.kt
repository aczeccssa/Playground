package com.lestere.ksp.currying

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate

/**
 * Currying annotation processor
 * @author LesterE (159.759dcsvdu@gmail.com)
 * @since 2025-06-06
 */
internal class CurryingKspSymbolProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Currying::class.qualifiedName!!)
        val unparsed = mutableListOf<KSAnnotated>()
        symbols.toList().forEach { ksAnnotated ->
            if (!ksAnnotated.validate())
                unparsed.add(ksAnnotated)
            else
                ksAnnotated.accept(CurryingKspVisitor(environment), Unit) // Process symbol
        }
        // Return symbols that cannot be parsed
        return unparsed
    }
}