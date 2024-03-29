package com.nova.plugin.main

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.nova.transform.asm.abstarct.ClassTransformer
import com.nova.transform.gradle.compat.AGP
import com.nova.transform.spi.Transformer
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.plugins.PluginContainer
import java.io.Serializable


val SCOPE_PROJECT: MutableSet<in QualifiedContent.Scope> = TransformManager.PROJECT_ONLY

val SCOPE_FULL_PROJECT: MutableSet<in QualifiedContent.Scope> = TransformManager.SCOPE_FULL_PROJECT

val SCOPE_FULL_WITH_FEATURES: MutableSet<in QualifiedContent.Scope> = AGP.run { scopeFullWithFeatures }

class CoreTransform(val parameter: TransformParameter) : Transform() {

    internal val verifyEnabled by lazy {
        false
    }
    override fun isIncremental() = !verifyEnabled

    override fun isCacheable() = !verifyEnabled

    override fun getName(): String  = parameter.name

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = when {
        parameter.transformers.isEmpty() -> mutableSetOf()
        parameter.plugins.hasPlugin("com.android.library") -> SCOPE_PROJECT
        parameter.plugins.hasPlugin("com.android.application") -> SCOPE_FULL_PROJECT
        parameter.plugins.hasPlugin("com.android.dynamic-feature") -> SCOPE_FULL_WITH_FEATURES
        else -> TODO("Not an Android project")
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> = when {
        parameter.transformers.isEmpty() -> when {
            parameter.plugins.hasPlugin("com.android.library") -> SCOPE_PROJECT
            parameter.plugins.hasPlugin("com.android.application") -> SCOPE_FULL_PROJECT
            parameter.plugins.hasPlugin("com.android.dynamic-feature") -> SCOPE_FULL_WITH_FEATURES
            else -> TODO("Not an Android project")
        }
        else -> super.getReferencedScopes()
    }


    override fun transform(transformInvocation: TransformInvocation) {
        // super.transform(transformInvocation) todo: no super
        CoreTransformInvocation(transformInvocation,this).apply {
            if (isIncremental) {
                doIncrementalTransform()
            } else {
                outputProvider?.deleteAll()
                doFullTransform()
            }
        }
    }
}

data class TransformParameter(
    val name: String,
    val buildscript: ScriptHandler,
    val plugins: PluginContainer,
    val properties: Map<String, Any?>,
    val transformers: Set<Class<Transformer>>
) : Serializable


fun Project.newTransformParameter(name: String,transformers: Set<Class<Transformer>>) = TransformParameter(name, buildscript, plugins, properties, transformers)