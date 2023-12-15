package com.nova.transform.gradle.v74

import com.android.repository.Revision
import com.google.auto.service.AutoService
import com.nova.transform.gradle.compat.AGPInterface
import com.nova.transform.gradle.compat.AGPInterfaceFactory
import com.nova.transform.gradle.v74.V74

@AutoService(AGPInterfaceFactory::class)
class V74Factory : AGPInterfaceFactory {

    override val revision: Revision = Revision(7, 4, 2)

    override fun newAGPInterface(): AGPInterface = V74

}
