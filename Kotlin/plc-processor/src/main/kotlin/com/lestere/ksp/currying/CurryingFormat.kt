package com.lestere.ksp.currying

/**
 * Specifies the formatting strategy for generated curried functions.
 * Determines how the curried function signatures and type references are represented in the generated code.
 *
 * @property QUALIFIED Generates fully qualified type names and function signatures.
 * @property TYPEALIAS Uses type aliases to simplify complex function types, improving readability.
 */
enum class CurryingFormat {
    /**
     * Generates curried functions using fully qualified type names.
     * Example output:
     * ```kotlin
     * fun kotlin.reflect.KFunction2<Int, String, Unit>.currying(): (Int) -> (String) -> Unit = ...
     * ```
     *
     * **Use Cases**:
     * - Projects requiring explicit type references for clarity.
     * - Avoiding naming conflicts in large codebases.
     * - Interop with Java code that relies on fully qualified names.
     */
    QUALIFIED,

    /**
     * Generates curried functions using type aliases for complex function types.
     * Example output:
     * ```kotlin
     * private typealias MyFunctionType = (Int, String) -> Unit
     * private typealias MyCurriedType = (Int) -> (String) -> Unit
     *
     * fun MyFunctionType.currying(): MyCurriedType = ...
     * ```
     *
     * **Use Cases**:
     * - Improving code readability for deeply nested function types.
     * - Reducing verbosity in generated code.
     * - Projects with strict line length or style constraints.
     */
    TYPEALIAS;
}