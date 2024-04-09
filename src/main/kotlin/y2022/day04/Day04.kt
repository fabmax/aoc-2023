package y2022.day04

import AocPuzzle

fun main() = Day04.runAll()

object Day04 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int = input.count { ranges ->
        val (a,b,c,d) = ranges.split(',', '-').map { it.toInt() }
        (a <= c && b >= d) || (c <= a && d >= b)
    }

    override fun solve2(input: List<String>): Int  = input.count { ranges ->
        val (a,b,c,d) = ranges.split(',', '-').map { it.toInt() }
        c in a..b || d in a..b || a in c..d || b in c..d
    }
}