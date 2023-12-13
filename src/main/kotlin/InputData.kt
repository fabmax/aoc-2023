import java.io.File

class InputData(val day: Int) {

    val puzzleInput = File("inputs/day%02d.txt".format(day)).readLines().dropLastWhile { it.isBlank() }
    val testInputs = mutableListOf<TestInput>()

    val puzzle1: Long?
        get() = testInputs.firstOrNull { it.puzzle1 != null }?.puzzle1

    val puzzle2: Long?
        get() = testInputs.firstOrNull { it.puzzle2 != null }?.puzzle2

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
        var puzzle1: Long? = null
        var puzzle2: Long? = null

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
                        "test1" -> { test1 = value.toLong() }
                        "test2" -> { test2 = value.toLong() }
                        "puzzle1" -> { puzzle1 = value.toLong() }
                        "puzzle2" -> { puzzle2 = value.toLong() }
                    }
                }
        }

    }
}