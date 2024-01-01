package y2022.day18

import AocPuzzle
import de.fabmax.kool.math.Vec3i

fun main() = Day18.runAll()

object Day18 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val cubes = input.map {  l ->
            val (x, y, z) = l.split(",").map { it.toInt() }
            Vec3i(x, y, z)
        }.toSet()

        return cubes.sumOf { pos -> SIDES.count { it + pos !in cubes } }
    }

    override fun solve2(input: List<String>): Int {
        val cubes = input.map {  l ->
            val (x, y, z) = l.split(",").map { it.toInt() }
            Vec3i(x, y, z)
        }.toSet()

        val min = Vec3i(cubes.minOf { it.x }, cubes.minOf { it.y }, cubes.minOf { it.z })
        val max = Vec3i(cubes.maxOf { it.x }, cubes.maxOf { it.y }, cubes.maxOf { it.z })

        val airPockets = mutableSetOf<Vec3i>()
        val open = mutableSetOf<Vec3i>()

        fun collectAirPocket(start: Vec3i, result: MutableSet<Vec3i>): Boolean {
            val border = mutableSetOf(start)
            while (border.isNotEmpty()) {
                val current = border.first()
                border -= current
                result += current

                if (current.x !in min.x .. max.x ||
                    current.y !in min.y .. max.y ||
                    current.z !in min.z .. max.z
                ) {
                    return false
                }

                SIDES.map { it + current }
                    .filter { it !in border && it !in result && it !in cubes }
                    .forEach { border += it }
            }
            return true
        }

        return cubes
            .flatMap { c -> SIDES.map { c + it } }
            .count { side ->
                when (side) {
                    in cubes -> false
                    in airPockets -> false
                    in open -> true
                    else -> {
                        val result = mutableSetOf<Vec3i>()
                        if (collectAirPocket(side, result)) {
                            airPockets += result
                        } else {
                            open += result
                        }
                        side !in airPockets
                    }
                }
            }
    }

    val SIDES = listOf(
        Vec3i.X_AXIS, Vec3i.NEG_X_AXIS,
        Vec3i.Y_AXIS, Vec3i.NEG_Y_AXIS,
        Vec3i.Z_AXIS, Vec3i.NEG_Z_AXIS,
    )
}