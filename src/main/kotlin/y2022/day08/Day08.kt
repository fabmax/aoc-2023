package y2022.day08

import AocPuzzle

fun main() = Day08.runAll()

object Day08 : AocPuzzle<Int, Int>() {

    override fun solve1(input: List<String>): Int = (input.indices).asSequence()
        .flatMap { y -> input[y].indices.asSequence().map { x -> x to y } }
        .count { (x, y) ->
            val h = input[y][x].digitToInt()

            (0 until x).all { input[y][it].digitToInt() < h } ||
                    (0 until y).all { input[it][x].digitToInt() < h } ||
                    (x + 1 until input[y].length).all { input[y][it].digitToInt() < h } ||
                    (y + 1 until input.size).all { input[it][x].digitToInt() < h }
        }

    override fun solve2(input: List<String>): Int = (input.indices).asSequence()
        .flatMap { y -> input[y].indices.asSequence().map { x -> x to y } }
        .maxOf { (x, y) ->
            val w = input[y].length
            val h = input.size
            val l = input[y][x].digitToInt()

            var distLt = (x-1 downTo 0).takeWhile { input[y][it].digitToInt() < l }.count()
            var distRt = (x+1 until w).takeWhile { input[y][it].digitToInt() < l }.count()
            var distUp = (y-1 downTo 0).takeWhile { input[it][x].digitToInt() < l }.count()
            var distDn = (y+1 until w).takeWhile { input[it][x].digitToInt() < l }.count()

            // add 1 for blocking tree if view is blocked
            if (distLt < x) distLt++
            if (distRt < w-x-1) distRt++
            if (distUp < y) distUp++
            if (distDn < h-y-1) distDn++

            distLt * distRt * distUp * distDn
        }
}