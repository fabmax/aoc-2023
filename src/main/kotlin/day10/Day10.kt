package day10

import AnsiColor
import AocPuzzle
import ansiColor

fun main() = Day10().start()

class Day10 : AocPuzzle() {

    override val answer1 = 6778
    override val answer2 = 433

    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val maze = Maze(input)

        maze.printColored()

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
        // based on pnpoly: https://wrfranklin.org/Research/Short_Notes/pnpoly.html
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

    private fun Pipe.isConnecting(other: Pipe): Boolean = when {
        x == other.x && y > other.y ->    // other is up
            shape in openUp && other.shape in openDn
        y == other.y && x < other.x ->    // other is right
            shape in openRt && other.shape in openLt
        x == other.x && y < other.y ->    // other is down
            shape in openDn && other.shape in openUp
        y == other.y && x > other.x ->    // other is left
            shape in openLt && other.shape in openRt
        else -> false
    }

    private fun Maze.printColored() {
        val loop = traverseMaze()
        val loopPoly = loop.toList()

        maze.forEach { row ->
            print("      ")
            row.forEach {
                val fgColor = when {
                    it.shape == 'S' -> AnsiColor.BLACK
                    it in loop -> AnsiColor.BRIGHT_BLUE
                    it.isInside(loopPoly) -> AnsiColor.RED
                    else -> AnsiColor.BRIGHT_BLACK
                }
                val bgColor = if (it.shape == 'S') AnsiColor.BRIGHT_YELLOW else null
                print(ansiColor("${charMap[it.shape] ?: it.shape}", fgColor, bgColor))
            }
            println()
        }
    }

    companion object {
        private val openUp = setOf('|', 'J', 'L', 'S')
        private val openDn = setOf('|', '7', 'F', 'S')
        private val openLt = setOf('-', 'J', '7', 'S')
        private val openRt = setOf('-', 'L', 'F', 'S')

        private val charMap = mapOf(
            '|' to '┃',
            '-' to '━',
            'J' to '┛',
            'L' to '┗',
            'F' to '┏',
            '7' to '┓'
        )
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
            expected2 = 1
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
        lines.mapIndexed { y, row ->
            row.mapIndexed { x, c ->
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
