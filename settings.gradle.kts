rootProject.name = "CryptoTracker"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // FIX: Remove the hardcoded version assignment to prevent catalog mismatches!
    plugins {
        id("com.google.devtools.ksp")
    }

}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":androidApp")
include(":shared")