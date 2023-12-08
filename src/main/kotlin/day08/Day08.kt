package day08

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
    part1(readInput("day08.txt", dropBlanks = false))
    part2(readInput("day08.txt", dropBlanks = false))
}

fun part1(lines: List<String>) {
    val instructions = lines.first().map { instr -> Instruction.entries.first { it.id == instr } }
    val network = parseNetwork(lines.drop(2))

    var stepCount = 0
    var instructionPtr = 0
    var node = network["AAA"]!!
    while (node.name != "ZZZ") {
        node = node.next(instructions[instructionPtr])
        instructionPtr = (instructionPtr + 1) % instructions.size
        stepCount++
    }

    println("answer part 1: $stepCount")
}

fun part2(lines: List<String>) {
    val instructions = lines.first().map { instr -> Instruction.entries.first { it.id == instr } }
    val network = parseNetwork(lines.drop(2))

    val nodePositions = network.values.filter { it.name.endsWith("A") }.toMutableList()
    val distances = mutableMapOf<String, Int>()

    var stepCount = 0
    var instructionPtr = 0
    while (nodePositions.isNotEmpty()) {
        val instruction = instructions[instructionPtr]
        instructionPtr = (instructionPtr + 1) % instructions.size
        for (i in nodePositions.indices) {
            nodePositions[i] = nodePositions[i].next(instruction)
        }
        stepCount++

        val dests = nodePositions.filter { it.name.endsWith("Z") }
        dests.forEach { distances[it.name] = stepCount }
        nodePositions.removeAll(dests)
    }

    val primes = findPrimes(distances.values.max())
    val primeFactors = distances.values.flatMap { findPrimeFactors(it, primes) }.distinct()
    val answer = primeFactors.fold(1L) { prod, value -> prod * value }
    println("answer part 2: $answer")
}

fun findPrimeFactors(number: Int, primes: List<Int>): List<Int> {
    return primes.filter { prime -> number % prime == 0 }
}

fun findPrimes(upperLimit: Int): List<Int> = (2..upperLimit).filter { isPrime(it) }

fun isPrime(x: Int): Boolean = (2 .. (x / 2)).none { x % it == 0 }

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
