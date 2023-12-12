package day12

import AocPuzzle

fun main() = Day12().start()

class Day12 : AocPuzzle() {

    override val answer1 = 7753L
    override val answer2 = 280382734828319L

    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val conditionsAndGroups = input.map { line ->
            val (conditions, groups) = line.split(" ")
            val counts = groups.split(",").map { it.toInt() }
            conditions to counts
        }

        val answer1 = conditionsAndGroups.sumOf { (conditions, groups) ->
            countArrangements(conditions, groups)
        }

        val answer2 = conditionsAndGroups.sumOf { (conds, grps) ->
            val conditions = (1..5).joinToString(separator = "?") { conds }
            val groups = (1..5).flatMap { grps }
            countArrangements(conditions, groups)
        }

        return answer1 to answer2
    }

    private fun countArrangements(conditions: String, groups: List<Int>): Long {
        val cache = mutableMapOf<Pair<Int, Int>, Long>()

        fun recurse(charPos: Int, groupPos: Int): Long {
            cache[charPos to groupPos]?.let { return it }
            var count = 0L

            if (groupPos == groups.size) {
                count = if ((charPos..conditions.lastIndex).none { conditions[it] == '#' }) 1L else 0L

            } else {
                val groupSize = groups[groupPos]
                for (i in charPos .. conditions.length - groupSize) {
                    val groupEnd = i + groupSize
                    val isGroupFit = (i until groupEnd).all { conditions[it] != '.' }
                    val isDelimited = (conditions.length == groupEnd || conditions[groupEnd] != '#')

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

    init {
        testInput(
            text = """
                ???.### 1,1,3
                .??..??...?##. 1,1,3
                ?#?#?#?#?#?#?#? 1,3,1,6
                ????.#...#... 4,1,1
                ????.######..#####. 1,6,5
                ?###???????? 3,2,1
            """.trimIndent(),
            expected1 = 21L,
            expected2 = 525152L
        )
    }
}