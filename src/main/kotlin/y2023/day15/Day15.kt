package y2023.day15

import AocPuzzle

fun main() = Day15.runAll()

object Day15 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        return input.flatMap { it.split(",") }.sumOf { Hashmap.hash(it) }
    }

    override fun solve2(input: List<String>): Int {
        val lensMap = Hashmap()
        input
            .flatMap { it.split(",") }
            .forEach { item ->
                val key = item.takeWhile { it.isLetterOrDigit() }
                val op = item.removePrefix(key).first()
                if (op == '-') {
                    lensMap -= key
                } else if (op == '=') {
                    val focalLength = item.removePrefix(key).drop(1).toInt()
                    lensMap[key] = focalLength
                }
            }

        return lensMap.boxes
            .mapIndexed { boxI, entries ->
                entries.mapIndexed { entryI, entry -> (boxI+1) * (entryI+1) * entry.value }.sum()
            }.sum()
    }
}

class Hashmap {
    val boxes = List(256) { mutableListOf<Entry>() }

    operator fun set(key: String, value: Int) {
        val entry = Entry(key, value)
        val box = boxes[hash(key)]
        val existingIdx = box.indexOfFirst { it.key == key }
        if (existingIdx >= 0) {
            box[existingIdx] = entry
        } else {
            box.add(entry)
        }
    }

    operator fun minusAssign(key: String) {
        val box = boxes[hash(key)]
        box.removeAll { it.key == key }
    }

    data class Entry(val key: String, val value: Int)

    companion object {
        fun hash(string: String): Int = string.fold(0) { acc, c -> ((acc + c.code) * 17) and 0xff }
    }
}
