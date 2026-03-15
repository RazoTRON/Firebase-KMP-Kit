package extension.ci

import org.gradle.api.Project

fun Project.buildAndPublishLibrary(version: String) {
    commitAndPushLibrary(version)

    tasks.register("buildAndPublishLibrary") {
        group = "publishing"
        dependsOn("buildLibrary")
        dependsOn("commitAndPushLibrary")
        dependsOn("publishIosLibrary")
        dependsOn("publishAndroidLibrary")
    }
}
