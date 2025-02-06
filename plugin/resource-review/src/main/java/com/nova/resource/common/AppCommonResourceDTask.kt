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
import java.nio.file.Files
import java.util.Date
import java.util.concurrent.CountDownLatch
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.io.path.Path
import kotlin.random.Random
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


@Suppress("UNCHECKED_CAST")
class AppCommonResourceDTask : Action<Task> {

    // ---- 是否全部资源去重 ------
    private var isFullReviewModel = true
    // ---- 资源备份记录 ------
    private val backupFileRecord = hashMapOf<File,File>()
    override fun execute(task: Task) {
        if(task.name != "assembleDebug" && task.name != "assembleRelease") return
        if(task.name == "assembleDebug" || task.name == "assembleRelease") {
            task.doLast {
                backupFileRecord.forEach { (key, value) ->
                    value.copyRecursively(key,true)
                }
                backupFileRecord.clear()
                task.project.logger.log(LogLevel.WARN,"恢复资源混淆文件完毕")
            }
        }
        if(!task.name.startsWith("merge")) return
//        if(!task.name.endsWith("Assets")) return
        if(!task.name.endsWith("Resources")) return
        if(task is MergeResources){
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
                    // ---- Image,注册 WebP 读写支持 ------
                    ImageIO.scanForPlugins()
                    //ImageIO.getImageReadersByFormatName("webp").next()
                    resourceDir.listFiles()?.forEach { resourceFile ->
                        runCatching {
                            //URLConnection.guessContentTypeFromName(resourceFile.name) // -> application/xml
                            //Files.probeContentType(Path(resourceFile.absolutePath)) // -> text/xml
                            val mimeType = Files.probeContentType(Path(resourceFile.absolutePath))
                            when(mimeType){
                                "text/xml" -> {
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
                                "image/png" -> {
                                    if(resourceFile.absolutePath.contains("9.png")) return@runCatching
                                    val image = ImageIO.read(resourceFile)
                                    // 获取图片的宽和高
                                    val width = image.width
                                    val height = image.height
                                    // 2. 生成随机的像素坐标
                                    val randomX = Random.nextInt(0, width)
                                    val randomY = Random.nextInt(0, height)
                                    // 获取当前像素的颜色值，并随机更改一个像素的颜色
                                    val rgb = image.getRGB(randomX, randomY)
                                    // 提取原来的 RGB 值
                                    val originalAlpha = (rgb shr 24) and 0xFF  // 提取原始 alpha 值
                                    val red = (rgb shr 16) and 0xFF            // 提取红色通道
                                    val green = (rgb shr 8) and 0xFF           // 提取绿色通道
                                    val blue = rgb and 0xFF                    // 提取蓝色通道
                                    // 随机生成一个新的 alpha 值（0-255）
                                    val newAlpha = Random.nextInt(256)
                                    // 组合新的 ARGB 值
                                    val newRgb = (newAlpha shl 24) or (red shl 16) or (green shl 8) or blue
                                    // 设置新的像素颜色（带有新 alpha）
                                    image.setRGB(randomX, randomY, newRgb)
                                    // 将修改后的图片写回文件
                                    ImageIO.write(image, mimeType.split("/")[1].trim(), resourceFile)
                                }
                                else -> {
                                    task.project.logger.log(LogLevel.ERROR,"Unknown Resource File -> $resourceFile")
                                }
                            }
                        }.getOrElse {
                            task.project.logger.log(LogLevel.ERROR,"Modify Resource Content Error -> ${it.message}")
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
        }
    }

    private fun modifyPngContent(task: MergeSourceSetFolders,outputPath:String){
        val imageHandlePyFile = PythonHelper.copyPythonFile(task.project,"image_remake.py")
        PythonHelper.executePythonImageFileHandle(imageHandlePyFile,outputPath)
    }
}