package y2015

import AocPuzzle
import java.security.MessageDigest
import kotlin.experimental.and
import kotlin.experimental.or

fun main() = Day04.runAll()

object Day04 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val digest = MessageDigest.getInstance("MD5")
        val s = input[0]
        var counter = 0
        while (true) {
            val hash = digest.digest("${s}${counter}".toByteArray())
            val check = hash[0] or hash[1] or (hash[2] and 0xf0.toByte())
            if (check == 0.toByte()) {
                return counter
            }
            counter++
        }
    }

    override fun solve2(input: List<String>): Int {
        val digest = MessageDigest.getInstance("MD5")
        val s = input[0]
        var counter = 0
        while (true) {
            val hash = digest.digest("${s}${counter}".toByteArray())
            val check = hash[0] or hash[1] or hash[2]
            if (check == 0.toByte()) {
                return counter
            }
            counter++
        }
    }
}