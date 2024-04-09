@file:Suppress("unused")

package y2023.benchmarks

import AocPuzzle
import y2023.day24.Day24
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
class Day24Benchmark {

    val target = Day24

    @Benchmark
    fun test1() {
        target.prepareRun(AocPuzzle.Run.TestRun(0))
        target.solve1(target.input)
    }

    @Benchmark
    fun test2() {
        target.prepareRun(AocPuzzle.Run.TestRun(0))
        target.solve2(target.input)
    }

    @Benchmark
    fun part1() {
        target.prepareRun(AocPuzzle.Run.PuzzleRun)
        target.solve1(target.input)
    }

    @Benchmark
    fun part2() {
        target.prepareRun(AocPuzzle.Run.PuzzleRun)
        target.solve2(target.input)
    }

}