package com.nova.transform.spi

import java.io.File

interface TransformContext {

    /**
     * The name of transform
     */
    val name: String

    /**
     * The project directory
     */
    val projectDir: File

    /**
     * The build directory
     */
    val buildDir: File

    /**
     * The temporary directory
     */
    val temporaryDir: File

    /**
     * The reports directory
     */
    val reportsDir: File

    /**
     * The boot classpath
     */
    val bootClasspath: Collection<File>

    /**
     * The compile classpath
     */
    val compileClasspath: Collection<File>

    /**
     * The runtime classpath
     */
    val runtimeClasspath: Collection<File>

    /**
     * The artifact manager
     */
    val artifacts: TransformerArtifactManager

//    /**
//     * The original dependencies, may be same to [compileClasspath]
//     */
//    val dependencies: Collection<String>
//
//    /**
//     * The class pool
//     */
//    val classPool: TransformerClassPool
//
//    /**
//     * The application identifier
//     */
//    val applicationId: String
//
//    /**
//     * The original application ID before any overrides from flavors
//     */
//    val originalApplicationId: String
//
//    /**
//     * The buildType is debuggable
//     */
//    val isDebuggable: Boolean
//
//    /**
//     * is dataBinding enabled or not
//     */
//    val isDataBindingEnabled: Boolean
//
//    /**
//     * Check if has the specified property. Generally, the property is equivalent to project property
//     *
//     * @param name the name of property
//     */
//    fun hasProperty(name: String): Boolean
//
//    /**
//     * Returns the value of the specified property. Generally, the property is equivalent to project property
//     *
//     * @param name the name of property
//     * @param default the default value
//     */
//    fun <T> getProperty(name: String, default: T): T = default
//
//    fun <R> registerCollector(collector: TransformerCollector<R>)
//    fun <R> unregisterCollector(collector: TransformerCollector<R>)

}