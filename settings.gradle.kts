rootProject.name = "CryptoTracker"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // Enforces centralized repo control
    repositories {
        google()
        mavenCentral()
    }
}

include(":androidApp")
include(":shared")