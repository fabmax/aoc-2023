package day19

import AocPuzzle
import splitByBlankLines

fun main() = Day19().start()

class Day19 : AocPuzzle() {
    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val (workflowEnc, partsEnc) = input.splitByBlankLines()

        val workflows = workflowEnc.map { Workflow(it) }.associateBy { it.name }
        val parts = partsEnc.map { Part(it) }

        return part1(workflows, parts) to part2(workflows)
    }

    fun part2(workflows: Map<String, Workflow>): Int {
        return 0
    }

    fun part1(workflows: Map<String, Workflow>, parts: List<Part>): Int {
        val accepted = parts.filter { part ->
            var wf: Workflow? = workflows["in"]
            var isAccepted = false
            while (wf != null) {
                val next = wf.nextWorkflow(part)
                wf = workflows[next]
                if (wf == null && next == "A") {
                    isAccepted = true
                }
            }
            isAccepted
        }
        return accepted.sumOf { it.x + it.m + it.a + it.s }
    }



    fun Workflow.nextWorkflow(part: Part): String {
        return rules.firstOrNull { it.test(part) }?.next ?: elseNext
    }

    fun Workflow(line: String): Workflow {
        val (name, compares) = line.split("{")
        val elseNext = compares.substringAfterLast(",").removeSuffix("}")
        val rules = compares.substringBeforeLast(",")
            .split(",")
            .map { WorkflowRule(it) }
        return Workflow(name, rules, elseNext)
    }

    fun WorkflowRule(encoded: String): WorkflowRule {
        if (encoded == "A") {
            return ACCEPT
        } else if (encoded == "R") {
            return REJECT
        }

        val (compare, next) = encoded.split(":")
        return if ('<' in encoded) {
            val (prop, value) = compare.split("<")
            WorkflowRule(prop[0], '<', value.toInt(), next)
        } else {
            val (prop, value) = compare.split(">")
            WorkflowRule(prop[0], '>', value.toInt(), next)
        }
    }

    fun Part(encoded: String): Part {
        val (x, m, a, s) = NUMBERS.findAll(encoded).map { it.value.toInt() }.toList()
        return Part(x, m, a, s)
    }

    data class Workflow(val name: String, val rules: List<WorkflowRule>, val elseNext: String)

    data class WorkflowRule(val prop: Char, val op: Char, val thresh: Int, val next: String) {
        fun test(part: Part): Boolean {
            val value = when (prop) {
                'x' -> part.x
                'm' -> part.m
                'a' -> part.a
                's' -> part.s
                else -> error("unreachable")
            }
            return if (op == '>') value > thresh else value < thresh
        }
    }

    data class Part(val x: Int, val m: Int, val a: Int, val s: Int)

    companion object {
        val NUMBERS = Regex("""(\d+)""")
        val ACCEPT = WorkflowRule('A', 'A', 0, "")
        val REJECT = WorkflowRule('R', 'R', 0, "")
    }
}