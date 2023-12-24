package day24

import AocPuzzle
import de.fabmax.kool.math.Vec3d

fun main() = Day24.runAll()

object Day24 : AocPuzzle<Int, Int>() {

    override fun solve1(input: List<String>): Int {
        val testAreaMin = if (isTestRun()) 7.0 else 200000000000000.0
        val testAreaMax = if (isTestRun()) 27.0 else 400000000000000.0

        val hailstones = input.map {
            it.split(" @ ").let { (p, v) -> HailStone(Vec3d(p), Vec3d(v)) }
        }

        var count = 0
        for (i in hailstones.indices) {
            for (j in i+1 until hailstones.size) {
                val a = hailstones[i]
                val b = hailstones[j]

                val x = intersectLinesXy(a.pos, a.pos + a.vel, b.pos, b.pos + b.vel)
                if (x != null &&
                    x.x in testAreaMin..testAreaMax &&
                    x.y in testAreaMin..testAreaMax
                ) {
                    val xyMask = Vec3d(1.0, 1.0, 0.0)
                    val da = (x - a.pos * xyMask) dot a.vel
                    val db = (x - b.pos * xyMask) dot b.vel
                    if (da > 0.0 && db > 0.0) {
                        count++
                    }
                }
            }
        }
        return count
    }

    fun Vec3d(str: String): Vec3d {
        val (x, y, z) = str.split(", ").map { it.toDouble() }
        return Vec3d(x, y, z)
    }

    fun intersectLinesXy(a1: Vec3d, a2: Vec3d, b1: Vec3d, b2: Vec3d): Vec3d? {
        val denom = (a1.x - a2.x) * (b1.y - b2.y) - (a1.y - a2.y) * (b1.x - b2.x)
        if (denom != 0.0) {
            // lines are not parallel
            val a = a1.x * a2.y - a1.y * a2.x
            val b = b1.x * b2.y - b1.y * b2.x
            val x = (a * (b1.x - b2.x) - b * (a1.x - a2.x)) / denom
            val y = (a * (b1.y - b2.y) - b * (a1.y - a2.y)) / denom
            return Vec3d(x, y, 0.0)
        }
        return null
    }

    data class HailStone(val pos: Vec3d, val vel: Vec3d)
}