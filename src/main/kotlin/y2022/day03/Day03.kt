package y2022.day03

import AocPuzzle

fun main() = Day03.runAll()

object Day03 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int = input.sumOf {  content ->
        val first = content.substring(0 ..< content.length / 2)
        val second = content.removePrefix(first)
        val duplicate = first.first { it in second }
        if (duplicate.isLowerCase()) {
            duplicate.code - 'a'.code + 1
        } else {
            duplicate.code - 'A'.code + 27
        }
    }

    override fun solve2(input: List<String>): Int = input.chunked(3).sumOf { group ->
        val badge = group.joinToString("").toSet().maxBy { c -> group.count { c in it } }
        if (badge.isLowerCase()) {
            badge.code - 'a'.code + 1
        } else {
            badge.code - 'A'.code + 27
        }
    }
}