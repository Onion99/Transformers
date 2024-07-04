package com.nova.plugin.main

data class SoObscureDetail(
    var name:String,
    var newName:String,
    var sections:List<SoSection>?
)
data class SoSection(
    var name:String,
    var data:List<Int>
)
