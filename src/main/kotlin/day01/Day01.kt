package day01

import AocPuzzle

fun main() = Day01.runAll()

object Day01 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        return partOne(input)
    }

    override fun solve2(input: List<String>): Int {
        return partTwo(input)
    }

    fun partOne(input: List<String>): Int {
        return input
            .map { line -> "${line.first { it.isDigit() }}${line.last { it.isDigit() }}" }
            .sumOf { num -> num.toInt() }
    }

    fun partTwo(input: List<String>): Int {
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

        val answer = input
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
}
