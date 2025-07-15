package com.lestere.ksp

import com.lestere.common.ResponseOutputGroup
import com.lestere.common.ResponseOutputMode
import com.lestere.ksp.currying.Currying
import com.lestere.ksp.currying.CurryingFormat
import com.lestere.specific.target.test_ksp_compiler_plugin_curry.currying

/**
 * Test for Currying annotation ksp symbol processor compiler plugin
 * @author LesterE (159.759dcsvdu@gmail.com)
 * @since 2025-06-07
 */
object TestKspCompilerPluginCurry {
    @Currying("com.lestere.specific.target", CurryingFormat.QUALIFIED)
    private fun f(x: Int, y: Int, z: Int): Int = x + y + z

    // Functional
    @JvmStatic
    fun main(args: Array<String>) {
        // Mark: Simple type
        val result = ::f.currying(10)(20)(30)
        println("Currying calculate result ==> $result")

        // Mark: Type with generic and imported package
        val isThereSameGroupWithMode = ::isThereSameGroup.currying(ResponseOutputMode.Text.Json)
        val isThereSameGroupWithGroup = isThereSameGroupWithMode(ResponseOutputMode.Text)
        val isThereSameGroupOnSuccess = isThereSameGroupWithGroup {
            println("These mode are matched!!!")
        }
        isThereSameGroupOnSuccess {
            println("Their not match TUT")
        }
    }
}

@Currying(format = CurryingFormat.TYPEALIAS)
private fun isThereSameGroup(
    mode: ResponseOutputMode,
    group: ResponseOutputGroup,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
): Unit = if (mode.group == group) onSuccess() else onFailure()
