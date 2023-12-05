import java.io.File

fun readInput(name: String, dropBlanks: Boolean = true): List<String> {
    val lines = File("inputs/$name").readLines()
    return if (dropBlanks) lines.filter { it.isNotBlank() } else lines
}

fun parseTestInput(input: String, dropBlanks: Boolean = true): List<String> {
    val lines = input.trimIndent().lines()
    return if (dropBlanks) lines.filter { it.isNotBlank() } else lines
}
