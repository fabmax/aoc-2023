package y2022.day12

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import manhattanDistance
import printColored
import java.util.PriorityQueue

fun main() = Day12.runAll()

object Day12 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val (startX, startY) = input.indexOfFirst { 'S' in it }.let { input[it].indexOf('S') to it }
        val (destX, destY) = input.indexOfFirst { 'E' in it }.let { input[it].indexOf('E') to it }
        return dijkstra(input, Field(Vec2i(startX, startY), 1), Field(Vec2i(destX, destY), 26))
    }

    override fun solve2(input: List<String>): Int {
        val (destX, destY) = input.indexOfFirst { 'E' in it }.let { input[it].indexOf('E') to it }

        return input.indices.asSequence()
            .flatMap { y -> input[y].indices.map { x -> x to y } }
            .filter { (x, y) -> input[y][x] == 'a' }
            .map { (x, y) -> dijkstra(input, Field(Vec2i(x, y), 1), Field(Vec2i(destX, destY), 26)) }
            .filter { it > 0 }
            .min()
    }

    fun dijkstra(input: List<String>, start: Field, dest: Field): Int {
        val open = PriorityQueue<Field>(compareBy { it.p.manhattanDistance(dest.p) })
        val dists = mutableMapOf(start to 0)

        open += start

        while (open.isNotEmpty()) {
            val next = open.poll()
            val dist = dists[next]!!

            next.neighbors(input)
                .filter { f -> f.h <= next.h + 1 &&  dists[f]?.let { it > dist + 1 } != false }
                .forEach { f ->
                    dists[f] = dist + 1
                    open += f
                }
        }
        //printMap(input, dest, dists)
        return dists[dest] ?: -1
    }

    fun Field.neighbors(input: List<String>) = neighborOffsets
        .map { p + it }
        .filter { p -> p.y in input.indices && p.x in input[0].indices }
        .map { p -> Field(p, input[p.y][p.x].toHeight()) }

    @Suppress("unused")
    fun printMap(input: List<String>, dest: Field, dists: Map<Field, Int>) {
        val path = generateSequence(dest) { f ->
            f.neighbors(input)
                .filter { it.h >= f.h - 1 }
                .map { it to dists[it] }
                .filter { (_, d) -> d != null }
                .filter { (_, d) -> d!! < dists[f]!! }
                .minByOrNull { (_, d) -> d!! }?.first
        }.map { it.p }.toSet()

        for (y in input.indices) {
            for (x in input[y].indices) {
                val c = input[y][x]
                if (Vec2i(x, y) in path) {
                    printColored("$c", AnsiColor.BRIGHT_RED)
                } else if (c == 'a') {
                    printColored("$c", AnsiColor.BRIGHT_BLUE)
                } else if (c == 'c') {
                    printColored("$c", AnsiColor.GREEN)
                } else {
                    print("$c")
                }
            }
            println()
        }
    }

    fun Char.toHeight(): Int = when (this) {
        'S' -> 1
        'E' -> 26
        else -> code - 'a'.code + 1
    }

    val neighborOffsets = listOf(
        Vec2i(-1, 0),
        Vec2i(1, 0),
        Vec2i(0, -1),
        Vec2i(0, 1),
    )

    data class Field(val p: Vec2i, val h: Int)
}