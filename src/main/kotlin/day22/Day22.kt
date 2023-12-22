package day22

import AocPuzzle
import de.fabmax.kool.KoolApplication
import de.fabmax.kool.math.*
import de.fabmax.kool.math.spatial.BoundingBoxD
import de.fabmax.kool.modules.ksl.KslLitShader
import de.fabmax.kool.modules.ksl.KslPbrShader
import de.fabmax.kool.pipeline.ao.AoPipeline
import de.fabmax.kool.scene.addColorMesh
import de.fabmax.kool.scene.orbitCamera
import de.fabmax.kool.scene.scene
import de.fabmax.kool.util.MdColor
import de.fabmax.kool.util.Time
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.min

fun main() = Day22.runAll()

object Day22 : AocPuzzle<Int, Int>() {
    override fun solve1(input: List<String>): Int {
        val brickMap = settleBricks(input)
//        if (!isTestRun()) {
//            drawBricks(brickMap)
//        }
        val bricks = brickMap.values.distinct()
        return bricks.count { brick -> brick.isSafeToRemove(brickMap) }
    }

    override fun solve2(input: List<String>): Int {
        val brickMap = settleBricks(input)
        val bricks = brickMap.values.distinct()
        return bricks.sumOf { (it.collectFalling(brickMap, mutableSetOf(it)) - it).size }
    }

    private fun Brick.collectFalling(brickMap: Map<Vec3i, Brick>, result: MutableSet<Brick>): Set<Brick> {
        val tops = topBricks(brickMap).filter { it !in result }
        tops.filter { it.bottomBricks(brickMap).all { sup -> sup in result } }.forEach { result += it }
        tops.filter { it in result }.forEach { it.collectFalling(brickMap, result) }
        return result
    }

    private fun settleBricks(input: List<String>): Map<Vec3i, Brick> {
        val bricks = input.mapIndexed { i, it ->
            val (start, end) = it.split("~")
            val brick = Brick(i, Vec3i(start), Vec3i(end))
            brick
        }

        val brickMap = mutableMapOf<Vec3i, Brick>()
        bricks.sortedBy { it.bottom }.forEach { b ->
            var brick = b
            while (brick.bottom > 1 && brick.points.map { it - Vec3i.Z_AXIS }.count { it in brickMap.keys } == 0) {
                brick = brick.moveDown()
            }
            brick.points.forEach { brickMap[it] = brick }
        }
        return brickMap
    }

    fun Vec3i(str: String): Vec3i {
        val (x, y, z) = str.split(",").map { it.toInt() }
        return Vec3i(x, y, z)
    }

    data class Brick(val id: Int, val start: Vec3i, val end: Vec3i) {
        val xPoints: List<Vec3i>
            get() = (start.x..end.x).map { Vec3i(it, start.y, start.z) }
        val yPoints: List<Vec3i>
            get() = (start.y..end.y).map { Vec3i(start.x, it, start.z) }
        val zPoints: List<Vec3i>
            get() = (start.z..end.z).map { Vec3i(start.x, start.y, it) }

        val points: Set<Vec3i>
            get() = xPoints.toSet() + yPoints + zPoints

        val bottom: Int
            get() = min(start.z, end.z)

        fun moveDown() = copy(start = start - Vec3i.Z_AXIS, end = end - Vec3i.Z_AXIS)

        fun topBricks(brickMap: Map<Vec3i, Brick>): List<Brick> {
            return points
                .map { it + Vec3i.Z_AXIS }
                .mapNotNull { brickMap[it] }
                .distinct()
                .filter { it != this }
        }

        fun bottomBricks(brickMap: Map<Vec3i, Brick>): List<Brick> {
            return points
                .map { it - Vec3i.Z_AXIS }
                .mapNotNull { brickMap[it] }
                .distinct()
                .filter { it != this }
        }

        fun isSafeToRemove(brickMap: Map<Vec3i, Brick>): Boolean {
            return topBricks(brickMap).all { supportedBrick -> supportedBrick.bottomBricks(brickMap).size > 1}
        }

        override fun toString(): String {
            return if (id < 26) "${'A' + id}" else "$id"
        }
    }

    @Suppress("unused")
    fun drawBricks(brickMap: Map<Vec3i, Brick>) = thread {
        KoolApplication {
            it.scenes += scene {
                tryEnableInfiniteDepth()
                mainRenderPass.clearColor = MdColor.GREY tone 300
                orbitCamera {
                    maxZoom = 500.0
                    zoom = 60.0
                    camera.setClipRange(0.1f, 1000f)
                    translationBounds = BoundingBoxD(Vec3d(0.0, 0.0, -10.0), Vec3d(10.0, 500.0, 00.0))
                    setMouseTranslation(0f, 30f, 0f)
                    setMouseRotation(0f, 52f)
                }
                val ao = AoPipeline.createForward(this)

                addColorMesh {
                    generate {
                        // z up
                        rotate(90f.deg, Vec3f.NEG_X_AXIS)
                        translate(-5f, -5f, 0f)

                        brickMap.values.distinct().forEach { brick ->
                            val c = if (brick.isSafeToRemove(brickMap)) MdColor.LIGHT_GREEN.toLinear() else MdColor.RED.toLinear()
                            color = c.toOklab().shiftLightness(randomF(-0.3f, 0.3f)).toLinearRgb()

                            cube {
                                val s = brick.start.toVec3f()
                                val e = brick.end.toVec3f()
                                origin.set((s + e) * 0.5f + Vec3f(0.5f))
                                size.set(abs(e.x - s.x) + 1f, abs(e.y - s.y) + 1f, abs(e.z - s.z) + 1f)
                                size -= Vec3f(0.1f)
                            }
                        }
                        shader = KslPbrShader {
                            color { vertexColor() }
                            ao { enableSsao(ao.aoMap) }
                            ambientColor = KslLitShader.AmbientColor.Uniform(MdColor.GREY toneLin 300)
                        }
                    }

                    onUpdate {
                        transform.rotate(90f.deg * Time.deltaT, Vec3f.Y_AXIS)
                    }
                }
            }
        }
    }
}