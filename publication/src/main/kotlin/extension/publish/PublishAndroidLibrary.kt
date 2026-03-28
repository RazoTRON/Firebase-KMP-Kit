package extension.publish

import gradle.kotlin.dsl.accessors._fd942f86fa329c352a3fb0ff6494f851.publishing
import org.gradle.api.Project
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.withType

fun Project.publishAndroidLibrary(
    groupId: String,
    version: String,
    projectId: String,
    gitlabAccesToken: String,
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
                    name = "GitLab"
                    url = project.uri("https://gitlab.com/api/v4/projects/$projectId/packages/maven")
                    credentials(HttpHeaderCredentials::class) {
                        name = "Authorization"
                        value = "Bearer $gitlabAccesToken"
                    }
                    authentication {
                        create("header", HttpHeaderAuthentication::class)
                    }
                }
            }
        }

        publications
            .withType<MavenPublication>()
            .configureEach {
                this.groupId = groupId
                this.version = version

                pom {
                    name.set(projectName)
                    description.set(projectDescription)
                    url.set("https://gitlab.com/api/v4/projects/$projectId/packages/maven")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    val gitUrl = httpGitUrl.substringAfter("https://")

                    scm {
                        connection = "scm:git:git://$gitUrl"
                        developerConnection = "scm:git:ssh://$gitUrl"
                        url = "https://$gitUrl"
                    }

                    val url = gitUrl.substringBefore(".git")

                    issueManagement {
                        this.system = "GitLab"
                        this.url = "https://$url/-/issues"
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

    tasks.register("publishAndroidLibrary") {
        group = "publishing"
        mustRunAfter("fetchChangesFromRemote")
        dependsOn("publishAllPublicationsToGitLabRepository")
    }
}
