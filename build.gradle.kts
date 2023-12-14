plugins {
    kotlin("jvm") version "1.9.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("de.fabmax.kool:kool-core:0.13.0")
    implementation("de.fabmax.kool:kool-physics:0.13.0")

    listOf("natives-windows", "natives-linux", "natives-macos").forEach { platform ->
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