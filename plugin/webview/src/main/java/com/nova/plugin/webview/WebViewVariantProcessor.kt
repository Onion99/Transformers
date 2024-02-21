package com.nova.plugin.webview

import com.android.build.gradle.api.BaseVariant
import com.google.auto.service.AutoService
import com.nova.transform.spi.VariantProcessor
import com.android.build.gradle.api.LibraryVariant
import com.nova.transform.gradle.ext.isDynamicFeature
import com.nova.transform.gradle.ext.project
import org.gradle.api.attributes.Attribute

@AutoService(VariantProcessor::class)
class WebViewVariantProcessor : VariantProcessor {

    override fun process(variant: BaseVariant) {
        if (variant !is LibraryVariant && !variant.isDynamicFeature) {
            variant.project.configurations.getByName("pluginAttr").attributes {
                val attrGroup = Attribute.of("pluginGroup", String::class.java)
                val attrVersion = Attribute.of("pluginVersion", String::class.java)
                variant.project.dependencies.add("implementation", "${it.getAttribute(attrGroup)}:webview-instrument:${it.getAttribute(attrVersion)}")
            }
        }
    }
}
