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
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
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
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
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