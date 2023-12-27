abstract class AocPuzzle<A: Any, B: Any> {

    val year = Regex("""\Ay(\d+)""").find(this::class.qualifiedName!!)?.groups?.get(1)?.value?.toInt() ?: 2023
    val day = Regex("""\d+""").find(this::class.simpleName!!)!!.value.toInt()

    var run: Run = Run.TestRun(0)
        private set

    val inputData = InputData(year, day)

    val input: List<String>
        get() = when(val r = run) {
            is Run.TestRun -> inputData.testInputs[r.testIdx].testInput
            is Run.PuzzleRun -> inputData.puzzleInput
        }
    val rawInput: String
        get() = when(val r = run) {
            is Run.TestRun -> inputData.testInputs[r.testIdx].testInputRaw
            is Run.PuzzleRun -> inputData.puzzleInputRaw
        }

    val expected1: String?
        get() = when(val r = run) {
            is Run.TestRun -> inputData.testInputs[r.testIdx].test1
            is Run.PuzzleRun -> inputData.answerPart1
        }

    val expected2: String?
        get() = when(val r = run) {
            is Run.TestRun -> inputData.testInputs[r.testIdx].test2
            is Run.PuzzleRun -> inputData.answerPart2
        }

    fun isTestRun(idx: Int = -1): Boolean {
        return when (val r = run) {
            is Run.TestRun -> idx < 0 || idx == r.testIdx
            is Run.PuzzleRun -> false
        }
    }

    open fun prepareRun(run: Run) {
        this.run = run
    }
    
    open fun solve1(input: List<String>): A {
        throw PartNotImplementedException(1)
    }

    open fun solve2(input: List<String>): B {
        throw PartNotImplementedException(2)
    }
    
    fun runAll() {
        runTests()
        println()
        runPuzzle()
    }

    fun runPuzzle() {
        println("Day $day Puzzle:")

        prepareRun(Run.PuzzleRun)
        runParts(part1 = true, part2 = true)
    }

    fun runTests() {
        println("Day $day Tests:")

        inputData.testInputs.forEachIndexed { i, test ->
            println("  [Test ${i+1}]:")
            prepareRun(Run.TestRun(i))

            val isTestPart1 = test.test1 != null
            val isTestPart2 = test.test2 != null
            runParts(isTestPart1, isTestPart2)
        }
    }

    private fun runParts(part1: Boolean, part2: Boolean) {
        if (part1) {
            runPart(1, expected1)
        }
        if (part2) {
            runPart(2, expected2)
        }
    }

    private fun runPart(part: Int, expected: String?) {
        try {
            val t = System.nanoTime()
            val answer: Any = if (part == 1) {
                solve1(input)
            } else {
                solve2(input)
            }
            val t1 = (System.nanoTime() - t) / 1e6
            val answerStr = "${prefix(answer, expected)}Answer part $part: $answer"
            println("  %-36s%9.3f ms".format(answerStr, t1))
        } catch (e: PartNotImplementedException) {
            println("  Part ${e.part} not yet implemented")
        }
    }

    private fun prefix(answer: Any?, expected: String?): String {
        return when {
            expected == null -> "❔ "
            answer.toString() == expected -> "✅ "
            else -> "❌ "
        }
    }

    sealed class Run {
        data class TestRun(val testIdx: Int) : Run()
        data object PuzzleRun : Run()
    }
    
    class PartNotImplementedException(val part: Int) :  IllegalStateException()
}