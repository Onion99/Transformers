package com.nova.transform.spi

import java.io.File
import java.net.URLClassLoader

abstract class AbstractTranformClassPool(private val classpath: Collection<File>, final override val parent: TransformerClassPool? = null) : TransformerClassPool {

    private val classes = mutableMapOf<String, TransformerClass>()

    private val imports = mutableMapOf<String, Collection<String>>()

    override val classLoader: ClassLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray(), parent?.classLoader)

    override operator fun get(type: String) = normalize(type).let { name ->
        classes.getOrDefault(name, findClass(name))
    }

    override fun close() {
        val classLoader = this.classLoader
        if (classLoader is URLClassLoader) {
            classLoader.close()
        }
    }

    override fun toString() = "classpath: $classpath"

    internal fun getImports(name: String): Collection<String> = this.imports[name] ?: this.parent?.let { it ->
        if (it is AbstractTranformClassPool) it.getImports(name) else null
    } ?: emptyList()

    internal fun findClass(name: String): TransformerClass {
        return try {
            LoadedClass(this, classLoader.loadClass(name)).also {
                classes[name] = it
            }
        } catch (e: Throwable) {
            DefaultClass(name)
        }
    }

}
private class DefaultClass(name: String) : TransformerClass {

    override val qualifiedName: String = name

    override fun isAssignableFrom(type: String) = false

    override fun isAssignableFrom(klass: TransformerClass) = klass.qualifiedName == this.qualifiedName

}

private class LoadedClass(val pool: AbstractTranformClassPool, val clazz: Class<out Any>) : TransformerClass {

    override val qualifiedName: String = clazz.name

    override fun isAssignableFrom(type: String) = isAssignableFrom(pool.findClass(normalize(type)))

    override fun isAssignableFrom(klass: TransformerClass) = klass is LoadedClass && clazz.isAssignableFrom(klass.clazz)

}
private fun normalize(type: String) = if (type.contains('/')) {
    type.replace('/', '.')
} else {
    type
}