package y2022.day21

import AocPuzzle

fun main() = Day21.runAll()

object Day21 : AocPuzzle<Long, Long>() {
    override fun solve1(input: List<String>): Long {
        val monkeys = input.map { Monkey(it) }.associateBy { it.name }
        return monkeys["root"]!!.resolve(monkeys)
    }

    override fun solve2(input: List<String>): Long {
        val monkeys = input.map { Monkey(it) }.associateBy { it.name }

        val root = monkeys["root"]!!
        val monkeyLt = monkeys[root.left]!!
        val monkeyRt = monkeys[root.right]!!
        val left = monkeyLt.resolveOrNull(monkeys)
        val right = monkeyRt.resolveOrNull(monkeys)

        return if (right != null) {
            solveForHuman(right, monkeyLt, monkeys)
        } else {
            solveForHuman(left!!, monkeyRt, monkeys)
        }
    }

    private fun solveForHuman(result: Long, it: Monkey, monkeys: Map<String, Monkey>): Long {
        if (it.name == "humn") {
            return result
        }

        val monkeyLt = monkeys[it.left]!!
        val monkeyRt = monkeys[it.right]!!
        val left = monkeyLt.resolveOrNull(monkeys)
        val right = monkeyRt.resolveOrNull(monkeys)

        return if (left != null) {
            val r = when (it.job) {
                Job.Add -> result - left
                Job.Multiply -> result / left
                Job.Subtract -> left - result
                Job.Divide -> left / result
                is Job.Number -> error("invalid job")
            }
            solveForHuman(r, monkeyRt, monkeys)

        } else {
            right!!
            val r = when (it.job) {
                Job.Add -> result - right
                Job.Multiply -> result / right
                Job.Subtract -> result + right
                Job.Divide -> result * right
                is Job.Number -> error("invalid job")
            }
            solveForHuman(r, monkeyLt, monkeys)
        }
    }

    fun Monkey.resolve(monkeys: Map<String, Monkey>): Long {
        return when (job) {
            is Job.Number -> job.number
            Job.Add -> monkeys[left]!!.resolve(monkeys) + monkeys[right]!!.resolve(monkeys)
            Job.Subtract -> monkeys[left]!!.resolve(monkeys) - monkeys[right]!!.resolve(monkeys)
            Job.Multiply -> monkeys[left]!!.resolve(monkeys) * monkeys[right]!!.resolve(monkeys)
            Job.Divide -> monkeys[left]!!.resolve(monkeys) / monkeys[right]!!.resolve(monkeys)
        }
    }

    @Suppress("KotlinConstantConditions")
    fun Monkey.resolveOrNull(monkeys: Map<String, Monkey>): Long? {
        return if (name == "humn") {
            null
        } else if (job is Job.Number) {
            job.number
        } else {
            val left = monkeys[left]!!.resolveOrNull(monkeys) ?: return null
            val right = monkeys[right]!!.resolveOrNull(monkeys) ?: return null
            when (job) {
                Job.Add -> left + right
                Job.Subtract -> left - right
                Job.Divide -> left / right
                Job.Multiply -> left * right
                is Job.Number -> error("unreachable")
            }
        }
    }

    fun Monkey(line: String): Monkey {
        val name = line.substringBefore(':')
        val jobParts = line.substringAfter(": ").split(" ")

        return if (jobParts.size == 1) {
            Monkey(name, Job.Number(jobParts[0].toLong()), emptyList())
        } else {
            when (jobParts[1]) {
                "+" -> Monkey(name, Job.Add, listOf(jobParts[0], jobParts[2]))
                "-" -> Monkey(name, Job.Subtract, listOf(jobParts[0], jobParts[2]))
                "*" -> Monkey(name, Job.Multiply, listOf(jobParts[0], jobParts[2]))
                "/" -> Monkey(name, Job.Divide, listOf(jobParts[0], jobParts[2]))
                else -> error("invalid: ${jobParts[1]}")
            }
        }
    }

    data class Monkey(val name: String, val job: Job, val dependsOn: List<String>) {
        val left: String
            get() = dependsOn[0]
        val right: String
            get() = dependsOn[1]
    }

    sealed class Job {
        data class Number(val number: Long) : Job()
        data object Add : Job()
        data object Subtract : Job()
        data object Multiply : Job()
        data object Divide : Job()
    }
}