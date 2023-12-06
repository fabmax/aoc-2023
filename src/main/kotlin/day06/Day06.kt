package day06

import parseTestInput

// val testInput = parseTestInput("""
//     Time:      7  15   30
//     Distance:  9  40  200
// """.trimIndent())

val input = parseTestInput("""
    Time:        34     90     89     86
    Distance:   204   1713   1210   1780
""".trimIndent())

fun main() {
    part1(input)
    part2(input)
}

fun part1(input: List<String>) {
    val times = input[0].substringAfter(':')
        .split(" ")
        .filter { it.isNotBlank() }
        .map { it.trim().toInt() }
    val distances = input[1].substringAfter(':')
        .split(" ")
        .filter { it.isNotBlank() }
        .map { it.trim().toInt() }

    val answer = times.zip(distances)
        .map { (time, distance) -> getPossibleRuns(time).filter { it.distance > distance } }
        .fold(1) { value, betterOptions -> value * betterOptions.size }

    println("Answer part 1: $answer")
}

fun part2(input: List<String>) {
    val time = input[0].substringAfter(':')
        .filter { !it.isWhitespace() }
        .trim().toLong()
    val distance = input[1].substringAfter(':')
        .filter { !it.isWhitespace() }
        .trim().toLong()

    val answer = (0..time)
        .map { chargeTime -> (time - chargeTime) * chargeTime }
        .count { it > distance }

    println("Answer part 2: $answer")
}

data class Run(val totalTime: Int, val chargeTime: Int, val distance: Int)

fun getPossibleRuns(time: Int): List<Run> {
    return (0 .. time).map { chargeTime ->
        Run(time, chargeTime, (time - chargeTime) * chargeTime)
    }
}
