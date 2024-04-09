package y2023.day17

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import de.fabmax.kool.util.PriorityQueue

fun main() = Day17().runAll()

class Day17 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val start = Vec2i.ZERO
        val dest = Vec2i(input[0].lastIndex, input.lastIndex)
        return dijkstra(start, dest, input, 0, 3)
    }

    override fun solve2(input: List<String>): Int {
        val start = Vec2i.ZERO
        val dest = Vec2i(input[0].lastIndex, input.lastIndex)
        return dijkstra(start, dest, input, 4, 10)
    }
}

typealias OpenNode = Pair<Int, TravelState>

fun dijkstra(start: Vec2i, dest: Vec2i, map: List<String>, minStraight: Int, maxStraight: Int): Int {
    val costs = IntArray(2  shl 22) { Int.MAX_VALUE }
    val open = PriorityQueue<OpenNode>(compareBy { it.first })

    val startState = TravelState(start, TravelDir.RT, 1)
    costs[startState.encoded] = map.getCost(startState)
    open += OpenNode(0, startState)

    while (open.isNotEmpty()) {
        val state = open.poll().second
        if (state.pos == dest) {
            return state.minCost
        }

        state.nexts(map, minStraight, maxStraight).forEach {
            val oldCost = costs[it.encoded]
            val newCost = state.minCost + map.getCost(it)
            if (newCost < oldCost) {
                costs[it.encoded] = newCost
                it.minCost = newCost
                open += OpenNode(newCost - it.pos.x - it.pos.y, it)
            }
        }
    }
    return -1
}

fun List<String>.getCost(state: TravelState): Int {
    return get(state.pos.y)[state.pos.x].digitToInt()
}

data class TravelState(
    val pos: Vec2i,
    val dir: TravelDir,
    val straightCnt: Int
) {
    val encoded: Int = (pos.y shl (14)) + (pos.x shl 6) + (straightCnt.shl(2)) + dir.ordinal
    var minCost = 0

    fun nexts(map: List<String>, minStraight: Int, maxStraight: Int): List<TravelState> {
        return if (straightCnt < minStraight) {
            listOf(TravelState(pos + dir.step, dir, straightCnt + 1)).filter {
                it.pos.x in map[0].indices && it.pos.y in map.indices
            }

        } else {
            TravelDir.entries.map {
                val cnt = if (it == dir) straightCnt + 1 else 1
                TravelState(pos + it.step, it, cnt)
            }.filter {
                it.dir.step dot dir.step >= 0
                        && it.pos.x in map[0].indices && it.pos.y in map.indices
                        && it.straightCnt <= maxStraight
            }
        }
    }
}

enum class TravelDir(val step: Vec2i) {
    UP(Vec2i(0, -1)),
    DN(Vec2i(0, 1)),
    LT(Vec2i(-1, 0)),
    RT(Vec2i(1, 0)),
}
