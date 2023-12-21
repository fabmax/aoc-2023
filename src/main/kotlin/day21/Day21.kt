package day21

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import printColored

fun main() = Day21.runAll()

object Day21 : AocPuzzle<Int, Long>() {

    override fun solve(input: List<String>): Pair<Int, Long> {
        val answer1 = GardenMap(input).possiblePositions(64).size
        val answer2 = part2(input)

        return answer1 to answer2
    }

    fun part2(input: List<String>): Long {
        val n = 26501365L
        val s = 131

        val map = GardenMap(input)
        if (map.width != s) {
            // skip test inputs
            return 0L
        }

        //
        // repeated map pattern:
        // compute counts in individual fields and add them all up multiplied by their occurrences
        //
        //         .^.
        //        *+-+*
        //       .+-+-+.        ^, v, <, >   diamond corners, entered by center of an edge
        //       <-+S+->        ., *         diamond borders, entered by a corner field, contain 65 (.) or 131+65 (*) steps
        //       .+-+-+.        +, -         fully filled center fields, alternating even and odd
        //        *+-+*
        //         .v.
        //

        // entry points for diamond corners
        val cornerStartL = Vec2i(0, s / 2)
        val cornerStartR = Vec2i(s - 1, s / 2)
        val cornerStartT = Vec2i(s / 2, 0)
        val cornerStartB = Vec2i(s / 2, s - 1)
        val cornerStarts = listOf(cornerStartL, cornerStartR, cornerStartT, cornerStartB)

        // entry points for diamond borders
        val borderStartTL = Vec2i(0, 0)
        val borderStartTR = Vec2i(s - 1, 0)
        val borderStartBL = Vec2i(0, s - 1)
        val borderStartBR = Vec2i(s - 1, s - 1)
        val borderStarts = listOf(borderStartTL, borderStartTR, borderStartBL, borderStartBR)

        // counts per field type
        // number of steps are n-1 to account for start position which already was the first step
        val borderCountsA = borderStarts.associateWith { map.possiblePositions(64, it).size.toLong() }      // . type
        val borderCountsB = borderStarts.associateWith { map.possiblePositions(64+131, it).size.toLong() }  // * type
        val cornerCounts = cornerStarts.associateWith { map.possiblePositions(130, it).size.toLong() }

        val oddCount = map.possiblePositions(131).size.toLong()
        val evenCount = map.possiblePositions(132).size.toLong()

        val extent = n / s
        val evenFields = extent * extent
        val oddFields = (extent-1) * (extent-1)
        val borderFieldsA = extent
        val borderFieldsB = extent-1

        val sum = evenFields * evenCount +
                oddFields * oddCount +
                borderStarts.sumOf { borderCountsA[it]!! * borderFieldsA + borderCountsB[it]!! * borderFieldsB } +
                cornerStarts.sumOf { cornerCounts[it]!! }

        return sum
    }

    @Suppress("unused")
    fun checkExplicit(input: List<String>, e: Int, n: Int): Long {
        val extLines = input.map { (1 until e).fold(it) { acc, _ -> acc + it } }
        val extended = (1 until e).fold(extLines) { acc, _ -> acc + extLines }

        val map = GardenMap(extended)
        val points = map.possiblePositions(n, Vec2i(map.width / 2, map.height / 2))

        val sectors = mutableMapOf<Vec2i, Int>()
        points.forEach {
            val x = it.x / 131
            val y = it.y / 131
            sectors[Vec2i(x, y)] = 1 + (sectors[Vec2i(x, y)] ?: 0)
        }

        for (y in 0 until e) {
            for (x in 0 until e) {
                print("%4d ".format(sectors[Vec2i(x, y)] ?: 0))
            }
            println()
        }

        return points.size.toLong()
    }

    class GardenMap(val input: List<String>) {
        val width: Int
            get() = input[0].length
        val height: Int
            get() = input.size

        val startPos: Vec2i

        init {
            val y = input.indexOfFirst { 'S' in it }
            val x = input[y].indexOfFirst { it == 'S' }
            startPos = Vec2i(x, y)
        }

        fun possiblePositions(nSteps: Int, start: Vec2i = startPos): Set<Vec2i> {
            return (0 until nSteps).fold(setOf(start)) { border, _ ->
                border.flatMap { it.walkableNeighbors }.toSet()
            }
        }

        private val Vec2i.walkableNeighbors: List<Vec2i> get() {
            return neighbors.map { this + it }.filter { canWalkAt(it) }
        }

        private fun canWalkAt(pos: Vec2i): Boolean {
            if (pos.x !in 0 until width || pos.y !in 0 until height) {
                return false
            }
            val c = input[pos.y][pos.x]
            return c == '.' || c == 'S'
        }

        @Suppress("unused")
        fun printPositionsOnMap(positions: Set<Vec2i>) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (Vec2i(x, y) in positions) {
                        printColored("O", AnsiColor.BLUE)
                    } else {
                        print(input[y][x])
                    }
                }
                println()
            }
        }

        companion object {
            val neighbors = listOf(Vec2i(-1, 0), Vec2i(1, 0), Vec2i(0, -1), Vec2i(0, 1))
        }
    }
}