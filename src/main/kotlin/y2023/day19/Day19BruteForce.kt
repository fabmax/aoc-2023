package y2023.day19

import AocPuzzle
import splitByBlankLines
import kotlin.concurrent.thread

fun main() = Day19BruteForce.runPuzzle()

object Day19BruteForce : AocPuzzle<Int, Long>() {

    override fun solve1(input: List<String>): Int {
        val (workflowDefs, partDefs) = input.splitByBlankLines()
        val workflowsByName = workflowDefs.map { Workflow(it) }.associateBy { it.name } + ("A" to Day19.ACCEPT) + ("R" to Day19.REJECT)
        val workflows = Workflows(workflowsByName)

        val parts = partDefs.map { partDef ->
            Day19.NUMBERS.findAll(partDef).map { it.value.toInt() }.toList().toIntArray()
        }
        return parts.filter { workflows.isAccepted(it) }.sumOf { it.sum() }
    }

    override fun solve2(input: List<String>): Long {
        val (workflowDefs, _) = input.splitByBlankLines()
        val workflowsByName = workflowDefs.map { Workflow(it) }.associateBy { it.name } + ("A" to Day19.ACCEPT) + ("R" to Day19.REJECT)
        val workflows = Workflows(workflowsByName)

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
                val xFrom = 1 + t * workSize
                val xTo = (t+1) * workSize
                var accepted = 0L
                var total = 0L

                outer@
                for (x in xFrom..xTo) {
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

        println("Stopping after %.3f s seconds\n%.3f Mops (%.3f Mops/thread), estimated total time: %.3f days"
            .format(totalTime, totalOps/1e6/totalTime, totalOps/1e6/totalTime/jobs, (totalTime / totalProgress) / 86400)
        )

        return accepted
    }
}

class Workflows(val workflows: Map<String, Workflow>) {
    val workflowIds = workflows.values.mapIndexed { i, workflow -> workflow to i }.toMap()

    val nexts = IntArray(workflows.size)
    val ruleOffsets = IntArray(workflows.size)
    val rules = IntArray(workflows.values.sumOf { it.rules.size })

    val startIdx = workflows["in"]!!.id
    val acceptIdx = Day19.ACCEPT.id
    val rejectIdx = Day19.REJECT.id

    init {
        val orderedWfs = workflows.values.sortedBy { it.id }
        var nextRuleIdx = 0
        orderedWfs.forEachIndexed { i, wf ->
            ruleOffsets[i] = (nextRuleIdx shl 16) or (nextRuleIdx + wf.rules.size)
            nexts[i] = wf.elseNextId
            for (j in wf.rules.indices) {
                rules[nextRuleIdx] = wf.rules[j].encoded
                nextRuleIdx++
            }
        }
    }

    fun isAccepted(part: IntArray): Boolean {
        var wfIdx = startIdx

        outer@
        while (wfIdx != acceptIdx && wfIdx != rejectIdx) {
            val offsets = ruleOffsets[wfIdx]
            val ruleStart = offsets shr 16
            val ruleEnd = offsets and 0xffff

            for (i in ruleStart ..< ruleEnd) {
                val rule = rules[i]

                val op = rule and (1 shl 18)
                val pi = (rule and (3 shl 16)) shr 16
                val th = rule and 0xffff
                val pp = part[pi]

                if ((op == 0 && pp < th) || (op != 0 && pp > th)) {
                    wfIdx = rule shr 20
                    continue@outer
                }
            }
            wfIdx = nexts[wfIdx]
        }
        return wfIdx == acceptIdx
    }

    val Workflow.id: Int
        get() = workflowIds[this]!!
    val Workflow.elseNextId: Int
        get() = workflows[elseNext]?.id ?: -1
    val Rule.nextId: Int
        get() = workflows[next]?.id ?: -1

    val Rule.encoded: Int
        get() = when (this) {
            is RuleGt -> 1 shl 18 or (prop shl 16) or thresh or (nextId shl 20)
            is RuleLt -> (prop shl 16) or thresh or (nextId shl 20)
        }
}