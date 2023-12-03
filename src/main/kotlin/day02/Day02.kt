package day02

import readInput

fun testInput(): List<String> {
    val test = """
        Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
        Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
        Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
        Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
        Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green
    """.trimIndent()
    return test.lines().filter { it.isNotBlank() }
}

fun main() {
    val lines = readInput("day02.txt")
    val games = lines.map { Game.parse(it) }

    part1(games)
    part2(games)
}

fun part2(games: List<Game>) {
    val sumOfPowers = games.sumOf { it.getMinimumSet().power }
    println("answer part 2: $sumOfPowers")
}

fun part1(games: List<Game>) {
    val filterRed = 12
    val filterGreen = 13
    val filterBlue = 14

    val filteredGames = games.filter { it.isPossibleWith(filterRed, filterGreen, filterBlue) }
    println("answer part 1: " + filteredGames.sumOf { it.id })
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