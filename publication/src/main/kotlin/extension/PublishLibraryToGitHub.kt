package extension

import extension.publish.publishAndroidLibraryToGitHub
import extension.publish.publishIosLibraryToGitHub
import org.gradle.api.Project

fun Project.publishLibraryToGitHub(
    groupId: String,
    version: String,
    owner: String,
    repo: String,
    githubToken: String,
    httpGitUrl: String,
    branch: String,
    projectName: String,
    projectDescription: String,
    developerId: String = "",
    developerName: String = "",
    contactEmail: String = ""
) {
//    publishIosLibraryToGitHub(
//        version = version,
//        owner = owner,
//        repo = repo,
//        githubToken = githubToken,
//        branch = branch
//    )

    publishAndroidLibraryToGitHub(
        groupId = groupId,
        version = version,
        owner = owner,
        repo = repo,
        githubToken = githubToken,
        httpGitUrl = httpGitUrl,
        projectName = projectName,
        projectDescription = projectDescription,
        developerId = developerId,
        developerName = developerName,
        contactEmail = contactEmail
    )

    tasks.register("publishLibraryToGitHub") {
        group = "publishing"
        dependsOn("buildLibrary")
//        dependsOn("publishIosLibraryToGitHub")
        dependsOn("publishAndroidLibraryToGitHub")
    }
}
