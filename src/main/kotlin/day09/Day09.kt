package day09

import AocPuzzle

fun main() {
    Day09().run(false)
}

class Day09 : AocPuzzle() {

    override val expectedTest1 = 114
    override val expectedTest2 = 2

    override val expected1 = 1789635132
    override val expected2 = 913

    override val testInput = """
         0  3  6  9 12 15
         1  3  6 10 15 21
        10 13 16 21 30 45
    """.trimIndent()

    override fun solve(input: List<String>): Pair<Any, Any> {
        val sequences = input
            .map { line -> Regex("""-?\d+""").findAll(line).map { it.value.toInt() }.toList() }
            .map { it.extendSequence() }

        val answer1 = sequences.sumOf { it.last() }
        val answer2 = sequences.sumOf { it.first() }

        return answer1 to answer2
    }

    private fun List<Int>.extendSequence(): List<Int> {
        val deltas = mutableListOf(toMutableList())

        deltas += windowed(2) { (a, b) -> b - a }.toMutableList()
        while (deltas.last().any { it != 0 }) {
            deltas += deltas.last().windowed(2) { (a, b) -> b - a }.toMutableList()
        }

        for (i in (deltas.lastIndex - 1) downTo 0) {
            val last = deltas[i].last() + deltas[i+1].last()
            val first = deltas[i].first() - deltas[i+1].first()
            deltas[i] += last
            deltas[i].add(0, first)
        }

        return deltas[0]
    }
}
