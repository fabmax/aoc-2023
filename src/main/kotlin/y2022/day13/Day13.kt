package y2022.day13

import AocPuzzle
import splitByBlankLines
import takeAndRemoveWhile
import kotlin.math.min

fun main() = Day13.runAll()

object Day13 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        var answer = 0
        input.splitByBlankLines().forEachIndexed { i, pair ->
            val leftPacket = parsePacket(ArrayDeque(pair[0].toList()))
            val rightPacket = parsePacket(ArrayDeque(pair[1].toList()))
            val order = checkOrder(leftPacket, rightPacket)
            if (order == CompareResult.RIGHT_ORDER) {
                answer += i + 1
            }
        }

        return answer
    }

    override fun solve2(input: List<String>): Int {
        val packets = (input + "[[2]]" + "[[6]]").filter { it.isNotBlank() }.map { parsePacket(ArrayDeque(it.toList())) }
        val sorted = packets.sortedWith { a, b -> checkOrder(a, b).order }
        val idx2 = sorted.indexOfFirst { it.toString() == "[[2]]" } + 1
        val idx6 = sorted.indexOfFirst { it.toString() == "[[6]]" } + 1
        return idx2 * idx6
    }

    fun parsePacket(encoded: ArrayDeque<Char>): ListData {
        check(encoded.removeFirst() == '[')
        val data = mutableListOf<Data>()
        while (encoded.first() != ']') {
            when (encoded.first()) {
                ',' -> encoded.removeFirst()
                '[' -> data += parsePacket(encoded)
                else -> data += IntData(encoded.takeAndRemoveWhile { it.isDigit() }.joinToString("").toInt())
            }
        }
        encoded.removeFirst()
        return ListData(data)
    }

    fun checkOrder(left: Data, right: Data): CompareResult {
        if (left is IntData && right is IntData) {
            return when {
                left.i < right.i -> CompareResult.RIGHT_ORDER
                left.i > right.i -> CompareResult.WRONG_ORDER
                else -> CompareResult.EQUAL
            }

        } else {
            val leftList = if (left is ListData) left else ListData(listOf(left))
            val rightList = if (right is ListData) right else ListData(listOf(right))

            val decider = (0 until min(leftList.data.size, rightList.data.size)).dropWhile {
                checkOrder(leftList.data[it], rightList.data[it]) == CompareResult.EQUAL
            }.firstOrNull()

            return if (decider == null) {
                // all items equal until one list ran out of items
                if (rightList.data.size < leftList.data.size) {
                    // right list ran out of items first -> wrong order
                    CompareResult.WRONG_ORDER
                } else {
                    // left list ran out of items first -> right order
                    CompareResult.RIGHT_ORDER
                }
            } else {
                checkOrder(leftList.data[decider], rightList.data[decider])
            }
        }
    }

    sealed class Data

    data class IntData(val i: Int) : Data() {
        override fun toString() = "$i"
    }

    data class ListData(val data: List<Data>) : Data() {
        override fun toString() = "[${data.joinToString(",")}]"
    }

    enum class CompareResult(val order: Int) {
        RIGHT_ORDER(-1),
        WRONG_ORDER(1),
        EQUAL(0)
    }
}