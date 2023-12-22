package day13

import AocPuzzle
import splitByBlankLines
import kotlin.math.min

fun main() = Day13.runAll()

object Day13 : AocPuzzle<Int, Int>() {

    override fun solve1(input: List<String>): Int {
        return input.splitByBlankLines().map { Pattern(it) }.sumOf {
            100 * it.findReflectionCenterRow(0) + it.findReflectionCenterCol(0)
        }
    }

    override fun solve2(input: List<String>): Int {
        return input.splitByBlankLines().map { Pattern(it) }.sumOf {
            100 * it.findReflectionCenterRow(1) + it.findReflectionCenterCol(1)
        }
    }

    class Pattern(val lines: List<String>) {
        private val transposed = lines[0].indices.map { col ->
            lines.map { it[col] }.joinToString(separator = "")
        }

        fun findReflectionCenterRow(smudgeCount: Int): Int {
            return (1 .. lines.lastIndex).firstOrNull { lines.isReflectionAt(it, smudgeCount) } ?: 0
        }

        fun findReflectionCenterCol(smudgeCount: Int): Int {
            return (1 .. transposed.lastIndex).firstOrNull { transposed.isReflectionAt(it, smudgeCount) } ?: 0
        }

        private fun List<String>.isReflectionAt(index: Int, smudgeCount: Int): Boolean {
            val iterations = min(index, size - index)
            return smudgeCount == (0 ..< iterations).sumOf {
                get(index - it - 1).distance(get(index + it))
            }
        }

        private fun String.distance(b: String): Int = indices.map { if (this[it] == b[it]) 0 else 1 }.sum()
    }
}