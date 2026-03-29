package extension

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register

fun Project.buildLibrary() {
    tasks.register<Zip>("zipXCFramework") {
        group = "build"
        dependsOn("assembleFirebaseKitRemoteConfigXCFramework")

        archiveFileName.set("FirebaseKitRemoteConfig.xcframework.zip")
        destinationDirectory.set(rootProject.projectDir)

        from(file("$projectDir/build/XCFrameworks/release"))
    }

    tasks.register("createPackageSwift") {
        group = "build"
        dependsOn("zipXCFramework")

        doLast {
            val swiftPackageFile = file("${rootProject.projectDir}/Package.swift")

            val packageSwiftContent =
                """
                // swift-tools-version:5.9
                import PackageDescription

                let package = Package(
                   name: "FirebaseKitRemoteConfig",
                   platforms: [
                     .iOS(.v16),
                   ],
                   products: [
                      .library(name: "FirebaseKitRemoteConfig", targets: ["Shared"])
                   ],
                   targets: [
                      .binaryTarget(
                          name: "Shared",
                          path: "FirebaseKitRemoteConfig.xcframework.zip"
                      )
                   ]
                )
                """.trimIndent()

            swiftPackageFile.writeText(packageSwiftContent)
            println("Package.swift file created.")
        }
    }

    tasks.register("buildIosLibrary") {
        group = "build"
        dependsOn("createPackageSwift")
        doLast {
            println("XCFramework zip and Package.swift have been successfully created.")
        }
    }

    tasks.register("buildLibrary") {
        group = "build"
        dependsOn("build")
        dependsOn("buildIosLibrary")
    }
}
