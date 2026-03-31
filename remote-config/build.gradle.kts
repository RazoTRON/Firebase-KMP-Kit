import extension.buildLibrary
import extension.defaultTargets
import extension.publish.githubPublishConfiguration
import extension.publish.publishAndroidLibraryToMavenLocal
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import task.generateDefFiles

plugins {
    id("build-config")
    id("publication")
    alias(libs.plugins.kotlinx.serialization)
}

group = "com.firebasekit"
version = "0.0.8"

kotlin {
    val xcf = XCFramework("FirebaseKitRemoteConfig")

    defaultTargets(
        iOSConfig = {
            it.binaries.framework {
                baseName = "FirebaseKitRemoteConfig"
                binaryOption("bundleId", "FirebaseKitRemoteConfig")
                xcf.add(this)
                isStatic = true
            }

            it.compilations["main"].cinterops {
                create("RemoteConfig") {
                    defFile(project.layout.projectDirectory.file("src/interop/RemoteConfig.def"))
                }
            }
        }
    )

    sourceSets {
        commonMain.dependencies {
            api(projects.core)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.firebase.remoteConfigs)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.java)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        webMain.dependencies {
            implementation(devNpm("firebase", libs.versions.firebase.webNpm.remoteConfigs.get()))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(libs.ktor.client.mock)
        }

        androidUnitTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

generateDefFiles("RemoteConfig")

buildLibrary()

githubPublishConfiguration(
    httpGitUrl = "https://github.com/RazoTRON/Firebase-KMP-Kit",
    contactEmail = "vmihalatiuk@gmail.com",
    owner = "RazoTRON",
    repo = "Firebase-KMP-Kit",
    groupId = project.group.toString(),
    version = project.version.toString(),
    projectName = project.name,
    projectDescription = project.description.toString(),
    developerId = "RazoTRON",
    developerName = "Vladislav Mihalatiuk"
)

publishAndroidLibraryToMavenLocal(
    groupId = project.group.toString(),
    version = project.version.toString(),
    projectName = project.name,
    projectDescription = project.name
)