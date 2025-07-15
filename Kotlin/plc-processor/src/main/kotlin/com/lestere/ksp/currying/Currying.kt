package com.lestere.ksp.currying

/**
 * Annotation for functions or constructors to generate curried implementations.
 * Currying transforms a multi-parameter function into a sequence of nested single-parameter functions,
 * allowing partial application of arguments. For example:
 * ```kotlin
 * @Currying
 * fun add(x: Int, y: Int): Int = x + y
 *
 * // Generated curried function:
 * fun KFunction2<Int, Int, Int>.currying(x: Int): (Int) -> Int = { y -> this.invoke(x, y) }
 * ```
 *
 * **Key Features**:
 * - Supports functions and constructors with 2+ parameters.
 * - Generates extension functions for `KFunctionN` types (e.g., `KFunction2`, `KFunction3`).
 * - Allows specifying a custom `packageName` to override the default package resolution from the source file.
 * - Preserves type information and generics in the generated code.
 *
 * **Usage Notes**:
 * - Applied at the source level (retention policy `SOURCE`), so it does not affect compiled bytecode.
 * - The generated code is output as Kotlin files in the specified or inferred package.
 * - Functions with fewer than 2 parameters will produce a warning and skip generation.
 *
 * @param packageName Custom package name for the generated code (optional, defaults to the source file's package)
 * @param format Format for generated kotlin code (optional, default is [CurryingFormat.QUALIFIED])
 * @author LesterE (159.759dcsvdu@gmail.com)
 * @since 2025-06-06
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Currying(val packageName: String = "", val format: CurryingFormat = CurryingFormat.QUALIFIED)
