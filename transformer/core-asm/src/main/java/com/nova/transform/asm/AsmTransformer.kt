package com.nova.transform.asm


import com.google.auto.service.AutoService
import com.nova.transform.asm.abstarct.ClassTransformer
import com.nova.transform.asm.ext.className
import com.nova.transform.asm.ext.textify
import com.nova.transform.kotlinx.touch
import com.nova.transform.spi.TransformContext
import com.nova.transform.spi.Transformer
import com.nova.transform.spi.annotation.Priority
import com.nova.transform.util.diff
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.InputStream
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean
import java.time.Duration
import java.util.ServiceLoader
import java.util.jar.JarFile

/**
 * Represents bytecode transformer using ASM
 *
 * @author johnsonlee
 */
@AutoService(Transformer::class)
class AsmTransformer : Transformer {

    private val threadMxBean = ManagementFactory.getThreadMXBean()

    private val durations = mutableMapOf<ClassTransformer, Duration>()

    private val classLoader: ClassLoader

    internal val transformers: Iterable<ClassTransformer>

    constructor() : this(Thread.currentThread().contextClassLoader)

    constructor(classLoader: ClassLoader = Thread.currentThread().contextClassLoader) : this(ServiceLoader.load(ClassTransformer::class.java, classLoader).sortedBy {
        it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
    }, classLoader)

    constructor(transformers: Iterable<ClassTransformer>, classLoader: ClassLoader = Thread.currentThread().contextClassLoader) {
        this.classLoader = classLoader
        this.transformers = transformers
    }

    override fun onStartTransform(context: TransformContext) {
        this.transformers.forEach { transformer ->
            this.threadMxBean.sumCpuTime(transformer) {
                transformer.onStartTransform(context)
            }
        }
    }

    override fun transform(context: TransformContext, bytecode: ByteArray): ByteArray {
        val diffEnabled = context.getProperty("nova.transform.diff", false)
        return ClassWriter(ClassWriter.COMPUTE_MAXS).also { writer ->
            this.transformers.fold(ClassNode().also { klass ->
                ClassReader(bytecode).accept(klass, 0)
            }) { a, transformer ->
                this.threadMxBean.sumCpuTime(transformer) {
                    if (diffEnabled) {
                        val left = a.textify()
                        transformer.transform(context, a).also trans@{ b ->
                            val right = b.textify()
                            val diff = if (left == right) "" else left diff right
                            if (diff.isEmpty() || diff.isBlank()) {
                                return@trans
                            }
                            transformer.getReport(context, "${a.className}.diff").touch().writeText(diff)
                        }
                    } else {
                        transformer.transform(context, a)
                    }
                }
            }.accept(writer)
        }.toByteArray()
    }

    override fun onEndTransform(context: TransformContext) {
        this.transformers.forEach { transformer ->
            this.threadMxBean.sumCpuTime(transformer) {
                transformer.onEndTransform(context)
            }
        }

        val w1 = this.durations.keys.maxOfOrNull {
            it.javaClass.name.length
        } ?: 20
        this.durations.forEach { (transformer, ns) ->
            println("${transformer.javaClass.name.padEnd(w1 + 1)}: ${ns.toMillis()} ms")
        }
    }

    private fun <R> ThreadMXBean.sumCpuTime(transformer: ClassTransformer, action: () -> R): R {
        val currentCpuTime0 = this.currentThreadCpuTime
        val result = action()
        val currentCpuTime1 = this.currentThreadCpuTime
        durations[transformer] = durations.getOrPut(transformer) {
            Duration.ofNanos(0)
        } + Duration.ofNanos(currentCpuTime1 - currentCpuTime0)
        return result
    }

}

fun JarFile.transform(name: String, consumer: (ClassNode) -> Unit) = getJarEntry(name)?.let { entry ->
    getInputStream(entry).use { input ->
        consumer(input.asClassNode())
    }
}

fun ByteArray.asClassNode() = ClassNode().also { klass ->
    ClassReader(this).accept(klass, 0)
}

fun InputStream.asClassNode() = readBytes().asClassNode()

fun File.asClassNode(): ClassNode = readBytes().asClassNode()
