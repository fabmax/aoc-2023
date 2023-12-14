package day14

import AocPuzzle
import timed

fun main() = Day14().start()

class Day14 : AocPuzzle() {
    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val grid1 = RockGrid(input)
        grid1.tiltNorth()
        val answer1 = grid1.computeWeight()
        val answer2 = part2(input)

//        for (i in 0..10) {
//            timed {
//                part2(input)
//            }
//        }

        return answer1 to answer2
    }

    fun part2(input: List<String>): Int {
        val grid = RockGrid(input)
        val arrangements = mutableMapOf<String, Int>()
        val weights = mutableListOf<Int>()

        var cycleI = 0
        var arrangement = grid.toString()
        while (arrangement !in arrangements.keys) {
            arrangements[arrangement] = cycleI++
            weights += grid.computeWeight()
            grid.spinCycle()
            arrangement = grid.toString()
        }

        val cycleStart = arrangements[arrangement]!!
        val cyclePeriod = cycleI - cycleStart
        val finalIndex = (1_000_000_000 - cycleStart) % cyclePeriod
        return weights[cycleStart + finalIndex]
    }

    class RockGrid(input: List<String>) {
        // input is square
        val size = input.size
        val array = CharArray(size * size)

        init {
            for (y in input.indices) {
                for (x in input[y].indices) {
                    array[y * size + x] = input[y][x]
                }
            }
        }

        override fun toString(): String {
            return buildString {
                for (y in 0 until size) {
                    for (x in 0 until size) {
                        append(array[y * size + x])
                    }
                    append('\n')
                }
            }
        }

        fun computeWeight(): Int {
            var weight = 0
            for (y in 0 until size) {
                val fac = size - y
                for (x in 0 until size) {
                    if (get(x, y) == 'O') {
                        weight += fac
                    }
                }
            }
            return weight
        }

        operator fun get(x: Int, y: Int): Char = array[y * size + x]

        operator fun set(x: Int, y: Int, value: Char) {
            array[y * size + x] = value
        }

        fun spinCycle() {
            tiltNorth()
            tiltWest()
            tiltSouth()
            tiltEast()
        }

        fun tiltNorth() = tilt(
            getAt = { col, j -> get(col, j) },
            setAt = { col, j, value -> set(col, j, value) }
        )

        fun tiltSouth() = tilt(
            getAt = { col, j -> get(col, size - j - 1) },
            setAt = { col, j, value -> set(col, size - j - 1, value) }
        )

        fun tiltEast() = tilt(
            getAt = { col, j -> get(size - j - 1, col) },
            setAt = { col, j, value -> set(size - j - 1, col, value) }
        )

        fun tiltWest() = tilt(
            getAt = { col, j -> get(j, col) },
            setAt = { col, j, value -> set(j, col, value) }
        )

        inline fun tilt(getAt: (Int, Int) -> Char, setAt: (Int, Int, Char) -> Unit) {
            for (col in 0 until size) {
                var scanPos = 0
                while (scanPos < size) {
                    var rockCount = 0
                    val scanStart = scanPos

                    while (scanPos < size) {
                        when (getAt(col, scanPos++)) {
                            '#' -> break
                            'O' -> rockCount++
                        }
                    }
                    for (j in scanStart until scanStart + rockCount) {
                        setAt(col, j, 'O')
                    }

                    if (scanStart + rockCount < scanPos) {
                        for (j in scanStart + rockCount until scanPos - 1) {
                            setAt(col, j, '.')
                        }
                        if (scanPos == size) {
                            val c = getAt(col, scanPos - 1)
                            val s = if (c == 'O') '.' else c
                            setAt(col, scanPos - 1, s)
                        } else {
                            setAt(col, scanPos - 1, '#')
                        }
                    }
                }
            }
        }
    }
}