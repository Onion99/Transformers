package com.nova.transform.spi

import java.io.Closeable

/**
 * Represents a class pool
 *
 * @author johnsonlee
 */
interface TransformerClassPool : Closeable {

    /**
     * Returns the parent
     */
    val parent: TransformerClassPool?

    /**
     * Returns the class loader
     */
    val classLoader: ClassLoader

    /**
     * Returns an instance [Klass]
     *
     * @param type the qualified name of class
     */
    operator fun get(type: String): TransformerClass

}
