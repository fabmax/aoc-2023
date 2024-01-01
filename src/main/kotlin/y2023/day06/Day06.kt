package y2023.day06

import AocPuzzle

fun main() = Day06.runAll()

object Day06 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
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

    override fun solve2(input: List<String>): Int {
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

    private fun getPossibleRuns(time: Int): List<BoatRun> {
        return (0 .. time).map { chargeTime ->
            BoatRun(time, chargeTime, (time - chargeTime) * chargeTime)
        }
    }
}

data class BoatRun(val totalTime: Int, val chargeTime: Int, val distance: Int)
