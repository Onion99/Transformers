package com.nova.resource.clazz

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.tools.r8.internal.cv
import groovyjarjarasm.asm.Opcodes
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor


// ------------------------------------------------------------------------
// 声明类访问工厂,注意这里需要一个抽象类！
// refer : https://juejin.cn/post/7296111218720555043#heading-4
// ------------------------------------------------------------------------

abstract class ConfuseClassVisitorFactory: AsmClassVisitorFactory<InstrumentationParameters.None> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        // 创建自定义的ClassVisitor并返回
        return ConfuseClassVisitor(nextClassVisitor)
    }

    // ------------------------------------------------------------------------
    // 过滤处理的class
    // ------------------------------------------------------------------------
    override fun isInstrumentable(classData: ClassData): Boolean {
        // 处理className: com.silencefly96.module_base.base.BaseActivity
        val className = with(classData.className) {
            val index = lastIndexOf(".") + 1
            substring(index)
        }

        // 筛选要处理的class
        return !className.startsWith("R$")
                && "R" != className
                && "BuildConfig" != className
                // 这两个我加的，代替的类小心无限迭代
                && !classData.className.startsWith("android")
                && !classData.className.startsWith("kotlin")
                && !classData.className.startsWith("org.jet")
                // 指定修改的类范围
                && classData.className.startsWith("com.nova")
                && "AsmMethods" != className    }
}
class ConfuseClassVisitor(private val classVisitor:ClassVisitor) : ClassVisitor(Opcodes.ASM9,classVisitor) {

    override fun visitEnd() {
        super.visitEnd()
        // 添加垃圾实例字段 private String garbageInstance = "Hello";
        // 使用 ProGuard 或 R8 时，垃圾字段可能被优化掉，可以在混淆规则中添加保护
        val instanceField: FieldVisitor = cv.visitField(
            Opcodes.ACC_PRIVATE,  // 修饰符：private
            "garbageInstance",  // 字段名
            "Ljava/lang/String;",  // 字段类型：String
            null,  // 泛型签名
            "Hello" // 初始值
        )
        instanceField.visitEnd()
    }
}