package day13

import AocPuzzle
import kotlin.math.min

fun main() = Day13().start()

class Day13 : AocPuzzle() {

    override val answer1 = 27502
    override val answer2 = 31947

    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val patterns = buildList {
            input.plus("").fold(mutableListOf<String>()) { group, line ->
                when {
                    line.isBlank() -> mutableListOf<String>().also { add(Pattern(group)) }
                    else -> group.also { it.add(line) }
                }
            }
        }

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
            return (1 .. lines.lastIndex).firstOrNull { lines.isReflectingAt(it, smudgeCount) } ?: 0
        }

        fun findReflectionCenterCol(smudgeCount: Int): Int {
            return (1 .. transposed.lastIndex).firstOrNull { transposed.isReflectingAt(it, smudgeCount) } ?: 0
        }

        private fun List<String>.isReflectingAt(index: Int, smudgeCount: Int): Boolean {
            val iterations = min(index, lastIndex - index + 1)
            return smudgeCount == (0 ..< iterations).sumOf {
                get(index - it - 1).distance(get(index + it))
            }
        }

        private fun String.distance(b: String): Int = indices.map { if (this[it] == b[it]) 0 else 1 }.sum()
    }

    init {
        testInput(
            text = """
                #.##..##.
                ..#.##.#.
                ##......#
                ##......#
                ..#.##.#.
                ..##..##.
                #.#.##.#.

                #...##..#
                #....#..#
                ..##..###
                #####.##.
                #####.##.
                ..##..###
                #....#..#
            """.trimIndent(),
            expected1 = 405,
            expected2 = 400
        )
    }
}