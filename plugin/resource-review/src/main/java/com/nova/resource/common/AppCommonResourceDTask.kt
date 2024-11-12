package com.nova.resource.common

import com.android.build.gradle.tasks.MergeResources
import com.android.build.gradle.tasks.MergeSourceSetFolders
import com.android.ide.common.resources.ResourceSet
import com.nova.resource.helper.PythonHelper
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import java.io.File
import java.util.Date
import java.util.concurrent.CountDownLatch
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


@Suppress("UNCHECKED_CAST")
class AppCommonResourceDTask : Action<Task> {

    // ---- 是否全部资源去重 ------
    var isFullReviewModel = true

    override fun execute(task: Task) {
        if(task.name.contains("AndroidTest",true)) return
        if(!task.name.startsWith("merge")) return
//        if(!task.name.endsWith("Assets")) return
        if(!task.name.endsWith("Resources")) return
        if(task is MergeResources){
            val backupFileRecord = hashMapOf<File,File>()
            task.doFirst {
                /*val getProcessMethod = MergeResources::class.java.getDeclaredMethod("getPreprocessor", *arrayOf())
                getProcessMethod.isAccessible = true
                val resourcePreprocessor = (kotlin.runCatching { getProcessMethod.invoke(task, *arrayOf()) }.getOrNull() as? ResourcePreprocessor) ?: return@doFirst
                var getConfiguredResourceSetsMethod:Method? = null
                val declaredMethods = MergeResources::class.java.declaredMethods
                for (index in declaredMethods.indices){
                    val method = declaredMethods[index]
                    if(method.name == "getConfiguredResourceSets"){
                        getConfiguredResourceSetsMethod = method
                    }
                }
                getConfiguredResourceSetsMethod?.isAccessible = true
                if(getConfiguredResourceSetsMethod?.parameterCount == 2){
                    val getAaptEnvMethod = MergeResources::class.java.getDeclaredMethod("getAaptEnv", *arrayOf())
                    getAaptEnvMethod.isAccessible = true
                    val aaptEnv = getAaptEnvMethod.invoke(task,*arrayOf()) as Property<*>
                    val objArr = arrayOfNulls<Any>(2)
                    objArr[0] = resourcePreprocessor
                    objArr[1] = aaptEnv.getOrNull() as? String
                    getConfiguredResourceSetsMethod.invoke(task, objArr)
                }else getConfiguredResourceSetsMethod?.invoke(task,resourcePreprocessor)*/

                val resourcePreprocessor = MergeResources::class.members.find { it.name == "preprocessor" }?.run {
                    isAccessible = true
                    call(task)
                } ?: return@doFirst

                MergeResources::class.members.find { it.name =="getConfiguredResourceSets" }?.run {
                    isAccessible = true
                    val aaptEnv = MergeResources::class.memberProperties.find { it.name =="aaptEnv" }?.run {
                        isAccessible = true
                        (get(task) as Property<String?>).orNull
                    }
                    call(task,resourcePreprocessor,aaptEnv) as List<ResourceSet>
                }

                val resourceDirList = ArrayList<File>()
                MergeResources::class.memberProperties.find { it.name =="processedInputs" }?.run {
                    isAccessible = true
                    (get(task) as List<ResourceSet>).forEach { resourceset ->
                        resourceset.sourceFiles.forEach { file ->
                            if(file.exists() && file.isDirectory){
                                file.listFiles().forEach {
                                    if(it.isDirectory){
                                        resourceDirList.add(it)
                                    }
                                }
                            }
                        }
                    }
                }
                val targetProjectResourceList = ArrayList<File>()
                resourceDirList.forEach { file ->
                    if(isFullReviewModel){
                        targetProjectResourceList.add(file)
                    } else if(file.absolutePath.startsWith(task.project.rootDir.toString())){
                        targetProjectResourceList.add(file)
                    }
                }
                val resourceBackupDir = task.project.buildDir.absolutePath + File.separator + "backup"
                val xmlCopyCountDown = CountDownLatch(targetProjectResourceList.size)

                targetProjectResourceList.forEach { resourceDir ->
                    // ---- Backup Resource Dir ------
                    val backupFile = File(resourceBackupDir+File.separator+resourceDir.absolutePath.hashCode()+File.separator+resourceDir.name)
                    resourceDir.copyRecursively(backupFile,true)
                    backupFileRecord[resourceDir] = backupFile
                    xmlCopyCountDown.countDown()
                }
                task.project.logger.log(LogLevel.WARN,"资源文件备份完毕")
                xmlCopyCountDown.await()
                val xmlHandlePyFile = PythonHelper.copyPythonFile(task.project,"obscure_xml.py")
                PythonHelper.currentProject = task.project
                targetProjectResourceList.forEach { resourceDir ->
                    // ---- XML 通过Python文件修改 执行太慢了------
                    // PythonHelper.executeCommonPythonFileHandle(xmlHandlePyFile,resourceDir.absolutePath)
                    // ---- XML ------
                    resourceDir.listFiles()?.forEach { resourceFile ->
                        runCatching {
                            if(resourceFile.name.endsWith(".xml")){
                                // 1. 加载 XML 文件
                                val xmlFile = File(resourceFile.absolutePath)
                                val documentBuilder = DocumentBuilderFactory.newInstance().apply {
                                    isNamespaceAware = true
                                }.newDocumentBuilder()
                                val document = documentBuilder.parse(xmlFile)
                                val comment = document.createComment("Test In ${Date(System.currentTimeMillis())}")
                                // 在根元素前插入注释
                                val root = document.documentElement
                                root.setAttribute("xmlns:tools", "http://schemas.android.com/tools")
                                document.insertBefore(comment, root)
                                // 保存修改后的XML文件
                                val transformer = TransformerFactory.newInstance().newTransformer()
                                transformer.setOutputProperty(OutputKeys.INDENT, "yes")  // 设置格式缩进
                                transformer.setOutputProperty(OutputKeys.METHOD, "xml")
                                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")  // 设置编码
                                transformer.setOutputProperty(OutputKeys.VERSION, "1.0")  // 设置XML版本
                                val source = DOMSource(document)
                                val result = StreamResult(xmlFile)
                                transformer.transform(source, result)
                            }
                        }.getOrElse {
                            task.project.logger.log(LogLevel.ERROR,"Modify XML Content Error -> ${it.message}")
                        }
                    }
                }
                task.project.logger.log(LogLevel.WARN,"资源混淆处理完毕")
                /*runCatching {
                    val outputPath = task.outputDir.get().asFile.absolutePath
                    modifyPngContent(task,outputPath)
                }.onFailure {
                    task.logger.log(LogLevel.WARN,it.message)
                }*/
            }
            task.doLast {
                backupFileRecord.forEach { (key, value) ->
                    value.copyRecursively(key,true)
                }
                backupFileRecord.clear()
                task.project.logger.log(LogLevel.WARN,"恢复资源混淆文件完毕")
            }
        }
    }

    private fun modifyPngContent(task: MergeSourceSetFolders,outputPath:String){
        val imageHandlePyFile = PythonHelper.copyPythonFile(task.project,"image_remake.py")
        PythonHelper.executePythonImageFileHandle(imageHandlePyFile,outputPath)
    }
}