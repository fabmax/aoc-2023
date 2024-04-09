package y2015

import AocPuzzle

fun main() = Day11.runAll()

object Day11 : AocPuzzle<String, String>() {
    override fun solve1(input: List<String>): String = Password(input[0]).nextValid()

    override fun solve2(input: List<String>): String {
        val passwd = Password(input[0])
        passwd.nextValid()
        passwd.inc()
        return passwd.nextValid()
    }

    fun Password(string: String): Password {
        return Password(string.map { it.code - 'a'.code }.toMutableList())
    }

    class Password(val chars: MutableList<Int>) {
        operator fun inc(): Password {
            for (i in chars.indices.reversed()) {
                chars[i]++
                if (chars[i] >= 26) {
                    chars[i] = 0
                } else {
                    break
                }
            }
            return this
        }

        fun nextValid(): String {
            while (!isValid()) {
                inc()
            }
            return toString()
        }

        fun isValid(): Boolean {
            var pairCount = 0
            var i = 0
            while (i < chars.size - 1) {
                if (chars[i] == chars[i+1]) {
                    pairCount++
                    i++
                }
                i++
            }
            return pairCount >= 2
                    && chars.windowed(3).any { (a, b, c) -> c == b + 1 && b == a + 1 }
                    && chars.none { it == 'i'.code || it == 'o'.code || it == 'l'.code }
        }

        override fun toString(): String {
            return chars.map { (it + 'a'.code).toChar() }.joinToString("")
        }
    }
}