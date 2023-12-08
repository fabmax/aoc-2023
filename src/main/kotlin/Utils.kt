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

fun findPrimes(upperLimit: Int): List<Int> = (2..upperLimit).filter { it.isPrime }

val Int.isPrime: Boolean get() = (2 .. (this / 2)).none { this % it == 0 }
