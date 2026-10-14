import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.serialization)

    // 1. Leave SQLDelight fully commented out
    // alias(libs.plugins.sqldelight)

    // 2. Keep modern KSP and Room plugins active at the module layer
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

val envProperties = Properties().apply {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        load(envFile.inputStream())
    }
}

buildConfig {
    packageName("com.rshea.cryptotracker")
    buildConfigField("MY_API_KEY", envProperties.getProperty("MY_API_KEY") ?: "")
}

// 3. Keep schema diagnostics blocks fully commented out for now
// room {
//     schemaDirectory("$projectDir/schemas")
// }

// ksp {
//     arg("room.generateKotlin", "true")
// }

// 4. REMOVE THE ENTIRE SQLDELIGHT CONFIGURATION BLOCK COMPLETELY:
// Leaving this un-commented without the plugin active causes immediate build crashes!

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    val iosTargets = listOf(
        iosArm64(),
        iosSimulatorArm64()
    )

    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = false
        }
    }

    android {
        namespace = "com.rshea.cryptotracker.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.activity.compose)

            // REMOVE OLD DRIVER REFERENCE:
            // implementation(libs.sqldelight.android.driver)
        }
        commonMain.dependencies {
            implementation(libs.ktor.client.websockets)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // REMOVE OLD DESCRIPTORS REFERENCE:
            // implementation(libs.sqldelight.runtime)
            // implementation(libs.sqldelight.coroutine)

            // Leave your multiplatform Room runtime libraries active
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }

        val androidHostTest by getting {
            dependencies {
                // REMOVE INCOMPATIBLE TEST DRIVER:
                // implementation(libs.sqldelight.sqlite.driver)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:${libs.versions.kotlinx.coroutines.get()}")
            }
        }

        val androidDeviceTest by getting {
            dependencies {
                // REMOVE INCOMPATIBLE DEVICE DRIVER:
                // implementation(libs.sqldelight.android.driver)
                implementation(libs.androidx.testExt.junit)
            }
        }

        // FIX: Define iosMain cleanly using the configuration method block matching your targets hierarchy
        val iosMain by creating {
            // FIXED: Reference commonMain by name string to bypass the variable delegation reference issue
            dependsOn(sourceSets.getByName("commonMain"))
            dependencies {
                implementation(libs.ktor.client.darwin)

                // REMOVE INCOMPATIBLE DRIVER:
                // implementation(libs.sqldelight.native.driver)
            }
        }

        val iosTest by creating {
            dependsOn(sourceSets.getByName("commonTest"))
        }

        iosTargets.forEach { target ->
            target.compilations.getByName("main").defaultSourceSet.dependsOn(iosMain)
            target.compilations.getByName("test").defaultSourceSet.dependsOn(iosTest)
        }
    }
}

// 5. UNCOMMENT AND ACTIVATE THE DEPENDENCY CODE-GENERATION LOOPS AT THE BOTTOM:
// This handles injection via standard string tags, completely bypassing the unresolved ksp() method issue!
dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)

    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
