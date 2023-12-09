package day09

import readInput

//
// val testInput = parseTestInput("""
//      0  3  6  9 12 15
//      1  3  6 10 15 21
//     10 13 16 21 30 45
//  """.trimIndent())
//

fun main() {
    val sequences = readInput("day09.txt")
        .map { line -> Regex("""-?\d+""").findAll(line).map { it.value.toInt() }.toList() }
        .map { it.extendSequence() }

    sequences.sumOf { it.last() }.also {
        println("answer part 1: $it")
    }

    sequences.sumOf { it.first() }.also {
        println("answer part 2: $it")
    }
}

fun List<Int>.extendSequence(): List<Int> {
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
