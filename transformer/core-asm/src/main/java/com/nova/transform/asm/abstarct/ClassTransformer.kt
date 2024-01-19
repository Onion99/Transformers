package com.nova.transform.asm.abstarct

import com.nova.transform.spi.TransformContext
import com.nova.transform.spi.TransformLifeCycle
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean
import java.time.Duration


interface ClassTransformer : TransformLifeCycle {

    val threadMxBean: ThreadMXBean
        get() = ManagementFactory.getThreadMXBean()

    val name: String
        get() = javaClass.simpleName

    val durations
        get() = mutableMapOf<ClassTransformer, Duration>()

    fun getReportDir(context: TransformContext): File = File(File(context.reportsDir, name), context.name)

    fun getReport(context: TransformContext, name: String): File {
        val report: File by lazy {
            val dir = getReportDir(context)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, name)
            if (!file.exists()) {
                file.createNewFile()
            }
            file
        }
        return report
    }

    // ------------------------------------------------------------------------
    //  Transform the specified class node
    // ------------------------------------------------------------------------
    fun transform(context: TransformContext, klass: ClassNode) = klass

}
