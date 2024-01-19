package com.nova.transform.javassist


import com.google.auto.service.AutoService
import com.nova.transform.kotlinx.touch
import com.nova.transform.spi.TransformContext
import com.nova.transform.spi.Transformer
import com.nova.transform.spi.annotation.Priority
import com.nova.transform.util.diff
import javassist.ClassPool
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean
import java.time.Duration
import java.util.ServiceLoader

// ------------------------------------------------------------------------
// Represents bytecode transformer using Javassist
// ------------------------------------------------------------------------
@AutoService(Transformer::class)
class JavassistTransformer : Transformer {

    private val pool = ClassPool()

    private val threadMxBean = ManagementFactory.getThreadMXBean()

    private val durations = mutableMapOf<ClassTransformer, Duration>()

    private val classLoader: ClassLoader

    private val transformers: Iterable<ClassTransformer>

    constructor() : this(Thread.currentThread().contextClassLoader)

    constructor(classLoader: ClassLoader = Thread.currentThread().contextClassLoader) : this(classLoader, ServiceLoader.load(
        ClassTransformer::class.java, classLoader).sortedBy {
        it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0
    })

    constructor(classLoader: ClassLoader = Thread.currentThread().contextClassLoader, transformers: Iterable<ClassTransformer>) {
        this.classLoader = classLoader
        this.transformers = transformers
    }

    override fun onStartTransform(context: TransformContext) {
        context.bootClasspath.forEach { file ->
            this.pool.appendClassPath(file.canonicalPath)
        }
        context.compileClasspath.forEach { file ->
            this.pool.appendClassPath(file.canonicalPath)
        }
        this.transformers.forEach { transformer ->
            this.threadMxBean.sumCpuTime(transformer) {
                transformer.onStartTransform(context)
            }
        }
    }

    override fun transform(context: TransformContext, bytecode: ByteArray): ByteArray {
        val diffEnabled = context.getProperty("nova.transform.diff", false)
        return ByteArrayOutputStream().use { output ->
            bytecode.inputStream().use { input ->
                this.transformers.fold(this.pool.makeClass(input)) { a, transformer ->
                    this.threadMxBean.sumCpuTime(transformer) {
                        if (diffEnabled) {
                            val left = a.textConvert()
                            transformer.transform(context, a).also trans@{ b ->
                                val right = b.textConvert()
                                val diff = if (left == right) "" else left diff right
                                if (diff.isEmpty() || diff.isBlank()) {
                                    return@trans
                                }
                                transformer.getReport(context, "${a.name}.diff").touch().writeText(diff)
                            }
                        } else {
                            transformer.transform(context, a)
                        }
                    }
                }.classFile.write(DataOutputStream(output))
            }
            output.toByteArray()
        }
    }

    override fun onEndTransform(context: TransformContext) {
        this.transformers.forEach {
            it.onEndTransform(context)
        }

        val w1 = this.durations.keys.maxOfOrNull {
            it.javaClass.name.length
        } ?: 20
        this.durations.forEach { (transformer, ns) ->
            println("${transformer.javaClass.name.padEnd(w1 + 1)}: ${ns.toMillis()} ms")
        }
    }

    private fun <R> ThreadMXBean.sumCpuTime(transformer: ClassTransformer, action: () -> R): R {
        val currentCpuTime1 = this.currentThreadCpuTime
        val result = action()
        val currentCpuTime2 = this.currentThreadCpuTime
        durations[transformer] = durations.getOrPut(transformer) {
            Duration.ofNanos(0)
        } + Duration.ofNanos(currentCpuTime2 - currentCpuTime1)
        return result
    }

}
