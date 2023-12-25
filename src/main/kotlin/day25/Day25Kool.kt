package day25

import day14.LabeledText
import de.fabmax.kool.KoolApplication
import de.fabmax.kool.math.*
import de.fabmax.kool.modules.ksl.KslBlinnPhongShader
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.pipeline.Attribute
import de.fabmax.kool.scene.*
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.MdColor
import de.fabmax.kool.util.MsdfFont
import de.fabmax.kool.util.Time
import java.io.File
import kotlin.math.pow

fun main() = Day25Kool.solve1(File("inputs/day25.txt").readLines().filter { it.isNotBlank() })

object Day25Kool {
    val redCount = mutableStateOf(0)
    val blueCount = mutableStateOf(0)

    fun solve1(input: List<String>) {
        val nodes = input.map {
            val (node, cons) = it.split(": ")
            Node(node, cons.split(" "))
        }.associateBy { it.name }.toMutableMap()

        nodes.values.flatMap { it.connectedNames }.filter { it !in nodes }.forEach { nodes[it] = Node(it, emptyList()) }
        nodes.values.forEach { it.connect(it.connectedNames.map { id -> nodes[id]!! }) }

        KoolApplication { ctx ->
            ctx.scenes += renderGraph(nodes)
            ctx.scenes += makeUiScene()
        }
    }

    fun renderGraph(graph: Map<String, Node>) = scene {
        val nodeList = graph.values.toList()
        val splitPlane = Plane()

        camera.setClipRange(0.1f, 1000f)
        addGroup {
            addNode(orbitCamera(mainRenderPass.screenView) {
                maxZoom = 500.0
                zoom = 150.0
            })
            onUpdate {
                transform.rotate(15f.deg * Time.deltaT, Vec3f.Y_AXIS)
            }
        }

        addColorMesh("nodes") {
            val insts = MeshInstanceList(listOf(Attribute.INSTANCE_MODEL_MAT, Attribute.INSTANCE_COLOR))
            generate {
                cube { }
            }
            shader = KslBlinnPhongShader {
                color { instanceColor() }
                pipeline { vertices { isInstanced = true } }
                instances = insts
            }
            onUpdate {
                insts.clear()
                insts.addInstances(graph.size) { buf ->
                    val m = MutableMat4f()
                    graph.values.forEach {
                        m.setIdentity().translate(it).putTo(buf)
                        it.color.putTo(buf)
                    }
                }
            }
        }

        addTriangulatedLineMesh {
            onUpdate {
                clear()

                val samplePlane = (1..50)
                    .map { Plane(Vec3f.ZERO, nodeList.random().norm(MutableVec3f())) }
                    .maxBy { nodeList.evalPlaneDist(it) }

                if (samplePlane.n dot splitPlane.n < 0f) {
                    samplePlane.n.mul(-1f)
                }
                splitPlane.n.mul(0.9f).add(samplePlane.n.mul(0.1f)).norm()

                var red = 0
                var blue = 0
                graph.values.forEach { nd ->
                    nd.updatePosition(nodeList)

                    if (splitPlane.distance(nd) > 0f) {
                        red++
                        nd.color.set(MdColor.PINK.toLinear())
                    } else {
                        blue++
                        nd.color.set(MdColor.BLUE.toLinear())
                    }

                    nd.connections.filter { it.name > nd.name }.forEach { con ->
                        addLine(
                            nd,
                            nd.color.withAlpha(0.5f),
                            3f,
                            con,
                            con.color.withAlpha(0.5f),
                            3f
                        )
                    }
                }
                redCount.set(red)
                blueCount.set(blue)
            }
        }
    }

    fun List<Node>.evalPlaneDist(plane: Plane, n: Int = 50): Double {
        return (1..n).sumOf {
            plane.distance(random()).toDouble()
        }
    }

    class Node(val name: String, val connectedNames: List<String>) : MutableVec3f() {
        val color = MdColor.PINK.toLinear().toOklab().shiftHue(randomF(0f, 360f)).toLinearRgb()
        val connections = mutableSetOf<Node>()

        init {
            randomInUnitSphere(this).mul(50f)
        }

        fun connect(others: Collection<Node>) {
            connections += others
            others.forEach { it.connections += this }
        }

        fun updatePosition(nodeList: List<Node>) {
            // keep graph close to origin
            this -= this * Time.deltaT * 0.1f

            // attract connected nodes
            connections.forEach { con ->
                val d = MutableVec3f(con - this)
                val dist = d.length()
                d.mul(1f / dist)

                val attraction = dist / 5f
                val repulsion = dist.pow(-2) * 100
                this += d * Time.deltaT * (attraction - repulsion)
            }

            // repel random other nodes
            for (i in 1..50) {
                val nd = nodeList.random()
                if (nd != this) {
                    val d = MutableVec3f(this - nd)
                    val dist = d.length()
                    d.mul(1f / dist)

                    val randomRepulsion = 20f / dist
                    this += d * Time.deltaT * randomRepulsion
                }
            }
        }
    }

    fun makeUiScene() = UiScene {
        addPanelSurface {
            modifier
                .align(AlignmentX.End, AlignmentY.Top)
                .margin(100.dp)
                .background(RoundRectBackground(Color.BLACK.withAlpha(0.7f), sizes.gap))
                .width(250.dp)

            Column {
                modifier.width(Grow.Std)

                Text("AoC 2023 - Day 25") {
                    val fnt = sizes.largeText as MsdfFont
                    modifier
                        .font(fnt.copy(sizes.largeText.sizePts * 1.25f, weight = MsdfFont.WEIGHT_BOLD))
                        .textAlignX(AlignmentX.Center)
                        .margin(sizes.largeGap)
                        .width(Grow.Std)
                }

                LabeledText("Red Nodes:", "${redCount.use()}", sizes.largeText, MdColor.PINK)
                LabeledText("Blue Nodes:", "${blueCount.use()}", sizes.largeText, MdColor.BLUE)
                LabeledText("Solution:", "${redCount.use() * blueCount.use()}", sizes.largeText)
            }
        }
    }
}