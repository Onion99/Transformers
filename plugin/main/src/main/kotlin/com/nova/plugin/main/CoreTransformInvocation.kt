
package com.nova.plugin.main
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import com.nova.transform.core.abstarct.ClassTransformer
import com.nova.transform.core.ext.className
import com.nova.transform.core.ext.textify
import com.nova.transform.gradle.compat.getAndroid
import com.nova.transform.gradle.ext.getProperty
import com.nova.transform.gradle.transform.applicationId
import com.nova.transform.gradle.transform.artifacts
import com.nova.transform.gradle.transform.bootClasspath
import com.nova.transform.gradle.transform.compileClasspath
import com.nova.transform.gradle.transform.isDataBindingEnabled
import com.nova.transform.gradle.transform.originalApplicationId
import com.nova.transform.gradle.transform.project
import com.nova.transform.gradle.transform.runtimeClasspath
import com.nova.transform.gradle.transform.variant
import com.nova.transform.kotlinx.CPU_NUM
import com.nova.transform.kotlinx.touch
import com.nova.transform.spi.AbstractTranformClassPool
import com.nova.transform.spi.TransformContext
import com.nova.transform.spi.TransformerArtifactManager
import com.nova.transform.spi.TransformerClassPool
import com.nova.transform.util.Collector
import com.nova.transform.util.CompositeCollector
import com.nova.transform.util.collect
import com.nova.transform.util.diff
import com.nova.transform.util.transform
import org.gradle.internal.impldep.org.apache.commons.codec.digest.DigestUtils.md5Hex
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.lang.management.ThreadMXBean
import java.net.URI
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@SuppressWarnings("deprecation")
class CoreTransformInvocation (
    private val delegate: TransformInvocation,
    private val transform: CoreTransform
) : TransformInvocation by delegate,TransformContext,TransformerArtifactManager{


    private val outputs = CopyOnWriteArrayList<File>()
    private val collectors = CopyOnWriteArrayList<Collector<*>>()

    override val name: String = delegate.context.variantName

    override val projectDir: File = project.projectDir

    override val buildDir: File = project.buildDir

    override val temporaryDir: File = delegate.context.temporaryDir

    override val reportsDir: File = File(buildDir, "reports").also { it.mkdirs() }

    override val bootClasspath = delegate.bootClasspath

    override val compileClasspath = delegate.compileClasspath

    override val runtimeClasspath = delegate.runtimeClasspath

    override val artifacts = this

    override val applicationId = delegate.applicationId

    override val originalApplicationId = delegate.originalApplicationId

    override val isDebuggable = variant.buildType.isDebuggable

    override val isDataBindingEnabled = delegate.isDataBindingEnabled

    override fun hasProperty(name: String) = project.hasProperty(name)

    override fun <T> getProperty(name: String, default: T): T = project.getProperty(name, default)

    override fun get(type: String) = variant.artifacts.get(type)

    override fun <R> registerCollector(collector: Collector<R>) {
        this.collectors += collector
    }

    override fun <R> unregisterCollector(collector: Collector<R>) {
        this.collectors -= collector
    }

    override val dependencies: Collection<String> by lazy {
        emptyList()
    }

    override val classPool by lazy {
        object : AbstractTranformClassPool(project.getAndroid<BaseExtension>().bootClasspath) {}
    }


    private fun onPreTransform() {
        transform.parameter.transformers.forEach {
            it.onStartTransform(this)
        }
    }

    internal fun doFullTransform() = doTransform(this::transformFully)

    internal fun doIncrementalTransform() = doTransform(this::transformIncrementally)

    private fun onEndTransform() {
        transform.parameter.transformers.forEach {
            it.onEndTransform(this)
        }
    }


    private fun doTransform(block: (ExecutorService, Set<File>) -> Iterable<Future<*>>) {
        this.outputs.clear()

        val executor = Executors.newFixedThreadPool(CPU_NUM)

        this.onPreTransform()

        // Look ahead to determine which input need to be transformed even incremental build
        val outOfDate = this.lookAhead(executor).onEach {
            project.logger.info("✨ ${it.canonicalPath} OUT-OF-DATE ")
        }

        try {
            block(executor, outOfDate).forEach {
                it.get()
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.HOURS)
        }

        this.onEndTransform()
    }

    private fun lookAhead(executor: ExecutorService): Set<File> {
        return this.inputs.asSequence().map {
            it.jarInputs + it.directoryInputs
        }.flatten().map { input ->
            executor.submit(Callable {
                input.file.takeIf { file ->
                    file.collect(CompositeCollector(collectors)).isNotEmpty()
                }
            })
        }.mapNotNull {
            it.get()
        }.toSet()
    }

    private fun transformFully(executor: ExecutorService, @Suppress("UNUSED_PARAMETER") outOfDate: Set<File>) = this.inputs.map {
        it.jarInputs + it.directoryInputs
    }.flatten().map { input ->
        executor.submit {
            val format = if (input is DirectoryInput) Format.DIRECTORY else Format.JAR
            outputProvider?.let { provider ->
                input.transform(provider.getContentLocation(input.id, input.contentTypes, input.scopes, format))
            }
        }
    }

    private fun transformIncrementally(executor: ExecutorService, outOfDate: Set<File>) = this.inputs.map { input ->
        input.jarInputs.filter {
            it.status != Status.NOTCHANGED || outOfDate.contains(it.file)
        }.map { jarInput ->
            executor.submit {
                doIncrementalTransform(jarInput)
            }
        } + input.directoryInputs.filter {
            it.changedFiles.isNotEmpty() || outOfDate.contains(it.file)
        }.map { dirInput ->
            executor.submit {
                doIncrementalTransform(dirInput, dirInput.file.toURI())
            }
        }
    }.flatten()


    private fun doIncrementalTransform(dirInput: DirectoryInput, base: URI) {
        dirInput.changedFiles.forEach { (file, status) ->
            when (status) {
                Status.REMOVED -> {
                    outputProvider?.let { provider ->
                        provider.getContentLocation(dirInput.id, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY).parentFile.listFiles()?.asSequence()
                            ?.filter { it.isDirectory }
                            ?.map { File(it, dirInput.file.toURI().relativize(file.toURI()).path) }
                            ?.filter { it.exists() }
                            ?.forEach { it.delete() }
                    }
                    file.delete()
                }
                else -> {
                    outputProvider?.let { provider ->
                        val root = provider.getContentLocation(dirInput.id, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                        val output = File(root, base.relativize(file.toURI()).path)
                        file.transform(output)
                    }
                }
            }
        }
    }

    private fun doIncrementalTransform(jarInput: JarInput) {
        when (jarInput.status) {
            Status.REMOVED -> {
                outputProvider?.getContentLocation(jarInput.id, jarInput.contentTypes, jarInput.scopes, Format.JAR)?.takeIf {
                    it.exists()
                }?.delete()
            }
            else -> {
                outputProvider?.let { provider ->
                    jarInput.transform(provider.getContentLocation(jarInput.id, jarInput.contentTypes, jarInput.scopes, Format.JAR))
                }
            }
        }
    }

    private val QualifiedContent.id: String get() = md5Hex(file.absolutePath)
    private fun QualifiedContent.transform(output: File) = this.file.transform(output)
    private fun File.transform(output: File) {
        outputs += output
        project.logger.info("Nova transforming $this => $output")
        transform(output) { bytecode ->
            bytecode.transform()
        }
    }
    private fun ByteArray.transform(): ByteArray {
        return transform.parameter.transformers.fold(this) { bytecode, transformer ->
            ClassWriter(ClassWriter.COMPUTE_MAXS).also { writer ->
                transform.parameter.transformers.fold(ClassNode().also { klass -> ClassReader(bytecode).accept(klass, 0) }) { a, transformer ->
                    transformer.threadMxBean.sumCpuTime(transformer) {
                        if (/*diffEnabled*/false) {
                            val left = a.textify()
                            transformer.transform(this@CoreTransformInvocation, a).also trans@{ b ->
                                val right = b.textify()
                                val diff = if (left == right) "" else left diff right
                                if (diff.isEmpty() || diff.isBlank()) {
                                    return@trans
                                }
                                transformer.getReport(this@CoreTransformInvocation, "${a.className}.diff").touch().writeText(diff)
                            }
                        } else {
                            transformer.transform(this@CoreTransformInvocation, a)
                        }
                    }
                }.accept(writer)
            }.toByteArray()
        }
    }

    private fun <R> ThreadMXBean.sumCpuTime(transformer: ClassTransformer, action: () -> R): R {
        val ct0 = this.currentThreadCpuTime
        val result = action()
        val ct1 = this.currentThreadCpuTime
        transformer.durations[transformer] = transformer.durations.getOrPut(transformer) {
            Duration.ofNanos(0)
        } + Duration.ofNanos(ct1 - ct0)
        return result
    }
}
