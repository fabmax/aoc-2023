@file:Suppress("unused")

package y2023.benchmarks

import AocPuzzle
import y2023.day25.Day25
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
class Day25Benchmark {

    val target = Day25

    @Benchmark
    fun test1() {
        target.prepareRun(AocPuzzle.Run.TestRun(0))
        target.solve1(target.input)
    }

    @Benchmark
    fun part1() {
        target.prepareRun(AocPuzzle.Run.PuzzleRun)
        target.solve1(target.input)
    }

}