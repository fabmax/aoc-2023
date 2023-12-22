package day02

import AocPuzzle

fun main() = Day02.runAll()

object Day02 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val games = input.map { Game.parse(it) }

        val filterRed = 12
        val filterGreen = 13
        val filterBlue = 14

        val filteredGames = games.filter { it.isPossibleWith(filterRed, filterGreen, filterBlue) }
        return filteredGames.sumOf { it.id }
    }

    override fun solve2(input: List<String>): Int {
        return input.map { Game.parse(it) }.sumOf { it.getMinimumSet().power }
    }
}

data class Game(val id: Int, val sets: List<GameSet>) {

    fun isPossibleWith(red: Int, green: Int, blue: Int): Boolean {
        return sets.all { it.red <= red && it.green <= green && it.blue <= blue }
    }

    fun getMinimumSet(): GameSet {
        val minRed = sets.maxOf { it.red }
        val minGreen = sets.maxOf { it.green }
        val minBlue = sets.maxOf { it.blue }
        return GameSet(minRed, minGreen, minBlue)
    }

    companion object {
        fun parse(line: String): Game {
            val id = line.substringAfter("Game ").substringBefore(':').toInt()
            val sets = line.substringAfter(':').trim().split(';').map { it.trim() }
                .map { GameSet.parse(it) }
            return Game(id, sets)
        }
    }
}

data class GameSet(val red: Int, val green: Int, val blue: Int) {

    val power: Int get() = red * green * blue

    companion object {
        fun parse(setString: String): GameSet {
            var red = 0
            var green = 0
            var blue = 0
            setString.split(",").map { it.trim() }.forEach { cubes ->
                val (number, color) = cubes.split(' ')
                when (color) {
                    "red" -> red = number.toInt()
                    "green" -> green = number.toInt()
                    "blue" -> blue = number.toInt()
                }
            }
            return GameSet(red, green, blue)
        }
    }
}