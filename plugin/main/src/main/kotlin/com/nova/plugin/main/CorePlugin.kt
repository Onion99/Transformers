package com.nova.plugin.main

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformTask
import com.nova.plugin.webview.WebViewTransformer
import com.nova.transform.gradle.GTE_V3_6
import com.nova.transform.gradle.compat.getAndroid
import com.nova.transform.kotlinx.call
import com.nova.transform.kotlinx.get
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
        // ---- transform plugin list ------
        project.getAndroid<BaseExtension>().registerTransform(CoreTransform(
            project.newTransformParameter("Onion transformer", setOf(WebViewTransformer()))
        ))
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