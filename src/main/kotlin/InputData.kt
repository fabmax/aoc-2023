import java.io.File

class InputData(val day: Int) {

    val puzzleInput = File("inputs/day%02d.txt".format(day)).readLines().dropLastWhile { it.isBlank() }
    val testInputs = mutableListOf<TestInput>()

    val answerPart1: Long?
        get() = testInputs.firstOrNull { it.part1 != null }?.part1

    val answerPart2: Long?
        get() = testInputs.firstOrNull { it.part2 != null }?.part2

    init {
        File("inputs/").listFiles { f: File -> f.name.startsWith("day%02d_test".format(day)) }?.let { testFiles ->
            testFiles
                .sortedBy { it.name }
                .forEach {
                    testInputs += TestInput(it)
                }
        }
    }

    class TestInput(file: File) {
        val testInput: List<String>

        var test1: Long? = null
        var test2: Long? = null
        var part1: Long? = null
        var part2: Long? = null

        init {
            val allLines = file.readLines()
            testInput = allLines.drop(1).dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }

            allLines[0]
                .split(";")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .forEach { meta ->
                    val (key, value) = meta.split("=").map { it.trim() }
                    when (key) {
                        "test1" -> { test1 = value.toLongOrNull() }
                        "test2" -> { test2 = value.toLongOrNull() }
                        "part1" -> { part1 = value.toLongOrNull() }
                        "part2" -> { part2 = value.toLongOrNull() }
                    }
                }
        }

    }
}