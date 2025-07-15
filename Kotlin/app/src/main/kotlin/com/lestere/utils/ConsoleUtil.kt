package com.lestere

import com.sun.jna.Native
import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.win32.StdCallLibrary
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 控制台工具类，提供跨平台的控制台操作功能
 */
object ConsoleUtils {
    /**
     * 获取控制台宽度
     * @return 控制台宽度，如果获取失败则返回默认值80
     */
    fun getConsoleWidth(): Int {
        return when {
            isWindows() -> getConsoleWidthWindows()
            isLinuxOrMac() -> getConsoleWidthUnix()
            else -> 80 // 默认值，适用于无法检测的平台
        }
    }

    private fun isWindows(): Boolean {
        return System.getProperty("os.name").lowercase().contains("win")
    }

    private fun isLinuxOrMac(): Boolean {
        val os = System.getProperty("os.name").lowercase()
        return os.contains("nix") || os.contains("nux") || os.contains("mac")
    }

    private fun getConsoleWidthWindows(): Int {
        return try {
            val handle = WinKernel32.INSTANCE.getStdHandle(-11) // STD_OUTPUT_HANDLE
            val info = ConsoleScreenBufferInfo()
            WinKernel32.INSTANCE.getConsoleScreenBufferInfo(handle, info)
            info.srWindow.right - info.srWindow.left + 1
        } catch (e: Exception) {
            println("获取 Windows 控制台宽度失败: ${e.message}")
            80 // 默认宽度
        }
    }

    private fun getConsoleWidthUnix(): Int {
        return try {
            // 尝试使用stty命令获取控制台大小
            val process = ProcessBuilder("stty", "size").redirectErrorStream(true).start()
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                val line = reader.readLine() ?: return 80
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size >= 2) parts[1].toInt() else 80
            }
        } catch (e: Exception) {
            // 尝试使用tput命令获取控制台宽度
            try {
                val process = ProcessBuilder("tput", "cols").redirectErrorStream(true).start()
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    val line = reader.readLine() ?: return 80
                    line.toIntOrNull() ?: 80
                }
            } catch (e: Exception) {
                println("获取 Unix 控制台宽度失败: ${e.message}")
                80 // 默认宽度
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val width = getConsoleWidth()
        println("Command line width: $width symbols")
    }
}

// Windows 平台相关接口和结构体
private interface WinKernel32 : StdCallLibrary {
    companion object {
        val INSTANCE: WinKernel32 = Native.load("kernel32", WinKernel32::class.java)
    }

    fun getStdHandle(nStdHandle: Int): WinNT.HANDLE
    fun getConsoleScreenBufferInfo(
        hConsoleOutput: WinNT.HANDLE,
        lpConsoleScreenBufferInfo: ConsoleScreenBufferInfo
    ): Boolean
}

@Structure.FieldOrder("dwSize", "dwCursorPosition", "wAttributes", "srWindow", "dwMaximumWindowSize")
private open class ConsoleScreenBufferInfo : Structure() {
    class ByReference : ConsoleScreenBufferInfo(), Structure.ByReference

    var dwSize = Coord()
    var dwCursorPosition = Coord()
    var wAttributes = 0
    var srWindow = SmallRect()
    var dwMaximumWindowSize = Coord()
}

@Structure.FieldOrder("X", "Y")
private class Coord : Structure() {
    var X = 0
    var Y = 0
}

@Structure.FieldOrder("Left", "Top", "Right", "Bottom")
private class SmallRect : Structure() {
    var left = 0
    var top = 0
    var right = 0
    var bottom = 0
}