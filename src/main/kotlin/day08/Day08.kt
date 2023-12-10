package day08

import AocPuzzle
import findPrimeFactors
import findPrimes

fun main() = Day08().start()

class Day08 : AocPuzzle() {

    override val answer1 = 16409
    override val answer2 = 11795205644011L

    override fun solve(input: List<String>): Pair<Any, Any> {
        val answer1 = part1(input)
        val answer2 = part2(input)
        return answer1 to answer2
    }

    override fun test1(input: List<String>): Any = part1(input)

    override fun test2(input: List<String>): Any = part2(input)

    private fun part1(input: List<String>): Int {
        val instructions = input.first().map { instr -> Instruction.entries.first { it.id == instr } }
        val network = parseNetwork(input.drop(2))
        return network["AAA"]!!.walk(instructions) { it.name == "ZZZ" }
    }

    private fun part2(input: List<String>): Long {
        val instructions = input.first().map { instr -> Instruction.entries.first { it.id == instr } }
        val network = parseNetwork(input.drop(2))

        val distances = network.values
            .filter { it.name.endsWith("A") }.toMutableList()
            .map { start -> start.walk(instructions) { it.name.endsWith("Z") } }

        val primes = findPrimes(distances.max())
        return distances
            .flatMap { findPrimeFactors(it, primes) }
            .distinct()
            .fold(1L) { prod, value -> prod * value }
    }

    init {
        testInput(
            text = """
                RL

                AAA = (BBB, CCC)
                BBB = (DDD, EEE)
                CCC = (ZZZ, GGG)
                DDD = (DDD, DDD)
                EEE = (EEE, EEE)
                GGG = (GGG, GGG)
                ZZZ = (ZZZ, ZZZ)
            """.trimIndent(),
            expected1 = 2,
            parts = PART1
        )

        testInput(
            text = """
                LLR
            
                AAA = (BBB, BBB)
                BBB = (AAA, ZZZ)
                ZZZ = (ZZZ, ZZZ)
            """.trimIndent(),
            expected1 = 6,
            parts = PART1
        )

        testInput(
            text = """
                LR
           
                11A = (11B, XXX)
                11B = (XXX, 11Z)
                11Z = (11B, XXX)
                22A = (22B, XXX)
                22B = (22C, 22C)
                22C = (22Z, 22Z)
                22Z = (22B, 22B)
                XXX = (XXX, XXX)
            """.trimIndent(),
            expected2 = 6L,
            parts = PART2
        )
    }

}

fun parseNetwork(lines: List<String>): Map<String, Node> {
    val nodes = mutableMapOf<String, Node>()
    lines.forEach {
        val name = it.substringBefore('=').trim()
        val (left, right) = it.substringAfter('=').trim().removeSurrounding("(", ")").split(", ")
        nodes[name] = Node(name, left, right)
    }
    nodes.values.forEach {
        it.left = nodes[it.leftName]!!
        it.right = nodes[it.rightName]!!
    }
    return nodes
}

class Node(val name: String, val leftName: String, val rightName: String) {
    lateinit var left: Node
    lateinit var right: Node

    fun next(instruction: Instruction): Node {
        return if (instruction == Instruction.L) left else right
    }

    fun walk(instructions: List<Instruction>, isDestination: (Node) -> Boolean): Int {
        var nodeIt = this
        var instructionPtr = 0
        var stepCount = 0
        while (!isDestination(nodeIt)) {
            nodeIt = nodeIt.next(instructions[instructionPtr])
            instructionPtr = (instructionPtr + 1) % instructions.size
            stepCount++
        }
        return stepCount
    }
}

enum class Instruction(val id: Char) {
    L('L'),
    R('R')
}
