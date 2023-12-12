package day12

import AocPuzzle

fun main() = Day12().start()

class Day12 : AocPuzzle() {

    override val answer1 = 7753L
    override val answer2 = 280382734828319L

    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val patterns = input.map { line ->
            val (pattern, shape) = line.split(" ")
            val counts = shape.split(",").map { it.toInt() }
            pattern to counts
        }

        val answer1 = patterns.sumOf { (p, s) ->
            //matchBruteForce(p, s).size
            matchRecursive(p, s)
        }

        val answer2 = patterns.sumOf { (p, s) ->
            val expP = "$p?$p?$p?$p?$p"
            val expS = s + s + s + s + s
            matchRecursive(expP, expS)
        }

        return answer1 to answer2
    }

    /**
     * Fancy (memoized) recursive method: Wouldn't have come up with that on my own without browsing through several
     * discussions and tips.
     */
    private fun matchRecursive(pattern: String, shape: List<Int>): Long {
        val resultCache = mutableMapOf<MatchState, Long>()

        fun memoize(state: MatchState): Long {
            resultCache[state]?.let { return it }

            val requiredGroupSize = shape.getOrElse(state.groupIndex) { 0 }
            val result = when {
                // terminal checks: either successfully matched (return 1) or not (return 0)
                state.groupIndex > shape.size -> 0L
                state.groupProgress < requiredGroupSize && !state.isInsideGroup -> 0L
                state.groupProgress > requiredGroupSize -> 0L
                state.nextCharIndex == pattern.length -> {
                    if (state.groupProgress == requiredGroupSize && state.groupIndex == shape.size - 1) 1L else 0L
                }

                // continue with parsing / matching
                else -> when (val c = pattern[state.nextCharIndex]) {
                    '.' -> memoize(state.advance(nextInsideGroup = false))
                    '#' -> memoize(state.advance(nextInsideGroup = true))
                    '?' -> memoize(state.advance(nextInsideGroup = false)) +
                           memoize(state.advance(nextInsideGroup = true))

                    else -> error("Unexpected pattern char: $c")
                }
            }

            return result.also { resultCache[state] = it }
        }

        return memoize(MatchState())
    }

    data class MatchState(
        val nextCharIndex: Int = 0,
        val groupIndex: Int = -1,
        val groupProgress: Int = 0,
        val isInsideGroup: Boolean = false
    ) {
        fun advance(nextInsideGroup: Boolean): MatchState {
            val nextGroupIndex = when {
                !isInsideGroup && nextInsideGroup -> groupIndex + 1     // entering new group
                else -> groupIndex
            }
            val nextGroupProgress = when {
                !isInsideGroup && nextInsideGroup -> 1                  // entering new group
                nextInsideGroup -> groupProgress + 1                    // progress in group
                else -> groupProgress                                   // leaving group or outside group
            }
            return copy(
                nextCharIndex = nextCharIndex + 1,
                groupIndex = nextGroupIndex,
                groupProgress = nextGroupProgress,
                isInsideGroup = nextInsideGroup
            )
        }
    }

    /**
     * Not used anymore, only here for melancholic reasons: My initial terrible part 1 solution.
     */
    private fun matchBruteForce(pattern: String, shape: List<Int>): Set<String> {
        val requiredPlaces = shape.sum()
        val maxPlaces = pattern.count { c -> c == '?' || c == '#' }
        val numWildcards = pattern.count { c -> c == '?' }
        val numOptions = (1 shl numWildcards)

        val fittedPatterns = mutableSetOf<String>()

        for (mask in 0 ..< numOptions) {
            val numOnes = mask.countOneBits()
            if (maxPlaces - numOnes < requiredPlaces) {
                continue
            }

            var replaceI = 0
            val testPattern = pattern.map {
                if (it == '?') {
                    if (mask and (1 shl replaceI++) != 0) '.' else '?'
                } else {
                    it
                }
            }.joinToString("")

            matchGreedy(testPattern, shape)?.let {
                check(it.length == pattern.length)
                fittedPatterns += it
            }
        }

        return fittedPatterns
    }

    private fun matchGreedy(pattern: String, shape: List<Int>): String? {
        var shapeI = 0
        var patternPos = 0
        var matched = ""

        while (shapeI < shape.size) {
            if (patternPos >= pattern.length) {
                return null
            }

            val count = shape[shapeI]
            val leadingSpace = pattern.substring(patternPos).takeWhile { it == '.' }
            patternPos += leadingSpace.length
            matched += leadingSpace

            val group = pattern.substring(patternPos).takeWhile { it == '#' || it == '?' }
            if (group.length >= count) {
                if (group.count { it == '#' || it == '?' } >= count && (group.length == count || group[count] == '?')) {
                    shapeI++
                    patternPos += count
                    repeat(count) { matched += "#" }

                    if (patternPos < pattern.length) {
                        patternPos++
                        matched += "."
                    }

                } else if (group[0] == '?') {
                    patternPos++
                    matched += '.'

                } else {
                    // group starts with unmatched '#'
                    return null
                }

            } else if (group.all { it == '?' }) {
                repeat(group.length) { matched += "." }
                patternPos += group.length

            } else {
                // group contains unmatched '#'
                return null
            }
        }

        if (pattern.length > patternPos && pattern.substring(patternPos).any { it == '#' }) {
            // remaining pattern has unmatched '#'
            return null
        } else {
            repeat(pattern.length - patternPos) { matched += '.' }
        }

        return matched
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