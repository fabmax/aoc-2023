package y2022.day25

import AocPuzzle

fun main() = Day25.runAll()

object Day25 : AocPuzzle<String, Unit>() {
    override fun solve1(input: List<String>): String = input.sumOf { it.snafuToLong() }.toSnafu()

    fun Long.toSnafu(): String = generateSequence("" to this@toSnafu) { (txt, num) ->
        val c = snafuReverseMap[num % 5]
        val rem = num / 5 + if (c == '=' || c == '-') 1 else 0
        if (rem > 0 || num > 0) "${snafuReverseMap[num % 5]}$txt" to rem else null
    }.last().first

    fun String.snafuToLong(): Long = reversed().mapIndexed { i, c -> snafuMap[c]!! * 5L.pow(i) }.sum()

    fun Long.pow(n: Int) = if (n == 0) 1L else (1 ..< n).fold(this) { acc, _ -> acc * this }

    val snafuMap = mapOf(
        '2' to 2L,
        '1' to 1L,
        '0' to 0L,
        '-' to -1L,
        '=' to -2L,
    )

    val snafuReverseMap = mapOf(
        0L to '0',
        1L to '1',
        2L to '2',
        3L to '=',
        4L to '-',
    )
}