package com.nova.transform.util


import com.nova.transform.kotlinx.search
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import java.io.File
import java.io.IOException

typealias Collector<T> = com.nova.transform.spi.TransformerCollector<T>

internal const val PATTERN_FQN = "(([a-zA-Z_\$][a-zA-Z\\d_\$]*\\.)*[a-zA-Z_\$][a-zA-Z\\d_\$]*)"


// ------------------------------------------------------------------------
//  Collecting information from file with [collector], the supported file types are as follows:
// - directories
// - archive files
// ------------------------------------------------------------------------
fun <R> File.collect(collector: Collector<R>): List<R> = when {
    this.isDirectory -> {
        val base = this.toURI()
        this.search { f ->
            f.isFile && collector.accept(base.relativize(f.toURI()).normalize().path)
        }.map { f ->
            collector.collect(base.relativize(f.toURI()).normalize().path, f::readBytes)
        }
    }
    this.isFile -> {
        this.inputStream().buffered().use {
            ArchiveStreamFactory().createArchiveInputStream(it).let { archive ->
                generateSequence {
                    try {
                        archive.nextEntry
                    } catch (e: IOException) {
                        null
                    }
                }.filterNot(ArchiveEntry::isDirectory).filter { entry ->
                    collector.accept(entry.name)
                }.map { entry ->
                    collector.collect(entry.name, archive::readBytes)
                }.toList()
            }
        }
    }
    else -> emptyList()
}

