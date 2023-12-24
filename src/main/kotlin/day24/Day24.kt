package day24

import AocPuzzle
import de.fabmax.kool.math.MutableVec3d
import de.fabmax.kool.math.Vec3d
import kotlin.math.abs

fun main() = Day24.runAll()

object Day24 : AocPuzzle<Int, Long>() {

    override fun solve1(input: List<String>): Int {
        val testAreaMin = if (isTestRun()) 7.0 else 200000000000000.0
        val testAreaMax = if (isTestRun()) 27.0 else 400000000000000.0

        val hailStones = input.map {
            it.split(" @ ").let { (p, v) -> HailStone(Vec3d(p), Vec3d(v)) }
        }

        var count = 0
        for (i in hailStones.indices) {
            for (j in i+1 until hailStones.size) {
                val a = hailStones[i]
                val b = hailStones[j]

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

    override fun solve2(input: List<String>): Long {
        val hailStones = input.map {
            it.split(" @ ").let { (p, v) -> HailStone(Vec3d(p), Vec3d(v)) }
        }

        val (h1, h2) = hailStones.take(2)
        var t1 = 0L
        var t2 = 0L

        var step1 = if (isTestRun()) 1 else 1e10.toLong()
        var step2 = if (isTestRun()) 1 else 1e10.toLong()
        var isStepA = true

        // approximate collision times 1 and 2
        var prevDist: Double
        var dist = Double.POSITIVE_INFINITY
        while (abs(step1) != 0L || abs(step2) != 0L) {
            do {
                prevDist = dist
                when (isStepA) {
                    true  -> t1 += step1
                    false -> t2 += step2
                }
                val a = h1.at(t1)
                val b = h2.at(t2)
                dist = hailStones.eval(a, b)
            } while (dist < prevDist)

            when (isStepA) {
                true  -> step1 /= -10
                false -> step2 /= -10
            }
            isStepA = !isStepA
        }

        // approximated collision times are 1 off -> do final refinement
        var bestDist = dist
        for (s1 in t1 - 5L .. t1 + 5L) {
            for (s2 in t2 - 5L .. t2 + 5L) {
                val a = h1.at(s1)
                val b = h2.at(s2)
                val d = hailStones.eval(a, b)
                if (d < bestDist) {
                    bestDist = d
                    t1 = s1
                    t2 = s2
                }
            }
        }

        val hailPos1 = h1.at(t1)
        val hailPos2 = h2.at(t2)

        val vx = (hailPos2.x - hailPos1.x).toLong() / (t2 - t1)
        val vy = (hailPos2.y - hailPos1.y).toLong() / (t2 - t1)
        val vz = (hailPos2.z - hailPos1.z).toLong() / (t2 - t1)

        val px = hailPos1.x.toLong() - vx * t1
        val py = hailPos1.y.toLong() - vy * t1
        val pz = hailPos1.z.toLong() - vz * t1

        return px + py + pz
    }

    fun List<HailStone>.eval(a: Vec3d, b: Vec3d): Double = sumOf {
        it.distanceToLine(a, b)
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

    data class HailStone(val pos: Vec3d, val vel: Vec3d) {
        fun at(t: Long) = pos + vel * t.toDouble()

        fun distanceToLine(l1: Vec3d, l2: Vec3d): Double {
            val e1 = l2 - l1
            val e2 = vel
            val n: Vec3d = e1.cross(e2, MutableVec3d())
            return abs(n dot (l1 - pos)) / n.length()
        }
    }
}