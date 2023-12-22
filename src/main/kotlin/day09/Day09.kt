package day09

import AocPuzzle

fun main() = Day09.runAll()

object Day09 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        return input
            .map { line -> Regex("""-?\d+""").findAll(line).map { it.value.toInt() }.toList() }
            .map { it.extendSequence() }
            .sumOf { it.last() }
    }

    override fun solve2(input: List<String>): Int {
        return input
            .map { line -> Regex("""-?\d+""").findAll(line).map { it.value.toInt() }.toList() }
            .map { it.extendSequence() }
            .sumOf { it.first() }
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
