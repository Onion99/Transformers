package com.nova.resource.so

import com.android.build.gradle.internal.tasks.MergeNativeLibsTask
import com.nova.resource.helper.PythonHelper
import org.gradle.api.Action
import org.gradle.api.Task
import java.io.File

class SoResourceTask : Action<Task>{
    override fun execute(task: Task) {
        val project = task.project
        if(task is MergeNativeLibsTask){
            task.doLast {
                val separator = File.separator
                val buildDir = project.buildDir
                PythonHelper.currentProject = project
                val soHandlePyFile = PythonHelper.copyPythonFile(project,"obscure_so.py")
                val buildCacheFilePath = File(
                    buildDir.absolutePath + separator + "generated"
                            + separator + "obscure_plugin_cache"
                            + separator + "tempt"
                            + separator + task.variantName
                )
                if(soHandlePyFile.exists() && soHandlePyFile.length() > 1){
                    val outputDir = task.outputDir.get().asFile
                    val soOutputDir = outputDir.absolutePath + File.separator + "lib"
                    // 获取每一个abi对应的绝对路径
                    // 每个路径传进Python文件执行参数
                    PythonHelper.generateFileName(soOutputDir)
                    PythonHelper.executePythonSoFileHandle(soHandlePyFile,soOutputDir,buildCacheFilePath)
                }
            }
        }
    }
}