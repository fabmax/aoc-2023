package y2023.day25

import AocPuzzle
import kotlin.random.Random

fun main() = Day25.runPuzzle()

object Day25 : AocPuzzle<Int, Unit>() {

    val rand = Random(1337)

    override fun solve1(input: List<String>): Int {
        val nodes = input.map {
            val (node, cons) = it.split(": ")
            Node(node, cons.split(" "))
        }.associateBy { it.name }.toMutableMap()

        nodes.values.flatMap { it.connectedNames }.filter { it !in nodes }.forEach { nodes[it] = Node(it, emptyList()) }
        nodes.values.forEach { it.connect(it.connectedNames.map { id -> nodes[id]!! }) }

        val nodeList = nodes.values.toList()
        val edgeOccurrences = mutableMapOf<Edge, Int>()
        for (k in 0..500) {
            val from = nodeList.random(rand)
            val to = nodeList.random(rand)
            if (from != to) {
                from.findPathTo(to).forEach {
                    edgeOccurrences[it] = 1 + (edgeOccurrences[it] ?: 0)
                }
            }
        }

        val ranked = edgeOccurrences.toList().sortedByDescending { it.second }
        ranked.take(5).forEach { (ed, _) -> nodes[ed.a]!!.disconnect(nodes[ed.b]!!) }
        //ranked.take(10).forEach { (ed, cnt) -> println("$ed : $cnt") }

        val islandA = nodes[ranked[0].first.a]!!.collect().size
        val islandB = nodes[ranked[0].first.b]!!.collect().size
        return islandA * islandB
    }

    class Node(val name: String, val connectedNames: List<String>) {
        val connections = mutableSetOf<Node>()

        fun connect(others: Collection<Node>) {
            connections += others
            others.forEach { it.connections += this }
        }

        fun disconnect(node: Node) {
            connections.remove(node)
            node.connections.remove(this)
        }

        fun collect(result: MutableSet<Node> = mutableSetOf()): Set<Node> {
            if (result.add(this)) {
                connections.forEach { it.collect(result) }
            }
            return result
        }

        fun findPathTo(dest: Node): Set<Edge> {
            val recFunc = DeepRecursiveFunction<Args, Set<Edge>> { args ->
                if (!args.visited.add(args.self)) return@DeepRecursiveFunction emptySet()
                for (c in args.self.connections.shuffled(rand)) {
                    if (c in args.visited) continue
                    if (c == dest || callRecursive(args.copy(self = c)).isNotEmpty()) {
                        args.result += Edge(args.self, c)
                        break
                    }
                }
                return@DeepRecursiveFunction args.result
            }
            return recFunc.invoke(Args(this, dest))
        }

        data class Args(
            val self: Node,
            val dest: Node,
            val result: MutableSet<Edge> = mutableSetOf(),
            val visited: MutableSet<Node> = mutableSetOf()
        )
    }

    fun Edge(ndA: Node, ndB: Node): Edge = Edge(minOf(ndA.name, ndB.name), maxOf(ndA.name, ndB.name))

    data class Edge(val a: String, val b: String) {
        override fun toString() = "$a -> $b"
    }
}