package y2022.day24

import AocPuzzle
import de.fabmax.kool.math.Vec2i

fun main() = Day24.runAll()

object Day24 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val bounds = Vec2i(input[0].lastIndex, input.lastIndex)
        val blizzards = input.indices.asSequence()
            .flatMap { y -> input[y].indices.map { it to y } }
            .filter { (x, y) -> input[y][x] !in listOf('#', '.') }
            .map { (x, y) -> Blizzard(Vec2i(x, y), input[y][x], bounds) }
            .toList()

        val start = Vec2i(1, 0)
        val dest = Vec2i(input[0].length-2, input.lastIndex)

        return computeTime(ValleyState(blizzards, setOf(start)), start, dest, bounds).first
    }

    override fun solve2(input: List<String>): Int {
        val bounds = Vec2i(input[0].lastIndex, input.lastIndex)
        val blizzards = input.indices.asSequence()
            .flatMap { y -> input[y].indices.map { it to y } }
            .filter { (x, y) -> input[y][x] !in listOf('#', '.') }
            .map { (x, y) -> Blizzard(Vec2i(x, y), input[y][x], bounds) }
            .toList()

        val start = Vec2i(1, 0)
        val dest = Vec2i(input[0].length-2, input.lastIndex)

        val (tA, stateA) = computeTime(ValleyState(blizzards, setOf(start)), start, dest, bounds)
        val (tB, stateB) = computeTime(stateA, dest, start, bounds)
        val (tC, _) = computeTime(stateB, start, dest, bounds)

        return tA + tB + tC
    }

    val movements = listOf(
        Vec2i(0, 0),
        Vec2i(1, 0),
        Vec2i(-1, 0),
        Vec2i(0, -1),
        Vec2i(0, 1),
    )

    fun Vec2i.isInBounds(bounds: Vec2i): Boolean {
        return x in 1 ..< bounds.x && y in 1 ..< bounds.y
    }

    fun computeTime(initialState: ValleyState, start: Vec2i, dest: Vec2i, bounds: Vec2i): Pair<Int, ValleyState> {
        var time = 0
        var state = initialState.copy(possiblePositions = setOf(start))
        while (dest !in state.possiblePositions) {
            state = state.nextState(start, dest, bounds)
            time++
        }
        return time to state
    }

    data class ValleyState(val blizzards: List<Blizzard>, val possiblePositions: Set<Vec2i>) {
        fun nextState(start: Vec2i, dest: Vec2i, bounds: Vec2i): ValleyState {
            val nextBlizzards = blizzards.map { it.step() }
            val blizzardMap = nextBlizzards.associateBy { it.pos }
            val nextPossiblePositions = possiblePositions.flatMap { field ->
                movements
                    .map { it + field }
                    .filter { it !in blizzardMap && (it.isInBounds(bounds) || it == dest || it == start) }
            }
            return ValleyState(nextBlizzards, nextPossiblePositions.toSet())
        }
    }

    data class Blizzard(val pos: Vec2i, val sign: Char, val bounds: Vec2i) {
        fun step(): Blizzard {
            val dir = when (sign) {
                '>' -> Vec2i(1, 0)
                '<' -> Vec2i(-1, 0)
                '^' -> Vec2i(0, -1)
                'v' -> Vec2i(0, 1)
                else -> error("invalid dir: $sign")
            }
            val nextPos = pos + dir
            val wrapPos = if (!nextPos.isInBounds(bounds)) {
                when (sign) {
                    '>' -> Vec2i(1, pos.y)
                    '<' -> Vec2i(bounds.x - 1, pos.y)
                    '^' -> Vec2i(pos.x, bounds.y - 1)
                    'v' -> Vec2i(pos.x, 1)
                    else -> error("unreachable")
                }
            } else {
                nextPos
            }
            return copy(pos = wrapPos)
        }
    }
}