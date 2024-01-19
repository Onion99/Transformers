package com.nova.transform.javassist

import com.nova.transform.spi.TransformContext
import com.nova.transform.spi.TransformLifeCycle
import javassist.CtClass
import java.io.File


interface ClassTransformer : TransformLifeCycle {

    val name: String
        get() = javaClass.simpleName

    fun getReportDir(context: TransformContext): File = File(File(context.reportsDir, name), context.name)

    fun getReport(context: TransformContext, name: String): File = File(getReportDir(context), name)


    // ------------------------------------------------------------------------
    //  Transform the specified class node
    // ------------------------------------------------------------------------
    fun transform(context: TransformContext, klass: CtClass) = klass

}
