package com.lestere.ksp.currying

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Currying annotation processor provider
 * @author LesterE (159.759dcsvdu@gmail.com)
 * @since 2025-06-06
 */
internal class CurryingKspSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = CurryingKspSymbolProcessor(environment)
}