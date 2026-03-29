package extension.publish

import gradle.kotlin.dsl.accessors._fd942f86fa329c352a3fb0ff6494f851.publishing
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType

fun Project.githubPublishConfiguration(
    groupId: String,
    version: String,
    projectName: String,
    projectDescription: String,
    owner: String,
    repo: String,
    httpGitUrl: String,
    developerId: String,
    developerName: String,
    contactEmail: String
) {
    publishing {
        publications
            .withType<MavenPublication>()
            .configureEach {
                this.groupId = groupId
                this.version = version

                pom {
                    name.set(projectName)
                    description.set(projectDescription)
                    url.set("https://github.com/$owner/$repo")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    val gitUrl = httpGitUrl.substringAfter("https://")

                    scm {
                        connection.assign("scm:git:git://$gitUrl")
                        developerConnection = "scm:git:ssh://$gitUrl"
                        url = "https://$gitUrl"
                    }

                    val repoUrl = gitUrl.substringBefore(".git")

                    issueManagement {
                        this.system = "GitHub"
                        this.url = "https://$repoUrl/issues"
                    }

                    developers {
                        developer {
                            id = developerId
                            name = developerName
                            email = contactEmail
                        }
                    }
                }
            }
    }
}