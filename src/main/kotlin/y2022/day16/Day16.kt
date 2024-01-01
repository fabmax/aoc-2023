package y2022.day16

import AocPuzzle
import kotlin.math.max

fun main() = Day16.runAll()

object Day16 : AocPuzzle<Int, Int>() {

    override fun solve1(input: List<String>): Int {
        val graph = parseGraph(input)
        val distances = mutableMapOf<String, Int>()
        graph.values.asSequence()
            .flatMap { from -> graph.values.asSequence().map { from to it } }
            .filter { (from, to) -> (from.name == "AA" || from.flowRate > 0) && to.flowRate > 0 }
            .forEach { (from, to) -> distances["${from.name}${to.name}"] = graph.findDistance(from, to) }
        val toBeOpened = graph.values.filter { it.flowRate > 0 }.toList()

        return graph["AA"]!!.walkPipes(0, emptySet(), toBeOpened, distances)
    }

    override fun solve2(input: List<String>): Int {
        val graph = parseGraph(input)
        val distances = mutableMapOf<String, Int>()
        graph.values.asSequence()
            .flatMap { from -> graph.values.asSequence().map { from to it } }
            .filter { (from, to) -> (from.name == "AA" || from.flowRate > 0) && to.flowRate > 0 }
            .forEach { (from, to) -> distances["${from.name}${to.name}"] = graph.findDistance(from, to) }
        val toBeOpened = graph.values.filter { it.flowRate > 0 }.toList()

        var best = 0
        for (mask in 0 until (1 shl toBeOpened.size)) {
            val cntA = mask.countOneBits()
            if (cntA >= toBeOpened.size / 2 && cntA <= toBeOpened.size / 2 + 1) {
                val setA = mutableListOf<Valve>()
                val setB = mutableListOf<Valve>()
                for (j in toBeOpened.indices) {
                    if (mask and (1 shl j) != 0) {
                        setA += toBeOpened[j]
                    } else {
                        setB += toBeOpened[j]
                    }
                }
                val result = graph["AA"]!!.walkPipes(4, emptySet(), setA, distances) +
                        graph["AA"]!!.walkPipes(4, emptySet(), setB, distances)
                best = max(best, result)
            }
        }
        return best
    }

    fun Valve.walkPipes(time: Int, opened: Set<Valve>, toBeOpened: List<Valve>, distances: Map<String, Int>): Int {
        if (time > 30) {
            return 0
        }

        val flow = flowRate * (30 - time)
        val nowOpened = opened + this

        return (toBeOpened - nowOpened)
            .asSequence()
            .map { it to distances["${name}${it.name}"]!! }
            .sortedByDescending { (node, dist) -> node.flowRate.toDouble() / dist }
            .take(5)
            .maxOfOrNull { (nextToOpen, dist) ->
                nextToOpen.walkPipes(time + 1 + dist, nowOpened, toBeOpened, distances) + flow
            } ?: flow
    }

    fun Map<String, Valve>.findDistance(from: Valve, to: Valve): Int {
        val dists = mutableMapOf(from to 0)
        val q = ArrayDeque<Valve>()
        q += from

        while (q.isNotEmpty()) {
            val current = q.removeFirst()
            val d = dists[current]!! + 1
            current.nextNames.map { get(it)!! }.forEach { next ->
                if (dists.getOrDefault(next, Int.MAX_VALUE) > d) {
                    q.add(next)
                    dists[next] = d
                }
            }
        }
        return dists[to]!!
    }

    fun parseGraph(input: List<String>): Map<String, Valve> {
        val graph = input.map { Valve(it) }.associateBy { it.name }
        graph.values.forEach { node ->
            node.nextNames.forEach { node.nexts += graph[it]!! }
        }
        return graph
    }

    fun Valve(input: String): Valve {
        val (a, b) = input.split(";")
        val name = a.substring(6..7)
        val flowRate = a.substringAfter('=').toInt()
        val nexts = b.substringAfter("valve").substringAfter(' ').split(", ")
        return Valve(name, flowRate, nexts)
    }

    data class Valve(val name: String, val flowRate: Int, val nextNames: List<String>) {
        val nexts = mutableListOf<Valve>()
    }
}