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

includeBuild("build-logic")
includeBuild("publication")

include(":core")
include(":remote-config")

if (!gradle.startParameter.taskNames.any { it.contains("publish") }) {
    include(":sample:shared")
    include(":sample:androidApp")
    include(":sample:desktopApp")
    include(":sample:webApp")
}