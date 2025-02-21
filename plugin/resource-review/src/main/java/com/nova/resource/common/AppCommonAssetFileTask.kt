package com.nova.resource.common

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.nova.transform.gradle.compat.getAndroid
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import java.io.File
import java.io.FileOutputStream
import java.util.Random


class AppCommonAssetFileTask : Action<Task> {

    override fun execute(task: Task) {
        if(task.name.contains("AndroidTest")) return
        if(!task.name.startsWith("merge")) return
        if(!task.name.endsWith("Assets")) return
        val contentRandomWorker = Random()
        task.doLast {
            // 获取输出目录
            val outputDir = task.outputs.files.singleFile
            if (!outputDir.exists()) return@doLast

            // 如果开启了调试模式，打印处理信息
            task.project.logger.log(LogLevel.WARN,"AssetsObscureAction execute dir: ${outputDir.absolutePath}")
            outputDir.walk()
                .filter { it.isFile }
                .forEach { file ->
                    // 生成1KB随机数据（可根据需要调整大小）
                    val randomBytes = ByteArray(1024 * 1).apply {
                        contentRandomWorker.nextBytes(this)
                    }
                    // 追加随机内容到文件末尾（使用追加模式保留原始内容）
                    FileOutputStream(file, true).use { fos ->
                        fos.write(randomBytes)
                    }
                    task.project.logger.log(LogLevel.WARN,"AssetsObscureAction execute file: ${file.absolutePath}")
                }
        }
    }
}