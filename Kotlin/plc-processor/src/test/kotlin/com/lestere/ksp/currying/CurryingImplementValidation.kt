package com.lestere.ksp.currying

import kotlin.random.Random
import kotlin.random.nextUInt

/**
 * Validation currying function implement for kotlin
 * @Created by LesterE<159.759dcsvdu@gmail.com> at 6/6/25 2:04 PM
 */
object CurryingImplementValidation {

    @Currying(packageName = "com.lestere.ksp.currying.test")
    private fun f(x: Int, y: Int, z: Int) = x + y + z

    private fun g(x: Int): (y: Int) -> ((z: Int) -> Int) = { y ->
        { z ->
            f(x, y, z)
        }
    }

    private fun randomPInt(): Int = Random.nextUInt().toInt()

    @JvmStatic
    fun main(args: Array<String>) {
        ::f.parameters.forEach(::println)
        val dataset = mutableListOf<Triple<Int, Int, Int>>()
        repeat(10) {
            dataset.add(Triple(randomPInt(), randomPInt(), randomPInt()))
        }
        dataset.forEach { (x, y, z) ->
            val f = f(x, y, z)
            val g = g(x)(y)(z)
            println("$x + $y + $z ==> $f, validate ${f == g}")
        }
    }
}
