package day24

import AocPuzzle
import Vec3d
import component1
import component2
import de.fabmax.kool.math.MutableVec3d
import de.fabmax.kool.math.Vec2i
import de.fabmax.kool.math.Vec3d
import intersectLines
import xy
import kotlin.math.abs
import kotlin.math.max

fun main() = Day24.runAll()

object Day24 : AocPuzzle<Int, Long>() {

    override fun solve1(input: List<String>): Int {
        val boundsMin = if (isTestRun()) 7L else 200000000000000L
        val boundsMax = if (isTestRun()) 27L else 400000000000000L

        val hailStones = input.map { it.split(" @ ").let { (p, v) -> HailStone(Vec3d(p), Vec3d(v)) } }

        return generateSequence(Vec2i.ZERO) { (i, j) ->
            val incJ = (j + 1) % hailStones.size
            val nextI = if (incJ == 0) i + 1 else i
            val nextJ = max(nextI + 1, incJ)
            if (nextJ < hailStones.size) Vec2i(nextI, nextJ) else null
        }.count { (i, j) ->
            hailStones[i].intersectsXy(hailStones[j], boundsMin, boundsMax)
        }
    }

    override fun solve2(input: List<String>): Long {
        val hailStones = input.map { it.split(" @ ").let { (p, v) -> HailStone(Vec3d(p), Vec3d(v)) } }
        val (h1, h2) = hailStones.take(2)

        var t1 = 0L
        var t2 = 0L
        var step1 = if (isTestRun()) 1 else 1e10.toLong()
        var step2 = if (isTestRun()) 1 else 1e10.toLong()
        var isStep1 = true

        // approximate collision times 1 and 2
        var dist = Double.POSITIVE_INFINITY
        while (abs(step1) != 0L || abs(step2) != 0L) {
            do {
                val prevDist = dist
                when (isStep1) {
                    true  -> t1 += step1
                    false -> t2 += step2
                }
                val a = h1.at(t1)
                val b = h2.at(t2)
                dist = hailStones.sumOf { it.distanceToLine(a, b) }
            } while (dist < prevDist)

            when (isStep1) {
                true  -> step1 /= -10
                false -> step2 /= -10
            }
            isStep1 = !isStep1
        }

        // approximated collision times are 1 off -> do final refinement
        var bestDist = dist
        for (s1 in t1 - 5L .. t1 + 5L) {
            for (s2 in t2 - 5L .. t2 + 5L) {
                val a = h1.at(s1)
                val b = h2.at(s2)
                val d = hailStones.sumOf { it.distanceToLine(a, b) }
                if (d < bestDist) {
                    bestDist = d
                    t1 = s1
                    t2 = s2
                }
            }
        }

        // compute hailstone positions at t1 and t2 and derive rock start pos and velocity from that
        val ht1 = h1.at(t1)
        val ht2 = h2.at(t2)
        val rockVel = (ht2 - ht1) / (t2 - t1).toDouble()
        val rockPos = ht1 - rockVel * t1.toDouble()
        return (rockPos.x + rockPos.y + rockPos.z).toLong()
    }

    data class HailStone(val pos: Vec3d, val vel: Vec3d) {
        fun at(t: Long) = pos + vel * t.toDouble()

        fun distanceToLine(l1: Vec3d, l2: Vec3d): Double {
            val e1 = l2 - l1
            val e2 = vel
            val n: Vec3d = e1.cross(e2, MutableVec3d())
            return abs(n dot (l1 - pos)) / n.length()
        }

        fun intersectsXy(other: HailStone, min: Long, max: Long): Boolean {
            val intersectPos = intersectLines(pos.xy, (pos + vel).xy, other.pos.xy, (other.pos + other.vel).xy) ?: return false
            if (intersectPos.x.toLong() !in min..max || intersectPos.y.toLong() !in min..max) {
                return false
            }

            val da = (intersectPos - pos.xy) dot vel.xy
            val db = (intersectPos - other.pos.xy) dot other.vel.xy
            return da > 0.0 && db > 0.0
        }
    }
}