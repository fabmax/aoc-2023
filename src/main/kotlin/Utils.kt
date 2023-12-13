import java.io.File

fun readInput(name: String, dropBlanks: Boolean = true): List<String> {
    val lines = File("inputs/$name").readLines()
    return if (dropBlanks) lines.filter { it.isNotBlank() } else lines
}

fun parseTestInput(input: String, dropBlanks: Boolean = true): List<String> {
    val lines = input.trimIndent().lines()
    return if (dropBlanks) lines.filter { it.isNotBlank() } else lines
}

fun findPrimeFactors(number: Int, primes: List<Int>): List<Int> {
    return primes.filter { prime -> number % prime == 0 }
}

inline fun <T> List<T>.splitBy(predicate: (T) -> Boolean): List<List<T>> {
    return (listOf(-1) + indices.filter { predicate(get(it)) } + listOf(lastIndex))
        .zipWithNext { from, to -> subList(from + 1, to) }
}

fun List<String>.splitByBlankLines(): List<List<String>> {
    return splitBy { it.isBlank() }
}

fun findPrimes(upperLimit: Int): List<Int> = (2..upperLimit).filter { it.isPrime }

val Int.isPrime: Boolean get() = (2 .. (this / 2)).none { this % it == 0 }

fun ansiColor(text: String, fg: AnsiColor? = null, bg: AnsiColor? = null): String {
    val fgCode = fg?.let { "${it.fg}" } ?: ""
    val bgCode = bg?.let { "${it.bg}" } ?: ""
    val sep = if (fgCode.isNotBlank() && bgCode.isNotBlank()) ";" else ""
    return "\u001b[${fgCode}${sep}${bgCode}m$text\u001B[0m"
}

enum class AnsiColor(val fg: Int, val bg: Int) {
    BLACK(30, 40),
    RED(31, 41),
    GREEN(32, 42),
    YELLOW(33, 43),
    BLUE(34, 44),
    MAGENTA(35, 45),
    CYAN(36, 46),
    WHITE(37, 47),

    BRIGHT_BLACK(90, 100),
    BRIGHT_RED(91, 101),
    BRIGHT_GREEN(92, 102),
    BRIGHT_YELLOW(93, 103),
    BRIGHT_BLUE(94, 104),
    BRIGHT_MAGENTA(95, 105),
    BRIGHT_CYAN(96, 106),
    BRIGHT_WHITE(97, 107),
}
