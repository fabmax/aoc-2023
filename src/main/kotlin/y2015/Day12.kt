package y2015

import AocPuzzle
import extractNumbers
import kotlinx.serialization.json.*

fun main() = Day12.runAll()

object Day12 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        return input.sumOf { it.extractNumbers().sum() }
    }

    override fun solve2(input: List<String>): Int {
        return input.sumOf { Json.parseToJsonElement(it).sumNonRedNumbers() }
    }

    fun JsonElement.sumNonRedNumbers(): Int {
        return when (this) {
            is JsonPrimitive -> if (!isString) content.toIntOrNull() ?: 0 else 0
            is JsonArray -> sumOf { it.sumNonRedNumbers() }
            is JsonObject -> {
                if (values.any { it is JsonPrimitive && it.isString && it.content == "red" }) {
                    0
                } else {
                    values.sumOf { it.sumNonRedNumbers() }
                }
            }
            else -> 0
        }
    }
}