package y2023.day05

import AocPuzzle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun main() = Day05().runAll()

class Day05 : AocPuzzle<Long, Long>() {
    override fun solve1(input: List<String>): Long {
        val almanac = parseAlmanac(input)
        return almanac.seeds.minOf { almanac.seedToLocation(it) }
    }

    override fun solve2(input: List<String>): Long {
        val almanac = parseAlmanac(input)
        return runBlocking(Dispatchers.Default) {
            almanac.seeds
                .chunked(2)
                .map { (start, len) ->
                    async {
                        (start..<(start + len)).minOf { almanac.seedToLocation(it) }
                            .also { println("  Min of seeds $start [len: $len]: $it") }
                    }
                }
                .minOf { it.await() }
        }
    }

    private fun parseAlmanac(input: List<String>): Almanac {
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

    private fun parseRanges(input: List<String>, key: String): Category {
        val ranges = input
            .dropWhile { !it.startsWith(key) }.drop(1)
            .takeWhile { it.isNotBlank() }
            .map { line ->
                val (dst, src, len) = line.split(" ").map { it.trim().toLong() }
                RangeMap(src, dst, len)
            }
        return Category(key, ranges)
    }
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
