package extension.publish

import extension.exec.execHelper
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import javax.inject.Inject
import org.gradle.api.Action

fun Project.publishIosLibrary(version: String, gitlabAccessToken: String, projectId: String, branch: String) {
    tasks.register("createGitLabRelease") {
        group = "publishing"
        description = "Creates a release on GitLab using the GitLab API."

        doLast {
            val description = "Release $version"
            val releaseUrl = "https://gitlab.com/api/v4/projects/$projectId/releases"

            execHelper().exec {
                commandLine(
                    "curl",
                    "-X",
                    "POST",
                    releaseUrl,
                    "--header",
                    "PRIVATE-TOKEN: $gitlabAccessToken",
                    "--header",
                    "Content-Type: application/json",
                    "--data",
                    """
                    {
                        "name": "$version",
                        "tag_name": "$version",
                        "description": "$description",
                        "ref": "$branch"
                    }
                """
                )
            }
        }
    }

    tasks.register("publishIosLibrary") {
        group = "publishing"
        dependsOn("createGitLabRelease")
    }
}