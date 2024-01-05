package y2015

import AocPuzzle

fun main() = Day01.runAll()

object Day01 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int = input[0].fold(0) { acc, c -> if (c == '(') acc + 1 else acc - 1 }

    override fun solve2(input: List<String>): Int {
        var level = 0
        return input[0].takeWhile {
            level += if (it == '(') 1 else -1
            level != -1
        }.length + 1
    }
}