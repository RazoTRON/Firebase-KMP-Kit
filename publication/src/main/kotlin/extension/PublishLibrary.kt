package extension

import extension.publish.fetchChangesFromRemote
import extension.publish.publishAndroidLibrary
import extension.publish.publishIosLibrary
import org.gradle.api.Project

fun Project.publishLibrary(
    groupId: String,
    version: String,
    projectId: String,
    gitlabAccesToken: String,
    httpGitUrl: String,
    branch: String,
    projectName: String,
    projectDescription: String,
    developerId: String = "",
    developerName: String = "",
    contactEmail: String = ""
) {
    fetchChangesFromRemote(branch = branch)

    publishIosLibrary(
        version = version,
        projectId = projectId,
        gitlabAccessToken = gitlabAccesToken,
        branch = branch
    )

    publishAndroidLibrary(
        groupId = groupId,
        version = version,
        projectId = projectId,
        gitlabAccesToken = gitlabAccesToken,
        httpGitUrl = httpGitUrl,
        projectName = projectName,
        projectDescription = projectDescription,
        developerId = developerId,
        developerName = developerName,
        contactEmail = contactEmail
    )

    tasks.register("publishLibrary") {
        group = "publishing"
        dependsOn("fetchChangesFromRemote")
        dependsOn("buildLibrary")
        dependsOn("publishIosLibrary")
        dependsOn("publishAndroidLibrary")
    }
}
