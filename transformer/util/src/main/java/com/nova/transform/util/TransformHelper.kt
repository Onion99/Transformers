package com.nova.transform.util

import com.nova.transform.kotlinx.CPU_NUM
import com.nova.transform.kotlinx.file
import com.nova.transform.spi.AbstractTransformContext
import com.nova.transform.spi.TransformContext
import com.nova.transform.spi.Transformer
import com.nova.transform.spi.TransformerArtifactManager
import com.nova.transform.util.build.AndroidSdk
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private val TMPDIR = File(System.getProperty("java.io.tmpdir"))
open class TransformHelper(
    val input: File,
    val platform: File = AndroidSdk.getAndroidJar().parentFile,
    val artifacts: TransformerArtifactManager = object : TransformerArtifactManager {},
    val applicationId: String = UUID.randomUUID().toString(),
    val variant: String = "debug"
) {

    fun transform(output: File = TMPDIR, transformer: (TransformContext, ByteArray) -> ByteArray = { _, it -> it }) = transform(output, object : Transformer {
        override fun transform(context: TransformContext, bytecode: ByteArray) = transformer(context, bytecode)
    })

    fun transform(transformer: (TransformContext, ByteArray) -> ByteArray = { _, it -> it }, output: File = TMPDIR) = transform(output, transformer)

    fun transform(output: File = TMPDIR, vararg transformers: Transformer) {
        val inputs = if (this.input.isDirectory) this.input.listFiles()?.toList() ?: emptyList() else listOf(this.input)
        val classpath = inputs.filter {
            it.isDirectory || it.extension.run {
                equals("class", true) || equals("jar", true)
            }
        }
        val context = object : AbstractTransformContext(
                applicationId,
                variant,
                platform.resolve("android.jar").takeIf { it.exists() }?.let { listOf(it) } ?: emptyList(),
                classpath,
                classpath
        ) {
            override val projectDir = output
            override val artifacts = this@TransformHelper.artifacts
        }
        val executor = Executors.newFixedThreadPool(CPU_NUM)

        try {
            transformers.map {
                executor.submit {
                    it.onPreTransform(context)
                }
            }.forEach {
                it.get()
            }

            inputs.map { input ->
                executor.submit {
                    input.takeIf {
                        it.collect(CompositeCollector(context.collectors)).isNotEmpty()
                    }
                }
            }.forEach {
                it.get()
            }

            inputs.map {
                executor.submit {
                    it.transform(context.buildDir.file("transforms", it.name)) { bytecode ->
                        transformers.fold(bytecode) { bytes, transformer ->
                            transformer.transform(context, bytes)
                        }
                    }
                }
            }.forEach {
                it.get()
            }

            transformers.map {
                executor.submit {
                    it.onPostTransform(context)
                }
            }.forEach {
                it.get()
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1L, TimeUnit.HOURS)
        }
    }

    fun transform(vararg transformers: Transformer, output: File = TMPDIR) = transform(output, *transformers)

}