package com.nova.plugin.main

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import com.nova.plugin.main.service.loadVariantProcessors
import com.nova.plugin.main.service.lookupTransformers
import com.nova.resource.clazz.ConfuseClassVisitorFactory
import com.nova.resource.common.AppCommonResourceDTask
import com.nova.resource.so.SoResourceTask
import com.nova.transform.gradle.GTE_V3_6
import com.nova.transform.gradle.compat.getAndroid
import com.nova.transform.kotlinx.call
import com.nova.transform.kotlinx.get
import com.nova.transform.spi.VariantProcessor
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionAdapter


class CorePlugin :Plugin<Project> {
    override fun apply(project: Project) {
        // ---- check ------
        project.extensions.findByName("android") ?: throw GradleException("$project is not an Android project")
        // ---- before ------
        if (!GTE_V3_6) {
            project.gradle.addListener(TransformTaskExecutionListener(project))
        }
        // ---- init processors  ------
        val processors = loadVariantProcessors(project)
        if (project.state.executed) {
            project.setup(processors)
        } else {
            project.afterEvaluate {
                project.setup(processors)
            }
        }
        // ---- transform plugin list ------
        project.getAndroid<BaseExtension>().registerTransform(CoreTransform(
            project.newTransformParameter("Nova transformer", lookupTransformers(project.buildscript.classLoader))
        ))
        // resource plugin
        project.tasks.whenTaskAdded(SoResourceTask())
        project.tasks.whenTaskAdded(AppCommonResourceDTask())
        // class plugin
        project.extensions.getByType(AndroidComponentsExtension::class.java).onVariants {
            // 可设置不同的栈帧计算模式
            it.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_CLASSES)
            // 将 ALL 改为 PROJECT，只对项目本身的类进行插桩
            // InstrumentationScope.ALL 会对所有类（包括依赖库中的类）进行插桩
            // 当尝试对不存在的类或不应该被修改的类进行插桩时，可能会导致 ClassNotFoundException
            it.instrumentation.transformClassesWith(
                ConfuseClassVisitorFactory::class.java,
                InstrumentationScope.PROJECT
            ){}
        }
    }

    private fun Project.setup(processors: List<VariantProcessor>) {
        val android = project.getAndroid<BaseExtension>()
        when (android) {
            is AppExtension -> android.applicationVariants
            is LibraryExtension -> android.libraryVariants
            else -> emptyList<BaseVariant>()
        }.takeIf<Collection<BaseVariant>>(Collection<BaseVariant>::isNotEmpty)?.let { variants ->
            variants.forEach { variant ->
                processors.forEach { processor ->
                    processor.process(variant)
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// 执行之前处理
// ------------------------------------------------------------------------
class TransformTaskExecutionListener(private val project: Project) : TaskExecutionAdapter() {

    override fun beforeExecute(task: Task) {
        // ---- 处理增量 transform的时候,TransformTask::outputStream 没有初始化的问题  ------
        task.takeIf {
            it.project == project && it is TransformTask && it.transform.scopes.isNotEmpty()
        }?.run {
            task["outputStream"]?.call<Unit>("init")
        }
    }

}