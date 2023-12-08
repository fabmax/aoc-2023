package day08

import findPrimeFactors
import findPrimes
import readInput

//
// val testInput = parseTestInput("""
//     LLR
//
//     AAA = (BBB, BBB)
//     BBB = (AAA, ZZZ)
//     ZZZ = (ZZZ, ZZZ)
// """.trimIndent(), dropBlanks = false)
//
// val testInput2 = parseTestInput("""
//     LR
//
//     11A = (11B, XXX)
//     11B = (XXX, 11Z)
//     11Z = (11B, XXX)
//     22A = (22B, XXX)
//     22B = (22C, 22C)
//     22C = (22Z, 22Z)
//     22Z = (22B, 22B)
//     XXX = (XXX, XXX)
// """.trimIndent(), dropBlanks = false)
//

fun main() {
    val lines = readInput("day08.txt", dropBlanks = false)
    val instructions = lines.first().map { instr -> Instruction.entries.first { it.id == instr } }
    val network = parseNetwork(lines.drop(2))

    // part 1
    network["AAA"]!!.walk(instructions) { it.name == "ZZZ" }
        .also { println("answer part 1: $it") }

    // part 2
    val distances = network.values
        .filter { it.name.endsWith("A") }.toMutableList()
        .map { start -> start.walk(instructions) { it.name.endsWith("Z") } }

    val primes = findPrimes(distances.max())
    distances
        .flatMap { findPrimeFactors(it, primes) }
        .distinct()
        .fold(1L) { prod, value -> prod * value }
        .also { println("answer part 2: $it") }
}

fun Node.walk(instructions: List<Instruction>, isDestination: (Node) -> Boolean): Int {
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
}

enum class Instruction(val id: Char) {
    L('L'),
    R('R')
}
