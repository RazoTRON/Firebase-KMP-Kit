package extension.publish

import extension.exec.execHelper
import org.gradle.api.Project

fun Project.publishIosLibraryToGitHub(version: String, githubToken: String, owner: String, repo: String, branch: String) {
    tasks.register("createGitHubRelease") {
        group = "publishing"
        description = "Creates a release on GitHub using the GitHub API."

        doLast {
            val description = "Release $version"
            val releaseUrl = "https://api.github.com/repos/$owner/$repo/releases"

            execHelper().exec {
                commandLine(
                    "curl",
                    "-X",
                    "POST",
                    releaseUrl,
                    "--header",
                    "Authorization: Bearer $githubToken",
                    "--header",
                    "Accept: application/vnd.github+json",
                    "--header",
                    "Content-Type: application/json",
                    "--data",
                    """
                    {
                        "tag_name": "$version",
                        "target_commitish": "$branch",
                        "name": "$version",
                        "body": "$description",
                        "draft": false,
                        "prerelease": false
                    }
                """
                )
            }
        }
    }

    tasks.register("publishIosLibraryToGitHub") {
        group = "publishing"
        dependsOn("createGitHubRelease")
    }
}
