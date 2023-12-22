package day04

import AocPuzzle

fun main() = Day04.runAll()

object Day04 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        return input
            .map { Card(it) }
            .sumOf { it.points }
    }

    override fun solve2(input: List<String>): Int {
        val cards = input.map { Card(it) }
        for (i in cards.indices) {
            val card = cards[i]
            for (j in (i+1) ..< (i+1+card.matches).coerceAtMost(cards.size)) {
                cards[j].copies += card.copies
            }
        }
        return cards.sumOf { it.copies }
    }
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
