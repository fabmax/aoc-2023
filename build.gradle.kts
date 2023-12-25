plugins {
    kotlin("jvm") version "1.9.21"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.21"
}

repositories {
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.10")
    implementation("io.ksmt:ksmt-core:0.5.6")
    implementation("io.ksmt:ksmt-z3:0.5.6")

    implementation("de.fabmax.kool:kool-core:0.14.0-SNAPSHOT")
    implementation("de.fabmax.kool:kool-physics:0.14.0-SNAPSHOT")

    listOf("natives-windows", "natives-linux", "natives-macos", "natives-macos-arm64").forEach { platform ->
        val lwjglVersion = "3.3.3"
        val physxJniVersion = "2.3.1"

        // lwjgl runtime libs
        runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$platform")
        listOf("glfw", "opengl", "jemalloc", "nfd", "stb", "vma", "shaderc").forEach { lib ->
            runtimeOnly("org.lwjgl:lwjgl-$lib:$lwjglVersion:$platform")
        }

        // physx-jni runtime libs
        runtimeOnly("de.fabmax:physx-jni:$physxJniVersion:$platform")
    }
}

kotlin {
    jvmToolchain(17)
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

benchmark {
    configurations {
        named("main") {
            warmups = 3
            iterations = 5
            iterationTime = 1000
            iterationTimeUnit = "millis"
            include("Day25")
        }
    }
    targets {
        register("main") { }
    }
}