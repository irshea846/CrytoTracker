import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("com.android.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)

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

room {
    schemaDirectory("$projectDir/schemas")
}

sqldelight {
    databases {
        create("CryptoDatabase") {
            packageName.set("com.rshea.cryptotracker.database")
        }
    }
}

configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "com.rshea.cryptotracker.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

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

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.android.driver)
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
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutine)

            // Leave your multiplatform Room runtime libraries active
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }

        val androidUnitTest by getting {
            kotlin.srcDir("src/androidHostTest/kotlin")
            dependencies {
                implementation(libs.sqldelight.sqlite.driver)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:${libs.versions.kotlinx.coroutines.get()}")
            }
        }

        val androidInstrumentedTest by getting {
            kotlin.srcDir("src/androidDeviceTest/kotlin")
            dependencies {
                implementation(libs.sqldelight.android.driver)
                implementation(libs.androidx.testExt.junit)
            }
        }

        // FIX: Define iosMain cleanly using the configuration method block matching your targets hierarchy
        val iosMain by creating {
            // FIXED: Reference commonMain by name string to bypass the variable delegation reference issue
            dependsOn(sourceSets.getByName("commonMain"))
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.native.driver)
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
    add("ksp", libs.androidx.room.compiler)
}
