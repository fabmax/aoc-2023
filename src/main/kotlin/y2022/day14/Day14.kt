package y2022.day14

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import kotlin.math.max
import kotlin.math.min

fun main() = Day14.runAll()

object Day14 : AocPuzzle<Int, Int>() {

    @Suppress("ControlFlowWithEmptyBody")
    override fun solve1(input: List<String>): Int {
        val occupancy = makeMap(input)
        val (_, max) = occupancy.keys.bounds()
        while (occupancy.pourSand(max.y));
        return occupancy.values.count { it == 'o' }
    }

    @Suppress("ControlFlowWithEmptyBody")
    override fun solve2(input: List<String>): Int {
        val occupancy = makeMap(input)
        val (_, max) = occupancy.keys.bounds()
        for (gndX in -1000 .. 1000) {
            occupancy[Vec2i(gndX, max.y + 2)] = '#'
        }
        while (occupancy.pourSand(Int.MAX_VALUE));
        return occupancy.values.count { it == 'o' }
    }

    fun makeMap(input: List<String>): MutableMap<Vec2i, Char> {
        val occupancy = mutableMapOf<Vec2i, Char>()
        input.forEach { line ->
            line.split(" -> ").windowed(2).forEach { (from, to) ->
                val (xFrom, yFrom) = from.split(",").map { it.toInt() }
                val (xTo, yTo) = to.split(",").map { it.toInt() }

                if (xFrom == xTo) {
                    for (y in min(yFrom, yTo) .. max(yFrom, yTo)) {
                        occupancy[Vec2i(xFrom, y)] = '#'
                    }
                } else {
                    for (x in min(xFrom, xTo) .. max(xFrom, xTo)) {
                        occupancy[Vec2i(x, yFrom)] = '#'
                    }
                }
            }
        }
        return occupancy
    }

    fun Set<Vec2i>.bounds(): Pair<Vec2i, Vec2i> {
        val minX = minOf { it.x }
        val minY = minOf { it.y }
        val maxX = maxOf { it.x }
        val maxY = maxOf { it.y }
        return Vec2i(minX, minY) to Vec2i(maxX, maxY)
    }

    fun MutableMap<Vec2i, Char>.pourSand(maxY: Int, insertPos: Vec2i = Vec2i(500, 0)): Boolean {
        if (insertPos in keys) {
            return false
        }

        var sandPos = Vec2i(insertPos)
        val flowDirs = listOf(Vec2i(0, 1), Vec2i(-1, 1), Vec2i(1, 1))
        while (sandPos.y <= maxY) {
            val nextPos = flowDirs.firstOrNull { sandPos + it !in this }
            if (nextPos == null) {
                put(sandPos, 'o')
                return true
            } else {
                sandPos += nextPos
            }
        }
        return false
    }
}