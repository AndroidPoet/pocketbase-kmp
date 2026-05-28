pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "pocketbase-kmp"

include(":pocketbase-core")
include(":pocketbase-client")
include(":pocketbase-auth")
include(":pocketbase-records")
include(":pocketbase-files")
include(":pocketbase-realtime")
