package day08

import AocPuzzle
import leastCommonMultiple

fun main() = Day08.runAll()

object Day08 : AocPuzzle<Int, Long>() {

    override fun solve1(input: List<String>): Int {
        val instructions = input.first().map { instr -> Instruction.entries.first { it.id == instr } }
        val network = parseNetwork(input.drop(2))
        return network["AAA"]!!.walk(instructions) { it.name == "ZZZ" }
    }

    override fun solve2(input: List<String>): Long {
        val instructions = input.first().map { instr -> Instruction.entries.first { it.id == instr } }
        val network = parseNetwork(input.drop(2))

        val distances = network.values
            .filter { it.name.endsWith("A") }.toMutableList()
            .map { start -> start.walk(instructions) { it.name.endsWith("Z") } }

        return leastCommonMultiple(distances)
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
