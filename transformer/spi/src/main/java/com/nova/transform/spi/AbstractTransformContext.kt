package com.nova.transform.spi

import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

abstract class AbstractTransformContext(
        final override val applicationId: String,
        final override val name: String,
        final override val bootClasspath: Collection<File>,
        final override val compileClasspath: Collection<File> = emptyList(),
        final override val runtimeClasspath: Collection<File> = emptyList(),
        val bootKlassPool: TransformerClassPool = makeClassPool(bootClasspath)
) : TransformContext {

    val collectors = CopyOnWriteArrayList<TransformerCollector<*>>()

    override val projectDir = File(System.getProperty("user.dir"))

    override val buildDir: File
        get() = File(projectDir, "build")

    override val reportsDir: File
        get() = File(buildDir, "reports")

    override val temporaryDir: File
        get() = File(buildDir, "temp")

    override val artifacts = object : TransformerArtifactManager {}

    override val dependencies: Collection<String> by lazy {
        compileClasspath.map { it.canonicalPath }
    }

    override val classPool = object : AbstractTranformClassPool(runtimeClasspath, bootKlassPool) {}

    override val originalApplicationId = applicationId

    override val isDebuggable = true

    override val isDataBindingEnabled = false

    override fun hasProperty(name: String) = false

    override fun <R> registerCollector(collector: TransformerCollector<R>) {
        this.collectors += collector
    }

    override fun <R> unregisterCollector(collector: TransformerCollector<R>) {
        this.collectors -= collector
    }

}

private fun makeClassPool(bootClasspath: Collection<File>): TransformerClassPool {
    return object : AbstractTranformClassPool(bootClasspath) {}
}