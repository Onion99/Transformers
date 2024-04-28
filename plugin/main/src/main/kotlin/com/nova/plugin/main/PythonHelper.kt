package com.nova.plugin.main

import okio.BufferedSink
import okio.buffer
import okio.sink
import okio.source
import org.gradle.api.Project
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object PythonHelper {

    private val supportAbi = arrayOf("armeabi-v7a", "arm64-v8a")

    fun copyPythonFile(project: Project, fileName: String):File{
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

    fun executePythonSoFileHandle(pyFile: File,dirPath: String,buildCacheFile:File){
        val abi = StringBuffer()
        supportAbi.forEach {
            abi.append(it).append(",")
        }
        val detailRecordFile = File(buildCacheFile,"soObscureDetail.txt")
        val cmdPythonFile = "python3 " + pyFile.absolutePath + ' ' + dirPath + ' ' + abi + ' ' + detailRecordFile.absolutePath
    }

    private fun executeCmd(command:String){
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

    fun generateFileName(dirPath:String){
        val nameList = arrayListOf<String>()
        supportAbi.forEach {
            putChildFileNames(nameList,dirPath,it)
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
    }

    private fun putChildFileNames(arrayList:ArrayList<String>, dirPath:String , filename:String ){
        val file = File(dirPath + File.separator + filename)
        if( file.exists() ){
            file.list()?.let { arrayList.addAll(it) }
        }
    }
}