package com.lestere.ksp.currying

import com.google.devtools.ksp.symbol.Modifier

/**
 * Base exception class for errors encountered during currying annotation processing.
 * Extend this class to create specific exceptions with formatted error messages.
 * Each subclass represents a distinct type of currying-related error.
 */
internal abstract class CurryingExceptions(override val message: String) : Exception() {

    /**
     * Exception thrown when a function annotated with @Currying contains unsupported modifiers.
     * @property modifiers List of unsupported modifiers applied to the function.
     */
    class UnsupportedModifierWithFunction(modifiers: List<Modifier>) : CurryingExceptions("") {
        private val modifierStr = modifiers.joinToString(", ") { it.name }
        override val message: String = "Currying annotation is not supported to be marked with modifiers: $modifierStr"
    }

    /**
     * Exception thrown when a function annotated with @Currying has fewer than two parameters.
     * Currying requires at least two parameters to be meaningful.
     * @property qualifiedName The fully qualified name of the invalid function.
     */
    class LessThanTwoParametersFunction(qualifiedName: String) :
        CurryingExceptions("Function $qualifiedName is less than two parameters")

    /**
     * Exception thrown when the return type of curried function cannot be resolved.
     * This typically indicates a type resolution issue during KSP processing.
     * @property qualifiedName The fully qualified name of the function with unresolved return type.
     */
    class UnresolvedFunctionReturnType(qualifiedName: String) :
        CurryingExceptions("$qualifiedName could not be curried, failed when resolving function return type.")

    /**
     * Exception thrown when the origin package of curried function cannot be resolved.
     * This typically indicates a package name resolution issue during KSP processing.
     *
     * @property simpleName The simple name of the function with unresolved origin package.
     */
    class UnresolvedFunctionOriginPackage(simpleName: String) :
        CurryingExceptions("$simpleName could not be resolved from an origin")

    /**
     * Exception thrown when the target function is a class constructor.
     * This typically indicates the class constructor resolution issue during KSP processing.
     *
     * @property target The target of the constructor with unresolved class name.
     */
    class UnsupportedConstructor(target: String) :
        CurryingExceptions("Constructor for $target is not support yet.")
}
