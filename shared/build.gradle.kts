import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight) // ◄── Triggers the code-generation engine
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

// Configure the database schema package mapping
sqldelight {
    databases {
        create("CryptoDatabase") {
            packageName.set("com.rshea.cryptotracker.database")
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    // 1. Define iOS Targets once (Apple Silicon only to avoid Intel dependency issues)
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
            implementation(libs.ktor.client.okhttp) // Native Android engine
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.androidx.lifecycle.viewmodel.compose) // ◄── Add this line here
            implementation(libs.androidx.activity.compose) // Ensures ComponentActivity dependencies link up
            implementation(libs.sqldelight.android.driver) // Native Android SQLite handle
        }
        commonMain.dependencies {
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
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutine) // For streaming DB entries as Flow
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }

        // ADD THIS LAYER: Ensures the local JVM test engines can index the JDBC driver files natively!
        // FIXED: Inject the JVM driver explicitly into your template's host-test group!
        val androidHostTest by getting {
            dependencies {
                implementation(libs.sqldelight.sqlite.driver)
            }
        }

        val androidDeviceTest by getting {
            dependencies {
                implementation(libs.sqldelight.android.driver)
                implementation(libs.androidx.testExt.junit)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.native.driver) // Native Apple CoreData/SQLite handle
            }
        }

        // 2. FIXED: Universal iOS Test Mapping
        // Creates a common iosTest folder that automatically distributes the actual code to all Apple chips
        val iosTest by creating {
            dependsOn(commonTest.get())
        }

        iosTargets.forEach { target ->
            target.compilations.getByName("main").defaultSourceSet.dependsOn(iosMain)
            target.compilations.getByName("test").defaultSourceSet.dependsOn(iosTest)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
