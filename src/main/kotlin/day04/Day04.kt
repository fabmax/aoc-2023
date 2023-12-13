package day04

import AocPuzzle

fun main() = Day04().start()

class Day04 : AocPuzzle() {
    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val answer1 = input
            .map { Card(it) }
            .sumOf { it.points }

        val cards = input.map { Card(it) }
        for (i in cards.indices) {
            val card = cards[i]
            for (j in (i+1) ..< (i+1+card.matches).coerceAtMost(cards.size)) {
                cards[j].copies += card.copies
            }
        }
        val answer2 = cards.sumOf { it.copies }

        return answer1 to answer2
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
