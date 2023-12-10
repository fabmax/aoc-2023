package day10

import AocPuzzle
import kotlin.math.abs

fun main() = Day10().start()

class Day10 : AocPuzzle() {

    override val answer1 = 6778
    override val answer2 = 433

    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val maze = Maze(input)

        val loop = maze.traverseMaze()
        val answer1 = loop.size / 2
        val answer2 = (maze.maze.flatten() - loop).count { it.isInside(loop.toList()) }

        return answer1 to answer2
    }

    private fun Maze.traverseMaze(): Set<Pipe> {
        val result = mutableSetOf<Pipe>()
        var mazeIt = start

        while (result.add(mazeIt)) {
            val lt = mazeIt.left(this)
            val rt = mazeIt.right(this)
            val up = mazeIt.up(this)
            val dn = mazeIt.down(this)

            mazeIt = when {
                lt != null && lt !in result && mazeIt.isConnecting(lt) -> lt
                rt != null && rt !in result && mazeIt.isConnecting(rt) -> rt
                up != null && up !in result && mazeIt.isConnecting(up) -> up
                dn != null && dn !in result && mazeIt.isConnecting(dn) -> dn
                else -> break
            }
        }
        return result
    }

    private fun Pipe.isInside(loop: List<Pipe>): Boolean {
        // good old inside polygon check...
        var isInside = false
        for (i in loop.indices) {
            val li = loop[i]
            val lj = if (i == 0) loop.last() else loop[i-1]

            if ((li.y > y) != (lj.y > y) && (x < (lj.x - li.x) * (y - li.y) / (lj.y - li.y) + li.x)) {
                isInside = !isInside
            }
        }
        return isInside
    }

    private fun Pipe.up(maze: Maze): Pipe? = maze[x, y-1]
    private fun Pipe.down(maze: Maze): Pipe? = maze[x, y+1]
    private fun Pipe.left(maze: Maze): Pipe? = maze[x-1, y]
    private fun Pipe.right(maze: Maze): Pipe? = maze[x+1, y]

    private fun Pipe.isConnecting(other: Pipe?): Boolean {
        other ?: return false
        check(abs(x - other.x) + abs(y - other.y) == 1)
        if (shape == 'S' || other.shape == 'S') {
            return true
        }

        return when {
            x == other.x && y > other.y ->    // other is up
                return shape in openUp && other.shape in openDn
            y == other.y && x < other.x ->    // other is right
                return shape in openRt && other.shape in openLt
            x == other.x && y < other.y ->    // other is down
                return shape in openDn && other.shape in openUp
            y == other.y && x > other.x ->    // other is left
                return shape in openLt && other.shape in openRt
            else -> false
        }
    }

    companion object {
        private val openUp = setOf('|', 'J', 'L')
        private val openDn = setOf('|', '7', 'F')
        private val openLt = setOf('-', 'J', '7')
        private val openRt = setOf('-', 'L', 'F')
    }

    init {
        testInput(
            text = """
                ..F7.
                .FJ|.
                SJ.L7
                |F--J
                LJ...
            """.trimIndent(),
            expected1 = 8,
            parts = PART1
        )
        testInput(
            text = """
                FF7FSF7F7F7F7F7F---7
                L|LJ||||||||||||F--J
                FL-7LJLJ||||||LJL-77
                F--JF--7||LJLJ7F7FJ-
                L---JF-JLJ.||-FJLJJ7
                |F|F-JF---7F7-L7L|7|
                |FFJF7L7F-JF7|JL---7
                7-L-JL7||F7|L7F-7F7|
                L.L7LFJ|||||FJL7||LJ
                L7JLJL-JLJLJL--JLJ.L
            """.trimIndent().trimIndent(),
            expected2 = 10,
            parts = PART2
        )
    }
}

data class Pipe(val shape: Char, val x: Int, val y: Int)

class Maze(lines: List<String>) {
    val maze: List<List<Pipe>> =
        lines.mapIndexed { y, line ->
            line.mapIndexed { x, c ->
                Pipe(c, x, y)
            }
        }

    val width = maze[0].size
    val height = maze.size

    val start: Pipe = maze.flatten().first { it.shape == 'S' }

    operator fun get(x: Int, y: Int): Pipe? {
        if (x !in 0 until width || y !in 0 until height) {
            return null
        }
        return maze[y][x]
    }
}
