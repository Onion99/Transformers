package com.nova.transform.gradle.compat

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION
import com.android.repository.Revision
import org.gradle.api.Project
import java.util.ServiceLoader

interface AGPInterface {
    val revision: Revision
        get() = REVISION
    val scopeFullWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = TransformManager.SCOPE_FULL_PROJECT
}

inline fun <reified T : BaseExtension> Project.getAndroid(): T = extensions.getByName("android") as T

private val REVISION: Revision by lazy { Revision.parseRevision(ANDROID_GRADLE_PLUGIN_VERSION) }

private val FACTORIES: List<AGPInterfaceFactory> by lazy {
    ServiceLoader.load(AGPInterfaceFactory::class.java, AGPInterface::class.java.classLoader)
        .sortedByDescending(AGPInterfaceFactory::revision)
        .toList()
}

val AGP: AGPInterface by lazy {
    val factory = FACTORIES.firstOrNull {
        it.revision.major == REVISION.major && it.revision.minor == REVISION.minor
    } ?: FACTORIES.firstOrNull {
        it.revision.major == REVISION.major
    } ?: FACTORIES.first()
    factory.newAGPInterface()
}
