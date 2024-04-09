package y2015

import AocPuzzle
import extractNumbers

fun main() = Day14.runAll()

object Day14 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val t = if (isTestRun()) 1000 else 2503
        return input.map {
            val (topSpeed, moveTime, restTime) = it.extractNumbers()
            Reindeer(it.substringBefore(' '), topSpeed, moveTime, restTime)
        }.maxOf { reindeer ->
            repeat(t) { reindeer.simulate() }
            reindeer.dist
        }
    }

    override fun solve2(input: List<String>): Int {
        val reindeers = input.map {
            val (topSpeed, moveTime, restTime) = it.extractNumbers()
            Reindeer(it.substringBefore(' '), topSpeed, moveTime, restTime)
        }

        val t = if (isTestRun()) 1000 else 2503
        repeat(t) {
            val maxDist = reindeers.maxOf { it.simulate() }
            reindeers.filter { it.dist == maxDist }.forEach { it.score++ }
        }
        return reindeers.maxOf { it.score }
    }

    class Reindeer(val name: String, val topSpeed: Int, val moveTime: Int, val restTime: Int) {
        var resting = 0
        var moving = moveTime
        var dist = 0
        var score = 0

        fun simulate(): Int {
            if (moving > 0) {
                dist += topSpeed
                if (--moving == 0) {
                    resting = restTime
                }
            } else if (restTime > 0) {
                if (--resting == 0) {
                    moving = moveTime
                }
            }
            return dist
        }
    }
}