import java.io.File

fun readInput(name: String): List<String> {
    return File("inputs/$name")
        .readLines()
        .filter { it.isNotBlank() }
}

fun parseTestInput(input: String): List<String> {
    return input.trimIndent().lines().filter { it.isNotBlank() }
}
