package y2015

import AocPuzzle
import permutations

fun main() = Day09.runAll()

object Day09 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val places = mutableMapOf<String, Place>()
        input.forEach {
            val dist = it.substringAfter("= ").toInt()
            val (from, to) = it.substringBefore(" =").split(" to ")
            places.getOrPut(from) { Place(from) }.distances[to] = dist
            places.getOrPut(to) { Place(to) }.distances[from] = dist
        }

        return places.values.permutations().minOf { cities ->
            cities.windowed(2).sumOf { (a, b) -> a.distances[b.name]!! }
        }
    }

    override fun solve2(input: List<String>): Int {
        val places = mutableMapOf<String, Place>()
        input.forEach {
            val dist = it.substringAfter("= ").toInt()
            val (from, to) = it.substringBefore(" =").split(" to ")
            places.getOrPut(from) { Place(from) }.distances[to] = dist
            places.getOrPut(to) { Place(to) }.distances[from] = dist
        }

        return places.values.permutations().maxOf { cities ->
            cities.windowed(2).sumOf { (a, b) -> a.distances[b.name]!! }
        }
    }

    class Place(val name: String) {
        val distances = mutableMapOf<String, Int>()
    }
}