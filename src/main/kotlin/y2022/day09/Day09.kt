package y2022.day09

import AocPuzzle
import de.fabmax.kool.math.MutableVec2i
import de.fabmax.kool.math.Vec2i
import kotlin.math.abs

fun main() = Day09.runAll()

object Day09 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val rope = Rope(2)
        input.forEach { line ->
            val dir = directions[line[0]]!!
            val steps = line.substring(2).toInt()
            rope.moveHead(dir, steps)
        }
        return rope.tailMap.size
    }

    override fun solve2(input: List<String>): Int {
        val rope = Rope(10)
        input.forEach { line ->
            val dir = directions[line[0]]!!
            val steps = line.substring(2).toInt()
            rope.moveHead(dir, steps)
        }
        return rope.tailMap.size
    }

    class Rope(length: Int) {
        val knots = List(length) { MutableVec2i(0, 0) }
        val tailMap = mutableSetOf(Vec2i(knots.last()))

        fun moveHead(dir: Vec2i, steps: Int) {
            repeat(steps) {
                knots.first().add(dir)
                updateRope()
                tailMap += Vec2i(knots.last())
            }
        }

        fun updateRope() {
            knots.windowed(2).forEach { (h, t) ->
                if (abs(h.x - t.x) > 1 || abs(h.y - t.y) > 1) {
                    when {
                        h.x == t.x -> t += if (h.y < t.y) Vec2i(0, -1) else Vec2i(0, 1)
                        h.y == t.y -> t += if (h.x < t.x) Vec2i(-1, 0) else Vec2i(1, 0)
                        h.x < t.x && h.y < t.y -> t += Vec2i(-1, -1)
                        h.x < t.x && h.y > t.y -> t += Vec2i(-1, 1)
                        h.x > t.x && h.y < t.y -> t += Vec2i(1, -1)
                        h.x > t.x && h.y > t.y -> t += Vec2i(1, 1)
                    }
                }
            }
        }
    }

    val directions = mapOf(
        'R' to Vec2i(1, 0),
        'L' to Vec2i(-1, 0),
        'U' to Vec2i(0, 1),
        'D' to Vec2i(0, -1)
    )
}