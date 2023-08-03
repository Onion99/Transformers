package com.nova.transform.spi

/**
 * Represents a mirror of a specific class
 *
 * @author johnsonlee
 */
interface TransformerClass {

    /**
     * The qualified name of class
     */
    val qualifiedName: String

    /**
     * Tests if this class is assignable from the specific type
     *
     * @param type the qualified name of type
     */
    fun isAssignableFrom(type: String): Boolean

    /**
     * Tests if this class is assignable from the specific type
     *
     * @param klass the [TransformerClass] object to be checked
     */
    fun isAssignableFrom(klass: TransformerClass): Boolean

}
