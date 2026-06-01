package extension

import gradle.kotlin.dsl.accessors._d0c1960d1ce7aed7d123822309a9e8e4.mavenPublishing
import org.gradle.api.Project

fun Project.publishLibrary(
    name: String,
    description: String,
    artifactId: String,
    groupId: String = "io.github.razotron.firebase-kit",
    libVersion: String = version.toString()
) {
    mavenPublishing {
        publishToMavenCentral()
        signAllPublications()

        coordinates(groupId, artifactId, libVersion)

        pom {
            this.name.set(name)
            this.description.set(description)
            inceptionYear.set("2026")
            url.set("https://github.com/RazoTRON/Firebase-KMP-Kit")
            licenses {
                license {
                    this.name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("RazoTRON")
                    this.name.set("Vladyslav Mihalatiuk")
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