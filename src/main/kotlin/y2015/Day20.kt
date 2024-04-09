package y2015

import AocPuzzle

fun main() = Day20.runAll()

object Day20 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val numPresents = input[0].toInt()
        val houses = IntArray(numPresents / 10)
        for (i in 1 until houses.size) {
            for (j in i until houses.lastIndex step i) {
                houses[j] += i * 10
            }
        }
        return houses.indexOfFirst { it >= numPresents }
    }

    override fun solve2(input: List<String>): Int {
        val numPresents = input[0].toInt()
        val houses = IntArray(numPresents / 11)
        for (i in 1 until houses.size) {
            (i until houses.lastIndex step i).asSequence().take(50).forEach { j ->
                houses[j] += i * 11
            }
        }
        return houses.indexOfFirst { it >= numPresents }
    }
}