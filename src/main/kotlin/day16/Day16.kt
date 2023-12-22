package day16

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import kotlin.math.max

fun main() = Day16.runAll()

object Day16 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val contraption = Contraption(input)
        contraption.recurseBeam(Vec2i.ZERO, BeamDir.RIGHT)
        return contraption.energized.size
    }

    override fun solve2(input: List<String>): Int {
        val contraption = Contraption(input)
        val energys = input.indices.map { y ->
            contraption.energized.clear()
            contraption.recurseBeam(Vec2i(0, y), BeamDir.RIGHT)
            val fromLeft = contraption.energized.size

            contraption.energized.clear()
            contraption.recurseBeam(Vec2i(input[0].lastIndex, y), BeamDir.LEFT)
            val fromRight = contraption.energized.size

            max(fromLeft, fromRight)

        } + input[0].indices.map { x ->
            contraption.energized.clear()
            contraption.recurseBeam(Vec2i(x, 0), BeamDir.DOWN)
            val fromTop = contraption.energized.size

            contraption.energized.clear()
            contraption.recurseBeam(Vec2i(x, input.lastIndex), BeamDir.UP)
            val fromBottom = contraption.energized.size

            max(fromTop, fromBottom)
        }
        return energys.max()
    }
}

class Contraption(val layout: List<String>) {

    val energized = mutableMapOf<Vec2i, MutableSet<BeamDir>>()

    fun recurseBeam(start: Vec2i, dir: BeamDir) {
        if (!energize(start, dir)) {
            return
        }

        val end = walkBeam(start, dir)
        if (end !in this) {
            return
        }

        when (val hit = get(end)) {
            '/', '\\' -> {
                val rot = rotateBeam(hit, dir)
                recurseBeam(end + rot.step, rot)
            }
            '|' -> {
                recurseBeam(end + BeamDir.UP.step, BeamDir.UP)
                recurseBeam(end + BeamDir.DOWN.step, BeamDir.DOWN)
            }
            '-' -> {
                recurseBeam(end + BeamDir.LEFT.step, BeamDir.LEFT)
                recurseBeam(end + BeamDir.RIGHT.step, BeamDir.RIGHT)
            }
        }
    }

    fun rotateBeam(hit: Char, dir: BeamDir): BeamDir {
        return if (hit == '/') {
            when (dir) {
                BeamDir.LEFT -> BeamDir.DOWN
                BeamDir.RIGHT -> BeamDir.UP
                BeamDir.UP -> BeamDir.RIGHT
                BeamDir.DOWN -> BeamDir.LEFT
            }
        } else if (hit == '\\') {
            when (dir) {
                BeamDir.LEFT -> BeamDir.UP
                BeamDir.RIGHT -> BeamDir.DOWN
                BeamDir.UP -> BeamDir.LEFT
                BeamDir.DOWN -> BeamDir.RIGHT
            }
        } else {
            error("invalid mirror: $hit")
        }
    }

    fun walkBeam(start: Vec2i, dir: BeamDir): Vec2i {
        val blocking = setOf(dir.blockingSplitter, '/', '\\')
        var pos = start

        while (pos in this && get(pos) !in blocking) {
            energize(pos, dir)
            pos += dir.step
        }

        energize(pos, dir)
        return pos
    }

    fun energize(pos: Vec2i, dir: BeamDir): Boolean {
        return if (pos in this) {
            energized.getOrPut(pos) { mutableSetOf() }.add(dir)
        } else {
            false
        }
    }

    operator fun get(pos: Vec2i): Char {
        return layout[pos.y][pos.x]
    }

    operator fun contains(pos: Vec2i): Boolean {
        return pos.x in layout[0].indices && pos.y in layout.indices
    }
}

enum class BeamDir(val step: Vec2i, val blockingSplitter: Char) {
    LEFT(Vec2i(-1, 0), '|'),
    RIGHT(Vec2i(1, 0), '|'),
    UP(Vec2i(0, -1), '-'),
    DOWN(Vec2i(0, 1), '-')
}
