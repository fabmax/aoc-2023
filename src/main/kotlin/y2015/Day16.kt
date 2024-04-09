package y2015

import AocPuzzle
import extractNumbers

fun main() = Day16.runAll()

object Day16 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        return input.parseSues().first { sue ->
            sue.children.matches(3) &&
            sue.cats.matches(7) &&
            sue.samoyeds.matches(2) &&
            sue.pomeranians.matches(3) &&
            sue.akitas.matches(0) &&
            sue.vizslas.matches(0) &&
            sue.goldfish.matches(5) &&
            sue.trees.matches(3) &&
            sue.cars.matches(2) &&
            sue.perfumes.matches(1)
        }.id
    }

    override fun solve2(input: List<String>): Int {
        return input.parseSues().first { sue ->
            sue.children.matches(3) &&
            sue.cats.greaterThan(7) &&
            sue.samoyeds.matches(2) &&
            sue.pomeranians.lessThan(3) &&
            sue.akitas.matches(0) &&
            sue.vizslas.matches(0) &&
            sue.goldfish.lessThan(5) &&
            sue.trees.greaterThan(3) &&
            sue.cars.matches(2) &&
            sue.perfumes.matches(1)
        }.id
    }

    fun List<String>.parseSues(): List<Sue> = map { line ->
        line.substringAfter(": ")
            .split(", ")
            .map { info -> info.split(": ") }
            .map { (fact, value) -> fact to value.toInt() }
            .fold(Sue(line.extractNumbers()[0])) { sue, (fact, value) ->
                when (fact) {
                    "children" -> sue.copy(children = value)
                    "cats" -> sue.copy(cats = value)
                    "samoyeds" -> sue.copy(samoyeds = value)
                    "pomeranians" -> sue.copy(pomeranians = value)
                    "akitas" -> sue.copy(akitas = value)
                    "vizslas" -> sue.copy(vizslas = value)
                    "goldfish" -> sue.copy(goldfish = value)
                    "trees" -> sue.copy(trees = value)
                    "cars" -> sue.copy(cars = value)
                    "perfumes" -> sue.copy(perfumes = value)
                    else -> error("invalid fact: $fact")
                }
            }
    }

    fun Int?.matches(value: Int): Boolean = this == null || this == value
    fun Int?.greaterThan(value: Int): Boolean = this == null || this > value
    fun Int?.lessThan(value: Int): Boolean = this == null || this < value

    data class Sue(
        val id: Int,
        val children: Int? = null,
        val cats: Int? = null,
        val samoyeds: Int? = null,
        val pomeranians: Int? = null,
        val akitas: Int? = null,
        val vizslas: Int? = null,
        val goldfish: Int? = null,
        val trees: Int? = null,
        val cars: Int? = null,
        val perfumes: Int? = null,
    )
}