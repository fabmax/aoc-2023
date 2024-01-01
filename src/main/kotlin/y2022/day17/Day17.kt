package y2022.day17

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import kotlin.math.max

fun main() = Day17.runAll()

object Day17 : AocPuzzle<Int, Long>() {

    override fun solve1(input: List<String>): Int {
        val directions = RepeatingList(input[0].map { if (it == '<') Vec2i(-1, 0) else Vec2i(1, 0) })
        val shapes = RepeatingList(shapeTypes)

        val stack = StackedShapes()
        return stack.stackShapes(2022, shapes, directions)
    }

    override fun solve2(input: List<String>): Long {
        val targetIterations = 1_000_000_000_000L
        val directions = RepeatingList(input[0].map { if (it == '<') Vec2i(-1, 0) else Vec2i(1, 0) })
        val shapes = RepeatingList(shapeTypes)

        val repeatLen = input[0].length * shapeTypes.size
        val stack = StackedShapes()
        val stackHashes = mutableMapOf<Long, Int>()

        var its = 0
        var cycleStart = 0
        var cyclePeriod = 0
        for (i in 1..500) {
            stack.stackShapes(repeatLen, shapes, directions)
            its += repeatLen

            val hash = stack.stackHash()
            if (hash in stackHashes) {
                cycleStart = stackHashes[hash]!!
                cyclePeriod = its - cycleStart
                break
            } else {
                stackHashes[hash] = its
            }
        }

        val initHeight = stack.stackHeights[cycleStart - 1].toLong()
        val cycleIncrement = stack.stackHeights[cycleStart + cyclePeriod - 1].toLong() - initHeight
        val nCycles = (targetIterations - cycleStart) / cyclePeriod
        val padI = ((targetIterations - cycleStart) % cyclePeriod).toInt()
        val padHeight = stack.stackHeights[cycleStart + padI - 1].toLong() - initHeight

        return initHeight + nCycles * cycleIncrement + padHeight
    }

    val shapeTypes = listOf(
        // ####
        Shape(listOf(Vec2i(0, 0), Vec2i(1, 0), Vec2i(2, 0), Vec2i(3, 0))),

        // .#.
        // ###
        // .#.
        Shape(listOf(Vec2i(1, 0), Vec2i(0, 1), Vec2i(1, 1), Vec2i(2, 1), Vec2i(1, 2))),

        // ..#
        // ..#
        // ###
        Shape(listOf(Vec2i(0, 0), Vec2i(1, 0), Vec2i(2, 0), Vec2i(2, 1), Vec2i(2, 2))),

        // #
        // #
        // #
        // #
        Shape(listOf(Vec2i(0, 0), Vec2i(0, 1), Vec2i(0, 2), Vec2i(0, 3))),

        // ##
        // ##
        Shape(listOf(Vec2i(0, 0), Vec2i(1, 0), Vec2i(0, 1), Vec2i(1, 1)))
    )

    class RepeatingList<T>(val elems: List<T>) {
        private var pos = 0
        fun next(): T {
            return elems[pos++ % elems.size]
        }
    }

    class Shape(blocks: List<Vec2i>) {
        val width: Int = blocks.maxOf { it.x + 1 }
        val height: Int = blocks.maxOf { it.y + 1 }
        val bits = IntArray(height)

        init {
            blocks.forEach {
                bits[it.y] = bits[it.y] or ((1 shl 7) shr it.x)
            }
        }
    }

    class StackedShapes {
        val windowSize = 64
        val window = ArrayDeque<UByte>(windowSize)

        var height = 0
        var windowTop = 3 + 4   // spawn y offset + tallest block height

        val stackHeights = mutableListOf<Int>()

        init {
            for (i in 0 until windowSize) {
                window += 0u
            }
        }

        fun stackHash(): Long {
            var hash = 0L
            for (i in 0 until windowSize step 8) {
                var h = 0L
                for (j in 0 until 8) {
                    h = (h shl 8) or window[i+j].toLong()
                }
                hash = hash xor h
            }
            return hash
        }

        fun stackShapes(
            n: Int,
            shapes: RepeatingList<Shape>,
            directions: RepeatingList<Vec2i>
        ): Int {
            val down = Vec2i(0, -1)
            repeat(n) {
                val shape = shapes.next()
                var pos = Vec2i(2, height + 3)

                while (true) {
                    val pushDir = directions.next()
                    if (canMoveTo(shape, pos + pushDir)) {
                        pos += pushDir
                    }

                    if (canMoveTo(shape, pos + down)) {
                        pos += down
                    } else {
                        placeAt(shape, pos)
                        break
                    }
                }
            }

            return height
        }

        fun canMoveTo(shape: Shape, pos: Vec2i): Boolean {
            if (pos.x !in 0.. (7-shape.width) || pos.y < 0) {
                return false
            }

            val windowY = windowTop - pos.y
            for (y in 0 until shape.height) {
                if (window[windowY - y] and (shape.bits[y] shr pos.x).toUByte() != 0.toUByte()) {
                    return false
                }
            }
            return true
        }

        fun placeAt(shape: Shape, pos: Vec2i) {
            val windowY = windowTop - pos.y
            for (y in 0 until shape.height) {
                window[windowY - y] = window[windowY - y] or (shape.bits[y] shr pos.x).toUByte()
            }

            val newHeight = max(height, pos.y + shape.height)
            val delta = newHeight - height
            height = newHeight
            windowTop += delta
            stackHeights += newHeight

            repeat(delta) {
                window.removeLast()
                window.addFirst(0u)
            }
        }
    }
}