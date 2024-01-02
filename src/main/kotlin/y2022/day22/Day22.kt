package y2022.day22

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import splitByBlankLines
import takeAndRemoveWhile

fun main() = Day22.runAll()

object Day22 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        return walkBoard(input, FlatLayout)
    }

    override fun solve2(input: List<String>): Int {
        return walkBoard(input, if (isTestRun()) CubeLayoutTest else CubeLayoutPuzzle)
    }

    fun walkBoard(input: List<String>, layout: BoardLayout): Int {
        val (boardTxt, path) = input.splitByBlankLines()
        val board = Board(boardTxt, layout)
        val instructions = parseInstructions(path[0])

        var pos = Vec2i(board.leftBorder[0], 0)
        var dir = Direction.RIGHT
        instructions.forEach { (dist, turn) ->
            for (i in 1 .. dist) {
                val next = board.move(pos, dir)
                if (next != null) {
                    pos = next.first
                    dir = next.second
                } else {
                    break
                }
            }
            dir = dir.turn(turn)
        }
        //board.printWalked()

        val col = pos.x + 1
        val row = pos.y + 1
        return 1000 * row + 4 * col + dir.ordinal
    }

    fun parseInstructions(txt: String): List<Pair<Int, Char>> {
        val instrs = mutableListOf<Pair<Int, Char>>()
        val remaining = ArrayDeque<Char>()
        remaining += txt.toList()
        while (remaining.isNotEmpty()) {
            val dist = remaining.takeAndRemoveWhile { it.isDigit() }.joinToString("").toInt()
            val turn = remaining.removeFirstOrNull() ?: ' '
            instrs += dist to turn
        }
        return instrs
    }

    class Board(val lines: List<String>, val layout: BoardLayout) {
        val leftBorder = lines.map { it.indexOfFirst { c -> c != ' ' } }

        fun isOutOfBounds(pos: Vec2i): Boolean {
            if (pos.y !in lines.indices) return true
            if (pos.x !in leftBorder[pos.y]..lines[pos.y].lastIndex) return true
            return false
        }

        fun canMoveTo(pos: Vec2i): Boolean {
            if (isOutOfBounds(pos)) return false
            return lines[pos.y][pos.x] == '.'
        }

        fun move(from: Vec2i, dir: Direction): Pair<Vec2i, Direction>? {
            val moved = if (isOutOfBounds(from + dir.step)) {
                layout.wrapPosition(this, from, dir)
            } else {
                from + dir.step to dir
            }
            return if (canMoveTo(moved.first)) {
                moved
            } else {
                null
            }
        }
    }

    fun Direction.turn(c: Char): Direction {
        return when (c) {
            'L' -> turnLeft()
            'R' -> turnRight()
            else -> this
        }
    }

    fun Direction.turnLeft(): Direction {
        val d = Vec2i(step.y, -step.x)
        return Direction.entries.first { it.step == d }
    }

    fun Direction.turnRight(): Direction {
        val d = Vec2i(-step.y, step.x)
        return Direction.entries.first { it.step == d }
    }

    enum class Direction(val step: Vec2i) {
        RIGHT(Vec2i(1, 0)),
        DOWN(Vec2i(0, 1)),
        LEFT(Vec2i(-1, 0)),
        UP(Vec2i(0, -1)),
    }

    interface BoardLayout {
        fun wrapPosition(board: Board, pos: Vec2i, dir: Direction): Pair<Vec2i, Direction>
    }

    object FlatLayout : BoardLayout {
        override fun wrapPosition(board: Board, pos: Vec2i, dir: Direction): Pair<Vec2i, Direction> {
            val wrapPos = when (dir) {
                Direction.LEFT -> Vec2i(board.lines[pos.y].lastIndex, pos.y)
                Direction.RIGHT -> Vec2i(board.leftBorder[pos.y], pos.y)
                Direction.UP -> Vec2i(pos.x, board.lines.indexOfLast { it.length > pos.x && it[pos.x] != ' ' })
                Direction.DOWN -> Vec2i(pos.x, board.lines.indexOfFirst { it.length > pos.x && it[pos.x] != ' ' })
            }
            return wrapPos to dir
        }
    }

    abstract class CubeLayout(val tileSize: Int) : BoardLayout {

        fun wrapPos(pos: Vec2i, exitDir: Direction, enterDir: Direction): Vec2i {
            val s = tileSize - 1
            val x = pos.x
            val y = pos.y
            return when (exitDir) {
                Direction.RIGHT -> when (enterDir) {
                    Direction.RIGHT -> Vec2i(s, s - y)
                    Direction.DOWN -> Vec2i(y, s)
                    Direction.LEFT -> Vec2i(0, y)
                    Direction.UP -> Vec2i(s - y, 0)
                }
                Direction.DOWN -> when (enterDir) {
                    Direction.RIGHT -> Vec2i(s, x)
                    Direction.DOWN -> Vec2i(s - x, s)
                    Direction.LEFT -> Vec2i(0, s - x)
                    Direction.UP -> Vec2i(x, 0)
                }
                Direction.LEFT -> when (enterDir) {
                    Direction.RIGHT -> Vec2i(s, y)
                    Direction.DOWN -> Vec2i(s - y, s)
                    Direction.LEFT -> Vec2i(0, s - y)
                    Direction.UP -> Vec2i(y, 0)
                }
                Direction.UP -> when (enterDir) {
                    Direction.RIGHT -> Vec2i(s, s - x)
                    Direction.DOWN -> Vec2i(x, s)
                    Direction.LEFT -> Vec2i(0, x)
                    Direction.UP -> Vec2i(s - x, 0)
                }
            }
        }

        fun enterSideToDir(enterSide: Direction): Direction = when (enterSide) {
            Direction.RIGHT -> Direction.LEFT
            Direction.DOWN -> Direction.UP
            Direction.LEFT -> Direction.RIGHT
            Direction.UP -> Direction.DOWN
        }
    }

    object CubeLayoutTest : CubeLayout(4) {
        // shape:
        // ..1
        // 234
        // ..56

        fun tileToXy(tileId: Int, tileXy: Vec2i): Vec2i {
            return tileXy + when (tileId) {
                1 -> Vec2i(tileSize * 2, 0)
                2 -> Vec2i(0, tileSize)
                3 -> Vec2i(tileSize, tileSize)
                4 -> Vec2i(tileSize * 2, tileSize)
                5 -> Vec2i(tileSize * 2, tileSize * 2)
                6 -> Vec2i(tileSize * 3, tileSize * 2)
                else -> error("invalid tile id")
            }
        }

        override fun wrapPosition(board: Board, pos: Vec2i, dir: Direction): Pair<Vec2i, Direction> {
            val tileX = pos.x / tileSize
            val tileY = pos.y / tileSize
            val xInTile = pos.x % tileSize
            val yInTile = pos.y % tileSize

            val tileId = when (tileY) {
                0 -> 1
                1 -> tileX + 2
                else -> tileX + 3
            }

            val (wrapTile, enterSide) = when {
                tileId == 1 && dir == Direction.UP -> 2 to Direction.UP
                tileId == 1 && dir == Direction.LEFT -> 3 to Direction.LEFT
                tileId == 1 && dir == Direction.RIGHT -> 6 to Direction.RIGHT
                tileId == 2 && dir == Direction.LEFT -> 6 to Direction.DOWN
                tileId == 2 && dir == Direction.UP -> 1 to Direction.UP
                tileId == 2 && dir == Direction.DOWN -> 5 to Direction.DOWN
                tileId == 3 && dir == Direction.UP -> 1 to Direction.LEFT
                tileId == 3 && dir == Direction.DOWN -> 5 to Direction.LEFT
                tileId == 4 && dir == Direction.RIGHT -> 6 to Direction.UP
                tileId == 5 && dir == Direction.LEFT -> 3 to Direction.DOWN
                tileId == 5 && dir == Direction.DOWN -> 2 to Direction.DOWN
                tileId == 6 && dir == Direction.DOWN -> 2 to Direction.LEFT
                tileId == 6 && dir == Direction.RIGHT -> 1 to Direction.RIGHT
                tileId == 6 && dir == Direction.UP -> 4 to Direction.RIGHT
                else -> error("tile id: $tileId, dir: $dir")
            }

            return tileToXy(wrapTile, wrapPos(Vec2i(xInTile, yInTile), dir, enterSide)) to enterSideToDir(enterSide)
        }
    }

    object CubeLayoutPuzzle: CubeLayout(50) {
        // shape:
        // .12
        // .3
        // 45
        // 6

        fun tileToXy(tileId: Int, tileXy: Vec2i): Vec2i {
            return tileXy + when (tileId) {
                1 -> Vec2i(tileSize, 0)
                2 -> Vec2i(tileSize * 2, 0)
                3 -> Vec2i(tileSize, tileSize)
                4 -> Vec2i(0, tileSize * 2)
                5 -> Vec2i(tileSize, tileSize * 2)
                6 -> Vec2i(0, tileSize * 3)
                else -> error("invalid tile id")
            }
        }

        override fun wrapPosition(board: Board, pos: Vec2i, dir: Direction): Pair<Vec2i, Direction> {
            val tileX = pos.x / tileSize
            val tileY = pos.y / tileSize
            val xInTile = pos.x % tileSize
            val yInTile = pos.y % tileSize

            val tileId = when (tileY) {
                0 -> tileX
                1 -> 3
                2 -> tileX + 4
                else -> 6
            }

            val (wrapTile, enterSide) = when {
                tileId == 1 && dir == Direction.UP -> 6 to Direction.LEFT
                tileId == 1 && dir == Direction.LEFT -> 4 to Direction.LEFT
                tileId == 2 && dir == Direction.UP -> 6 to Direction.DOWN
                tileId == 2 && dir == Direction.RIGHT -> 5 to Direction.RIGHT
                tileId == 2 && dir == Direction.DOWN -> 3 to Direction.RIGHT
                tileId == 3 && dir == Direction.LEFT -> 4 to Direction.UP
                tileId == 3 && dir == Direction.RIGHT -> 2 to Direction.DOWN
                tileId == 4 && dir == Direction.UP -> 3 to Direction.LEFT
                tileId == 4 && dir == Direction.LEFT -> 1 to Direction.LEFT
                tileId == 5 && dir == Direction.RIGHT -> 2 to Direction.RIGHT
                tileId == 5 && dir == Direction.DOWN -> 6 to Direction.RIGHT
                tileId == 6 && dir == Direction.LEFT -> 1 to Direction.UP
                tileId == 6 && dir == Direction.DOWN -> 2 to Direction.UP
                tileId == 6 && dir == Direction.RIGHT -> 5 to Direction.DOWN
                else -> error("tile id: $tileId, dir: $dir")
            }

            return tileToXy( wrapTile, wrapPos(Vec2i(xInTile, yInTile), dir, enterSide) ) to enterSideToDir(enterSide)
        }
    }
}