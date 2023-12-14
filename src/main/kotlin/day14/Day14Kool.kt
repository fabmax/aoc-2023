package day14

import de.fabmax.kool.KoolApplication
import de.fabmax.kool.math.*
import de.fabmax.kool.modules.ksl.KslLitShader
import de.fabmax.kool.modules.ksl.KslPbrShader
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.physics.PhysicsWorld
import de.fabmax.kool.physics.RigidDynamic
import de.fabmax.kool.physics.RigidStatic
import de.fabmax.kool.physics.Shape
import de.fabmax.kool.physics.geometry.SphereGeometry
import de.fabmax.kool.physics.geometry.TriangleMeshGeometry
import de.fabmax.kool.pipeline.Attribute
import de.fabmax.kool.pipeline.ao.AoPipeline
import de.fabmax.kool.scene.*
import de.fabmax.kool.util.*
import java.io.File
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max

fun main() = KoolApplication { ctx ->
    val puzzleInput = File("inputs/day14.txt").readLines().filter { it.isNotBlank() }

    val day14 = Day14Kool(puzzleInput)

    ctx.scenes += day14.scene
    ctx.scenes += makeUiScene(day14)
    ctx.scenes += debugOverlay()
}

class Day14Kool(input: List<String>) {
    val scene = Scene()

    val physicsWorld = PhysicsWorld(scene)
    val ao = AoPipeline.createForward(scene)
    val shadows = CascadedShadowMap(scene, scene.lighting.lights[0], maxRange = 200f)

    val staticRockMesh = makeStaticRocks(input, ao, shadows)
    val roundRockInstances = makeRoundRockInstances(input)
    val roundRockMesh = makeRoundRockMesh(roundRockInstances, ao, shadows)

    val tiltController = TiltController()
    val rocksWeight = mutableStateOf(0)

    init {
        setupPhysicsWorld()
        scene.apply {
            setupCamAndLighting()
            addNode(staticRockMesh)
            addNode(roundRockMesh)

            onUpdate {
                tiltController.update()

                val weight = roundRockInstances.sumOf {
                    (-it.pose.translation.z + input.size / 2 + 1).toInt()
                }
                rocksWeight.set(weight)
            }
        }
    }

    fun setupPhysicsWorld() {
        val staticActor = RigidStatic()
        staticActor.attachShape(Shape(TriangleMeshGeometry(staticRockMesh.geometry)))
        physicsWorld.addActor(staticActor)

        roundRockInstances.forEach {
            physicsWorld.addActor(it.actor)
        }
    }

    fun makeRoundRockInstances(input: List<String>): List<RoundRock> {
        val halfSize = input.size.toFloat() * 0.5f
        val result = mutableListOf<RoundRock>()

        for (z in input.indices) {
            for (x in input[z].indices) {
                if (input[z][x] == 'O') {

                    val colorX = x - halfSize
                    val colorY = z - halfSize
                    val h = atan2(colorY, colorX).toDeg()
                    val s = max(abs(colorX), abs(colorY)) / (halfSize * 3.5f)
                    val color = Color.Oklab.fromHueChroma(0.75f, h, s).toLinearRgb()

                    result += RoundRock(
                        MutableVec3f(x - halfSize + 0.5f, 0.5f, z - halfSize + 0.5f),
                        color
                    )
                }
            }
        }
        return result
    }

    fun makeRoundRockMesh(
        roundRocks: List<RoundRock>,
        ao: AoPipeline.ForwardAoPipeline,
        shadows: CascadedShadowMap
    ) = ColorMesh().apply {
        generate {
            icoSphere {
                steps = 1
                radius = 0.5f
            }
        }

        shader = KslPbrShader {
            color { instanceColor() }
            shadow { addShadowMap(shadows) }
            ao { enableSsao(ao.aoMap) }
            ambientColor = KslLitShader.AmbientColor.Uniform(MdColor.GREY)
            pipeline { vertices { isInstanced = true } }
        }

        val rockInstances = MeshInstanceList(listOf(Attribute.INSTANCE_MODEL_MAT, Attribute.INSTANCE_COLOR))
        instances = rockInstances
        onUpdate {
            rockInstances.clear()
            rockInstances.addInstances(roundRocks.size) { buffer ->
                roundRocks.forEach {
                    it.actor.wakeUp()
                    it.pose.matrixF.putTo(buffer)
                    it.color.putTo(buffer)
                }
            }
        }
    }

    fun makeStaticRocks(
        input: List<String>,
        ao: AoPipeline.ForwardAoPipeline,
        shadows: CascadedShadowMap
    ) = ColorMesh().apply {
        val gridSize = input.size.toFloat()
        generate {
            // static rocks
            withTransform {
                // move generator transform so that (50, 50) is in world origin
                translate(gridSize * -0.5f, 0f, gridSize * -0.5f)

                val rockGradient = ColorGradient(MdColor.BLUE_GREY toneLin 400, MdColor.BLUE_GREY toneLin 700)
                for (z in input.indices) {
                    for (x in input[z].indices) {
                        if (input[z][x] == '#') {
                            color = rockGradient.getColor(randomF())
                            cube {
                                size.set(0.9f, 0.9f, 0.9f)
                                origin.set(x + 0.5f, 0.45f, z + 0.5f)
                            }
                        }
                    }
                }
            }

            // ground plane
            color = MdColor.BLUE_GREY toneLin 300
            grid {
                sizeX = gridSize
                sizeY = gridSize
            }

            // walls
            val wallPos = gridSize * 0.5f + 0.25f
            color = MdColor.BLUE_GREY toneLin 600
            cube {
                origin.set(0f, 0.5f, wallPos)
                size.set(gridSize + 1f, 1f, 0.5f)
            }
            cube {
                origin.set(0f, 0.5f, -wallPos)
                size.set(gridSize + 1f, 1f, 0.5f)
            }
            cube {
                origin.set(wallPos, 0.5f, 0f)
                size.set(0.5f, 1f, gridSize + 1f)
            }
            cube {
                origin.set(-wallPos, 0.5f, 0f)
                size.set(0.5f, 1f, gridSize + 1f)
            }
        }

        shader = KslPbrShader {
            color { vertexColor() }
            shadow { addShadowMap(shadows) }
            ao { enableSsao(ao.aoMap) }
            ambientColor = KslLitShader.AmbientColor.Uniform(MdColor.GREY)
        }
    }

