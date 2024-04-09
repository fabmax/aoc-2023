package y2022.day15

import AocPuzzle
import de.fabmax.kool.math.Vec2i
import extractNumbers
import manhattanDistance
import size
import kotlin.math.abs
import kotlin.math.max

fun main() = Day15.runAll()

object Day15 : AocPuzzle<Int, Long>() {
    override fun solve1(input: List<String>): Int {
        val evalRow = if (isTestRun()) 10 else 2_000_000
        return input
            .mapSensorBeacon()
            .filter { it.isInRange(evalRow) }
            .map { it.sensorRange(evalRow, true) }
            .merged()
            .sumOf { it.size }
    }

    override fun solve2(input: List<String>): Long {
        val searchRange = if (isTestRun()) 20 else 4_000_000

        val sensorBeacons = input.mapSensorBeacon()
        val (y, ranges) = (0 .. searchRange).firstNotNullOf { row ->
            val ranges = sensorBeacons
                .filter { it.isInRange(row) }
                .map { it.sensorRange(row, false) }
                .merged()
            if (ranges.size > 1 || ranges.first().first > 0 || ranges.first().last < searchRange) {
                row to ranges
            } else {
                null
            }
        }

        val x = ranges.first().last + 1
        return x * 4_000_000L + y
    }

    fun List<String>.mapSensorBeacon(): List<SensorBeacon> = map { line ->
        val (sensorX, sensorY, beaconX, beaconY) = line.extractNumbers()
        val sensor = Vec2i(sensorX, sensorY)
        val beacon = Vec2i(beaconX, beaconY)
        SensorBeacon(sensor, beacon)
    }

    fun List<IntRange>.merged(): List<IntRange> {
        val sorted = sortedBy { it.first }
        return sorted.fold(mutableListOf(sorted.first())) { acc, rng ->
            if (rng.first <= (acc.last().last + 1)) {
                val newRng = acc.last().first() .. max(acc.last().last, rng.last)
                acc[acc.lastIndex] = newRng
            } else {
                acc += rng
            }
            acc
        }
    }

    data class SensorBeacon(val sensor: Vec2i, val beacon: Vec2i) {
        val distance = sensor.manhattanDistance(beacon)

        fun isInRange(row: Int): Boolean = distance > abs(sensor.y - row)

        fun sensorRange(row: Int, excludeBeacons: Boolean): IntRange {
            val r = distance - abs(sensor.y - row)
            val start = sensor.x - r
            val end = sensor.x + r
            val beaconStart = if (excludeBeacons && beacon.y == row && beacon.x < sensor.x) 1 else 0
            val beaconEnd = if (excludeBeacons && beacon.y == row && beacon.x > sensor.x) -1 else 0
            val range = (start + beaconStart) .. (end + beaconEnd)
            return range
        }
    }
}