package y2015

import AocPuzzle
import extractNumbers

fun main() = Day06.runAll()

object Day06 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val states = BooleanArray(1_000_000)
        input.forEach {
            val op = when {
                it.startsWith("turn on") -> Operation.TURN_ON
                it.startsWith("turn off") -> Operation.TURN_OFF
                it.startsWith("toggle") -> Operation.TOGGLE
                else -> error(it)
            }
            val (xFrom, yFrom, xTo, yTo) = it.extractNumbers()
            for (y in yFrom..yTo) {
                for (x in xFrom..xTo) {
                    states[y * 1000 + x] = op.apply(states[y * 1000 + x])
                }
            }
        }
        return states.count { it }
    }

    override fun solve2(input: List<String>): Int {
        val states = IntArray(1_000_000)
        input.forEach {
            val op = when {
                it.startsWith("turn on") -> Operation.TURN_ON
                it.startsWith("turn off") -> Operation.TURN_OFF
                it.startsWith("toggle") -> Operation.TOGGLE
                else -> error(it)
            }
            val (xFrom, yFrom, xTo, yTo) = it.extractNumbers()
            for (y in yFrom..yTo) {
                for (x in xFrom..xTo) {
                    states[y * 1000 + x] = op.applyBrightness(states[y * 1000 + x])
                }
            }
        }
        return states.sum()
    }

    enum class Operation {
        TURN_ON {
            override fun apply(state: Boolean): Boolean = true
            override fun applyBrightness(state: Int): Int = state + 1
        },
        TURN_OFF {
            override fun apply(state: Boolean): Boolean = false
            override fun applyBrightness(state: Int): Int = (state - 1).coerceAtLeast(0)
        },
        TOGGLE {
            override fun apply(state: Boolean): Boolean = !state
            override fun applyBrightness(state: Int): Int = state + 2
        };

        abstract fun apply(state: Boolean): Boolean
        abstract fun applyBrightness(state: Int): Int
    }
}