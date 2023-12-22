package day11

import AocPuzzle
import kotlin.math.max
import kotlin.math.min

fun main() = Day11.runAll()

object Day11 : AocPuzzle<Long, Long>() {

    override fun solve1(input: List<String>): Long {
        val image = Image(input)
        return determineExpandedDistances(image, image.getGalaxies(), 2)
    }

    override fun solve2(input: List<String>): Long {
        val image = Image(input)
        return determineExpandedDistances(image, image.getGalaxies(), 1_000_000)
    }

    private fun determineExpandedDistances(image: Image, galaxies: List<Galaxy>, expansion: Int): Long {
        return galaxies
            .mapIndexed { i, galaxy -> galaxies.drop(i + 1).map { galaxy to it } }
            .flatten()
            .sumOf { (from, to) ->
                val xDist = (min(from.x, to.x) ..< max(from.x, to.x)).sumOf { x ->
                    if (image.pixels[from.y][x] == ',') expansion - 1 else 1
                }
                val yDist = (min(from.y, to.y) ..< max(from.y, to.y)).sumOf { y ->
                    if (image.pixels[y][from.x] == ',') expansion - 1 else 1
                }
                (xDist + yDist).toLong()
            }
    }
}

class Image(source: List<String>) {

    val pixels = mutableListOf<MutableList<Char>>()

    init {
        source.forEach { line ->
            pixels += line.toMutableList()
        }
        expand()
    }

    fun getGalaxies(): List<Galaxy> {
        return pixels.flatMapIndexed { y: Int, row: MutableList<Char> ->
            row
                .mapIndexed { x: Int, type: Char -> if (type == '#') Galaxy(x, y) else null }
                .filterNotNull()
        }
    }

    @Suppress("unused")
    fun print() {
        pixels.forEach { row ->
            row.forEach { print(it) }
            println()
        }
    }

    private fun expand() {
        val emptyChars = setOf('.', ',')
        for (x in pixels[0].indices.reversed()) {
            if ((0 until pixels.size).all { pixels[it][x] in emptyChars }) {
                insertColumn(x+1)
            }
        }
        for (y in pixels.indices.reversed()) {
            if (pixels[y].indices.all { pixels[y][it] in emptyChars }) {
                insertRow(y+1)
            }
        }
    }

    private fun insertColumn(x: Int) {
        for (y in pixels.indices) {
            pixels[y].add(x, ',')
        }
    }

    private fun insertRow(y: Int) {
        val row = pixels[0].indices.map { ',' }.toMutableList()
        pixels.add(y, row)
    }
}

data class Galaxy(val x: Int, val y: Int)
