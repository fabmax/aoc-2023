package y2015

import AocPuzzle

fun main() = Day18.runAll()

object Day18 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val gol = GameOfLife(input)
        repeat(if (isTestRun()) 4 else 100) { gol.step() }
        return gol.state.count { it }
    }

    override fun solve2(input: List<String>): Int {
        val gol = GameOfLife(input)
        gol.turnOnCorners()
        repeat(if (isTestRun()) 5 else 100) {
            gol.step()
            gol.turnOnCorners()
        }
        return gol.state.count { it }
    }

    class GameOfLife(initialState: List<String>) {
        val width = initialState[0].length
        val height = initialState.size

        var state = BooleanArray(width * height)
        var nextState = BooleanArray(width * height)

        init {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (initialState[y][x] == '#') {
                        state[y * width + x] = true
                    }
                }
            }
        }

        fun step() {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val actNeighbors = countActiveNeighbors(x, y)
                    val oldState = state[y * width + x]
                    nextState[y * width + x] = if (oldState) {
                        actNeighbors in 2..3
                    } else {
                        actNeighbors == 3
                    }
                }
            }
            state = nextState.also { nextState = state }
        }

        fun turnOnCorners() {
            state[0] = true
            state[width-1] = true
            state[width * (height-1)] = true
            state[width * (height-1) + width-1] = true
        }

        fun countActiveNeighbors(cx: Int, cy: Int): Int {
            var cnt = 0
            for (y in cy-1 .. cy+1) {
                for (x in cx-1 .. cx+1) {
                    if ((x != cx || y != cy) &&
                        y in 0 ..< height &&
                        x in 0 ..< width &&
                        state[y * width + x]
                    ) {
                        cnt++
                    }
                }
            }
            return cnt
        }

        fun print() {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (state[y * width + x]) print('#') else print('.')
                }
                println()
            }
            println()
        }
    }
}