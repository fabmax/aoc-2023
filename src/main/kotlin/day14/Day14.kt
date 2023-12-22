package day14

import AocPuzzle

fun main() = Day14.runAll()

object Day14 : AocPuzzle<Int, Int>() {

    override fun solve1(input: List<String>): Int {
        val grid1 = RockGrid(input)
        grid1.tiltNorth()
        return grid1.computeWeightAndHash().first
    }

    override fun solve2(input: List<String>): Int {
        val grid = RockGrid(input)
        val arrangements = mutableMapOf<Long, Int>()
        val weights = mutableListOf<Int>()

        var cycleI = 0
        var (weight, hash) = grid.computeWeightAndHash()
        while (hash !in arrangements.keys) {
            arrangements[hash] = cycleI++
            weights += weight
            grid.spinCycle()
            grid.computeWeightAndHash().let { (w, h) -> weight = w; hash = h }
        }

        val cycleStart = arrangements[hash]!!
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

        fun computeWeightAndHash(): Pair<Int, Long> {
            var weight = 0
            var hash = 0L
            for (y in 0 until size) {
                val fac = size - y
                for (x in 0 until size) {
                    val t = get(x, y)
                    hash = hash * 31 + t.code
                    if (t == 'O') {
                        weight += fac
                    }
                }
            }
            return weight to hash
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
                        if (scanPos == size && getAt(col, scanPos - 1) == 'O') {
                            setAt(col, scanPos - 1, '.')
                        }
                    }
                }
            }
        }
    }
}