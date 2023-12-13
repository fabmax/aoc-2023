package day07

import readInput

fun main() {
    part1(readInput("day07.txt"))
    part2(readInput("day07.txt"))
}

fun part1(input: List<String>) {
    val hands = input.map { Hand(it) }.ranked()
    val winnings = hands.mapIndexed { rank, hand -> hand.bid * (rank+1) }.sum()
    println("answer part 1: $winnings")
}

fun part2(input: List<String>) {
    val hands = input.map { Hand(it) }.ranked(isJokerRule = true)
    val winnings = hands.mapIndexed { rank, hand -> hand.bid * (rank+1) }.sum()
    println("answer part 2: $winnings")
}

data class Hand(
    val cards: List<Card>,
    val bid: Int,
    val type: HandType = determineType(cards),
    val jokerType: HandType = determineTypeJoker(cards)
)

fun determineType(cards: List<Card>): HandType {
    val cardCounts = cards.distinct().associateWith { card -> cards.count { it == card } }
    return when (cardCounts.size) {
        1 -> HandType.FiveOfAKind
        2 -> {
            if (cardCounts.values.max() == 4) {
                HandType.FourOfAKind
            } else {
                HandType.FullHouse
            }
        }
        3 -> {
            if (cardCounts.values.max() == 3) {
                HandType.ThreeOfAKind
            } else {
                HandType.TwoPair
            }
        }
        4 -> HandType.OnePair
        5 -> HandType.HighCard
        else -> error("unreachable")
    }
}

fun determineTypeJoker(cards: List<Card>): HandType {
    val jokers = cards.count { it == Card.J }
    val cardCounts = cards
        .distinct()
        .filter { it != Card.J }
        .associateWith { card -> cards.count { it == card } }
    return when (cardCounts.size) {
        0, 1 -> HandType.FiveOfAKind
        2 -> {
            if (cardCounts.values.max() + jokers == 4) {
                HandType.FourOfAKind
            } else {
                HandType.FullHouse
            }
        }
        3 -> {
            if (cardCounts.values.max() + jokers == 3) {
                HandType.ThreeOfAKind
            } else {
                HandType.TwoPair
            }
        }
        4 -> HandType.OnePair
        5 -> HandType.HighCard
        else -> error("unreachable")
    }
}

fun Hand(line: String): Hand {
    val (cards, bid) = line.split(' ').filter { it.isNotBlank() }
    return Hand(
        cards.map { letter -> Card.entries.first { it.id == letter } },
        bid.toInt()
    )
}

fun List<Hand>.ranked(isJokerRule: Boolean = false): List<Hand> {
    val cardRanks = if (isJokerRule) {
        Card.entries.associateWith { it.ordinal }.toMutableMap().also { it[Card.J] = -1 }

    } else {
        Card.entries.associateWith { it.ordinal }
    }

    return sortedWith { a, b ->
        val aType = if (isJokerRule) a.jokerType else a.type
        val bType = if (isJokerRule) b.jokerType else b.type
        if (aType != bType) {
            aType.compareTo(bType)
        } else {
            a.cards.zip(b.cards)
                .firstOrNull { (ca, cb) -> ca != cb }
                ?.let { (ca, cb) ->
                    cardRanks[ca]!!.compareTo(cardRanks[cb]!!)
                } ?: 0
        }
    }
}

enum class HandType {
    HighCard,
    OnePair,
    TwoPair,
    ThreeOfAKind,
    FullHouse,
    FourOfAKind,
    FiveOfAKind,
}

enum class Card(val id: Char) {
    Two('2'),
    Three('3'),
    Four('4'),
    Five('5'),
    Six('6'),
    Seven('7'),
    Eight('8'),
    Nine('9'),
    Ten('T'),
    J('J'),
    Q('Q'),
    K('K'),
    A('A');

    override fun toString(): String {
        return "$id"
    }
}
