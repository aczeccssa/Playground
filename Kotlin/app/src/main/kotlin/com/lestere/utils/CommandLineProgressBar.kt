package com.lestere.utils

import com.lestere.ConsoleUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

class CommandLineProgressBar(
    val name: String,
    target: Long,
) {
    companion object {
        private const val CS = "■" // Leading symbol
        private const val AS = "▶" // Arrow symbol
        private const val TS = "·" // Trailing symbol
        private const val GAP = 20L // Watch render change gap millisecond
    }

    enum class CommandLineColor(val value: String) {
        Red("\u001B[91m"),
        Green("\u001B[92m"),
        Yellow("\u001B[93m"),
        Blue("\u001B[94m"),
        Purple("\u001B[95m"),
        Reset("\u001B[0m");
    }

    private var _target = AtomicLong(target)
    val target get() = _target.get()

    private val currency = AtomicLong(0L)
    private val startTime = System.currentTimeMillis()
    private val duration get() = System.currentTimeMillis() - startTime

    fun update(current: Long, target: Long? = null) {
        if (current > currency.get()) {
            currency.set(min(current, target ?: Long.MAX_VALUE))
        }
        target?.takeIf { it != _target.get() }?.let {
            _target.set(it)
        }
    }

    fun updateTarget(newVal: Long) {
        _target.set(newVal)
        redraw()
    }

    private fun redraw() {
        val maxWidth = ConsoleUtils.getConsoleWidth()
        val bar = progressBar(maxWidth)
        val endl = if (currency.get() == target) "\n" else ""
        print("\r$bar$endl")
    }

    private fun progressBar(max: Int): String {
        val statusList = listOf("|", "/", "-", "\\")
        val percentage = if (target == 0L) 0.0 else currency.get().toDouble() / target
        val clampedPercentage = min(1.0, percentage)
        val filledWidth = ((max - 12) * clampedPercentage).toInt().coerceIn(0, max - 12)
        val lead = CS.repeat(filledWidth)
        val arrow = if (filledWidth < max - 12 && currency.get() < target) AS else ""
        val trail = TS.repeat(max - 12 - filledWidth)
        val status = if (currency.get() >= target) "√" else statusList[(System.currentTimeMillis() / 200 % 4).toInt()]
        val percentText = "${(clampedPercentage * 100).toInt()}%"
        return "$name: [$lead$arrow$trail] [$status] $percentText using ${duration / 1000}s [${currency.get()}/$target]".trim()
    }

    private fun startDraw() {
        CoroutineScope(Dispatchers.IO).launch {
            while (currency.get() < target) {
                redraw()
                delay(GAP)
            }
            redraw() // 确保最终状态正确显示
        }
    }

    init {
        startDraw()
    }
}