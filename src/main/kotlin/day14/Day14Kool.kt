package day14

import de.fabmax.kool.KoolApplication
import de.fabmax.kool.KoolContext
import de.fabmax.kool.KoolSystem
import de.fabmax.kool.input.CursorShape
import de.fabmax.kool.input.PointerInput
import de.fabmax.kool.math.*
import de.fabmax.kool.math.spatial.BoundingBoxD
import de.fabmax.kool.modules.ksl.KslLitShader
import de.fabmax.kool.modules.ksl.KslPbrShader
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.physics.*
import de.fabmax.kool.physics.geometry.PlaneGeometry
import de.fabmax.kool.physics.geometry.SphereGeometry
import de.fabmax.kool.physics.geometry.TriangleMeshGeometry
import de.fabmax.kool.pipeline.Attribute
import de.fabmax.kool.pipeline.ao.AoPipeline
import de.fabmax.kool.scene.*
import de.fabmax.kool.toString
import de.fabmax.kool.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.max

fun main() = KoolApplication { ctx ->
    val puzzleInput = File("inputs/day14.txt").readLines().filter { it.isNotBlank() }

    launchApp(ctx, puzzleInput)
}

fun launchApp(ctx: KoolContext, input: List<String>) {
    launchOnMainThread {
        Physics.awaitLoaded()

        val day14 = Day14Kool(input)
        ctx.scenes += day14.scene
        ctx.scenes += makeUiScene(day14)
    }
}

class Day14Kool(val input: List<String>) {
    val gridSizeHalf = input.size.toFloat() * 0.5f

    val scene = Scene()

    val timeFactor = 1f

    val physicsWorld = PhysicsWorld(scene)
    val material = Material(0f, 0f)
    val ao = AoPipeline.createForward(scene)
    val shadows = CascadedShadowMap(scene, scene.lighting.lights[0], maxRange = 200f)

    val staticRockMesh = makeStaticRocks(input, ao, shadows)
    val roundRockInstances = mutableListOf<RoundRock>()
    val roundRockMesh = makeRoundRockMesh(roundRockInstances, ao, shadows)

    val tiltController = TiltController()
    val rocksWeight = mutableStateOf(0)

    init {
        setupPhysicsWorld()
        resetRocks()
        tiltController.isSpinCycling.set(true)

        scene.apply {
            setupCamAndLighting()
            addNode(staticRockMesh)
            addNode(roundRockMesh)

            onUpdate {
                tiltController.update()
                rocksWeight.set(computeWeight())
            }
        }
    }

    fun setupPhysicsWorld() {
        physicsWorld.simStepper.simTimeFactor = timeFactor

        val staticActor = RigidStatic()
        staticActor.attachShape(Shape(TriangleMeshGeometry(staticRockMesh.geometry)))
        physicsWorld.addActor(staticActor)

        // add an invisible collision plane on top of the scene to avoid spheres jumping over each other
        val guardPlane = RigidStatic()
        guardPlane.attachShape(Shape(PlaneGeometry(), localPose = MutableMat4f().translate(0f, 1.25f, 0f).rotate(90f.deg, Vec3f.NEG_Z_AXIS)))
        physicsWorld.addActor(guardPlane)
    }

