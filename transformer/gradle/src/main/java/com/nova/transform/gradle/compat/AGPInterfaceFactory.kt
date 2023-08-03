package com.nova.transform.gradle.compat

import com.android.repository.Revision

interface AGPInterfaceFactory {
    val revision: Revision
    fun newAGPInterface(): AGPInterface
}