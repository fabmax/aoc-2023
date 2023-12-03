package day01

import readInput
import java.io.File

fun main() {
    val answer1 = partOne()
    println("answer pt1: $answer1")
    val answer2 = partTwo()
    println("answer pt2: $answer2")
}

fun partOne(): Int {
    return readInput("day01.txt")
        .map { line -> "${line.first { it.isDigit() }}${line.last { it.isDigit() }}" }
        .sumOf { num -> num.toInt() }
}

fun partTwo(): Int {
    val numsAsWords = listOf(
        "one" to 1,
        "two" to 2,
        "three" to 3,
        "four" to 4,
        "five" to 5,
        "six" to 6,
        "seven" to 7,
        "eight" to 8,
        "nine" to 9,
    )

    val answer = readInput("day01.txt")
        .sumOf { line ->
            val firstDigit = numsAsWords
                .filter { (word, _) -> word in line }
                .minByOrNull { (word, _) -> line.indexOf(word) }
                .let { pair ->
                    pair?.let { (word, num) -> line.replace(word, "$num") } ?: line
                }
                .first { it.isDigit() }.digitToInt()

            val lastDigit = numsAsWords
                .filter { (word, _) -> word in line }
                .maxByOrNull { (word, _) -> line.lastIndexOf(word) + word.length }
                .let { pair ->
                    pair?.let { (word, num) -> line.replace(word, "$num") } ?: line
                }
                .last { it.isDigit() }.digitToInt()

            firstDigit * 10 + lastDigit
        }
    return answer
}
