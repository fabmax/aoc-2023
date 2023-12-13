abstract class AocPuzzle {

    private val day = Regex("""\d+""").find(this::class.simpleName!!)!!.value.toInt()

    val inputData = InputData(day)

    protected abstract fun solve(input: List<String>): Pair<Any?, Any?>

    protected open fun test1(input: List<String>): Any? {
        return solve(input).first
    }

    protected open fun test2(input: List<String>): Any? {
        return solve(input).second
    }

    fun start() {
        runTests()
        println()
        runPuzzle()
    }

    fun runPuzzle() {
        println("Day $day Puzzle:")

        val t = System.nanoTime()
        val (answer1, answer2) = solve(inputData.puzzleInput)
        val ms = (System.nanoTime() - t) / 1e6

        println("  ${prefix(answer1, inputData.answerPart1)}Answer part 1: $answer1")
        println("  ${prefix(answer2, inputData.answerPart2)}Answer part 2: $answer2")
        println("  Took %.3f ms".format(ms))
    }

    fun runTests() {
        println("Day $day Tests:")

        inputData.testInputs.forEachIndexed { i, test ->
            println("  [Test ${i+1}]:")

            val isTestPart1 = test.test1 != null
            val isTestPart2 = test.test2 != null

            val t = System.nanoTime()
            when {
                isTestPart1 && isTestPart2 -> {
                    val (answer1, answer2) = solve(test.testInput)
                    println("    ${prefix(answer1, test.test1)}Answer part 1: $answer1")
                    println("    ${prefix(answer2, test.test2)}Answer part 2: $answer2")
                }
                isTestPart1 -> {
                    val answer1 = test1(test.testInput)
                    println("    ${prefix(answer1, test.test1)}Answer part 1: $answer1")
                }
                isTestPart2 -> {
                    val answer2 = test2(test.testInput)
                    println("    ${prefix(answer2, test.test2)}Answer part 2: $answer2")
                }
            }
            val ms = (System.nanoTime() - t) / 1e6
            println("    Took %.3f ms".format(ms))
        }
    }

    private fun prefix(answer: Any?, expected: Long?): String {
        return when {
            expected == null -> "❔ "
            answer is Int -> if (answer.toLong() == expected) "✅ " else "❌ "
            answer is Long -> if (answer == expected) "✅ " else "❌ "
            else -> "❔ "
        }
    }
}