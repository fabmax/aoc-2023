@file:Suppress("unused")

import de.fabmax.kool.math.*
import kotlin.math.abs

fun findPrimeFactors(number: Int, primes: List<Int>): List<Int> {
    return primes.filter { prime -> number % prime == 0 }
}

fun <T> Collection<T>.permutations(): Sequence<List<T>> = sequence {
    // Heap's algorithm (non-recursive variant)
    val elems = toMutableList()
    val cnts = IntArray(elems.size)

    yield(elems.toList())
    var i = 0
    while (i < elems.size) {
        if (cnts[i] < i) {
            val swapIdx = if (i % 2 == 0) 0 else cnts[i]
            elems[swapIdx] = elems[i].also { elems[i] = elems[swapIdx] }
            yield(elems.toList())
            cnts[i]++
            i = 0
        } else {
            cnts[i] = 0
            i++
        }
    }
}

inline fun <T> List<T>.splitBy(predicate: (T) -> Boolean): List<List<T>> {
    return (listOf(-1) + indices.filter { predicate(get(it)) } + listOf(size))
        .zipWithNext { from, to -> subList(from + 1, to) }
}

fun List<String>.splitByBlankLines(): List<List<String>> {
    return splitBy { it.isBlank() }
}

fun <T> ArrayDeque<T>.takeAndRemoveWhile(predicate: (T) -> Boolean): List<T> {
    val taken = takeWhile(predicate)
    repeat(taken.size) { removeFirst() }
    return taken
}

val IntRange.size: Int get() = if (isEmpty()) 0 else last - first + 1

fun IntRange.clipLower(min: Int): IntRange {
    return (kotlin.math.max(first, min)..last)
}

fun IntRange.clipUpper(max: Int): IntRange {
    return (first..kotlin.math.min(last, max))
}

fun Vec2i.manhattanDistance(other: Vec2i):Int = abs(x - other.x) + abs(y - other.y)

fun Vec3i(str: String, delim: Char = ','): Vec3i {
    val (x, y, z) = str.split(delim).filter { it.isNotBlank() }.map { it.trim().toInt() }
    return Vec3i(x, y, z)
}

fun Vec3f(str: String, delim: Char = ','): Vec3f {
    val (x, y, z) = str.split(delim).filter { it.isNotBlank() }.map { it.trim().toFloat() }
    return Vec3f(x, y, z)
}

fun Vec3d(str: String, delim: Char = ','): Vec3d {
    val (x, y, z) = str.split(delim).filter { it.isNotBlank() }.map { it.trim().toDouble() }
    return Vec3d(x, y, z)
}

fun intersectLines(a1: Vec2d, a2: Vec2d, b1: Vec2d, b2: Vec2d): Vec2d? {
    val denom = (a1.x - a2.x) * (b1.y - b2.y) - (a1.y - a2.y) * (b1.x - b2.x)
    if (denom != 0.0) {
        val a = a1.x * a2.y - a1.y * a2.x
        val b = b1.x * b2.y - b1.y * b2.x
        val x = (a * (b1.x - b2.x) - b * (a1.x - a2.x)) / denom
        val y = (a * (b1.y - b2.y) - b * (a1.y - a2.y)) / denom
        return Vec2d(x, y)
    }
    // lines are parallel
    return null
}

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
