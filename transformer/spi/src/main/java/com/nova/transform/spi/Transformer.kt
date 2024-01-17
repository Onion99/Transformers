package com.nova.transform.spi

interface Transformer : TransformListener {

    /**
     * Returns the transformed bytecode
     *
     * @param context
     *         The transforming context
     * @param bytecode
     *         The bytecode to be transformed
     * @return the transformed bytecode
     */
    fun transform(context: TransformContext, bytecode: ByteArray): ByteArray
}
interface TransformListener {

    fun onPreTransform(context: TransformContext) {}

    fun onPostTransform(context: TransformContext) {}

}