    fun Scene.setupCamAndLighting() {
        mainRenderPass.clearColor = MdColor.GREY tone 400
        lighting.singleDirectionalLight {
            setup(Vec3f(-1f, -1f, -1f))
            setColor(Color.WHITE, 5f)
        }

        camera.apply {
            clipNear = 0.1f
            clipFar = 1000f
        }

        addGroup {
            addNode(orbitCamera(mainRenderPass.screenView) {
                setMouseRotation(yaw = 30f, pitch = -60f)
                minZoom = 10.0
                maxZoom = 200.0
                zoom = 50.0
            })

            onUpdate {
                transform.setIdentity().rotate(tiltController.tiltAngle.value, tiltController.tiltAxis)
            }
        }
    }

    enum class TiltMode(val rotationAxis: Vec3f, val lockX: Boolean, val lockZ: Boolean, val label: String) {
        NO_TILT(Vec3f.X_AXIS, false, false, "No Tilt"),
        TILT_NORTH(Vec3f.X_AXIS, true, false, "North"),
        TILT_WEST(Vec3f.NEG_Z_AXIS, false, true, "West"),
        TILT_SOUTH(Vec3f.NEG_X_AXIS, true, false, "South"),
        TILT_EAST(Vec3f.Z_AXIS, false, true, "East"),
    }

    inner class TiltController {
        val tiltMode = mutableStateOf(TiltMode.NO_TILT)
        val tiltAngle = mutableStateOf(0f.deg)
        val tiltAxis: Vec3f
            get() = prevTiltedTiltMode.rotationAxis

        private val tiltAnimator = AnimatedFloatBidir(0.25f, 0.25f, 0f)
        private var prevTiltMode = TiltMode.NO_TILT
        private var prevTiltedTiltMode = TiltMode.TILT_NORTH

        fun toggle(mode: TiltMode) {
            if (tiltMode.value == mode) {
                tiltMode.set(TiltMode.NO_TILT)
            } else {
                tiltMode.set(mode)
            }
        }

        fun update() {
            if (tiltMode.value != prevTiltMode) {
                when (tiltMode.value) {
                    TiltMode.NO_TILT -> tiltAnimator.start(0f)
                    TiltMode.TILT_NORTH -> tiltAnimator.start(1f)
                    TiltMode.TILT_WEST -> tiltAnimator.start(1f)
                    TiltMode.TILT_SOUTH -> tiltAnimator.start(1f)
                    TiltMode.TILT_EAST -> tiltAnimator.start(1f)
                }
                prevTiltMode = tiltMode.value
                if (prevTiltMode != TiltMode.NO_TILT) {
                    prevTiltedTiltMode = prevTiltMode
                }

                // constrain rock movement to tilt direction
                roundRockInstances.forEach {
                    it.actor.setLinearLockFlags(prevTiltMode.lockX, false, prevTiltMode.lockZ)
                }
            }

            tiltAnimator.progress(Time.deltaT)
            tiltAngle.set(10f.deg * Easing.smooth(tiltAnimator.value))
            physicsWorld.gravity = MutableVec3f(0f, -10f, 0f).rotate(tiltAngle.value, prevTiltedTiltMode.rotationAxis)
        }
    }
}

fun makeUiScene(day14: Day14Kool) = UiScene {
    addPanelSurface {
        modifier
            .align(AlignmentX.End, AlignmentY.Top)
            .margin(100.dp)
            .backgroundColor(Color.BLACK.withAlpha(0.7f))
            .width(250.dp)

        Column {
            modifier.width(Grow.Std)

            Row {
                modifier
                    .margin(sizes.largeGap)
                    .width(Grow.Std)

                Text("Weight:") {
                    modifier
                        .font(sizes.largeText)
                        .width(Grow.Std)
                }
                Text("${day14.rocksWeight.use()}") {
                    modifier
                        .font(sizes.largeText)
                }
            }

            Box(Grow.Std, 1.dp) {
                modifier
                    .margin(horizontal = sizes.largeGap)
                    .backgroundColor(MdColor.GREY tone 400)
            }

            Day14Kool.TiltMode.entries.filter { it != Day14Kool.TiltMode.NO_TILT }.forEach { mode ->
                Row {
                    modifier
                        .margin(sizes.largeGap)
                        .width(Grow.Std)

                    Text("Tilt ${mode.label}:") {
                        modifier
                            .font(sizes.largeText)
                            .width(Grow.Std)
                            .onClick { day14.tiltController.toggle(mode) }
                    }

                    Switch(day14.tiltController.tiltMode.use() == mode) {
                        modifier
                            .onToggle { day14.tiltController.toggle(mode) }
                            .alignY(AlignmentY.Center)
                    }
                }
            }
        }
    }
}

class RoundRock(position: Vec3f, val color: Color) {
    val actor = RigidDynamic(1f, Mat4f.translation(position))
    val pose: TrsTransformF
        get() = actor.transform

    init {
        actor.attachShape(Shape(SphereGeometry(0.5f)))
    }
}
