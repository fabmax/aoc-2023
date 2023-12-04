package day03

import parseTestInput
import readInput

val testInput = parseTestInput("""
    467..114..
    ...*......
    ..35..633.
    ......#...
    617*......
    .....+.58.
    ..592.....
    ......755.
    ...${'$'}.*....
    .664.598..
""")

fun main() {
    val symbols = mutableMapOf<Coordinate, Symbol>()
    val numbers = mutableMapOf<Coordinate, Symbol>()

    readInput("day03.txt").forEachIndexed { lineNum, line ->
        val lineSymbols = parseSymbols(line, lineNum)
        lineSymbols.forEach { symbol ->
            val dest = if (symbol.isNumber) numbers else symbols
            symbol.coords.forEach { dest[it] = symbol }
        }
    }

    part1(numbers, symbols)
    part2(numbers, symbols)
}

fun part1(numbers: Map<Coordinate, Symbol>, symbols: Map<Coordinate, Symbol>) {
    val answer = numbers.values.distinct()
        .filter { it.isNumber }
        .filter { number ->
            adjacentCoordinates(number.coords).any { it in symbols.keys }
        }
        .sumOf { it.text.toInt() }

    println("answer part1: $answer")
}

fun part2(numbers: Map<Coordinate, Symbol>, symbols: Map<Coordinate, Symbol>) {
    val answer = symbols.values
        .filter { it.text == "*" }
        .map { gear ->
            adjacentCoordinates(gear.coords)
                .mapNotNull { numbers[it] }
                .distinct()
        }
        .filter { it.size == 2 }
        .sumOf { gearNumbers ->
            val (a, b) = gearNumbers.map { it.text.toInt() }
            a * b
        }

    println("answer part2: $answer")
}

fun adjacentCoordinates(coords: List<Coordinate>): Set<Coordinate> {
    val adjacent = mutableSetOf<Coordinate>()
    coords.forEach {
        for (y in -1 .. 1) {
            for (x in -1..1) {
                adjacent += Coordinate(it.x + x, it.y + y)
            }
        }
    }
    return adjacent
}

fun parseSymbols(line: String, lineNum: Int): List<Symbol> {
    val symbols = mutableListOf<Symbol>()

    var pos = 0
    var parsed = line
    while (parsed.isNotEmpty()) {
        val token = getNextToken(parsed)

        if (!token.startsWith('.')) {
            val coords = token.mapIndexed { i, _ -> Coordinate(pos + i, lineNum) }
            symbols += Symbol(token, coords, token.first().isDigit())
        }

        pos += token.length
        parsed = parsed.removePrefix(token)
    }

    return symbols
}

fun getNextToken(line: String): String {
    return when {
        line.startsWith(".") -> line.takeWhile { it == '.' }
        line.first().isDigit() -> line.takeWhile { it.isDigit() }
        else -> line.first().toString()
    }
}

data class Coordinate(val x: Int, val y: Int)

data class Symbol(val text: String, val coords: List<Coordinate>, val isNumber: Boolean)
