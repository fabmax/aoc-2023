package y2015

import AocPuzzle
import takeAndRemoveWhile

fun main() = Day10.runAll()

object Day10 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val iterations = if (isTestRun()) 5 else 40
        return generateSequence(input[0]) { prev ->
            buildString {
                val q = ArrayDeque(prev.toList())
                while (q.isNotEmpty()) {
                    val c = q.removeFirst()
                    val same = q.takeAndRemoveWhile { it == c }
                    append("${same.size+1}$c")
                }
            }
        }.drop(iterations).first().length
    }

    override fun solve2(input: List<String>): Int {
        return generateSequence(input[0]) { prev ->
            buildString {
                val q = ArrayDeque(prev.toList())
                while (q.isNotEmpty()) {
                    val c = q.removeFirst()
                    val same = q.takeAndRemoveWhile { it == c }
                    append("${same.size+1}$c")
                }
            }
        }.drop(50).first().length
    }
}