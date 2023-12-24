@file:Suppress("unused")

package benchmark

import AocPuzzle
import day23.Day23
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
class Day23Benchmark {

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
    fun part2Fast() {
        target.prepareRun(AocPuzzle.Run.PuzzleRun)
        Day23.Maze(Day23.input, true).findLongestPath()
    }

    @Benchmark
    fun part2Exhaustive() {
        target.prepareRun(AocPuzzle.Run.PuzzleRun)
        Day23.Maze(Day23.input, true).findLongestPathExhaustive()
    }

}