package extension

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.FileInputStream
import java.util.Properties

// h4x so we can access version catalog from convention script
// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()

private val Project.kotlin get() = extensions.getByType<KotlinMultiplatformExtension>()
private fun Project.kotlin(block: KotlinMultiplatformExtension.() -> Unit) = kotlin.block()

val Project.localProperties: Properties
    get() = Properties().apply {
        load(FileInputStream(rootProject.file("local.properties")))
    }