    fun resetRocks() {
        tiltController.tiltMode.set(TiltMode.NO_TILT)
        tiltController.isSpinCycling.set(false)

        roundRockInstances.forEach {
            physicsWorld.removeActor(it.actor)
            it.actor.release()
        }
        roundRockInstances.clear()

        for (z in input.indices) {
            for (x in input[z].indices) {
                if (input[z][x] == 'O') {

                    val colorX = x - gridSizeHalf
                    val colorY = z - gridSizeHalf
                    val h = atan2(colorY, colorX).toDeg()
                    val s = max(abs(colorX), abs(colorY)) / (gridSizeHalf * 3.5f)
                    val color = Color.Oklab.fromHueChroma(0.75f, h, s).toLinearRgb()

                    roundRockInstances += RoundRock(
                        Vec3f(x - gridSizeHalf + 0.5f, 0.5f, z - gridSizeHalf + 0.5f),
                        color
                    )
                }
            }
        }

        roundRockInstances.forEach {
            physicsWorld.addActor(it.actor)
        }
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
        generate {
            // static rocks
            withTransform {
                // move generator transform so that (50, 50) is in world origin
                translate(-gridSizeHalf, 0f, -gridSizeHalf)

                val rockGradient = ColorGradient(MdColor.BLUE_GREY toneLin 400, MdColor.BLUE_GREY toneLin 700)
                for (z in input.indices) {
                    for (x in input[z].indices) {
                        if (input[z][x] == '#') {
                            color = rockGradient.getColor(randomF())
                            cube {
                                val h = randomF(0.8f, 1.2f)
                                size.set(0.9f, h, 0.9f)
                                origin.set(x + 0.5f, h * 0.5f, z + 0.5f)
                            }
                        }
                    }
                }
            }

            // ground plane
            val gridSize = gridSizeHalf * 2f
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

    fun computeWeight(): Int = roundRockInstances.sumOf {
        it.updateGridPos()
        (gridSizeHalf * 2).toInt() - it.gridPos.y
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
                setMouseRotation(yaw = 10f, pitch = -60f)
                minZoom = 10.0
                maxZoom = 200.0
                zoom = 80.0

                panMethod = yPlanePan()
                translationBounds = BoundingBoxD(Vec3d(-50.0, 0.0, -50.0), Vec3d(50.0, 0.0, 50.0))

                setMouseTranslation(3.0, 0.0, 18.0)
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

        private var spinLoop: Job? = null
        val spinLoopCount = mutableStateOf(0)
        val isSpinCycling = mutableStateOf(false).onChange {
            if (!it) {
                spinLoop?.cancel()
                spinLoop = null
            } else if(spinLoop == null) {
                spinLoop = launchSpinLoop()
            }
        }
        val cycleStart = mutableStateOf<Int?>(null)
        val cyclePeriod = mutableStateOf<Int?>(null)
        val cycleResult = mutableStateOf<Int?>(null)

        private val arrangements = mutableMapOf<Long, Int>()
        private val weights = mutableListOf<Int>()

        private fun isSimSettled(): Boolean {
            // getAndResetSettledFlag() needs to be executed on each instance -> is not the case with all { }
            //return roundRockInstances.all { it.getAndResetSettledFlag() }

            return roundRockInstances.count { it.getAndResetSettledFlag() } == roundRockInstances.size
        }

        private fun sanitizeActors() {
            roundRockInstances.forEach {
                val simPos = Vec3f(it.actor.position)
                it.reset(Vec3f(floor(simPos.x) + 0.5f, 0.5f, floor(simPos.z) + 0.5f))
            }
        }

        fun launchSpinLoop() = launchOnMainThread {
            suspend fun delayUntilSettled() {
                delay((1000 / timeFactor).toLong())
                while (!isSimSettled()) {
                    delay((2000 / timeFactor).toLong())
                }
                sanitizeActors()
            }

            val cycle = listOf(
                TiltMode.TILT_NORTH,
                TiltMode.TILT_WEST,
                TiltMode.TILT_SOUTH,
                TiltMode.TILT_EAST,
            )

            spinLoopCount.set(0)
            arrangements.clear()
            weights.clear()
            cycleStart.set(null)
            cyclePeriod.set(null)
            cycleResult.set(null)
            var cycleFound = false

            while (!cycleFound) {
                cycle.forEach { tilt ->
                    tiltMode.set(tilt)
                    delayUntilSettled()
                    spinLoopCount.value++

                    tiltMode.set(TiltMode.NO_TILT)
                    delay((250 / timeFactor).toLong())
                }
                cycleFound = captureSpinCycle()
            }
        }

        private fun captureSpinCycle(): Boolean {
            var hash = 0L
            roundRockInstances.forEach {
                hash = hash * 31L + it.gridPos.x
                hash = hash * 31L + it.gridPos.y
            }

            val cycleCount = spinLoopCount.value / 4
            return if (hash !in arrangements.keys) {
                arrangements[hash] = cycleCount
                weights += computeWeight()
                false

            } else {
                cycleStart.set(arrangements[hash]!!)
                cyclePeriod.set(cycleCount - cycleStart.value!!)
                cycleResult.set((1_000_000_000 - cycleStart.value!!) % cyclePeriod.value!!)
                true
            }
        }

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
            }

            // constrain rock movement to tilt direction
            roundRockInstances.forEach {
                it.actor.setLinearLockFlags(prevTiltMode.lockX, false, prevTiltMode.lockZ)
            }

            tiltAnimator.progress(Time.deltaT * timeFactor)
            tiltAngle.set(10f.deg * Easing.smooth(tiltAnimator.value))
            physicsWorld.gravity = MutableVec3f(0f, -20f, 0f).rotate(tiltAngle.value, prevTiltedTiltMode.rotationAxis)
        }
    }

    inner class RoundRock(position: Vec3f, val color: Color) {
        val actor = RigidDynamic(1f, Mat4f.translation(position))
        val pose: TrsTransformF
            get() = actor.transform

        val gridPos = MutableVec2i()
        private var posSettledFlag = false

        init {
            actor.attachShape(Shape(SphereGeometry(0.495f), material = material))
        }

        fun reset(position: Vec3f) {
            actor.linearVelocity = Vec3f.ZERO
            actor.angularVelocity = Vec3f.ZERO
            actor.position = position
        }

        fun getAndResetSettledFlag(): Boolean {
            val f = posSettledFlag
            posSettledFlag = true
            return f
        }

        fun updateGridPos() {
            val gridX = (actor.position.x + gridSizeHalf).toInt()
            val gridY = (actor.position.z + gridSizeHalf).toInt()

            if (gridX != gridPos.x || gridY != gridPos.y) {
                posSettledFlag = false
                gridPos.set(gridX, gridY)
            }
        }
    }
}

