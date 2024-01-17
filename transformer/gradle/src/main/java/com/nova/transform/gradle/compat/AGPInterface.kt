package com.nova.transform.gradle.compat

import com.android.build.api.transform.Context
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.model.ApiVersion
import com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION
import com.android.repository.Revision
import com.android.sdklib.BuildToolInfo
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.ServiceLoader

interface AGPInterface {
    val revision: Revision
        get() = REVISION
    val scopeFullWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = TransformManager.SCOPE_FULL_PROJECT

    val scopeFullLibraryWithFeatures: MutableSet<in QualifiedContent.Scope>
        get() = TransformManager.PROJECT_ONLY


    val BaseVariant.project: Project

    val BaseVariant.processJavaResourcesTask: Task
        get() = processJavaResourcesTaskProvider.get()

    fun BaseVariant.getTaskName(prefix: String): String

    fun BaseVariant.getTaskName(prefix: String, suffix: String): String

    val BaseVariant.variantData: BaseVariantData

    val BaseVariant.originalApplicationId: String

    val BaseVariant.hasDynamicFeature: Boolean

    val BaseVariant.localAndroidResources: FileCollection

    val BaseVariant.javaCompilerTaskProvider: TaskProvider<out Task>

    val BaseVariant.preBuildTaskProvider: TaskProvider<out Task>

    val BaseVariant.assembleTaskProvider: TaskProvider<out Task>

    val BaseVariant.mergeAssetsTaskProvider: TaskProvider<out Task>

    val BaseVariant.mergeResourcesTaskProvider: TaskProvider<out Task>

    val BaseVariant.mergeNativeLibsTaskProvider: TaskProvider<out Task>

    val BaseVariant.processJavaResourcesTaskProvider: TaskProvider<out Task>

    val BaseVariant.allArtifacts: Map<String, FileCollection>

    val BaseVariant.minSdkVersion: ApiVersion

    val BaseVariant.targetSdkVersion: ApiVersion

    val BaseVariant.isApplication: Boolean

    val BaseVariant.isLibrary: Boolean

    val BaseVariant.isDynamicFeature: Boolean

    val BaseVariant.aar: FileCollection

    val BaseVariant.apk: FileCollection

    val BaseVariant.mergedManifests: FileCollection

    val BaseVariant.mergedRes: FileCollection

    val BaseVariant.mergedAssets: FileCollection

    val BaseVariant.mergedNativeLibs: FileCollection

    val BaseVariant.processedRes: FileCollection

    val BaseVariant.symbolList: FileCollection

    val BaseVariant.symbolListWithPackageName: FileCollection

    val BaseVariant.dataBindingDependencyArtifacts: FileCollection

    val BaseVariant.allClasses: FileCollection

    val BaseVariant.buildTools: BuildToolInfo

    val BaseVariant.isPrecompileDependenciesResourcesEnabled: Boolean

    val Context.task: TransformTask

    val Project.aapt2Enabled: Boolean

    fun BaseVariant.getArtifactCollection(
        configType: AndroidArtifacts.ConsumedConfigType,
        scope: AndroidArtifacts.ArtifactScope,
        artifactType: AndroidArtifacts.ArtifactType
    ): ArtifactCollection

    fun BaseVariant.getArtifactFileCollection(
        configType: AndroidArtifacts.ConsumedConfigType,
        scope: AndroidArtifacts.ArtifactScope,
        artifactType: AndroidArtifacts.ArtifactType
    ): FileCollection
    val TransformInvocation.variant: BaseVariant
        get() = project.getAndroid<BaseExtension>().let { android ->
            this.context.variantName.let { variant ->
                when (android) {
                    is AppExtension -> when {
                        variant.endsWith("AndroidTest") -> android.testVariants.single { it.name == variant }
                        variant.endsWith("UnitTest") -> android.unitTestVariants.single { it.name == variant }
                        else -> android.applicationVariants.single { it.name == variant }
                    }
                    is LibraryExtension -> android.libraryVariants.single { it.name == variant }
                    else -> TODO("variant not found")
                }
            }
        }

    val TransformInvocation.project: Project
        get() = context.task.project

    val TransformInvocation.bootClasspath: Collection<File>
        get() = project.getAndroid<BaseExtension>().bootClasspath

    val TransformInvocation.isDataBindingEnabled: Boolean
        get() = project.getAndroid<BaseExtension>().dataBinding.isEnabled

    fun BaseVariant.getDependencies(
        transitive: Boolean = true,
        filter: (ComponentIdentifier) -> Boolean = { true }
    ): Collection<ResolvedArtifactResult>
}

inline fun <reified T : BaseExtension> Project.getAndroid(): T = extensions.getByName("android") as T
inline fun <reified T : BaseExtension> Project.getAndroidOrNull(): T? = try {
    extensions.getByName("android") as? T
} catch (e: UnknownDomainObjectException) {
    null
}

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
