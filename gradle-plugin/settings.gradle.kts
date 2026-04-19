pluginManagement {
    includeBuild("../publication")

    repositories {
        mavenLocal()
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "gradle-plugin"
