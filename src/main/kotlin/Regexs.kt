object Regexs {
    val NUMBERS = Regex("""-?\d+""")
}

fun String.extractNumbers(): List<Int> {
    return Regexs.NUMBERS.findAll(this).map { it.value.toInt() }.toList()
}