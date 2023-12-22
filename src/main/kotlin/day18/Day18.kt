package day18

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import kotlin.math.abs

fun main() = Day18.runAll()

object Day18 : AocPuzzle<Long, Long>() {
    override fun solve1(input: List<String>): Long {
        var pos = Vec2i(0, 0)
        val verts = input.map {
            val (a, b, _) = it.split(" ")
            val dir = Direction.valueOf(a)
            val dist = b.toInt()
            pos += dir.step * dist
            pos
        }
        return verts.computeArea()
    }

    override fun solve2(input: List<String>): Long {
        var pos = Vec2i(0, 0)
        val verts2 = input.map {
            val code = it.split(" ")[2].removeSurrounding("(#", ")")
            val dir = Direction.entries[code.last().digitToInt()]
            val dist = code.substring(0, 5).toInt(16)
            pos += dir.step * dist
            pos
        }
        return verts2.computeArea()
    }
}

fun List<Vec2i>.computeArea(): Long {
    val doubleArea = windowed(2) { (i, j) ->
        (i.y + j.y) * (i.x - j.x).toLong() + i.distance(j).toLong()
    }.sum() + first().distance(last()) + 2L
    return doubleArea / 2L
}

fun Vec2i.distance(other: Vec2i):Int = abs(x - other.x) + abs(y - other.y)

enum class Direction(val step: Vec2i) {
    R(Vec2i(1, 0)),
    D(Vec2i(0, 1)),
    L(Vec2i(-1, 0)),
    U(Vec2i(0, -1)),
}
