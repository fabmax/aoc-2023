package day23

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import java.util.*
import kotlin.math.max

fun main() = Day23.runAll()

object Day23 : AocPuzzle<Int, Int>() {

    override fun solve1(input: List<String>): Int {
        return Maze(input, false).findLongestPath()
    }

    override fun solve2(input: List<String>): Int {
        return Maze(input, true).findLongestPath()
    }

    class Maze(val input: List<String>, val allowReverseSlopes: Boolean) {
        val height = input.size
        val width = input[0].length

        val start = Vec2i(1, 0)
        val dest = Vec2i(width - 2, height - 1)

        val fields = mutableMapOf<Vec2i, Field>()
        val junctions: Map<Vec2i, Junction>

        init {
            var id = 0
            for (y in input.indices) {
                for (x in input[0].indices) {
                    val f = input[y][x]
                    if (f != '#') {
                        fields[Vec2i(x, y)] = Field(id++, Vec2i(x, y), f)
                    }
                }
            }

            junctions = fields.values
                .filter { it.neighbors().size != 2 }
                .mapIndexed { i, field -> Junction(i, field) }
                .associateBy { it.field.pos }
            junctions.values.forEach { it.connectTo(it.field.neighborJunctions()) }
        }

        fun findLongestPath(): Int {
            val startJunction = junctions[start]!!

            fun searchPaths(start: Junction, dest: Vec2i, path: BitSet, pathDist: Int): Int {
                if (start.field.pos == dest) {
                    return pathDist
                }

                val nexts = start.neighborDists
                    .filter { (junction, _) -> !path.get(junction.id) }
                    // might not work for all input but 2x faster
                    .filter { (junction, dist) -> pathDist + dist > junction.longestDist * 0.125 }

                return if (nexts.isEmpty()) -1 else {
                    nexts.maxOf { (next, dist) ->
                        next.longestDist = max(next.longestDist, pathDist + dist)
                        val nextPath = BitSet().apply { or(path); set(next.id) }
                        searchPaths(next, dest, nextPath, pathDist + dist)
                    }
                }
            }

            val bitSet = BitSet(junctions.size)
            bitSet.set(startJunction.id)
            return searchPaths(startJunction, dest, bitSet, 0)
        }

        fun Field.neighbors(): List<Field> {
            return if (allowReverseSlopes) {
                steps.mapNotNull { fields[pos + it] }
            } else {
                when (type) {
                    '.' -> steps.mapNotNull { fields[pos + it] }
                    '>' -> listOfNotNull(fields[pos + Vec2i.X_AXIS])
                    'v' -> listOfNotNull(fields[pos + Vec2i.Y_AXIS])
                    '<' -> listOfNotNull(fields[pos - Vec2i.X_AXIS])
                    '^' -> listOfNotNull(fields[pos - Vec2i.Y_AXIS])
                    else -> error("unreachable")
                }
            }
        }

        fun Field.neighborJunctions(): List<Pair<Field, Int>> {
            fun nextJunction(start: Field): Pair<Field, Int>? {
                var distance = 1
                var nexts = start.neighbors().filter { it != this }
                var prev = start

                while (nexts.isNotEmpty()) {
                    if (nexts.size == 1) {
                        val next = nexts[0]
                        if (next.pos == dest) {
                            return next to distance + 1
                        }
                        nexts = next.neighbors().filter { it != prev }
                        prev = next
                        distance++
                    } else {
                        return prev to distance
                    }
                }
                return null
            }

            return neighbors().mapNotNull { nextJunction(it) }
        }

        inner class Junction(val id: Int, val field: Field) {
            private val _nexts = mutableListOf<Pair<Junction, Int>>()
            val neighborDists: List<Pair<Junction, Int>>
                get() = _nexts

            var longestDist = 0

            fun connectTo(junctionsFields: List<Pair<Field, Int>>) {
                _nexts += junctionsFields.map { (field, dist) -> junctions[field.pos]!! to dist }
            }
        }
    }

    data class Field(val id: Int, val pos: Vec2i, val type: Char)

    val steps = listOf(Vec2i(1, 0), Vec2i(-1, 0), Vec2i(0, 1), Vec2i(0, -1))
}