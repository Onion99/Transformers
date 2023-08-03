package com.nova.transform.spi

interface TransformLifeCycle {
    fun onStartTransform(context: TransformContext) {}
    fun onEndTransform(context: TransformContext) {}

}