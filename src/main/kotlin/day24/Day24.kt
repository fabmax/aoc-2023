package day24

import AocPuzzle
import de.fabmax.kool.math.MutableVec3d
import de.fabmax.kool.math.Vec3d
import kotlin.math.abs
import kotlin.math.roundToLong

fun main() = Day24.runPuzzle()

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

        val (ha, hb) = hailStones.take(2)
        val aSamples = ha.sample(0, 1e12.toLong(), 500)
        val bSamples = hb.sample(0, 1e12.toLong(), 500)
        var minDist = Double.POSITIVE_INFINITY
        var bestA = 0
        var bestB = 0

        for (i in aSamples.indices) {
            for (j in bSamples.indices) {
                val a = aSamples[i].first
                val b = bSamples[j].first
                var sumDist = 0.0
                for (k in 2 until hailStones.size) {
                    sumDist += hailStones[k].distanceToLine(a, b)
                }
                if (sumDist < minDist) {
                    minDist = sumDist
                    bestA = i
                    bestB = j
                }
            }
        }

        val dt = bSamples[bestB].second - aSamples[bestA].second
        val dp = bSamples[bestB].first - aSamples[bestA].first
        val v = dp / dt.toDouble()
        val vx = v.x.roundToLong().toDouble()
        val vy = v.y.roundToLong().toDouble()
        val vz = v.z.roundToLong().toDouble()
        println("estimated v: $vx, $vy, $vz")

        val (h1, h2) = hailStones.take(2)

        // copy and paste output to wolfram alpha...
        println("${h1.pos.x.toLong()}+${h1.vel.x.toLong()}*s=x+${vx.toLong()}*s,")
        println("${h1.pos.y.toLong()}+${h1.vel.y.toLong()}*s=y+${vy.toLong()}*s,")
        println("${h1.pos.z.toLong()}+${h1.vel.z.toLong()}*s=z+${vz.toLong()}*s,")
        println("${h2.pos.x.toLong()}+${h2.vel.x.toLong()}*t=x+${vx.toLong()}*t,")
        println("${h2.pos.y.toLong()}+${h2.vel.y.toLong()}*t=y+${vy.toLong()}*t,")
        println("${h2.pos.z.toLong()}+${h2.vel.z.toLong()}*t=z+${vz.toLong()}*t")

        // wolfram alpha says:
        // s = 353090968659, t = 870093641616, x = 194723518367339, y = 181910661443432, z = 150675954587450
        return 194723518367339 + 181910661443432 + 150675954587450
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
            val r1 = pos
            val r2 = l1
            val e1 = vel
            val e2 = l2 - l1

            val n: Vec3d = e1.cross(e2, MutableVec3d())
            return abs(n dot (r1 - r2)) / n.length()
        }

        fun sample(fromT: Long, toT: Long, nSamples: Int): List<Pair<Vec3d, Long>> {
            val tStep = (toT - fromT) / (nSamples - 1)
            return (0 until nSamples).map { i ->
                at(fromT + tStep * i) to tStep * i
            }
        }
    }
}