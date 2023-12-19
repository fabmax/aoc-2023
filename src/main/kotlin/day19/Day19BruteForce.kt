package day19

import AocPuzzle
import splitByBlankLines
import kotlin.concurrent.thread

fun main() = Day19BruteForce().runPuzzle()

class Day19BruteForce : AocPuzzle() {
    override fun solve(input: List<String>): Pair<Any?, Any?> {
        val (workflowEnc, partDefs) = input.splitByBlankLines()

        nextWorkflowId = 2
        val workflowsByName = workflowEnc.map { Workflow(it) }.associateBy { it.name } + ("A" to ACCEPT) + ("R" to REJECT)
        workflowsByName.values.forEach { wf ->
            wf.elseNextRef = workflowsByName[wf.elseNext]
            wf.nextNames.forEachIndexed { i, n -> wf.nexts[i] = workflowsByName[n]!! }
        }
        val workflows = Workflows(workflowsByName)

        return part1(workflows, partDefs) to part2(workflows)
    }

    fun part1(workflows: Workflows, partDefs: List<String>): Int {
        val parts = partDefs.map { partDef ->
            Day19.NUMBERS.findAll(partDef).map { it.value.toInt() }.toList().toIntArray()
        }
        return parts.filter { workflows.isAccepted(it) }.sumOf { it.sum() }
    }

    fun part2(workflows: Workflows): Long {
        val startTime = System.nanoTime()

        val jobs = 32
        val maxTime = 60
        val acceptedCnt = LongArray(jobs)
        val totalCnt = LongArray(jobs)

        println("crunching for $maxTime seconds...")

        (0 until jobs).map { t ->
            thread {
                val part = IntArray(4)
                val workSize = 4000 / jobs
                val low = 1 + t * workSize
                val high = (t+1) * workSize
                var accepted = 0L
                var total = 0L

                outer@
                for (x in low..high) {
                    part[0] = x
                    for (m in 1..4000) {
                        part[1] = m
                        for (a in 1..4000) {
                            part[2] = a
                            for (s in 1..4000) {
                                part[3] = s

                                if (workflows.isAccepted(part)) {
                                    accepted++
                                }
                            }
                        }

                        total += 4096 * 4096
                        val elap = (System.nanoTime() - startTime) / 1e9
                        if (t == 0 && m % 5 == 0) {
                            println("${elap.toInt()} secs...")
                        }
                        if (elap > maxTime) {
                            break@outer
                        }
                    }
                }
                totalCnt[t] = total
                acceptedCnt[t] = accepted
            }
        }.forEach { it.join() }

        val accepted = acceptedCnt.sum()
        val totalTime = (System.nanoTime() - startTime)/1e9
        val totalOps = totalCnt.sum()
        val totalProgress = totalOps.toDouble() / (4000L * 4000 * 4000 * 4000)

        println("Stopping after %.3f s seconds\n%.3f Mops (%.3f Mops/thread), total time: %.3f days"
            .format(totalTime, totalOps/1e6/totalTime, totalOps/1e6/totalTime/jobs, (totalTime / totalProgress) / 86400)
        )

        return accepted
    }

    fun Workflow(line: String): Workflow {
        val (name, compares) = line.split("{")
        val elseNext = compares.substringAfterLast(",").removeSuffix("}")
        val rules = compares.substringBeforeLast(",")
            .split(",")
            .map { Rule(it) }

        return Workflow(name, elseNext, rules)
    }

    fun Rule(encoded: String): Pair<Int, String> {
        val ruleProps = listOf('x', 'm', 'a', 's')
        val (ruleDef, next) = encoded.split(":")
        val encOp = if ('<' in encoded) {
            val (prop, value) = ruleDef.split("<")
            (ruleProps.indexOf(prop[0]) shl 16) or value.toInt()
        } else {
            val (prop, value) = ruleDef.split(">")
            1 shl 18 or (ruleProps.indexOf(prop[0]) shl 16) or value.toInt()
        }
        return encOp to next
    }

    class Workflow(val name: String, val elseNext: String, r: List<Pair<Int, String>>) {
        val id = nextWorkflowId++
        var elseNextRef: Workflow? = null

        val rules = IntArray(r.size) { r[it].first }
        val nexts = Array<Workflow?>(r.size) { null }
        val nextNames = Array(r.size) { r[it].second }
    }

    class Workflows(workflows: Map<String, Workflow>) {
        val nexts = IntArray(workflows.size)
        val ruleOffsets = IntArray(workflows.size)
        val ruleEndOffsets = IntArray(workflows.size)
        val rules = IntArray(workflows.values.sumOf { it.rules.size })
        val rulesNexts = IntArray(workflows.values.sumOf { it.rules.size })

        val startIdx = workflows["in"]!!.id
        val acceptIdx = ACCEPT.id
        val rejectIdx = REJECT.id

        init {
            val orderedWfs = workflows.values.sortedBy { it.id }
            var nextRuleIdx = 0
            orderedWfs.forEachIndexed { i, wf ->
                ruleOffsets[i] = nextRuleIdx
                ruleEndOffsets[i] = nextRuleIdx + wf.rules.size
                nexts[i] = wf.elseNextRef?.id ?: -1
                for (j in wf.rules.indices) {
                    rules[nextRuleIdx] = wf.rules[j]
                    rulesNexts[nextRuleIdx] = wf.nexts[j]!!.id
                    nextRuleIdx++
                }
            }
        }

        fun isAccepted(part: IntArray): Boolean {
            var wfIdx = startIdx

            while (wfIdx != acceptIdx && wfIdx != rejectIdx) {
                val ruleStart = ruleOffsets[wfIdx]
                val ruleEnd = ruleEndOffsets[wfIdx]

                var nextWf = -1
                for (i in ruleStart ..< ruleEnd) {
                    val rule = rules[i]

                    val op = rule and (1 shl 18)
                    val pi = (rule and (3 shl 16)) shr 16
                    val th = rule and 0xffff
                    val pp = part[pi]

                    if ((op == 0 && pp < th) || (op != 0 && pp > th)) {
                        nextWf = rulesNexts[i]
                        break
                    }
                }
                wfIdx = if (nextWf != -1) nextWf else nexts[wfIdx]
            }
            return wfIdx == acceptIdx
        }
    }

    companion object {
        private var nextWorkflowId = 0

        val ACCEPT = Workflow("A", "", emptyList())
        val REJECT = Workflow("R", "", emptyList())
    }
}