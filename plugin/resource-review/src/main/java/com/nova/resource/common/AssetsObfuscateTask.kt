package com.nova.resource.common


import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.random.Random

abstract class AssetsObfuscateTask : DefaultTask() {

    @get:Input
    abstract val variantName: Property<String>

    @get:InputDirectory
    abstract val mergedAssetsDir: DirectoryProperty

    @TaskAction
    fun taskAction() {
        val assetsDir = mergedAssetsDir.get().asFile
        if (assetsDir.exists()) {
            processAssetsDirectory(assetsDir)
        }
    }

    private fun processAssetsDirectory(directory: File) {
        directory.walkTopDown().forEach { file ->
            when {
                file.isDirectory -> return@forEach
                file.extension.lowercase() in ASSET_EXTENSIONS -> {
                    obfuscateAssetFile(file)
                }
            }
        }
    }

    private fun obfuscateAssetFile(file: File) {
        val content = file.readBytes()
        val randomBytes = ByteArray(8) { Random.nextInt().toByte() }

        // 在文件末尾添加随机字节
        file.writeBytes(content + randomBytes)

        logger.info("Obfuscated asset file: ${file.absolutePath}")
    }

    companion object {
        private val ASSET_EXTENSIONS = setOf(
            "png", "jpg", "jpeg", "gif", "webp", "svga",
            "json", "bin", "mp4", "mp3", "wav"
        )
    }
}