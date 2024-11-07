package com.nova.resource.helper

import com.android.utils.FileUtils
import com.google.gson.Gson
import com.nova.resource.bean.SoObscureDetail
import okio.BufferedSink
import okio.buffer
import okio.sink
import okio.source
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.UUID

object PythonHelper {

    private val supportAbi = arrayOf("armeabi-v7a", "arm64-v8a")
    lateinit var currentProject: Project

    // ------------------------------------------------------------------------
    // in MainTest.java
    // this.getClass.getResource("/") -> "~/proj_dir/target/test-classes/"
    // this.getClass.getResource(".") -> "~/proj_dir/target/test-classes/com/github/xyz/proj/"
    // Thread.currentThread().getContextClassLoader().getResources(".") -> "~/proj_dir/target/test-classes/"
    // Thread.currentThread().getContextClassLoader().getResources("/") ->  null
    //  ├── src
    // │   ├── main
    // │   └── test
    //│       ├── java
    //│       │   └── com
    //│       │       └── github
    //│       │           └── xyz
    //│       │               └── proj
    //│       │                   ├── MainTest.java
    //│       │                   └── TestBase.java
    //│       └── resources
    //│           └── abcd.txt
    //└── target
    //    └── test-classes  <-- this.getClass.getResource("/")
    //        │              `--Thread.currentThread().getContextClassLoader().getResources(".")
    //        ├── com
    //        │   └── github
    //        │       └── xyz
    //        │           └── proj  <-- this.getClass.getResource(".")
    //        │               ├── MainTest.class
    //        │               └── TestBase.class
    //        └── resources
    //            └── abcd.txt
    // ------------------------------------------------------------------------
    fun copyPythonFile(project: Project, fileName: String):File{
        var targetSource: BufferedSink
        val buildFile = File(project.buildDir,fileName)
        if(!buildFile.exists()){
            buildFile.createNewFile()
        }
        runCatching {
            targetSource = buildFile.sink().buffer()
            val resourceStream = javaClass.getResourceAsStream("/$fileName")
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

    fun executePythonSoFileHandle(pyFile: File,dirPath: String,buildCacheFile:File){
        if(soObscureNameMap.isEmpty()) return
        val abi = StringBuffer()
        supportAbi.forEach {
            abi.append(it).append(",")
        }
        val detailRecordFile = File(buildCacheFile,"soObscureDetail.txt")
        FileUtils.writeToFile(detailRecordFile, Gson().toJson(soObscureNameMap))
        val cmdPythonFile = "python3 " + pyFile.absolutePath + ' ' + dirPath + ' ' + abi + ' ' + detailRecordFile.absolutePath
        executeCmd(cmdPythonFile)
    }

    fun executePythonImageFileHandle(pyFile: File,outputPath: String){
        val isDebug = true
        val cmdPythonFile = "python3 " + pyFile.absolutePath + ' ' + isDebug + ' ' + outputPath
        executeCmd(cmdPythonFile)
    }

    private fun executeCmd(command:String){
        currentProject.logger.log(LogLevel.WARN,"executeCmd -> $command")
        val process = Runtime.getRuntime().exec(command)
        val errorInput = StringBuilder()
        val inputStream = process.inputStream
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        runCatching {
            val sequence = bufferedReader.lineSequence()
            sequence.forEach {
                println(it)
            }
            bufferedReader.close()
        }
        val inputErrorStream = process.errorStream
        val bufferedErrorReader= BufferedReader(InputStreamReader(inputErrorStream))
        runCatching {
            val sequence = bufferedErrorReader.lineSequence()
            val errorString = StringBuilder()
            sequence.forEach {
                errorString.append(it).appendLine()
            }
            println(errorString)
            bufferedErrorReader.close()
        }
    }

    private var soObscureDetails = mutableListOf<SoObscureDetail>()
    private var soObscureNameMap = hashMapOf<String,String>()

    fun generateFileName(dirPath:String){
        val soNameList = arrayListOf<String>()
        supportAbi.forEach { abi ->
            putChildFileNames(soNameList,dirPath,abi)
        }
        val filterList = hashSetOf(
            //arrayOf()
            "libc++_shared.so",
            "libdu.so",
            "libZego*.so",
            "libfuai.so",
            "libjpegturbo_android.so",
            "libjpeg.so",
            "libimagepipeline.so",
            "libstatic-webp.so",
            "libnative-imagetranscoder.so",
            "libturbojpeg.so",
            "libNetHtProtect.so"
        )
        var index = 0
        soNameList.forEach { soFileName ->
            index++
            val generateFileName: String
            if(soFileName.endsWith(".so") && soFileName.startsWith("lib")){
                val randomStr: String = UUID.randomUUID().toString().substring(0, 6)
                generateFileName = "lib$index$randomStr.so"
                soObscureDetails.add(SoObscureDetail(soFileName,generateFileName,null))

                soObscureNameMap[soFileName] = generateFileName
                soObscureNameMap[soFileName.substring(3,soFileName.length -3)] = generateFileName.substring(3,generateFileName.length -3)
            }
        }
    }

    private fun putChildFileNames(arrayList:ArrayList<String>, dirPath:String , filename:String ){
        val file = File(dirPath + File.separator + filename)
        if( file.exists() ){
            file.list()?.let { arrayList.addAll(it) }
        }
    }
}