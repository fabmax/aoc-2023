package day06

import AocPuzzle

fun main() = Day06().start()

class Day06 : AocPuzzle() {
    override fun solve(input: List<String>): Pair<Any?, Any?> {
        return part1(input) to part2(input)
    }

    private fun part1(input: List<String>): Int {
        val times = input[0].substringAfter(':')
            .split(" ")
            .filter { it.isNotBlank() }
            .map { it.trim().toInt() }
        val distances = input[1].substringAfter(':')
            .split(" ")
            .filter { it.isNotBlank() }
            .map { it.trim().toInt() }

        return times.zip(distances)
            .map { (time, distance) -> getPossibleRuns(time).filter { it.distance > distance } }
            .fold(1) { value, betterOptions -> value * betterOptions.size }
    }

    private fun part2(input: List<String>): Int {
        val time = input[0].substringAfter(':')
            .filter { !it.isWhitespace() }
            .trim().toLong()
        val distance = input[1].substringAfter(':')
            .filter { !it.isWhitespace() }
            .trim().toLong()

        return (0..time)
            .map { chargeTime -> (time - chargeTime) * chargeTime }
            .count { it > distance }
    }

    private fun getPossibleRuns(time: Int): List<Run> {
        return (0 .. time).map { chargeTime ->
            Run(time, chargeTime, (time - chargeTime) * chargeTime)
        }
    }
}

data class Run(val totalTime: Int, val chargeTime: Int, val distance: Int)
