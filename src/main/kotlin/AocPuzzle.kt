import java.io.File

abstract class AocPuzzle {

    private val day = Regex("""\d+""").find(this::class.simpleName!!)!!.value.toInt()

    open val answer1: Any? = null
    open val answer2: Any? = null
    private val input = File("inputs/day%02d.txt".format(day)).readLines().dropLastWhile { it.isBlank() }

    protected val testInputs = mutableListOf<TestInput>()

    protected abstract fun solve(input: List<String>): Pair<Any, Any>

    protected open fun test1(input: List<String>): Any {
        return solve(input).first
    }

    protected open fun test2(input: List<String>): Any {
        return solve(input).second
    }

    fun start() {
        runTests()
        runPuzzle()
    }

    fun runPuzzle() {
        val t = System.nanoTime()
        val (answer1, answer2) = solve(input)
        val ms = (System.nanoTime() - t) / 1e6

        println("Day $day Puzzle:")
        println("  ${prefix(answer1, this.answer1)}Answer part 1: $answer1")
        println("  ${prefix(answer2, this.answer2)}Answer part 2: $answer2")
        println("  Took %.3f ms".format(ms))
    }

    fun runTests() {
        println("Day $day Tests:")

        testInputs.forEachIndexed { i, test ->
            println("  [Test ${i+1}]:")
            val t = System.nanoTime()
            when {
                test.parts and PART1 != 0 && test.parts and PART2 != 0 -> {
                    val (answer1, answer2) = solve(test.text.lines())
                    println("    ${prefix(answer1, test.expected1)}Answer part 1: $answer1")
                    println("    ${prefix(answer2, test.expected2)}Answer part 2: $answer2")
                }
                test.parts and PART1 != 0 -> {
                    val answer1 = test1(test.text.lines())
                    println("    ${prefix(answer1, test.expected1)}Answer part 1: $answer1")
                }
                test.parts and PART2 != 0 -> {
                    val answer2 = test2(test.text.lines())
                    println("    ${prefix(answer2, test.expected2)}Answer part 1: $answer2")
                }
            }
            val ms = (System.nanoTime() - t) / 1e6
            println("    Took %.3f ms".format(ms))
        }
    }

    private fun prefix(answer: Any?, expected: Any?): String {
        return expected?.let { if (answer == it) "✅ " else "❌ " } ?: "❔ "
    }

    fun testInput(text: String, expected1: Any? = null, expected2: Any? = null, parts: Int = PART1 or PART2) {
        testInputs += TestInput(text, expected1, expected2, parts)
    }

    data class TestInput(val text: String, val expected1: Any?, val expected2: Any?, val parts: Int)

    companion object {
        const val PART1 = 1
        const val PART2 = 2
    }
}