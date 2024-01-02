package y2022.day20

import AocPuzzle

fun main() = Day20.runAll()

object Day20 : AocPuzzle<Long, Long>() {
    override fun solve1(input: List<String>): Long {
        val entries = input.mapIndexed { i, txt -> ListEntry(txt.toLong(), i) }
        val mixed = entries.mix(1)

        val indexOfZero = mixed.indexOfFirst { it.value == 0L }
        val x1 = mixed[(indexOfZero + 1000) % entries.size].value
        val x2 = mixed[(indexOfZero + 2000) % entries.size].value
        val x3 = mixed[(indexOfZero + 3000) % entries.size].value
        return x1 + x2 + x3
    }

    override fun solve2(input: List<String>): Long {
        val entries = input.mapIndexed { i, txt -> ListEntry(txt.toLong() * 811589153L, i) }
        val mixed = entries.mix(10)

        val indexOfZero = mixed.indexOfFirst { it.value == 0L }
        val x1 = mixed[(indexOfZero + 1000) % entries.size].value
        val x2 = mixed[(indexOfZero + 2000) % entries.size].value
        val x3 = mixed[(indexOfZero + 3000) % entries.size].value
        return x1 + x2 + x3
    }

    fun List<ListEntry>.mix(rounds: Int): List<ListEntry> {
        val mixed = toMutableList()
        repeat(rounds) {
            forEach { entry ->
                var newPos = entry.listPos + entry.value
                if (newPos < 0) newPos = newPos % lastIndex + lastIndex
                if (newPos > lastIndex) newPos %= lastIndex

                mixed.removeAt(entry.listPos)
                if (newPos < entry.listPos) {
                    mixed.add(newPos.toInt(), entry)
                } else {
                    mixed.add(newPos.toInt(), entry)
                }
                mixed.forEachIndexed { j, it -> it.listPos = j }
            }
        }
        return mixed
    }

    class ListEntry(val value: Long, var listPos: Int)
}