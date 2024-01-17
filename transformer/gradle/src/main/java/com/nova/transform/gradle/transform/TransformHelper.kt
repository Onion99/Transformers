package com.nova.transform.gradle.transform

import com.android.build.gradle.api.BaseVariant
import com.nova.transform.gradle.ext.aar
import com.nova.transform.gradle.ext.allArtifacts
import com.nova.transform.gradle.ext.allClasses
import com.nova.transform.gradle.ext.apk
import com.nova.transform.gradle.ext.mergedAssets
import com.nova.transform.gradle.ext.mergedManifests
import com.nova.transform.gradle.ext.mergedRes
import com.nova.transform.gradle.ext.processedRes
import com.nova.transform.gradle.ext.symbolList
import com.nova.transform.gradle.ext.symbolListWithPackageName
import com.nova.transform.kotlinx.search
import com.nova.transform.spi.ArtifactManager
import com.android.SdkConstants
import java.io.File

val BaseVariant.artifacts: ArtifactManager
    get() = object : ArtifactManager {

        override fun get(type: String): Collection<File> = when (type) {
            ArtifactManager.AAR -> aar.files
            ArtifactManager.ALL_CLASSES -> allClasses.files
            ArtifactManager.APK -> apk.files
            ArtifactManager.MERGED_ASSETS -> mergedAssets.files
            ArtifactManager.MERGED_RES -> mergedRes.files
            ArtifactManager.MERGED_MANIFESTS -> mergedManifests.search { SdkConstants.FN_ANDROID_MANIFEST_XML == it.name }
            ArtifactManager.PROCESSED_RES -> processedRes.search { it.name.startsWith(SdkConstants.FN_RES_BASE) && it.name.endsWith(SdkConstants.EXT_RES) }
            ArtifactManager.SYMBOL_LIST -> symbolList.files
            ArtifactManager.SYMBOL_LIST_WITH_PACKAGE_NAME -> symbolListWithPackageName.files
            ArtifactManager.DATA_BINDING_DEPENDENCY_ARTIFACTS -> allArtifacts[ArtifactManager.DATA_BINDING_DEPENDENCY_ARTIFACTS]?.files ?: emptyList()
            else -> TODO("Unexpected type: $type")
        }

    }