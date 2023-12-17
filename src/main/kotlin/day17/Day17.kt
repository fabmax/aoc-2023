package day17

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import de.fabmax.kool.util.PriorityQueue

fun main() = Day17().start()

class Day17 : AocPuzzle() {
    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val start = Vec2i.ZERO
        val dest = Vec2i(input[0].lastIndex, input.lastIndex)
        val answer1 = dijkstra(start, dest, input, 0, 3)
        val answer2 = dijkstra(start, dest, input, 4, 10)

        return answer1 to answer2
    }
}

typealias OpenNode = Pair<Int, TravelState>

fun dijkstra(start: Vec2i, dest: Vec2i, map: List<String>, minStraight: Int, maxStraight: Int): Int {
    val costs = mutableMapOf<TravelState, Int>()
    val open = PriorityQueue<OpenNode>(compareBy { (cost, _) -> cost })

    val startState = TravelState(start, TravelDir.RT, 1)
    costs[startState] = map.getCost(startState)
    open += OpenNode(0, startState)

    while (open.isNotEmpty()) {
        val (cost, state) = open.poll()
        if (state.pos == dest) {
            return cost
        }

        state.nexts(map, minStraight, maxStraight).forEach {
            val oldCost = costs[it] ?: Int.MAX_VALUE
            val newCost = cost + map.getCost(it)
            if (newCost < oldCost) {
                it.prev = state

                check (state.dir != it.dir || state.straightCnt < it.straightCnt)
                check (it.pos - it.dir.step == state.pos)

                costs[it] = newCost
                open += OpenNode(newCost, it)
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
    var prev: TravelState? = null

    fun nexts(map: List<String>, minStraight: Int, maxStraight: Int): List<TravelState> {
        if (straightCnt < minStraight) {
            return listOf(TravelState(pos + dir.step, dir, straightCnt + 1)).filter {
                it.pos.x in map[0].indices && it.pos.y in map.indices
            }
        }

        val opp = when (dir) {
            TravelDir.UP -> TravelDir.DN
            TravelDir.DN -> TravelDir.UP
            TravelDir.LT -> TravelDir.RT
            TravelDir.RT -> TravelDir.LT
        }
        return TravelDir.entries.map {
            val cnt = if (it == dir) straightCnt + 1 else 1
            TravelState(pos + it.step, it, cnt)
        }.filter {
            it.dir != opp
                    && it.pos.x in map[0].indices && it.pos.y in map.indices
                    && it.straightCnt <= maxStraight
        }
    }
}

enum class TravelDir(val step: Vec2i) {
    UP(Vec2i(0, -1)),
    DN(Vec2i(0, 1)),
    LT(Vec2i(-1, 0)),
    RT(Vec2i(1, 0)),
}
