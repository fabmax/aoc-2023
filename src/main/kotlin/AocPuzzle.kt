import java.io.File

abstract class AocPuzzle {

    private val day = Regex("""\d+""").find(this::class.simpleName!!)!!.value.toInt()

    open val expectedTest1: Any? = null
    open val expectedTest2: Any? = null

    open val expected1: Any? = null
    open val expected2: Any? = null

    abstract val testInput: String
    private val input = File("inputs/day%02d.txt".format(day)).readLines().dropLastWhile { it.isBlank() }

    abstract fun solve(input: List<String>): Pair<Any, Any>

    fun run(isTest: Boolean) {
        val t = System.nanoTime()
        val (answer1, answer2) = solve(if (isTest) testInput.lines() else input)
        val ms = (System.nanoTime() - t) / 1e6

        val exp1 = if (isTest) expectedTest1 else expected1
        val exp2 = if (isTest) expectedTest2 else expected2
        val correct1 = exp1?.let { if (answer1 == it) "✅ " else "❌ " } ?: "❔ "
        val correct2 = exp2?.let { if (answer2 == it) "✅ " else "❌ " } ?: "❔ "

        println("Day $day:")
        println("  ${correct1}Answer part 1: $answer1")
        println("  ${correct2}Answer part 2: $answer2")
        println("  Took %.3f ms".format(ms))
    }
}