package y2015

import AocPuzzle

fun main() = Day05.runAll()

object Day05 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val vowels = listOf('a', 'e', 'i', 'o', 'u')
        val forbidden = listOf("ab", "cd", "pq", "xy")
        return input.count { line ->
            line.count { it in vowels } >= 3
                    && line.windowed(2).any { s -> s[0] == s[1] }
                    && forbidden.none { it in line }
        }
    }

    override fun solve2(input: List<String>): Int {
        // 52, 53
        return input.count { line ->
            (0 until line.length - 2).any { i ->
                val s = line.substring(i..i+1)
                s in line.substring(i+2)
            } && (0 until line.length - 2).any { i ->
                line[i] == line[i+2]
            }
        }
    }
}