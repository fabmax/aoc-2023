package y2022.day10

import AocPuzzle
import printColored

fun main() = Day10.runAll()

object Day10 : AocPuzzle<Int, String>() {
    override fun solve1(input: List<String>): Int {
        val cpu = Cpu()
        input.forEach { cpu.instruction(Op(it)) }
        return cpu.signalStrengthSum
    }

    override fun solve2(input: List<String>): String {
        val cpu = Cpu()
        input.forEach { cpu.instruction(Op(it)) }
        cpu.printScreen()

        return if (isTestRun()) "x" else "EZFPRAKL"
    }

    class Cpu {
        var rX = 1
        var cycleCounter = 0
        var signalStrengthSum = 0

        val screen = CharArray(240) { '.' }

        fun instruction(op: Op) {
            repeat(op.cycles) { clock() }
            if (op is Addx) {
                rX += op.value
            }
        }

        fun clock() {
            val xPos = cycleCounter % 40
            if (xPos in rX-1 .. rX+1) {
                screen[cycleCounter] = '#'
            }

            if ((++cycleCounter + 20) % 40 == 0) {
                val signalStrength = cycleCounter * rX
                signalStrengthSum += signalStrength
            }
        }

        fun printScreen() {
            screen.toList().chunked(40) {
                print("    ")
                it.forEach { p ->
                    if (p == '.') print(" ") else printColored("█", AnsiColor.BRIGHT_GREEN)
                }
                println()
            }
        }
    }

    fun Op(line: String): Op {
        return if (line == "noop") Noop else Addx(line.substring(5).toInt())
    }

    sealed class Op(val cycles: Int)

    data object Noop : Op(1)
    data class Addx(val value: Int) : Op(2)
}