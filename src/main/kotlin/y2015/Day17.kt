package y2015

import AocPuzzle

fun main() = Day17.runAll()

object Day17 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val target = if (isTestRun()) 25 else 150
        val containers = input.mapIndexed { i, line -> Container(1 shl i, line.toInt()) }

        return (1 until (1 shl containers.size))
            .asSequence()
            .map { mask -> containers.filter { it.mask and mask != 0 }.sumOf { it.capacity } }
            .filter { it == target }
            .count()
    }

    override fun solve2(input: List<String>): Int {
        val target = if (isTestRun()) 25 else 150
        val containers = input.mapIndexed { i, line -> Container(1 shl i, line.toInt()) }

        val minCount = (1 until (1 shl containers.size))
            .asSequence()
            .map { mask -> mask to containers.filter { it.mask and mask != 0 }.sumOf { it.capacity } }
            .filter { (_, cap) -> cap == target }
            .minOf { (mask, _) -> mask.countOneBits() }

        return (1 until (1 shl containers.size))
            .asSequence()
            .filter { it.countOneBits() == minCount }
            .map { mask -> containers.filter { it.mask and mask != 0 }.sumOf { it.capacity } }
            .filter { it == target }
            .count()
    }

    data class Container(val mask: Int, val capacity: Int)
}