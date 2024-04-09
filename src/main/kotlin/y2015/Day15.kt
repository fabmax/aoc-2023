package y2015

import AocPuzzle
import extractNumbers

fun main() = Day15.runAll()

object Day15 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val ingredients = input.map {
            val name = it.substringBefore(':')
            val values = it.extractNumbers()
            Ingredient(name, values[0], values[1], values[2], values[3], values[4])
        }
        return part1Fast(ingredients)
//        return split(100, ingredients.size).maxOf { computeScore(ingredients, it) }
    }

    override fun solve2(input: List<String>): Int {
        val ingredients = input.map {
            val name = it.substringBefore(':')
            val values = it.extractNumbers()
            Ingredient(name, values[0], values[1], values[2], values[3], values[4])
        }

        return split(100, ingredients.size)
            .filter { amounts ->
                amounts.foldIndexed(0) { i, acc, amount -> acc + ingredients[i].calories * amount } == 500
            }
            .maxOf { computeScore(ingredients, it) }
    }

    fun split(amount: Int, buckets: Int): Sequence<List<Int>> = sequence {
        if (buckets <= 1) {
            yield(listOf(amount))
        } else {
            for (i in 0 .. amount) {
                split(amount - i, buckets - 1).forEach {
                    yield(listOf(i) + it)
                }
            }
        }
    }

    fun computeScore(ingredients: List<Ingredient>, amounts: List<Int>): Int {
        val cap = ingredients.foldIndexed(0) { i, acc, ingredient -> acc + ingredient.capacity * amounts[i] }.coerceAtLeast(0)
        val dur = ingredients.foldIndexed(0) { i, acc, ingredient -> acc + ingredient.durability * amounts[i] }.coerceAtLeast(0)
        val flv = ingredients.foldIndexed(0) { i, acc, ingredient -> acc + ingredient.flavor * amounts[i] }.coerceAtLeast(0)
        val tex = ingredients.foldIndexed(0) { i, acc, ingredient -> acc + ingredient.texture * amounts[i] }.coerceAtLeast(0)
        return cap * dur * flv * tex
    }

    fun part1Fast(ingredients: List<Ingredient>): Int {
        val amounts = MutableList(ingredients.size) { 100 / ingredients.size }
        outer@
        while (true) {
            val prevScore = computeScore(ingredients, amounts)

            for (i in amounts.indices) {
                if (amounts[i] == 0) continue
                amounts[i]--

                for (j in amounts.indices) {
                    if (j == i) continue
                    amounts[j]++
                    if (computeScore(ingredients, amounts) > prevScore) {
                        continue@outer
                    } else {
                        amounts[j]--
                    }
                }
                amounts[i]++
            }
            break
        }
        return computeScore(ingredients, amounts)
    }

    data class Ingredient(val name: String, val capacity: Int, val durability: Int, val flavor: Int, val texture: Int, val calories: Int)
}