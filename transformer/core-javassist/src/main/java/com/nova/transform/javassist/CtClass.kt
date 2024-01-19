package com.nova.transform.javassist


import com.nova.transform.kotlinx.execute
import com.nova.transform.kotlinx.stdout
import javassist.CtClass

fun CtClass.textConvert(): String = "javap -c -cp ${classPool.classpath} $name".execute().stdout
