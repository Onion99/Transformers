package com.nova.resource.helper

import java.io.File


object FileTransformHelper {

    fun moveFiles(outputRootFile: File, buildPath: String?, set: Set<File>): Map<String, String> {
        val outputRootPath = outputRootFile.absolutePath
        val map = LinkedHashMap<String, String>()
        set.forEach {

        }
        return map
    }

}