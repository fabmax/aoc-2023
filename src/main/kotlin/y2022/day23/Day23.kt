package y2022.day23

import AocPuzzle
import de.fabmax.kool.math.Vec2i

fun main() = Day23.runAll()

object Day23 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val elves = input.indices.asSequence()
            .flatMap { y -> input[y].indices.map { it to y } }
            .filter { (x, y) -> input[y][x] == '#' }
            .map { (x, y) -> Elv(Vec2i(x, y)) }
            .toList()

        val checkOrder = checkPositions.toMutableList()
        repeat(10) { _ ->
            val elvMap = elves.associateBy { it.pos }
            val proposals = mutableMapOf<Vec2i, MutableList<Elv>>()
            elves.forEach { it.proposeNextPos(elvMap, checkOrder, proposals) }
            proposals
                .filter { (_, elves) -> elves.size == 1 }
                .forEach { (pos, elves) -> elves[0].pos = pos }

            checkOrder.add(checkOrder.removeAt(0))
        }

        val min = Vec2i(elves.minOf { it.pos.x }, elves.minOf { it.pos.y })
        val max = Vec2i(elves.maxOf { it.pos.x }, elves.maxOf { it.pos.y })
        val size = max - min

        return (size.x + 1) * (size.y + 1) - elves.size
    }

    override fun solve2(input: List<String>): Int {
        val elves = input.indices.asSequence()
            .flatMap { y -> input[y].indices.map { it to y } }
            .filter { (x, y) -> input[y][x] == '#' }
            .map { (x, y) -> Elv(Vec2i(x, y)) }
            .toList()

        val checkOrder = checkPositions.toMutableList()
        var iterations = 0
        do {
            val elvMap = elves.associateBy { it.pos }
            val proposals = mutableMapOf<Vec2i, MutableList<Elv>>()
            elves.forEach { it.proposeNextPos(elvMap, checkOrder, proposals) }
            proposals
                .filter { (_, elves) -> elves.size == 1 }
                .forEach { (pos, elves) -> elves[0].pos = pos }

            checkOrder.add(checkOrder.removeAt(0))
            iterations++
        } while (proposals.isNotEmpty())

        return iterations
    }

    class Elv(var pos: Vec2i) {
        fun proposeNextPos(
            elvMap: Map<Vec2i, Elv>,
            checkOrder: List<Pair<List<Vec2i>, Vec2i>>,
            proposals: MutableMap<Vec2i, MutableList<Elv>>
        ) {
            if (allAdjacent.none { pos + it in elvMap }) {
                return
            }

            checkOrder
                .firstOrNull { (checkFields, _) -> checkFields.none { pos + it in elvMap } }
                ?.let { (_, moveDir) -> proposals.getOrPut(pos + moveDir) { mutableListOf() } += this }
        }
    }

    val N = Vec2i(0, -1)
    val S = Vec2i(0, 1)
    val W = Vec2i(-1, 0)
    val E = Vec2i(1, 0)

    val NE = N+E
    val NW = N+W
    val SE = S+E
    val SW = S+W

    val allAdjacent = listOf(N, S, W, E, NE, NW, SE, SW)

    val checkPositions = listOf(
        listOf(N, NE, NW) to N,
        listOf(S, SE, SW) to S,
        listOf(W, NW, SW) to W,
        listOf(E, NE, SE) to E,
    )
}