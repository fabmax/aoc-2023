package day19

import AocPuzzle
import clipLower
import clipUpper
import size
import splitByBlankLines

fun main() = Day19().start()

typealias Part = List<Int>

class Day19 : AocPuzzle() {

    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val (workflowDefs, partsDefs) = input.splitByBlankLines()

        val workflows = workflowDefs.map { Workflow(it) }.associateBy { it.name } + ("A" to ACCEPT) + ("R" to REJECT)
        val parts = partsDefs.map { partDef ->
            NUMBERS.findAll(partDef).map { it.value.toInt() }.toList()
        }

        return part1(workflows, parts) to part2(workflows)
    }

    fun part2(workflows: Map<String, Workflow>): Long {
        val acceptedRanges = mutableListOf<PartRange>()

        fun Workflow.traverse(inRange: PartRange) {
            rules.forEach {
                val outRange = inRange.copy()
                it.applyToRange(outRange)

                val nextWorkflow = workflows[it.next]!!
                if (!nextWorkflow.isTerminal) {
                    nextWorkflow.traverse(outRange)
                } else if (nextWorkflow === ACCEPT) {
                    acceptedRanges += outRange
                }
                it.excludeFromRange(inRange)
            }

            val elseWf = workflows[elseNext]!!
            if (!elseWf.isTerminal) {
                elseWf.traverse(inRange)
            } else if (elseWf === ACCEPT) {
                acceptedRanges += inRange
            }
        }

        workflows["in"]!!.traverse(PartRange())
        return acceptedRanges.sumOf { it.combinations }
    }

    fun part1(workflows: Map<String, Workflow>, parts: List<Part>): Int {
        val accepted = parts.filter { part ->
            var wf: Workflow = workflows["in"]!!
            var isAccepted = false
            while (!isAccepted && wf != REJECT) {
                wf = workflows[wf.nextWorkflow(part)]!!
                if (wf == ACCEPT) {
                    isAccepted = true
                }
            }
            isAccepted
        }
        return accepted.sumOf { it.sum() }
    }

    fun Workflow.nextWorkflow(part: Part): String {
        return rules.firstOrNull { it.test(part) }?.next ?: elseNext
    }

    companion object {
        val NUMBERS = Regex("""(\d+)""")
        val ACCEPT = Workflow("A", emptyList(), "")
        val REJECT = Workflow("R", emptyList(), "")
    }
}

fun Workflow(line: String): Workflow {
    val (name, compares) = line.split("{")
    val elseNext = compares.substringAfterLast(",").removeSuffix("}")
    val rules = compares.substringBeforeLast(",")
        .split(",")
        .map { Rule(it) }
    return Workflow(name, rules, elseNext)
}

fun Rule(encoded: String): Rule {
    val ruleProps = listOf('x', 'm', 'a', 's')
    val (ruleDef, next) = encoded.split(":")
    return if ('<' in encoded) {
        val (prop, value) = ruleDef.split("<")
        RuleLt(ruleProps.indexOf(prop[0]), value.toInt(), next)
    } else {
        val (prop, value) = ruleDef.split(">")
        RuleGt(ruleProps.indexOf(prop[0]), value.toInt(), next)
    }
}

data class Workflow(val name: String, val rules: List<Rule>, val elseNext: String) {
    val isTerminal: Boolean
        get() = elseNext.isEmpty()
}

sealed class Rule(val prop: Int, val thresh: Int, val next: String) {
    abstract fun test(part: Part): Boolean
    abstract fun applyToRange(range: PartRange)
    abstract fun excludeFromRange(range: PartRange)
}

class RuleLt(prop: Int, thresh: Int, next: String) : Rule(prop, thresh, next) {
    override fun test(part: Part): Boolean = part[prop] < thresh
    override fun applyToRange(range: PartRange) { range.ranges[prop] = range.ranges[prop].clipUpper(thresh - 1) }
    override fun excludeFromRange(range: PartRange) { range.ranges[prop] = range.ranges[prop].clipLower(thresh) }
}

class RuleGt(prop: Int, thresh: Int, next: String) : Rule(prop, thresh, next) {
    override fun test(part: Part): Boolean = part[prop] > thresh
    override fun applyToRange(range: PartRange) { range.ranges[prop] = range.ranges[prop].clipLower(thresh + 1) }
    override fun excludeFromRange(range: PartRange) {  range.ranges[prop] = range.ranges[prop].clipUpper(thresh) }
}

class PartRange {
    val ranges = Array(4) { 1..4000 }

    fun copy(): PartRange {
        val range = PartRange()
        for (i in 0..3) {
            range.ranges[i] = ranges[i]
        }
        return range
    }

    val combinations: Long
        get() = (0..3).map { ranges[it].size }.fold(1L) { acc, it -> acc * it }
}
