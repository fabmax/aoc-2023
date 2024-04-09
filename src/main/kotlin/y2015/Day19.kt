package y2015

import AocPuzzle
import splitByBlankLines

fun main() = Day19.runAll()

object Day19 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val (replacements, molecule) = input.parseInput()
        return replacements.flatMap { (from, to) -> molecule.replaceAll(from, to) }.distinct().size
    }

    override fun solve2(input: List<String>): Int {
        val (replacements, targetMolecule) = input.parseInput()
        val func = DeepRecursiveFunction<String, Int?> { currentMolecule ->
            replacements.firstNotNullOfOrNull { (from, to) ->
                currentMolecule.replaceAll(to, from).firstNotNullOfOrNull { next ->
                    when (next) {
                        "e" -> 1
                        else -> callRecursive(next)?.plus(1)
                    }
                }
            }
        }
        return func.invoke(targetMolecule) ?: -1
    }

    fun String.replaceAll(match: String, replacement: String): List<String> {
        return Regex(match).findAll(this).map { replaceRange(it.range, replacement) }.toList()
    }

    fun List<String>.parseInput(): Pair<List<Pair<String, String>>, String> {
        val (replText, calibList) = splitByBlankLines()
        return replText.map { it.split(" => ").let { (a, b) -> a to b } } to calibList[0]
    }
}