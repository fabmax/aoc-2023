package day23

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger

fun main() = Day23.runAll()

object Day23 : AocPuzzle<Int, Int>() {

    override fun solve1(input: List<String>): Int {
        return Maze(input, false).findLongestPath()
    }

    override fun solve2(input: List<String>): Int {
        return Maze(input, true).findLongestPath()
    }

    class Maze(input: List<String>, val allowReverseSlopes: Boolean) {
        val height = input.size
        val width = input[0].length

        val start = Vec2i(1, 0)
        val dest = Vec2i(width - 2, height - 1)

        val fields = mutableMapOf<Vec2i, Field>()

        init {
            for (y in input.indices) {
                for (x in input[0].indices) {
                    val f = input[y][x]
                    if (f != '#') {
                        fields[Vec2i(x, y)] = Field(Vec2i(x, y), f)
                    }
                }
            }

            fields.values
                .filter { it.neighbors().size != 2 }
                .forEach { field ->
                    field.nexts += field.neighborJunctions()
                }
        }

        fun findLongestPath(): Int {
            val startField = fields[start]!!
            val destField = fields[dest]!!

            return runBlocking(Dispatchers.Default) {
                val maxWorkers = 128
                val workers = AtomicInteger(0)

                suspend fun searchPaths(start: Field, dest: Vec2i, path: Set<Field>, pathDist: Int): Int {
                    if (start.pos == dest) {
                        return pathDist
                    }

                    val nexts = start.nexts
                        .filter { it.first !in path }

                    return if (nexts.isEmpty()) -1 else {
                        if (workers.addAndGet(nexts.size) < maxWorkers) {
                            nexts.map { (next, dist) ->
                                async {
                                    searchPaths(next, dest, path + next, pathDist + dist)
                                }
                            }.awaitAll().max()
                        } else {
                            nexts.maxOf { (next, dist) ->
                                searchPaths(next, dest, path + next, pathDist + dist)
                            }
                        }
                    }
                }

                searchPaths(startField, destField.pos, setOf(startField), 0)
            }
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
    }

    data class Field(val pos: Vec2i, val type: Char) {
        val nexts = mutableListOf<Pair<Field, Int>>()
    }

    val steps = listOf(Vec2i(1, 0), Vec2i(-1, 0), Vec2i(0, 1), Vec2i(0, -1))
}