@file:Suppress("unused")

package benchmark

import AocPuzzle
import day23.Day23
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
class AocBenchmark {

    val target = Day23

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