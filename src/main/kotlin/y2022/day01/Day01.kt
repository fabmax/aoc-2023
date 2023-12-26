package y2022.day01

import AocPuzzle
import splitByBlankLines

fun main() = Day01.runAll()

object Day01 : AocPuzzle<Int, Int>() {

    override fun solve1(input: List<String>): Int {
        return input.splitByBlankLines().maxOf { it.sumOf { s -> s.toInt() } }
    }

    override fun solve2(input: List<String>): Int {
        return input.splitByBlankLines().map { it.sumOf { s -> s.toInt() } }.sortedDescending().take(3).sum()
    }
}