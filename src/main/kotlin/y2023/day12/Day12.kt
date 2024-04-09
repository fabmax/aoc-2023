package y2023.day12

import AocPuzzle

fun main() = Day12.runAll()

object Day12 : AocPuzzle<Long, Long>() {
    override fun solve1(input: List<String>): Long {
        val conditionsAndGroups = input.map { line ->
            val (conditions, groups) = line.split(" ")
            val counts = groups.split(",").map { it.toInt() }
            conditions to counts
        }
        return conditionsAndGroups.sumOf { (conditions, groups) -> countArrangements(conditions, groups) }
    }

    override fun solve2(input: List<String>): Long {
        val conditionsAndGroups = input.map { line ->
            val (conditions, groups) = line.split(" ")
            val counts = groups.split(",").map { it.toInt() }
            conditions to counts
        }
        return conditionsAndGroups.sumOf { (conds, grps) ->
            val conditions = (1..5).joinToString(separator = "?") { conds }
            val groups = (1..5).flatMap { grps }
            countArrangements(conditions, groups)
        }
    }

    private fun countArrangements(conditions: String, groups: List<Int>): Long {
        val cache = mutableMapOf<Pair<Int, Int>, Long>()
        fun recurse(charPos: Int, groupPos: Int): Long {
            cache[charPos to groupPos]?.let { return it }

            var count = 0L
            if (groupPos == groups.size) {
                count = if ((charPos..conditions.lastIndex).none { conditions[it] == '#' }) 1L else 0L

            } else {
                val minRequiredLen = (groupPos..groups.lastIndex).sumOf { groups[it] } + groups.lastIndex - groupPos
                for (i in charPos .. conditions.length - minRequiredLen) {
                    val groupEnd = i + groups[groupPos]
                    val isGroupFit = (i until groupEnd).all { conditions[it] != '.' }
                    val isDelimited = conditions.length == groupEnd || conditions[groupEnd] != '#'

                    if (isGroupFit && isDelimited) {
                        count += recurse(groupEnd + 1, groupPos + 1)
                    }
                    if (conditions[i] == '#') {
                        break
                    }
                }
            }
            return count.also { cache[charPos to groupPos] = it }
        }

        return recurse(0, 0)
    }
}