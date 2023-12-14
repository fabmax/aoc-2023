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

val rocksWeight = mutableStateOf(0)
val isTiltActive = mutableStateOf(false)
val tiltAngle = mutableStateOf(0f.deg)

fun main() = KoolApplication { ctx ->
    val puzzleInput = File("inputs/day14.txt").readLines().filter { it.isNotBlank() }

    ctx.scenes += scene {
        setupCamAndLighting()
        val ao = AoPipeline.createForward(this)
        val shadows = CascadedShadowMap(this, lighting.lights[0], maxRange = 200f)

        val collisionMesh = makeStaticRocks(puzzleInput, ao, shadows)
        addNode(collisionMesh)

        val roundRocks = makeRoundRocks(puzzleInput)
        addRoundRockMesh(roundRocks, ao, shadows)

        val physics = PhysicsWorld(this)

        val collisionBody = RigidStatic()
        collisionBody.attachShape(Shape(TriangleMeshGeometry(collisionMesh.geometry)))
        physics.addActor(collisionBody)

        roundRocks.forEach { physics.addActor(it.body) }

        val tiltAnimator = AnimatedFloatBidir(0.5f, 0.5f, 0f)
        var wasTilted = false
        onUpdate {
            if (isTiltActive.value && !wasTilted) {
                tiltAnimator.start(1f)
            } else if (!isTiltActive.value && wasTilted) {
                tiltAnimator.start(0f)
            }
            wasTilted = isTiltActive.value

            tiltAnimator.progress(Time.deltaT)
            tiltAngle.set(10f.deg * Easing.smooth(tiltAnimator.value))
            physics.gravity = MutableVec3f(0f, -10f, 0f).rotate(tiltAngle.value, Vec3f.X_AXIS)

            val weight = roundRocks.sumOf {
                it.body.wakeUp()
                (-it.pose.translation.z + puzzleInput.size / 2 + 1).toInt()
            }
            rocksWeight.set(weight)
        }
    }

    ctx.scenes += makeUiScene()
}

fun makeUiScene() = UiScene {
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
                Text("${rocksWeight.use()}") {
                    modifier
                        .font(sizes.largeText)
                }
            }

            Row {
                modifier
                    .margin(sizes.largeGap)
                    .width(Grow.Std)

                Text("Tilt Enabled:") {
                    modifier
                        .font(sizes.largeText)
                        .width(Grow.Std)
                        .onClick { isTiltActive.toggle() }
                }

                Switch(isTiltActive.use()) {
                   modifier
                       .onToggle { isTiltActive.set(it) }
                       .alignY(AlignmentY.Center)
                }
            }
        }
    }
}

fun makeRoundRocks(input: List<String>): List<RoundRock> {
    val halfSize = input.size.toFloat() * 0.5f
    val rockGradient = ColorGradient(MdColor.AMBER.toLinear(), MdColor.ORANGE.toLinear(), MdColor.RED.toLinear())
    val result = mutableListOf<RoundRock>()

    for (z in input.indices) {
        for (x in input[z].indices) {
            if (input[z][x] == 'O') {
                result += RoundRock(
                    MutableVec3f(x - halfSize + 0.5f, 0.5f, z - halfSize + 0.5f),
                    rockGradient.getColor(randomF())
                )
            }
        }
    }
    return result
}

fun Scene.addRoundRockMesh(
    roundRocks: List<RoundRock>,
    ao: AoPipeline.ForwardAoPipeline,
    shadows: CascadedShadowMap
) = addColorMesh {
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
                it.pose.matrixF.putTo(buffer)
                it.color.putTo(buffer)
            }
        }
    }

}

class RoundRock(position: Vec3f, val color: Color) {
    val body = RigidDynamic(1f, Mat4f.translation(position))
    val pose: TrsTransformF
        get() = body.transform

    init {
        body.attachShape(Shape(SphereGeometry(0.5f)))
        // lock x-position so sphere won't move sideways
        body.setLinearLockFlags(true, false, false)
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
            translate(gridSize * -0.5f, 0f, gridSize * -0.5f)

            val gradient = ColorGradient(MdColor.BLUE_GREY toneLin 300, MdColor.BLUE_GREY toneLin 600)
            for (z in input.indices) {
                for (x in input[z].indices) {
                    if (input[z][x] == '#') {
                        color = gradient.getColor(randomF())
                        cube {
                            size.set(0.9f, 0.9f, 0.9f)
                            origin.set(x + 0.5f, 0.45f, z + 0.5f)
                        }
                    }
                }
            }
        }

        // ground plane
        color = MdColor.BLUE_GREY toneLin 200
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
            transform.setIdentity().rotate(tiltAngle.value, Vec3f.X_AXIS)
        }
    }
}
