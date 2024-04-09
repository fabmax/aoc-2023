package y2022.day06

import AocPuzzle

fun main() = Day06.runAll()

object Day06 : AocPuzzle<Int, Int>() {

    override fun solve1(input: List<String>): Int {
        val sequence = input[0]
        return (4 .. sequence.length).first { sequence.substring(it-4 until it).toList().distinct().size == 4 }
    }

    override fun solve2(input: List<String>): Int {
        val sequence = input[0]
        return (14 .. sequence.length).first { sequence.substring(it-14 until it).toList().distinct().size == 14 }
    }
}