package y2015

import AocPuzzle
import extractNumbers
import permutations

fun main() = Day13.runAll()

object Day13 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        return parsePersons(input).values.permutations().maxOf { arrangement ->
            (arrangement + arrangement.first())
                .windowed(2)
                .sumOf { (a, b) -> a.relations[b.name]!! + b.relations[a.name]!! }
        }
    }

    override fun solve2(input: List<String>): Int {
        val persons = parsePersons(input)

        val me = Person("me")
        persons.values.forEach {
            me.relations[it.name] = 0
            it.relations["me"] = 0
        }
        persons["me"] = me

        return persons.values.permutations().maxOf { arrangement ->
            (arrangement + arrangement.first())
                .windowed(2)
                .sumOf { (a, b) -> a.relations[b.name]!! + b.relations[a.name]!! }
        }
    }

    fun parsePersons(input: List<String>): MutableMap<String, Person> {
        val persons = mutableMapOf<String, Person>()
        input.forEach {
            val name = it.substringBefore(' ')
            val person = persons.getOrPut(name) { Person(name) }
            val happiness = it.extractNumbers()[0] * if ("lose" in it) -1 else 1
            val other = it.substringAfterLast(' ').removeSuffix(".")
            person.relations[other] = happiness
        }
        return persons
    }

    class Person(val name: String) {
        val relations = mutableMapOf<String, Int>()
    }
}