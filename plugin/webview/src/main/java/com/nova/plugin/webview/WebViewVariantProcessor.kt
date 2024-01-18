package com.nova.plugin.webview

import com.android.build.gradle.api.BaseVariant
import com.google.auto.service.AutoService
import com.nova.transform.spi.VariantProcessor
import com.android.build.gradle.api.LibraryVariant
import com.nova.transform.gradle.ext.isDynamicFeature
import com.nova.transform.gradle.ext.project


@AutoService(VariantProcessor::class)
class WebViewVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        if (variant !is LibraryVariant && !variant.isDynamicFeature) {
            variant.project.dependencies.add("implementation", "${Build.GROUP}:plugin-webview-instrument:${Build.VERSION}")
        }
    }
}
