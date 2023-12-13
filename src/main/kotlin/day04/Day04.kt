package day04

import readInput

fun main() {
    part1()
    part2()
}

fun part1() {
    val answer = readInput("day04.txt")
        .map { Card(it) }
        .sumOf { it.points }
    println("Answer part 1: $answer")
}

fun part2() {
    val cards = readInput("day04.txt").map { Card(it) }
    for (i in cards.indices) {
        val card = cards[i]
        for (j in (i+1) ..< (i+1+card.matches).coerceAtMost(cards.size)) {
            cards[j].copies += card.copies
        }
    }
    println("Answer part 2: ${cards.sumOf { it.copies }}")
}

fun Card(line: String): Card {
    val id = line.removePrefix("Card ").substringBefore(':').trim().toInt()

    val winning = line.substringAfter(':').substringBefore('|')
        .split(" ")
        .filter { it.isNotBlank() }
        .map { it.trim().toInt() }
        .toSet()

    val drawn = line.substringAfterLast('|')
        .split(" ")
        .filter { it.isNotBlank() }
        .map { it.trim().toInt() }
        .toSet()

    return Card(id, winning, drawn)
}

data class Card(val id: Int, val winning: Set<Int>, val drawnNumbers: Set<Int>, var copies: Int = 1)

val Card.matches: Int get() = drawnNumbers.count { it in winning }

val Card.points: Int get() = matches.let { if (it == 0) 0 else 1 shl (matches - 1) }