fun makeUiScene(day14: Day14Kool) = UiScene {
    val tc = day14.tiltController
    addPanelSurface {
        modifier
            .align(AlignmentX.End, AlignmentY.Top)
            .margin(100.dp)
            .background(RoundRectBackground(Color.BLACK.withAlpha(0.7f), sizes.gap))
            .width(250.dp)

        Column {
            modifier.width(Grow.Std)

            Text("AoC 2023 - Day 14") {
                val fnt = sizes.largeText as MsdfFont
                modifier
                    .font(fnt.copy(sizes.largeText.sizePts * 1.25f, weight = MsdfFont.WEIGHT_BOLD))
                    .textColor(MdColor.LIGHT_BLUE)
                    .textAlignX(AlignmentX.Center)
                    .margin(sizes.largeGap)
                    .width(Grow.Std)
                    .onClick { KoolSystem.requireContext().openUrl("https://adventofcode.com/2023/day/14") }
                    .onHover { PointerInput.cursorShape = CursorShape.HAND }
            }

            LabeledText("Weight:", "${day14.rocksWeight.use()}", sizes.largeText)

            Button("Reset") {
                modifier
                    .width(Grow.Std)
                    .margin(sizes.largeGap)
                    .onClick { day14.resetRocks() }
            }

            Separator()

            Day14Kool.TiltMode.entries.filter { it != Day14Kool.TiltMode.NO_TILT }.forEach { mode ->
                LabeledRadioButton(
                    label = "Tilt ${mode.label}:",
                    state = tc.tiltMode.use() == mode,
                    isActive = !tc.isSpinCycling.use()
                ) {
                    day14.tiltController.toggle(mode)
                }
            }

            Separator()

            LabeledSwitch("Auto Search Cycle:", tc.isSpinCycling.use()) {
                tc.isSpinCycling.toggle()
            }
            if (tc.isSpinCycling.use()) {
                LabeledText("Iterations:", (tc.spinLoopCount.use() / 4f).toString(2))

                val cycleStart = tc.cycleStart.use()?.toString() ?: "Searching..."
                val cyclePeriod = tc.cyclePeriod.use()?.toString() ?: "Searching..."
                val cycleResult = tc.cycleResult.use()?.toString() ?: "Searching..."
                LabeledText("Cycle Start:", cycleStart)
                LabeledText("Cycle Period:", cyclePeriod)
                LabeledText("Result:", cycleResult)
            }

            Separator()

            Text("made with kool") {
                val fnt = sizes.normalText as MsdfFont
                modifier
                    .font(fnt.copy(sizes.normalText.sizePts * 0.8f))
                    .textColor(MdColor.YELLOW)
                    .textAlignX(AlignmentX.Center)
                    .margin(sizes.largeGap)
                    .width(Grow.Std)
                    .onClick { KoolSystem.requireContext().openUrl("https://github.com/fabmax/kool") }
                    .onHover { PointerInput.cursorShape = CursorShape.HAND }
            }
        }
    }
}

fun ColumnScope.LabeledText(label: String, text: String, font: Font = sizes.normalText) = Row {
    modifier
        .margin(sizes.largeGap)
        .width(Grow.Std)

    Text(label) {
        modifier
            .font(font)
            .width(Grow.Std)
    }
    Text(text) {
        modifier
            .font(font)
    }
}

fun ColumnScope.LabeledSwitch(label: String, state: Boolean, onToggle: () -> Unit) = Row {
    modifier
        .margin(sizes.largeGap)
        .width(Grow.Std)


    Text(label) {
        modifier
            .font(sizes.normalText)
            .width(Grow.Std)
            .onClick { onToggle() }
    }

    Switch(state) {
        modifier
            .onToggle { onToggle() }
            .alignY(AlignmentY.Center)
    }
}

fun ColumnScope.LabeledRadioButton(label: String, state: Boolean, isActive: Boolean, onToggle: () -> Unit) = Row {
    modifier
        .margin(sizes.largeGap)
        .width(Grow.Std)


    Text(label) {
        modifier
            .font(sizes.normalText)
            .width(Grow.Std)
            .onClick { onToggle() }
    }

    RadioButton(state) {
        modifier
            .alignY(AlignmentY.Center)

        if (isActive) {
            modifier
                .onToggle { onToggle() }
        } else {
            modifier.colors(
                borderColorOn = (MdColor.GREY tone 300).withAlpha(0.5f),
                borderColorOff = (MdColor.GREY tone 500).withAlpha(0.5f),
                backgroundColorOn = (MdColor.GREY tone 800).withAlpha(0.5f),
                backgroundColorOff = (MdColor.GREY tone 800).withAlpha(0.5f),
            )
        }
    }
}

fun ColumnScope.Separator() = Box(Grow.Std, 1.dp) {
    modifier
        .margin(horizontal = sizes.largeGap)
        .backgroundColor(MdColor.GREY tone 400)
}
