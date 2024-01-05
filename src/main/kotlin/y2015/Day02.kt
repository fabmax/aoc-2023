package y2015

import AocPuzzle
import Vec3i

fun main() = Day02.runAll()

object Day02 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        return input
            .map { Vec3i(it, 'x') }
            .sumOf { dimens ->
                val a = dimens.x * dimens.y
                val b = dimens.x * dimens.z
                val c = dimens.y * dimens.z
                2*a + 2*b + 2*c + minOf(a, b, c)
            }
    }

    override fun solve2(input: List<String>): Int {
        return input
            .map { Vec3i(it, 'x') }
            .sumOf { dimens ->
                val l = 2 * minOf(dimens.x + dimens.y, dimens.x + dimens.z, dimens.y + dimens.z)
                l + dimens.x * dimens.y * dimens.z
            }
    }
}