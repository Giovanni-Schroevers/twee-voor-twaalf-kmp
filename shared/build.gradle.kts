import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlinSerialization)
}

// Deployed backend URL, baked into commonMain at build time as BACKEND_URL.
// Empty when `backend.url` is unset (the default) — in that case appBaseUrl()
// falls back to a per-platform localhost default for local development. Set the
// property (gradle.properties / ~/.gradle/gradle.properties / -Pbackend.url=...)
// to point all platforms at a real backend. Env vars aren't used: they don't
// work on iOS / installed apps.
val backendUrl = (findProperty("backend.url") as String?).orEmpty()

val generateBackendConfig by tasks.registering {
    val outDir = layout.buildDirectory.dir("generated/backendConfig/kotlin")
    val url = backendUrl
    inputs.property("backendUrl", url)
    outputs.dir(outDir)
    doLast {
        outDir.get()
            .file("com/fsa_profgroep_4/twee_voor_twaalf_kmp/network/BackendConfig.kt")
            .asFile
            .apply {
                parentFile.mkdirs()
                writeText(
                    """
                    package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

                    /** Generated from the `backend.url` Gradle property — do not edit. */
                    internal const val BACKEND_URL: String = "$url"
                    """.trimIndent() + "\n",
                )
            }
    }
}

kotlin {
    // Room generates expect/actual classes (the database constructor); this opts
    // into that still-Beta feature so its warning doesn't clutter the build.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    androidLibrary {
       namespace = "com.fsa_profgroep_4.twee_voor_twaalf_kmp.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        // Pull the generated BackendConfig.kt (BACKEND_URL) into commonMain.
        commonMain {
            kotlin.srcDir(generateBackendConfig)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.jetbrains.lifecycle.viewmodelNavigation3)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            // koin-test's module verification (verify()) is JVM-only — it
            // statically checks that every dependency in the graph can be
            // resolved, catching missing bindings at build time.
            implementation(libs.koin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
