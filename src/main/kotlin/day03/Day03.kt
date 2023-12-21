package day03

import AocPuzzle

fun main() = Day03.runAll()

object Day03 : AocPuzzle<Int, Int>() {
    override fun solve(input: List<String>): Pair<Int, Int> {
        val symbols = mutableMapOf<Coordinate, Symbol>()
        val numbers = mutableMapOf<Coordinate, Symbol>()

        input.forEachIndexed { lineNum, line ->
            val lineSymbols = parseSymbols(line, lineNum)
            lineSymbols.forEach { symbol ->
                val dest = if (symbol.isNumber) numbers else symbols
                symbol.coords.forEach { dest[it] = symbol }
            }
        }

        return part1(numbers, symbols) to part2(numbers, symbols)
    }

    private fun part1(numbers: Map<Coordinate, Symbol>, symbols: Map<Coordinate, Symbol>): Int {
        return numbers.values.distinct()
            .filter { it.isNumber }
            .filter { number ->
                adjacentCoordinates(number.coords).any { it in symbols.keys }
            }
            .sumOf { it.text.toInt() }
    }

    private fun part2(numbers: Map<Coordinate, Symbol>, symbols: Map<Coordinate, Symbol>): Int {
        return symbols.values
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
    }


    private fun adjacentCoordinates(coords: List<Coordinate>): Set<Coordinate> {
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

    private fun parseSymbols(line: String, lineNum: Int): List<Symbol> {
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

    private fun getNextToken(line: String): String {
        return when {
            line.startsWith(".") -> line.takeWhile { it == '.' }
            line.first().isDigit() -> line.takeWhile { it.isDigit() }
            else -> line.first().toString()
        }
    }

    data class Coordinate(val x: Int, val y: Int)

    data class Symbol(val text: String, val coords: List<Coordinate>, val isNumber: Boolean)
}
