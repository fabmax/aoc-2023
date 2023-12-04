package day04

import parseTestInput
import readInput

val testInput = parseTestInput("""
    Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
    Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19
    Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1
    Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83
    Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
    Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11
""")

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
