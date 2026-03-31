rootProject.name = "Firebase-Kit-KMP"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        mavenCentral()
    }
}

val isJitPack = System.getenv("JITPACK")?.toBoolean() ?: false

includeBuild("build-logic")
includeBuild("publication")

include(":core")
include(":remote-config")

if (isJitPack.not()) {
    include(":sample:shared")
    include(":sample:androidApp")
    include(":sample:desktopApp")
    include(":sample:webApp")
}