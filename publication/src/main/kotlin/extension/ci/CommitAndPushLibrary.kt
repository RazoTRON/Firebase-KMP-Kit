package extension.ci

import extension.exec.execHelper
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

fun Project.commitAndPushLibrary(version: String) {
    val taskName = "commitAndPushLibrary"

    if (tasks.findByName(taskName) == null) {
        tasks.register(taskName) {
            mustRunAfter("buildIosLibrary")
            group = "publishing"

            doLast {
                val statusOutput = ByteArrayOutputStream()
                execHelper().exec {
                    commandLine("git", "status", "--porcelain", "--untracked-files=no")
                    standardOutput = statusOutput
                }
                val gitStatus = statusOutput.toString().trim()

                if (gitStatus.isNotBlank()) {
                    val commitMessage = "Publish library v$version"

                    try {
                        execHelper().exec {
                            commandLine("git", "commit", "-a", "-m", commitMessage)
                        }
                    } catch (e: Exception) {
                        throw GradleException("Commit failed, stopping the build. (${e.message})")
                    }
                }

                try {
                    execHelper().exec {
                        println("Git push changes")
                        commandLine("git", "push")
                    }
                } catch (e: Exception) {
                    throw GradleException("Git push failed, stopping the build. (${e.message})")
                }
            }
        }
    }
}
