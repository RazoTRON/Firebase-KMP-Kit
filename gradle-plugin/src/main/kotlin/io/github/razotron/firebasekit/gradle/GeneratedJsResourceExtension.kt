package io.github.razotron.firebasekit.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class GeneratedJsResourceExtension @Inject constructor(
    objects: ObjectFactory,
) {
    val fileName: Property<String> = objects.property(String::class.java)
    val content: Property<String> = objects.property(String::class.java)
}