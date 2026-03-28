package extension.publish

import extension.exec.execHelper
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import javax.inject.Inject
import org.gradle.api.Action

fun Project.fetchChangesFromRemote(branch: String) {
    val taskName = "fetchChangesFromRemote"

    if (tasks.findByName(taskName) == null) {
        tasks.register(taskName) {
            group = "publishing"

            doLast {
                checkIsTargetBranch(targetBranch = branch)
                checkUncommittedChanges()
                checkUnpushedChanges(branch = branch)
                fetchChanges(targetBranch = branch)
                margeChangesFromRemote(branch = branch)
            }
        }
    }
}

private fun Project.checkUncommittedChanges() {
    val statusOutput = ByteArrayOutputStream()
    execHelper().exec {
        commandLine("git", "status", "--porcelain", "--untracked-files=no")
        standardOutput = statusOutput
    }
    val gitStatus = statusOutput.toString().trim()

    if (gitStatus.isNotBlank()) {
        throw GradleException("You have uncommitted changes. Rollback any changes to publish the library.")
    }
}

private fun Project.checkIsTargetBranch(targetBranch: String) {
    val currentBranchOutput = ByteArrayOutputStream()
    execHelper().exec {
        commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        standardOutput = currentBranchOutput
    }

    if (targetBranch != currentBranchOutput.toString().trim()) {
        throw GradleException("You must switch to the '$targetBranch' branch to publish the library.")
    }
}

private fun Project.fetchChanges(targetBranch: String) {
    try {
        execHelper().exec {
            commandLine("git", "fetch")
        }
    } catch (e: Exception) {
        throw GradleException("Failed to fetch changes from the remote for '$targetBranch' branch: ${e.message}")
    }
}

private fun Project.margeChangesFromRemote(branch: String) {
    try {
        execHelper().exec {
            commandLine("git", "merge", "origin/$branch")
        }
    } catch (e: Exception) {
        throw GradleException("Failed to marge changes from remote: ${e.message}")
    }
}

private fun Project.checkUnpushedChanges(branch: String) {
    val unpushedCommitsOutput = ByteArrayOutputStream()
    execHelper().exec {
        commandLine("git", "log", "@{u}..HEAD", "--oneline")
        standardOutput = unpushedCommitsOutput
    }
    val unpushedCommits = unpushedCommitsOutput.toString().trim()

    if (unpushedCommits.isNotBlank()) {
        throw GradleException("The current branch '$branch' contains unpushed commits:\n$unpushedCommits\nPush or drop this commits to publish the library.")
    }
}