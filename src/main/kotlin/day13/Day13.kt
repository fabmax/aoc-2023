package day13

import AocPuzzle
import kotlin.math.min

fun main() = Day13().start()

class Day13 : AocPuzzle() {

    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val patterns = input
            .runningFold(mutableListOf<String>()) { group, line ->
                if (line.isBlank()) mutableListOf() else group.apply { add(line) }
            }
            .distinct()
            .map { Pattern(it) }

        val answer1 = patterns.sumOf {
            100 * it.findReflectionCenterRow(0) + it.findReflectionCenterCol(0)
        }
        val answer2 = patterns.sumOf {
            100 * it.findReflectionCenterRow(1) + it.findReflectionCenterCol(1)
        }
        return answer1 to answer2
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
            val iterations = min(index, lastIndex - index + 1)
            return smudgeCount == (0 ..< iterations).sumOf {
                get(index - it - 1).distance(get(index + it))
            }
        }

        private fun String.distance(b: String): Int = indices.map { if (this[it] == b[it]) 0 else 1 }.sum()
    }
}