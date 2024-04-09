package y2022.day05

import AocPuzzle
import splitByBlankLines

fun main() = Day05.runAll()

object Day05 : AocPuzzle<String, String>() {
    override fun solve1(input: List<String>): String {
        val (startStacks, instructions) = input.splitByBlankLines()
        val stacks = parseStacks(startStacks)

        val parser = Regex("""\D*(\d+)\D*(\d+)\D*(\d+)""")
        instructions.forEach { instr ->
            val (move, from, to) = parser.matchEntire(instr)!!.groupValues.drop(1).map { it.toInt() }
            repeat(move) {
                stacks[to-1] += stacks[from-1].removeLast()
            }
        }
        return stacks.joinToString(separator = "") { "${it.last()}" }
    }

    override fun solve2(input: List<String>): String {
        val (startStacks, instructions) = input.splitByBlankLines()
        val stacks = parseStacks(startStacks)

        val parser = Regex("""\D*(\d+)\D*(\d+)\D*(\d+)""")
        instructions.forEach { instr ->
            val (move, from, to) = parser.matchEntire(instr)!!.groupValues.drop(1).map { it.toInt() }
            stacks[to-1] += stacks[from-1].takeLast(move)
            repeat(move) {
                stacks[from-1].removeLast()
            }
        }
        return stacks.joinToString(separator = "") { "${it.last()}" }
    }

    fun parseStacks(stackInput: List<String>): List<MutableList<Char>> {
        val numStacks = stackInput.last().last().digitToInt()
        val stacks = List<MutableList<Char>>(numStacks) { mutableListOf() }
        for (row in stackInput.lastIndex-1 downTo 0) {
            for (j in 0 until numStacks) {
                val c = stackInput[row].getOrNull(1 + j * 4)
                if (c != null && c != ' ') {
                    stacks[j] += c
                }
            }
        }
        return stacks
    }
}