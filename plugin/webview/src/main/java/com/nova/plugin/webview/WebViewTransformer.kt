package com.nova.plugin.webview

import com.google.auto.service.AutoService
import com.nova.transform.asm.abstarct.ClassTransformer
import com.nova.transform.asm.ext.className
import com.nova.transform.asm.ext.findAll
import com.nova.transform.kotlinx.touch
import com.nova.transform.spi.TransformContext
import com.nova.transform.spi.TransformerArtifactManager
import com.nova.transform.util.ComponentHandler
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ATHROW
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.RETURN
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import java.io.PrintWriter
import javax.xml.parsers.SAXParserFactory

@AutoService(ClassTransformer::class)
class WebViewTransformer : ClassTransformer {

    private lateinit var logger: PrintWriter
    private val applications = mutableSetOf<String>()

    override val name: String = "WebViewTransformer"

    override fun onStartTransform(context: TransformContext) {
        val parser = SAXParserFactory.newInstance().newSAXParser()
        this.logger = getReport(context, "report.txt").touch().printWriter()
        context.artifacts.get(TransformerArtifactManager.MERGED_MANIFESTS).forEach { manifest ->
            val handler = ComponentHandler()
            parser.parse(manifest, handler)
            applications.addAll(handler.applications)
        }
        logger.println(applications)
    }

    override fun onEndTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (!this.applications.contains(klass.className)) {
            return klass
        }

        logger.println("WebViewTransformer.transform")
        val method = klass.methods?.find {
            "${it.name}${it.desc}" == "onCreate()V"
        } ?: klass.defaultOnCreate.also {
            klass.methods.add(it)
        }

        method.instructions?.let { insn ->
            insn.findAll(RETURN, ATHROW).forEach { ret ->
                insn.insertBefore(ret, VarInsnNode(ALOAD, 0))
                insn.insertBefore(ret, MethodInsnNode(INVOKESTATIC, SHADOW_WEBVIEW, "preloadWebView", "(Landroid/app/Application;)V", false))
                logger.println(" + $SHADOW_WEBVIEW.preloadWebView(Landroid/app/Application;)V: ${klass.name}.${method.name}${method.desc} ")
            }
        }
        return klass
    }
}

private val ClassNode.defaultOnCreate: MethodNode
    get() = MethodNode(ACC_PUBLIC, "onCreate", "()V", null, null).apply {
        maxStack = 1
        instructions.add(InsnList().apply {
            add(VarInsnNode(ALOAD, 0))
            add(MethodInsnNode(INVOKESPECIAL, superName, name, desc, false))
            add(InsnNode(RETURN))
        })
    }

private const val SHADOW_WEBVIEW = "com/nova/instrument/webview/ShadowWebView"