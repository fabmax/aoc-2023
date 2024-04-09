package y2023.day24

import AocPuzzle
import Vec3d
import io.ksmt.KContext
import io.ksmt.expr.KBitVec64Value
import io.ksmt.expr.KExpr
import io.ksmt.solver.KSolverStatus
import io.ksmt.solver.z3.KZ3Solver
import io.ksmt.sort.KBvSort
import io.ksmt.utils.getValue
import io.ksmt.utils.mkConst
import kotlin.time.Duration.Companion.seconds

fun main() = Day24Smt.runPuzzle()

object Day24Smt : AocPuzzle<Int, Long>() {
    /**
     * Just for reference: part 2 solution using an SMT solver (by Phil Davies)
     */
    override fun solve2(input: List<String>): Long {
        val hailstones = input.map {
            it.split(" @ ").let { (p, v) -> Day24.HailStone(Vec3d(p), Vec3d(v)) }
        }

        return with(KContext()) {
            operator fun <T: KBvSort> KExpr<T>.times(other: KExpr<T>) = mkBvMulExpr(this, other)
            operator fun <T: KBvSort> KExpr<T>.plus(other: KExpr<T>) = mkBvAddExpr(this, other)
            operator fun <T: KBvSort> KExpr<T>.times(other: Long) = times(mkBv(other, sort))
            operator fun <T: KBvSort> KExpr<T>.plus(other: Long) = plus(mkBv(other, sort))

            val x by bv64Sort
            val y by bv64Sort
            val z by bv64Sort
            val vx by bv64Sort
            val vy by bv64Sort
            val vz by bv64Sort

            val s = KZ3Solver(this)
            hailstones.take(3).forEachIndexed { i, stone ->
                val t = bv64Sort.mkConst("t$i")
                listOf(
                    mkBvSignedGreaterOrEqualExpr(t, mkBv(0L)),
                    (x + (vx * t)) eq ((t * stone.vel.x.toLong() + stone.pos.x.toLong())),
                    (y + (vy * t)) eq ((t * stone.vel.y.toLong() + stone.pos.y.toLong())),
                    (z + (vz * t)) eq ((t * stone.vel.z.toLong() + stone.pos.z.toLong())),
                ).forEach { s.assert(it) }
            }

            s.check(timeout = 15.seconds).let { require(it == KSolverStatus.SAT) { "$it" } }
            (s.model().eval(x + y + z) as KBitVec64Value).longValue
        }
    }
}