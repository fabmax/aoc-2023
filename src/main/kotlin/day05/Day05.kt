package day05

import kotlinx.coroutines.*
import parseTestInput
import readInput

val testInput = parseTestInput(dropBlanks = false, input = """
    seeds: 79 14 55 13

    seed-to-soil map:
    50 98 2
    52 50 48
    
    soil-to-fertilizer map:
    0 15 37
    37 52 2
    39 0 15
    
    fertilizer-to-water map:
    49 53 8
    0 11 42
    42 0 7
    57 7 4
    
    water-to-light map:
    88 18 7
    18 25 70
    
    light-to-temperature map:
    45 77 23
    81 45 19
    68 64 13
    
    temperature-to-humidity map:
    0 69 1
    1 0 69
    
    humidity-to-location map:
    60 56 37
    56 93 4
""")

fun main() {
    part1()
    part2()
}

fun part1() {
    //val almanac = parseInput(testInput)
    val almanac = parseInput(readInput("day05.txt", false))
    val answer = almanac.seeds.minOf { almanac.seedToLocation(it) }
    println("answer part 1: $answer")
}

fun part2() {
    //val almanac = parseInput(testInput)
    val almanac = parseInput(readInput("day05.txt", false))

    val minSeed = runBlocking(Dispatchers.Default) {
        almanac.seeds
            .chunked(2)
            .map { (start, len) ->
                async {
                    (start ..< (start+len)).minOf { almanac.seedToLocation(it) }
                        .also { println("  Min of seeds $start [len: $len]: $it") }
                }
            }
            .minOf { it.await() }
    }
    println("answer part 2: $minSeed")
}

fun parseInput(input: List<String>): Almanac {
    val seeds = input.first().substringAfter("seeds:").split(" ").filter { it.isNotBlank() }.map { it.trim().toLong() }
    return Almanac(seeds).apply {
        categories += parseRanges(input, "seed-to-soil")
        categories += parseRanges(input, "soil-to-fertilizer")
        categories += parseRanges(input, "fertilizer-to-water")
        categories += parseRanges(input, "water-to-light")
        categories += parseRanges(input, "light-to-temperature")
        categories += parseRanges(input, "temperature-to-humidity")
        categories += parseRanges(input, "humidity-to-location")
    }
}

fun parseRanges(input: List<String>, key: String): Category {
    val ranges = input
        .dropWhile { !it.startsWith(key) }.drop(1)
        .takeWhile { it.isNotBlank() }
        .map { line ->
            val (dst, src, len) = line.split(" ").map { it.trim().toLong() }
            RangeMap(src, dst, len)
        }
    return Category(key, ranges)
}

class Almanac(val seeds: List<Long>) {
    val categories = mutableListOf<Category>()

    fun seedToLocation(seed: Long): Long {
        var translated = seed
        for (cat in categories) {
            translated = cat.ranges.firstOrNull { translated in it }?.map(translated) ?: translated
        }
        return translated
    }
}

data class Category(val name: String, val ranges: List<RangeMap>)

data class RangeMap(val srcFrom: Long, val dstFrom: Long, val len: Long) {
    operator fun contains(value: Long) = value in srcFrom ..< (srcFrom+len)

    fun map(value: Long): Long = value - srcFrom + dstFrom
}
