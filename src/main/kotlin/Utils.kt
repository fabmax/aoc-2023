@file:Suppress("unused")

import de.fabmax.kool.math.Vec2i
import kotlin.math.abs

fun findPrimeFactors(number: Int, primes: List<Int>): List<Int> {
    return primes.filter { prime -> number % prime == 0 }
}

inline fun <T> List<T>.splitBy(predicate: (T) -> Boolean): List<List<T>> {
    return (listOf(-1) + indices.filter { predicate(get(it)) } + listOf(size))
        .zipWithNext { from, to -> subList(from + 1, to) }
}

fun List<String>.splitByBlankLines(): List<List<String>> {
    return splitBy { it.isBlank() }
}

val IntRange.size: Int get() = if (isEmpty()) 0 else last - first + 1

fun IntRange.clipLower(min: Int): IntRange {
    return (kotlin.math.max(first, min)..last)
}

fun IntRange.clipUpper(max: Int): IntRange {
    return (first..kotlin.math.min(last, max))
}

fun Vec2i.manhattanDistance(other: Vec2i):Int = abs(x - other.x) + abs(y - other.y)

fun findPrimes(upperLimit: Int): List<Int> = (2..upperLimit).filter { it.isPrime }

val Int.isPrime: Boolean get() = (2 .. (this / 2)).none { this % it == 0 }

fun leastCommonMultiple(ints: Collection<Int>): Long {
    val primes = findPrimes(ints.max())
    return ints
        .flatMap { findPrimeFactors(it, primes) }
        .distinct()
        .fold(1L) { prod, value -> prod * value }
}

fun printColored(text: String, fg: AnsiColor? = null, bg: AnsiColor? = null) {
    val fgCode = fg?.let { "${it.fg}" } ?: ""
    val bgCode = bg?.let { "${it.bg}" } ?: ""
    val sep = if (fgCode.isNotBlank() && bgCode.isNotBlank()) ";" else ""
    print("\u001b[${fgCode}${sep}${bgCode}m$text\u001B[0m")
}

inline fun <R> timed(block: () -> R): R {
    val t = System.nanoTime()
    val result = block()
    println("%.3f ms".format((System.nanoTime() - t) / 1e6))
    return result
}

@Suppress("unused")
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
