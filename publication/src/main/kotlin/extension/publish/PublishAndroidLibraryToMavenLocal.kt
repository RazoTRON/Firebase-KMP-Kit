package extension.publish

import gradle.kotlin.dsl.accessors._fd942f86fa329c352a3fb0ff6494f851.publishing
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.withType

fun Project.publishAndroidLibraryToMavenLocal(
    groupId: String,
    version: String,
    projectName: String,
    projectDescription: String,
) {
    publishing {
        publications {
            repositories {
                mavenLocal()
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

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                }
            }
    }
}