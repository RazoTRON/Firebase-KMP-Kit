package extension.exec

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import javax.inject.Inject

abstract class ExecHelper @Inject constructor(private val execOps: ExecOperations) {
    fun exec(block: ExecSpec.() -> Unit) {
        execOps.exec(object : Action<ExecSpec> {
            override fun execute(spec: ExecSpec) {
                spec.block()
            }
        })
    }
}

fun Project.execHelper(): ExecHelper = objects.newInstance(ExecHelper::class.java)