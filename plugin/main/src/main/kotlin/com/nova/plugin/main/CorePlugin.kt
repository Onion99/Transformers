package com.nova.plugin.main

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.tasks.MergeNativeLibsTask
import com.nova.plugin.main.service.loadVariantProcessors
import com.nova.plugin.main.service.lookupTransformers
import com.nova.transform.gradle.GTE_V3_6
import com.nova.transform.gradle.compat.getAndroid
import com.nova.transform.kotlinx.call
import com.nova.transform.kotlinx.get
import com.nova.transform.spi.VariantProcessor
import okio.BufferedSink
import okio.buffer
import okio.sink
import okio.source
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionAdapter
import java.io.File


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
        // obscure plugin
        val androidExtension = project.extensions.getByName("android") as AppExtension
        val isEnableSoInsertCode = true
        fun copyPythonFile(project: Project,fileName: String):File{
            var targetSource: BufferedSink
            val buildFile = File(project.buildDir,fileName)
            if(!buildFile.exists()){
                buildFile.createNewFile()
            }
            runCatching {
                targetSource = buildFile.sink().buffer()
                val resourceStream = javaClass.getResourceAsStream(File.separator + fileName)
                val resourceSource = resourceStream!!.source()
                targetSource.writeAll(resourceSource)
                resourceSource.close()
                targetSource.close()
                return  buildFile
            }.getOrElse {
                it.printStackTrace()
                return  buildFile
            }
        }
        project.tasks.whenTaskAdded {task ->
            if(task is MergeNativeLibsTask){
                if(isEnableSoInsertCode){
                    task.doLast {
                        val separator = File.separator
                        val buildDir = project.buildDir
                        val tempSoDir = File(buildDir.absolutePath + separator
                                + "generated" + separator
                                + "obscure_plugin_cache" + separator
                                + "tempt" + separator + task.variantName)
                        val soHandlePyFile = copyPythonFile(project,"obscure_so.py")
                        if(soHandlePyFile.exists()){
                            val outputDir = task.outputDir.get().asFile
                            val soOutputDir = outputDir.absolutePath + File.separator + "lib"
                            // 获取每一个abi对应的绝对路径
                            // 每个路径传进Python文件执行参数
                        }
                    }
                }
            }

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