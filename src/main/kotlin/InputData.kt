import java.io.File

class InputData(year: Int, day: Int) {

    val puzzleInputRaw = File("inputs/$year/day%02d.txt".format(day)).readText()
    val puzzleInput: List<String>
        get() = puzzleInputRaw.lines().dropLastWhile { it.isBlank() }

    val testInputs = mutableListOf<TestInput>()

    val answerPart1: String?
        get() = testInputs.firstOrNull { it.part1 != null }?.part1

    val answerPart2: String?
        get() = testInputs.firstOrNull { it.part2 != null }?.part2

    init {
        File("inputs/$year/").listFiles { f: File -> f.name.startsWith("day%02d_test".format(day)) }?.let { testFiles ->
            testFiles
                .sortedBy { it.name }
                .forEach {
                    testInputs += TestInput(it)
                }
        }
    }

    class TestInput(file: File) {
        val testInputRaw: String
        val testInput: List<String>

        var test1: String? = null
        var test2: String? = null
        var part1: String? = null
        var part2: String? = null

        init {
            val allLines = file.readLines()
            testInput = allLines.drop(1).dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }
            testInputRaw = testInput.joinToString(System.lineSeparator())

            allLines[0]
                .split(";")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .forEach { meta ->
                    val (key, value) = meta.split("=").map { it.trim() }
                    when (key) {
                        "test1" -> { test1 = parseExpected(value) }
                        "test2" -> { test2 = parseExpected(value) }
                        "part1" -> { part1 = parseExpected(value) }
                        "part2" -> { part2 = parseExpected(value) }
                    }
                }
        }

        private fun parseExpected(value: String): String? = if (value == "?" || value.isBlank()) null else value.trim()
    }
}