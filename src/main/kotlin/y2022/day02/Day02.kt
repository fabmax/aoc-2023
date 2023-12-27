package y2022.day02

import AocPuzzle

fun main() = Day02.runAll()

object Day02 : AocPuzzle<Int, Int>() {

    override fun solve1(input: List<String>): Int = input.sumOf { l ->
        val a = Shape.entries.first { it.opp == l[0] }
        val b = Shape.entries.first { it.me == l[2] }

        val gameResult = when {
            a == b -> 3
            a == Shape.SCISSOR && b == Shape.ROCK -> 6
            a == Shape.PAPER && b == Shape.SCISSOR -> 6
            a == Shape.ROCK && b == Shape.PAPER -> 6
            else -> 0
        }
        gameResult + b.score
    }

    override fun solve2(input: List<String>): Int = input.sumOf { l ->
        val a = Shape.entries.first { it.opp == l[0] }
        val desired = l[2]

        val b = when (desired) {
            'X' -> when (a) {   // loose
                Shape.ROCK -> Shape.SCISSOR
                Shape.PAPER -> Shape.ROCK
                Shape.SCISSOR -> Shape.PAPER
            }
            'Z' -> when (a) {   // win
                Shape.ROCK -> Shape.PAPER
                Shape.PAPER -> Shape.SCISSOR
                Shape.SCISSOR -> Shape.ROCK
            }
            else -> a   // draw
        }
        val gameResult = when(desired) {
            'X' -> 0
            'Y' -> 3
            else -> 6
        }
        gameResult + b.score
    }

    enum class Shape(val opp: Char, val me: Char, val score: Int) {
        ROCK('A', 'X', 1),
        PAPER('B', 'Y', 2),
        SCISSOR('C', 'Z', 3)
    }
}