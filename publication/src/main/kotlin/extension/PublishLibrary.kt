package extension

import gradle.kotlin.dsl.accessors._d0c1960d1ce7aed7d123822309a9e8e4.mavenPublishing
import org.gradle.api.Project

fun Project.publishLibrary(
    artifactId: String,
    groupId: String = "io.github.razotron.Firebase-KMP-Kit",
    libVersion: String = version.toString()
) {
    mavenPublishing {
        publishToMavenCentral()

        signAllPublications()
    }

    mavenPublishing {
        coordinates(groupId, artifactId, libVersion)

        pom {
            name.set("Firebase KMP Kit")
            description.set("A Kotlin Multiplatform library that provides Firebase Services in common code.")
            inceptionYear.set("2026")
            url.set("https://github.com/RazoTRON/Firebase-KMP-Kit")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("RazoTRON")
                    name.set("Vladyslav Mihalatiuk")
                    url.set("https://github.com/RazoTRON/")
                }
            }
            scm {
                url.set("https://github.com/RazoTRON/Firebase-KMP-Kit")
                connection.set("scm:git:git://github.com/RazoTRON/Firebase-KMP-Kit.git")
                developerConnection.set("scm:git:ssh://git@github.com/RazoTRON/Firebase-KMP-Kit.git")
            }
        }
    }
}