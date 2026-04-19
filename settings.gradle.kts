rootProject.name = "Firebase-Kit-KMP"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenLocal()
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
        mavenLocal()
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
includeBuild("gradle-plugin")

include(":core")
include(":messaging")
include(":remote-config")

if (!gradle.startParameter.taskNames.any { it.contains("publish") }) {
    include(":sample:shared")
    include(":sample:androidApp")
    include(":sample:desktopApp")
    include(":sample:webApp")
}
