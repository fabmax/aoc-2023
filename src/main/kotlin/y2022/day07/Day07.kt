package y2022.day07

import AocPuzzle

fun main() = Day07.runAll()

object Day07 : AocPuzzle<Int, Int>() {

    override fun solve1(input: List<String>): Int {
        val root = parseFileSystem(input)
        return root.collect { it is Dir && it.size <= 100000 }
            .flatMap { dir -> dir.collect { it is File } }
            .sumOf { it.size }
    }

    override fun solve2(input: List<String>): Int {
        val root = parseFileSystem(input)

        val totalSize = 70000000
        val required = 30000000
        val unused = totalSize - root.size
        val needToDelete = required - unused

        return root.collect { it is Dir }.sortedBy { it.size }.first { it.size >= needToDelete }.size
    }

    fun parseFileSystem(input: List<String>): Dir {
        val root = Dir("/", null)
        var lineI = 0
        var workingDir = root
        while (lineI < input.size) {
            val cmd = input[lineI++]

            when {
                cmd == "$ cd /" -> workingDir = root
                cmd.startsWith("$ cd ") -> workingDir = workingDir.cd(cmd.removePrefix("$ cd "))
                cmd == "$ ls" -> {
                    while (lineI < input.size && !input[lineI].startsWith("$")) {
                        val (a, b) = input[lineI].split(" ")
                        workingDir.content[b] = if (a == "dir") {
                            Dir(b, workingDir)
                        } else {
                            File(b, workingDir, a.toInt())
                        }
                        lineI++
                    }
                }
            }
        }
        return root
    }

    sealed class FsItem(val name: String, val parent: Dir?) {
        abstract val size: Int

        open fun collect(predicate: (FsItem) -> Boolean): List<FsItem> {
            return if (predicate(this)) {
                listOf(this)
            } else {
                emptyList()
            }
        }
    }

    class Dir(name: String, parent: Dir?) : FsItem(name, parent) {
        val content = mutableMapOf<String, FsItem>()

        override val size: Int
            get() = content.values.sumOf { it.size }

        fun cd(to: String): Dir {
            return if (to == "..") {
                parent!!
            } else {
                content.getOrPut(to) { Dir(to, this) } as Dir
            }
        }

        override fun collect(predicate: (FsItem) -> Boolean): List<FsItem> {
            return super.collect(predicate) + content.values.flatMap { it.collect(predicate) }
        }
    }

    class File(name: String, parent: Dir?, override val size: Int) : FsItem(name, parent)
}