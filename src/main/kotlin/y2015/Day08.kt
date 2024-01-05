package y2015

import AocPuzzle

fun main() = Day08.runAll()

object Day08 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val codeLen = input.sumOf { it.length }
        val regex = Regex("""(\\\\)|(\\")|\\x([0-9a-f]{2})""")
        val memLen = input.sumOf {
            var unescaped = it.removeSurrounding("\"")
            var idx = 0
            do {
                val match = regex.find(unescaped, idx)
                match?.let {
                    unescaped = unescaped.replaceRange(match.range, "_")
                    idx = match.range.first + 1
                }
            } while (match != null)
            unescaped.length
        }
        return codeLen - memLen
    }

    override fun solve2(input: List<String>): Int {
        val codeLen = input.sumOf { it.length }
        val encodedLen = input.sumOf {
            val escapeChars = it.count { c -> c == '\\' || c == '\"' }
            val cnt = (it.length - escapeChars) + escapeChars * 2 + 2
            cnt
        }
        return encodedLen - codeLen
    }
}