package y2022.day11

import AocPuzzle
import splitByBlankLines
import java.math.BigInteger

fun main() = Day11.runAll()

object Day11 : AocPuzzle<Int, Long>() {
    override fun solve1(input: List<String>): Int {
        val monkeys = input.splitByBlankLines().map { Monkey(it) }
        val modifier: (BigInteger) -> BigInteger = { wl -> wl / 3.toBigInteger() }
        repeat(20) { _ ->
            monkeys.forEach { it.turn(monkeys, modifier) }
        }
        val (maxA, maxB) = monkeys.sortedByDescending { it.inspectionCount }.take(2).map { it.inspectionCount }
        return maxA * maxB
    }

    override fun solve2(input: List<String>): Long {
        val monkeys = input.splitByBlankLines().map { Monkey(it) }
        val factor = monkeys.map { it.testDivisor }.reduce(BigInteger::times)
        val modifier: (BigInteger) -> BigInteger = { wl -> wl % factor }
        repeat(10000) { i ->
            monkeys.forEach { it.turn(monkeys, modifier) }
        }
        val (maxA, maxB) = monkeys.sortedByDescending { it.inspectionCount }.take(2).map { it.inspectionCount }
        return maxA.toLong() * maxB.toLong()
    }

    class Monkey(def: List<String>) {
        val testDivisor: BigInteger
        val trueMonkey: Int
        val falseMonkey: Int

        val op: Char
        val arg: BigInteger?

        val items = ArrayDeque<BigInteger>()
        var inspectionCount = 0

        init {
            items += def[1].removePrefix("  Starting items: ").split(", ").map { it.toBigInteger() }
            testDivisor = def[3].substringAfterLast(' ').toBigInteger()
            trueMonkey = def[4].substringAfterLast(' ').toInt()
            falseMonkey = def[5].substringAfterLast(' ').toInt()

            val operationDef = def[2].removePrefix("  Operation: new = old ")
            op = operationDef[0]
            arg = operationDef.substringAfterLast(' ').toBigIntegerOrNull()
        }

        fun turn(monkeys: List<Monkey>, worryModifier: (BigInteger) -> BigInteger) {
            while (items.isNotEmpty()) {
                inspectionCount++
                val old = items.removeFirst()
                val new = when (op) {
                    '+' -> old + (arg ?: old)
                    '*' -> old * (arg ?: old)
                    else -> error("unreachable")
                }
                val modded = worryModifier(new)
                val receivingMonkey = if (test(modded)) monkeys[trueMonkey] else monkeys[falseMonkey]
                receivingMonkey.items += modded
            }
        }

        fun test(item: BigInteger): Boolean = item % testDivisor == 0.toBigInteger()
    }
}