package extension

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register

fun Project.buildLibrary(libraryName: String) {
    val xcFrameworkTaskName = "assemble${libraryName}XCFramework"
    val archiveName = "$libraryName.xcframework.zip"

    tasks.register<Zip>("zipXCFramework") {
        group = "build"
        dependsOn(xcFrameworkTaskName)

        archiveFileName.set(archiveName)
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
                   name: "$libraryName",
                   platforms: [
                     .iOS(.v16),
                   ],
                   products: [
                      .library(name: "$libraryName", targets: ["Shared"])
                   ],
                   targets: [
                      .binaryTarget(
                          name: "Shared",
                          path: "$archiveName"
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
