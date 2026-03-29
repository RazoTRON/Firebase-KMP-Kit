package extension.publish

import gradle.kotlin.dsl.accessors._fd942f86fa329c352a3fb0ff6494f851.publishing
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.kotlin.dsl.credentials

fun Project.publishAndroidLibraryToGitHub(
    groupId: String,
    version: String,
    owner: String,
    repo: String,
    githubToken: String,
    httpGitUrl: String,
    projectName: String,
    projectDescription: String,
    developerId: String = "",
    developerName: String = "",
    contactEmail: String = ""
) {
    publishing {
        publications {
            repositories {
                maven {
                    name = "GitHub"
                    url = project.uri("https://maven.pkg.github.com/$owner/$repo")
                    credentials(PasswordCredentials::class) {
                        username = owner
                        password = githubToken
                    }
                }
            }
        }
    }

    githubPublishConfiguration(
        groupId = groupId,
        version = version,
        projectName = projectName,
        projectDescription = projectDescription,
        owner = owner,
        repo = repo,
        httpGitUrl = httpGitUrl,
        developerId = developerId,
        developerName = developerName,
        contactEmail = contactEmail
    )

    tasks.register("publishAndroidLibraryToGitHub") {
        group = "publishing"
        dependsOn("publishAllPublicationsToGitHubRepository")
    }
}