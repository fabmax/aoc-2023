package day19

import de.fabmax.kool.KoolApplication
import de.fabmax.kool.KoolConfigJvm
import de.fabmax.kool.modules.ksl.KslComputeShader
import de.fabmax.kool.modules.ksl.lang.*
import de.fabmax.kool.pipeline.ComputeRenderPass
import de.fabmax.kool.pipeline.StorageTexture2d
import de.fabmax.kool.pipeline.TexFormat
import de.fabmax.kool.scene.scene
import de.fabmax.kool.util.Int32Buffer
import splitByBlankLines
import java.io.File
import kotlin.math.max
import kotlin.system.exitProcess

fun main() = KoolApplication(
    KoolConfigJvm(isVsync = false)
) { ctx ->
    ctx.scenes += scene {

        val computeShader = KslComputeShader("Compute shader test") {
            computeStage(8, 8) {
                val workflowStorage = storage2d<KslInt1>("workflowStorage")
                val acceptCounts = storage2d<KslInt1>("acceptCounts")

                val startIndex = uniformInt1("startIndex")
                val acceptIndex = uniformInt1("acceptIndex")
                val rejectIndex = uniformInt1("rejectIndex")
                val x = uniformInt1("ux")
                val fromS = uniformInt1("fromS")
                val toS = uniformInt1("toS")

                val isAccepted = functionBool1("isAccepted") {
                    val part = paramInt4()

                    body {
                        val workflowIdx = int1Var(startIndex)
                        `while`((workflowIdx ne acceptIndex) and (workflowIdx ne rejectIndex)) {
                            val nextWorkflowIdx = int1Var(storageRead(workflowStorage, int2Value(workflowIdx, 0.const)))
                            val offsets = int1Var(storageRead(workflowStorage, int2Value(workflowIdx, 1.const)))
                            val ruleStart = int1Var(offsets shr 16.const)
                            val ruleEnd = int1Var(offsets and 0xffff.const)

                            workflowIdx set nextWorkflowIdx
                            fori(ruleStart, ruleEnd) { i ->
                                val rule = int1Var(storageRead(workflowStorage, int2Value(i, 2.const)))
                                val op = int1Var(rule and (1 shl 18).const)
                                val compI = int1Var((rule and (3 shl 16).const) shr 16.const)
                                val thresh = int1Var(rule and 0xffff.const)

                                val partVal = int1Var(part.x)
                                `if`(compI eq 1.const) {
                                    partVal set part.y
                                }.elseIf(compI eq 2.const) {
                                    partVal set part.z
                                }.elseIf(compI eq 3.const) {
                                    partVal set part.w
                                }

                                `if`(((op eq 0.const) and (partVal lt thresh)) or
                                        ((op ne 0.const) and (partVal gt thresh))) {
                                    workflowIdx set (rule shr 20.const)
                                    `break`()
                                }
                            }

                            `if`(workflowIdx eq (-1).const) {
                                workflowIdx set nextWorkflowIdx
                            }
                        }

                        workflowIdx eq acceptIndex
                    }
                }

                main {
                    val m = int1Var(inGlobalInvocationId.x.toInt1() + 1.const)
                    val a = int1Var(inGlobalInvocationId.y.toInt1() + 1.const)

                    val acceptCnt = int1Var(0.const)
                    fori(fromS, toS) { s ->
                        val xmas = int4Var(KslValueInt4(x, m, a, s))
                        `if`(isAccepted(xmas)) {
                            acceptCnt += 1.const
                        }
                    }
                    int1Var(storageAtomicAdd(acceptCounts, inGlobalInvocationId.xy.toInt2(), acceptCnt))
                }
            }
        }

        val input = File("inputs/day19.txt").readLines()
        val (workflowDefs, _) = input.splitByBlankLines()
        val workflowsByName = workflowDefs.map { Workflow(it) }.associateBy { it.name } + ("A" to Day19.ACCEPT) + ("R" to Day19.REJECT)

        val (workflows, storage) = loadWorkflows(workflowsByName)
        val acceptCounts = StorageTexture2d(4000, 4000, TexFormat.R_I32)

        computeShader.storage2d("workflowStorage", storage)
        computeShader.storage2d("acceptCounts", acceptCounts)
        computeShader.uniform1i("startIndex", workflows.startIdx)
        computeShader.uniform1i("acceptIndex", workflows.acceptIdx)
        computeShader.uniform1i("rejectIndex", workflows.rejectIdx)

        var x by computeShader.uniform1i("ux", 1)
        var fromS by computeShader.uniform1i("fromS", 1)
        var toS by computeShader.uniform1i("toS", 41)

        val computePass = ComputeRenderPass(computeShader, 4000, 4000)
        val computeTask = computePass.tasks[0]

        val startT = System.nanoTime()

        val xLimit = 10
        var subStep = 0

        // xLimit 400:
        // cpu: 12916024642920
        // gpu: 12916024642920
        // Stopping after 567,831 s seconds
        // 45,084 Gops, estimated total time: 1h:34m

        computeTask.onBeforeDispatch {
            fromS = subStep * 80 + 1
            toS = (subStep + 1) * 80 + 1
        }

        computeTask.onAfterDispatch {
            if (++subStep == 50) {
                println("  progress: %.1f %% (%.3f %% total) ...".format(100.0 * x / xLimit, 100.0 * x / 4000))
                if (x >= xLimit) {
                    val time = (System.nanoTime() - startT) / 1e9
                    val totalTime = time * (4000.0 / xLimit)
                    val totalH = (totalTime / 3600).toInt()
                    val totalM = ((totalTime % 3600) / 60).toInt()
                    val totalOps = xLimit * 4000L * 4000L * 4000L

                    println("Stopping after %.3f s seconds\n%.3f Gops, estimated total time: %dh:%02dm"
                        .format(time, totalOps / 1e9 / time, totalH, totalM)
                    )
                    removeOffscreenPass(computePass)

                    val expectedCount = Day19().part2(workflowsByName, 1..xLimit)
                    readAcceptCounts(acceptCounts, expectedCount)
                }

                subStep = 0
                x++
            }
        }

        addOffscreenPass(computePass)
    }
}

fun readAcceptCounts(acceptStorage: StorageTexture2d, expectedCount: Long) {
    acceptStorage.readbackTextureData {
        val buf = it.data as Int32Buffer

        var sum = 0L
        for (y in 0 ..< 4000) {
            for (x in 0 ..< 4000) {
                sum += buf[y * it.width + x]
            }
        }
        println("Accept count:   $sum")
        println("Expected count: $expectedCount")
        exitProcess(0)
    }
}

fun loadWorkflows(workflowsByName: Map<String, Workflow>): Pair<Workflows, StorageTexture2d> {
    val workflows = Workflows(workflowsByName)
    val numWorkflows = workflows.workflows.size
    val numRules = workflows.rules.size
    val storage = StorageTexture2d(max(numRules, numWorkflows), 3, TexFormat.R_I32)
    storage.loadTextureData {
        it as Int32Buffer
        for (i in 0 until numWorkflows) {
            it[i] = workflows.nexts[i]
        }
        for (i in 0 until numWorkflows) {
            it[storage.width + i] = workflows.ruleOffsets[i]
        }
        for (i in 0 until numRules) {
            it[storage.width * 2 + i] = workflows.rules[i]
        }
    }
    return workflows to storage
}
