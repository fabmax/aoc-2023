package y2015

import AocPuzzle
import de.fabmax.kool.math.Vec2i

fun main() = Day03.runAll()

object Day03 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        return input[0].runningFold(Vec2i.ZERO) { acc, dir ->
            acc + when (dir) {
                '<' -> Vec2i(-1, 0)
                '>' -> Vec2i(1, 0)
                'v' -> Vec2i(0, 1)
                '^' -> Vec2i(0, -1)
                else -> error(dir)
            }
        }.distinct().size
    }

    override fun solve2(input: List<String>): Int {
        var santaPos = Vec2i.ZERO
        var roboPos = Vec2i.ZERO
        val visited = mutableSetOf(santaPos)
        input[0].forEachIndexed { i, dir ->
            val d = when (dir) {
                '<' -> Vec2i(-1, 0)
                '>' -> Vec2i(1, 0)
                'v' -> Vec2i(0, 1)
                '^' -> Vec2i(0, -1)
                else -> error(dir)
            }
            if (i % 2 == 0) {
                santaPos += d
                visited += santaPos
            } else {
                roboPos += d
                visited += roboPos
            }
        }
        return visited.size
    }
}